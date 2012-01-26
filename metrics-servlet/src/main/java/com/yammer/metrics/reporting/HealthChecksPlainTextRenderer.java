package com.yammer.metrics.reporting;

import java.io.PrintWriter;
import java.util.Map;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheck.Result;

public class HealthChecksPlainTextRenderer implements HealthChecksRenderer {
    private static final String CONTENT_TYPE = "text/plain";

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void render(PrintWriter writer, Map<String, Result> results) {
        if (results.isEmpty()) {
            writer.println("! No health checks registered.");
        } else {
            for (Map.Entry<String, HealthCheck.Result> entry : results.entrySet()) {
                final HealthCheck.Result result = entry.getValue();
                if (result.isHealthy()) {
                    if (result.getMessage() != null) {
                        writer.format("* %s: OK\n  %s\n", entry.getKey(), result.getMessage());
                    } else {
                        writer.format("* %s: OK\n", entry.getKey());
                    }
                } else {
                    if (result.getMessage() != null) {
                        writer.format("! %s: ERROR\n!  %s\n", entry.getKey(), result.getMessage());
                    }

                    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                    final Throwable error = result.getError();
                    if (error != null) {
                        writer.println();
                        error.printStackTrace(writer);
                        writer.println();
                    }
                }
            }
        }
    }
}
