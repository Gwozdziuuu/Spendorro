package com.mrngwozdz.api.model.request;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

import java.io.InputStream;

public class FileUploadRequest {
    
    @RestForm("file")
    @PartType("application/octet-stream")
    public InputStream file;
    
    @RestForm("text")
    @PartType("text/plain")
    public String text;
    
    @RestForm("fileName")
    @PartType("text/plain")
    public String fileName;
}