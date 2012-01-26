package com.yammer.metrics.reporting;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import com.yammer.metrics.core.HealthCheck.Result;

public class HealthChecksJsonRenderer implements HealthChecksRenderer {
    private static final String CONTENT_TYPE = "application/json";
    private final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Inclusion.NON_NULL);
    
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void render(PrintWriter writer, Map<String, Result> results) throws IOException {
        mapper.writeValue(writer, results);
    }
}
