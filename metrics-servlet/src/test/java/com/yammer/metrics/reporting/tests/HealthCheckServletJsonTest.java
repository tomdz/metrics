package com.yammer.metrics.reporting.tests;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.TypeReference;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.reporting.HealthCheckServlet;
import com.yammer.metrics.reporting.HealthChecksJsonRenderer;

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

        Map<String, Object> expected = new HashMap<String, Object>();

        assertThat(mapper.readValue(output.toString(), new TypeReference<HashMap<String, Object>>(){}),
                   equalTo((Object)expected));

        verify(response).setStatus(501);
        verify(response).setContentType("application/json");
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void returnsOkIfAllHealthChecksAreHealthy() throws Exception {
        results.put("one", HealthCheck.Result.healthy());
        results.put("two", HealthCheck.Result.healthy("msg"));

        servlet.service(request, response);

        Map<String, Object> result = mapper.readValue(output.toString(), new TypeReference<HashMap<String, Object>>(){});

        assertThat(result.size(),
                   is(2));
        assertThat(result,
                   hasKey("one"));
        assertThat(result,
                   hasKey("two"));

        Map<String, ? extends Object> resultForOne = (Map<String, ? extends Object>)result.get("one");
        Map<String, ? extends Object> resultForTwo = (Map<String, ? extends Object>)result.get("two");

        assertThat(resultForOne,
                   allOf(hasEntry("healthy", Boolean.TRUE),
                         not(hasKey("message")),
                         not(hasKey("error"))));
        assertThat(resultForTwo,
                   allOf(hasEntry("healthy", Boolean.TRUE),
                         hasEntry("message", "msg"),
                         not(hasKey("error"))));

        verify(response).setStatus(200);
        verify(response).setContentType("application/json");
    }

    @Test
    @SuppressWarnings({ "ThrowableResultOfMethodCallIgnored", "rawtypes", "unchecked" })
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

        Map<String, Object> result = mapper.readValue(output.toString(), new TypeReference<HashMap<String, Object>>(){});

        assertThat(result.size(),
                   is(2));
        assertThat(result,
                   hasKey("one"));
        assertThat(result,
                   hasKey("two"));

        Map<String, ? extends Object> resultForOne = (Map<String, ? extends Object>)result.get("one");
        Map<String, ? extends Object> resultForTwo = (Map<String, ? extends Object>)result.get("two");

        assertThat(resultForOne,
                   allOf(hasEntry("healthy", Boolean.FALSE),
                         hasEntry("message", "msg"),
                         not(hasKey("error"))));
        assertThat(resultForTwo,
                   allOf(hasEntry("healthy", Boolean.FALSE),
                         hasEntry("message", "Oh no"),
                         hasKey("error")));

        Map<String, ? extends Object> resultForError = (Map<String, ? extends Object>)resultForTwo.get("error");

        assertThat(resultForError,
                   allOf(hasEntry("message", theEx.getMessage()),
                         hasEntry("localizedMessage", theEx.getLocalizedMessage()),
                         hasKey("stackTrace")));

        List resultStackTrace = (List)resultForError.get("stackTrace");

        assertThat(resultStackTrace.size(),
                   is(theEx.getStackTrace().length));

        for (int idx = 0; idx < theEx.getStackTrace().length; idx++) {
            StackTraceElement expectedElement = theEx.getStackTrace()[idx];
            // need to use raw types, otherwise the compiler will freak out
            Matcher matcher = allOf(expectedElement.getClassName() == null ? not(hasKey("className")) : hasEntry("className", expectedElement.getClassName()),
                                    expectedElement.getFileName() == null ? not(hasKey("fileName")) : hasEntry("fileName", expectedElement.getFileName()),
                                    hasEntry("lineNumber", expectedElement.getLineNumber()),
                                    expectedElement.getMethodName() == null ? not(hasKey("methodName")) : hasEntry("methodName", expectedElement.getMethodName()),
                                    hasEntry("nativeMethod", expectedElement.isNativeMethod()));

            assertThat(resultStackTrace.get(idx), matcher);
        }

        verify(response).setStatus(500);
        verify(response).setContentType("application/json");
    }
}
