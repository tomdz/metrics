package com.yammer.metrics.reporting;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import com.yammer.metrics.core.HealthCheck;

public interface HealthChecksRenderer {
    String getContentType();
    void render(PrintWriter writer, Map<String, HealthCheck.Result> results) throws IOException;
}
