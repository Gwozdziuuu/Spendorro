package com.mrngwozdz.configuration.properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "minio")
public interface MinioProperties {

    String endpoint();

    String accessKey();

    String secretKey();

    @WithDefault("images")
    String bucketName();
}