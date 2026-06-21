package com.channel.analysis.common;

import org.slf4j.MDC;
import java.util.UUID;

public final class TraceContext {

    private static final String TRACE_ID = "traceId";
    private static final String CHANNEL_ID = "channelId";
    private static final String OPERATION = "operation";

    private TraceContext() {
    }

    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    public static void generateTraceId() {
        MDC.put(TRACE_ID, UUID.randomUUID().toString().replace("-", ""));
    }

    public static void setChannelId(Long channelId) {
        if (channelId != null) {
            MDC.put(CHANNEL_ID, String.valueOf(channelId));
        } else {
            MDC.remove(CHANNEL_ID);
        }
    }

    public static void setOperation(String operation) {
        MDC.put(OPERATION, operation);
    }

    public static void clear() {
        MDC.remove(TRACE_ID);
        MDC.remove(CHANNEL_ID);
        MDC.remove(OPERATION);
    }
}
