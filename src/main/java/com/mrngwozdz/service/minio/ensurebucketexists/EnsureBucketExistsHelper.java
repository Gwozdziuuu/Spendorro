package com.mrngwozdz.service.minio.ensurebucketexists;

import lombok.Getter;

@Getter
public final class EnsureBucketExistsHelper {
    private boolean bucketExists;
    public EnsureBucketExistsHelper setBucketExists(boolean v) { this.bucketExists = v; return this; }
}