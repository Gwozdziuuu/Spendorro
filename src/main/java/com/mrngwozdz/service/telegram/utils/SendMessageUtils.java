package com.mrngwozdz.service.telegram.utils;

import com.mrngwozdz.platform.result.ErrorCode;
import com.mrngwozdz.platform.result.Failure;
import com.mrngwozdz.platform.result.Success;
import io.vavr.control.Either;

import jakarta.ws.rs.core.Response;

public final class SendMessageUtils {
    private SendMessageUtils() {}

    public static Either<Failure, Success<String>> processResponse(Response response) {
        if (response.getStatus() >= 200 && response.getStatus() < 300) {
            return Either.right(Success.of("Message sent successfully"));
        } else {
            return Either.left(
                Failure.of(ErrorCode.IO_ERROR, "Failed to send message")
                       .with("status", response.getStatus())
            );
        }
    }
}