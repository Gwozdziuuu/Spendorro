package com.mrngwozdz.service.appevent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrngwozdz.api.model.EventGroupDTO;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class SseEventService {

    @Inject
    ObjectMapper objectMapper;

    private static final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<String>> sseClients = new ConcurrentHashMap<>();
    private static final AtomicInteger clientIdCounter = new AtomicInteger(0);

    public void onEventGroupUpdate(@Observes EventGroupDTO eventGroup) {
        try {
            String json = objectMapper.writeValueAsString(eventGroup);
            broadcast(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(String message) {
        sseClients.values().forEach(queue -> queue.offer(message));
    }

    public static Multi<String> getEventStream() {
        int clientId = clientIdCounter.incrementAndGet();
        ConcurrentLinkedQueue<String> clientQueue = new ConcurrentLinkedQueue<>();
        sseClients.put(clientId, clientQueue);

        return Multi.createFrom().ticks().every(Duration.ofSeconds(1))
                .onItem().transform(tick -> {
                    String message = clientQueue.poll();
                    return message != null ? message : "";
                })
                .filter(message -> !message.isEmpty())
                .onCancellation().invoke(() -> sseClients.remove(clientId));
    }
}