package com.mrngwozdz.platform.result;

import jakarta.ws.rs.core.Response;

public record Failure(
        ErrorCode code,
        String message,
        java.util.Map<String, Object> context
) {
    public static Failure of(ErrorCode code, String message) {
        return new Failure(code, message, java.util.Map.of());
    }
    public Failure with(String key, Object value) {
        var copy = new java.util.HashMap<>(context);
        copy.put(key, value);
        return new Failure(code, message, java.util.Map.copyOf(copy));
    }
    
    public Response.Status getHttpStatus() {
        return switch (code) {
            case VALIDATION, INVALID_FILE_TYPE -> Response.Status.BAD_REQUEST;
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case CONFLICT -> Response.Status.CONFLICT;
            case TIMEOUT -> Response.Status.GATEWAY_TIMEOUT;
            case UNAVAILABLE -> Response.Status.SERVICE_UNAVAILABLE;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };
    }
}