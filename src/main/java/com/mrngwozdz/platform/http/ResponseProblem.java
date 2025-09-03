package com.mrngwozdz.platform.http;

public record ResponseProblem(
        String type,     // np. "about:blank" albo URL dokumentujący błąd
        String title,    // krótki tytuł
        int status,      // HTTP status
        String detail,   // opis błędu
        String instance, // np. traceId albo ścieżka
        java.util.Map<String, Object> extensions // dowolne pola z Failure.context
) {
    public static ResponseProblem of(int status, String title, String detail,
                                     java.util.Map<String, Object> extensions) {
        return new ResponseProblem("about:blank", title, status, detail, null, extensions);
    }
}