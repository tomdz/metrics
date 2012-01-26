package com.yammer.metrics.reporting.tests;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.reporting.HealthCheckServlet;
import com.yammer.metrics.reporting.HealthChecksJsonRenderer;
import com.yammer.metrics.reporting.HealthChecksPlainTextRenderer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HealthCheckServletJsonTest {
    private final HealthCheckRegistry registry = mock(HealthCheckRegistry.class);
    private final HealthCheckServlet servlet = new HealthCheckServlet(registry, new HealthChecksJsonRenderer());
    private final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Inclusion.NON_NULL);
    private final SortedMap<String, HealthCheck.Result> results = new TreeMap<String, HealthCheck.Result>();

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        when(request.getMethod()).thenReturn("GET");

        when(registry.runHealthChecks()).thenReturn(results);

        when(response.getWriter()).thenReturn(new PrintWriter(new OutputStreamWriter(output)));
    }

    @Test
    public void returnsNotImplementedIfNoHealthChecksAreRegistered() throws Exception {
        results.clear();

        servlet.service(request, response);

        Map<String, Object> expected = new LinkedHashMap<String, Object>();

        assertThat(output.toString(),
                   is(mapper.writeValueAsString(expected)));

        verify(response).setStatus(501);
        verify(response).setContentType("application/json");
    }

    @Test
    public void returnsOkIfAllHealthChecksAreHealthy() throws Exception {
        results.put("one", HealthCheck.Result.healthy());
        results.put("two", HealthCheck.Result.healthy("msg"));

        servlet.service(request, response);

        Map<String, Object> expected = new LinkedHashMap<String, Object>();
        Map<String, Object> one = new LinkedHashMap<String, Object>();
        Map<String, Object> two = new LinkedHashMap<String, Object>();

        one.put("healthy", true);
        two.put("healthy", true);
        two.put("message", "msg");
        expected.put("one", one);
        expected.put("two", two);

        assertThat(output.toString(),
                   is(mapper.writeValueAsString(expected)));

        verify(response).setStatus(200);
        verify(response).setContentType("application/json");
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void returnsServerErrorIfHealthChecksAreUnhealthy() throws Exception {
        IOException theEx = null;
        try {
            throw new IOException("Oh no");
        }
        catch (IOException ex) {
            theEx = ex;
        }

        results.put("one", HealthCheck.Result.unhealthy("msg"));
        results.put("two", HealthCheck.Result.unhealthy(theEx));

        servlet.service(request, response);

        Map<String, Object> expected = new LinkedHashMap<String, Object>();
        Map<String, Object> one = new LinkedHashMap<String, Object>();
        Map<String, Object> two = new LinkedHashMap<String, Object>();
        Map<String, Object> exMap = new LinkedHashMap<String, Object>();
        List<Map<String, Object>> exElements = new ArrayList<Map<String,Object>>(theEx.getStackTrace().length); 

        for (StackTraceElement element : theEx.getStackTrace()) {
            Map<String, Object> elementMap = new LinkedHashMap<String, Object>();
            elementMap.put("methodName", element.getMethodName());
            elementMap.put("fileName", element.getFileName());
            elementMap.put("lineNumber", element.getLineNumber());
            elementMap.put("className", element.getClassName());
            elementMap.put("nativeMethod", element.isNativeMethod());
            exElements.add(elementMap);
        }
        exMap.put("stackTrace", exElements);
        exMap.put("message", theEx.getMessage());
        exMap.put("localizedMessage", theEx.getLocalizedMessage());
        one.put("healthy", false);
        one.put("message", "msg");
        two.put("healthy", false);
        two.put("message", "Oh no");
        two.put("error", exMap);
        expected.put("one", one);
        expected.put("two", two);

        assertThat(output.toString(),
                   is(mapper.writeValueAsString(expected)));

        verify(response).setStatus(500);
        verify(response).setContentType("application/json");
    }
}
