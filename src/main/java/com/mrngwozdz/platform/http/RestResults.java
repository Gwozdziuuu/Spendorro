package com.mrngwozdz.platform.http;

import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.platform.result.Success;
import io.vavr.control.Either;
import org.jboss.resteasy.reactive.RestResponse;

public final class RestResults {
    private RestResults() {}

    public static <T> RestResponse<T> ok(Success<T> s) {
        return RestResponse.ok(s.value());
    }

    public static RestResponse<ResponseProblem> toResponse(Failure f) {
        int status = switch (f.code()) {
            case VALIDATION -> 400;
            case NOT_FOUND  -> 404;
            case CONFLICT   -> 409;
            case TIMEOUT    -> 504;
            case UNAVAILABLE-> 503;
            case IO_ERROR   -> 500;
            default         -> 500;
        };
        var problem = ResponseProblem.of(
                status,
                f.code().name(),
                f.message(),
                f.context()
        );
        return RestResponse.ResponseBuilder
                .create(RestResponse.Status.fromStatusCode(status), problem)
                .build();
    }

    public static <T> RestResponse<?> from(Either<Failure, Success<T>> e) {
        return e.fold(RestResults::toResponse, RestResults::ok);
    }
}