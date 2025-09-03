package com.mrngwozdz.platform.result;

public record Success<T>(T value) {
    public static Success<Void> unit() { return new Success<>(null); }
    public static <T> Success<T> of(T value) { return new Success<>(value); }
}