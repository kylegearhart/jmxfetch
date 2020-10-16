package org.datadog.jmxfetch.reporter;

import org.datadog.jmxfetch.util.StringUtils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReporterFactory {

    /** Gets the reporter for the corresponding type string (console, statsd). */
    public static Reporter getReporter(String type) {
        if (type == null || type.length() <= 0) {
            throw new IllegalArgumentException("Null or empty reporter type");
        }
        if ("console".equals(type)) {
            return new ConsoleReporter();
        } else if ("json".equals(type)) {
            return new JsonReporter();
        } else if (type.startsWith("statsd:")) {

            Matcher m = Pattern.compile("^statsd:(.*):(\\d+)$").matcher(type);
            if (m.find() && m.groupCount() == 2) {
                String host = m.group(1);
                Integer port = Integer.valueOf(m.group(2));
                return new StatsdReporter(host, port);
            }

            m = Pattern.compile("^statsd:(.*)$").matcher(type);
            if (m.find() && m.groupCount() == 1) {
                String host = m.group(1);
                return new StatsdReporter(host, 0);
            }
        }
        throw new IllegalArgumentException("Invalid reporter type: " + type);
    }

    private ReporterFactory() {}
}
