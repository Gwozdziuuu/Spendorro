package com.mrngwozdz.api.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MessageRequest {
    private String text;
    @JsonProperty("image_data")
    private byte[] imageData;
    @JsonProperty("image_filename")
    private String imageFilename;
}