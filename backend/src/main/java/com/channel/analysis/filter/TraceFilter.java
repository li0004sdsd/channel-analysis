package com.channel.analysis.filter;

import com.channel.analysis.common.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class TraceFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TraceFilter.class);
    private static final long SLOW_REQUEST_THRESHOLD_MS = 200L;
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            String traceId = request.getHeader(HEADER_TRACE_ID);
            if (traceId == null || traceId.isBlank()) {
                TraceContext.generateTraceId();
            } else {
                TraceContext.setTraceId(traceId);
            }
            TraceContext.setOperation(request.getMethod() + " " + request.getRequestURI());

            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                log.warn("慢请求: method={}, uri={}, duration={}ms, status={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        duration,
                        response.getStatus());
            }
            TraceContext.clear();
        }
    }
}
