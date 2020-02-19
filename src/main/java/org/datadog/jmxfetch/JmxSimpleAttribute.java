package org.datadog.jmxfetch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

@SuppressWarnings("unchecked")
public class JmxSimpleAttribute extends JmxAttribute {
    private String metricType;

    /** JmxSimpleAttribute constructor. */
    public JmxSimpleAttribute(
            MBeanAttributeInfo attribute,
            ObjectName beanName,
            String instanceName,
            Connection connection,
            Map<String, String> instanceTags,
            boolean cassandraAliasing,
            Boolean emptyDefaultHostname) {
        super(
                attribute,
                beanName,
                instanceName,
                connection,
                instanceTags,
                cassandraAliasing,
                emptyDefaultHostname);
    }

    @Override
    public List<HashMap<String, Object>> getMetrics()
            throws AttributeNotFoundException, InstanceNotFoundException, MBeanException,
                    ReflectionException, IOException {
        HashMap<String, Object> metric = new HashMap<String, Object>();

        metric.put("alias", getAlias());
        metric.put("value", castToDouble(getValue(), null));
        metric.put("tags", getTags());
        metric.put("metric_type", getMetricType());
        List<HashMap<String, Object>> metrics = new ArrayList<HashMap<String, Object>>(1);
        metrics.add(metric);
        return metrics;
    }

    /** Returns whether an attribute matches in a configuration spec. */
    public boolean match(Configuration configuration) {
        return matchDomain(configuration)
                && matchBean(configuration)
                && matchAttribute(configuration)
                && !(excludeMatchDomain(configuration)
                        || excludeMatchBean(configuration)
                        || excludeMatchAttribute(configuration));
    }

    private boolean excludeMatchAttribute(Configuration configuration) {
        Filter exclude = configuration.getExclude();
        if (exclude.getAttribute() == null) {
            return false;
        } else if ((exclude.getAttribute() instanceof Map<?, ?>)
                && ((Map<String, Object>) (exclude.getAttribute()))
                        .containsKey(getAttributeName())) {
            return true;

        } else if ((exclude.getAttribute() instanceof List<?>
                && ((List<String>) (exclude.getAttribute())).contains(getAttributeName()))) {
            return true;
        }
        return false;
    }

    private boolean matchAttribute(Configuration configuration) {
        Filter include = configuration.getInclude();
        if (include.getAttribute() == null) {
            return true;

        } else if ((include.getAttribute() instanceof Map<?, ?>)
                && ((Map<String, Object>) (include.getAttribute()))
                        .containsKey(getAttributeName())) {
            return true;

        } else if ((include.getAttribute() instanceof List<?>
                && ((List<String>) (include.getAttribute())).contains(getAttributeName()))) {
            return true;
        }

        return false;
    }

    private String getMetricType() {
        Filter include = getMatchingConf().getInclude();
        if (metricType != null) {
            return metricType;
        } else if (include.getAttribute() instanceof Map<?, ?>) {
            Map<String, Map<String, String>> attribute =
                    (Map<String, Map<String, String>>) (include.getAttribute());
            metricType = attribute.get(getAttributeName()).get(METRIC_TYPE);
            if (metricType == null) {
                metricType = attribute.get(getAttributeName()).get("type");
            }
        }

        if (metricType == null) { // Default to gauge
            metricType = "gauge";
        }

        return metricType;
    }

    private Object getValue()
            throws AttributeNotFoundException, InstanceNotFoundException, MBeanException,
                    ReflectionException, IOException, NumberFormatException {
        return this.getJmxValue();
    }
}
