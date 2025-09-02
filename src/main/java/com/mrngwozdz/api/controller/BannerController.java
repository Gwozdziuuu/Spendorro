package com.mrngwozdz.api.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Path("/banner")
public class BannerController {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getBanner() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("banner.txt")) {
            if (is == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Banner file not found")
                        .build();
            }
            
            String bannerText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return Response.ok(bannerText)
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error reading banner file")
                    .build();
        }
    }
}