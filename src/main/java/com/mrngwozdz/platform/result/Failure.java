package com.mrngwozdz.platform.result;

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
}