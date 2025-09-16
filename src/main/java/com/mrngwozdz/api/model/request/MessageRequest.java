package com.mrngwozdz.api.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class MessageRequest {
    private String text;
    private String token;
    private String model;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("output_example")
    private Map<String, Object> outputExample;
    @JsonProperty("image_data")
    private byte[] imageData;
    @JsonProperty("image_filename")
    private String imageFilename;
}