package com.mrngwozdz.service.publicmessage.process;

import com.mrngwozdz.integration.openai.OpenAiService;
import com.mrngwozdz.integration.openai.model.OpenAiProcessRequest;
import com.mrngwozdz.service.minio.MinioService;
import lombok.Getter;

import java.util.Optional;

@Getter
public class ProcessHelper {

    private Optional<Image> image = Optional.empty();
    private Optional<String> uploadedImageURL = Optional.empty();
    private String token;
    private String model;
    private String text;
    private String outputExample;
    private MinioService minioService;
    private OpenAiService openAiService;
    private OpenAiProcessRequest openAiProcessRequest;

    public ProcessHelper setUploadedImageURL(String uploadedImageURL) {
        this.uploadedImageURL = Optional.of(uploadedImageURL);
        return this;
    }

    public ProcessHelper setImage(Image image) {
        this.image = Optional.of(image);
        return this;
    }

    public ProcessHelper setText(String text) {
        this.text = text;
        return this;
    }

    public ProcessHelper setOutputExample(String outputExample) {
        this.outputExample = outputExample;
        return this;
    }

    public ProcessHelper setMinioService(MinioService minioService) {
        this.minioService = minioService;
        return this;
    }

    public ProcessHelper setToken(String token) {
        this.token = token;
        return this;
    }

    public ProcessHelper setModel(String model) {
        this.model = model;
        return this;
    }

    public ProcessHelper setOpenAiService(OpenAiService openAiService) {
        this.openAiService = openAiService;
        return this;
    }

    public ProcessHelper setOpenAiProcessRequest(OpenAiProcessRequest openAiProcessRequest) {
        this.openAiProcessRequest = openAiProcessRequest;
        return this;
    }

    public record Image(
            byte[] imageData,
            String imageFilename
    ) {}
}
