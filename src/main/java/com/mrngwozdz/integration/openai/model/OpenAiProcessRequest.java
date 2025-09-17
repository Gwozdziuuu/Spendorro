package com.mrngwozdz.integration.openai.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenAiProcessRequest(
        String text,
        String token,
        String model,
        @JsonProperty("image_url")
        String imageUrl,
        @JsonProperty("output_example")
        Object outputExample
        ) {
}
