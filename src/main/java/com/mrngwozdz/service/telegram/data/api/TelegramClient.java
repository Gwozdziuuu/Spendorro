package com.mrngwozdz.service.telegram.data.api;

import com.mrngwozdz.service.telegram.data.api.model.TelegramMessageRequest;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "telegram-api")
public interface TelegramClient {

    @POST
    @Path("/bot{token}/sendMessage")
    @Consumes(MediaType.APPLICATION_JSON)
    Response sendMessage(@PathParam("token") String token, TelegramMessageRequest message);

}