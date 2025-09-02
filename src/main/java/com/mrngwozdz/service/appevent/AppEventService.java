package com.mrngwozdz.service.appevent;

import com.mrngwozdz.service.appevent.data.EventRepository;
import com.mrngwozdz.api.model.request.EventRequest;
import com.mrngwozdz.api.model.EventGroupDTO;
import com.mrngwozdz.api.model.EventItemDTO;
import com.mrngwozdz.database.AppEvent;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class AppEventService {

    private final EventRepository eventRepository;
    private final Event<EventGroupDTO> eventBroadcast;

    @Transactional
    public void logEvent(UUID serial, EventRequest request) {
        AppEvent event = new AppEvent(
                serial,
                request.eventType(),
                request.description(),
                request.eventData()
        );
        eventRepository.persist(event);
        
        EventGroupDTO group = buildEventGroup(serial);
        if (group != null) {
            eventBroadcast.fire(group);
        }
    }

    public Multi<String> getEventStream() {
        return SseEventService.getEventStream();
    }

    private EventGroupDTO buildEventGroup(UUID serial) {
        List<AppEvent> groupEvents = eventRepository.findBySerial(serial);
        if (groupEvents.isEmpty()) {
            return null;
        }
        
        groupEvents.sort(Comparator.comparing(AppEvent::getCreatedAt));
        String methodName = extractMethodName(groupEvents.get(0).getDescription());
        Long duration = calculateDuration(groupEvents);
        String status = groupEvents.stream()
                .anyMatch(e -> "API_ERROR".equals(e.getEventType())) ? "FAILURE" : "SUCCESS";
        List<EventItemDTO> eventItems = groupEvents.stream()
                .map(e -> new EventItemDTO(e.getEventType(), e.getDescription(), e.getEventData(), e.getCreatedAt()))
                .toList();
        
        return new EventGroupDTO(serial, methodName, duration, status, eventItems);
    }

    public List<EventGroupDTO> getRecentEventGroups(int limit) {
        List<AppEvent> events = eventRepository.findRecent(limit * 10);
        
        Map<UUID, List<AppEvent>> groupedEvents = events.stream()
                .collect(Collectors.groupingBy(AppEvent::getSerial));
        
        return groupedEvents.entrySet().stream()
                .map(entry -> {
                    UUID serial = entry.getKey();
                    List<AppEvent> groupEvents = entry.getValue();
                    groupEvents.sort(Comparator.comparing(AppEvent::getCreatedAt));
                    String methodName = extractMethodName(groupEvents.getFirst().getDescription());
                    Long duration = calculateDuration(groupEvents);
                    String status = groupEvents.stream()
                            .anyMatch(e -> "API_ERROR".equals(e.getEventType())) ? "FAILURE" : "SUCCESS";
                    List<EventItemDTO> eventItems = groupEvents.stream()
                            .map(e -> new EventItemDTO(e.getEventType(), e.getDescription(), e.getEventData(), e.getCreatedAt()))
                            .toList();
                    
                    return new EventGroupDTO(serial, methodName, duration, status, eventItems);
                })
                .sorted((g1, g2) -> g2.events().getFirst().createdAt().compareTo(g1.events().getFirst().createdAt()))
                .limit(limit)
                .toList();
    }
    
    private String extractMethodName(String description) {
        if (description.contains(".")) {
            String[] parts = description.split("\\.");
            if (parts.length > 1) {
                return parts[parts.length - 1];
            }
        }
        return "unknown";
    }

    private long calculateDuration(List<AppEvent> events) {
        if (events == null || events.size() < 2) return 0L;

        var first = events.stream()
                .map(AppEvent::getCreatedAt)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);

        var last = events.stream()
                .map(AppEvent::getCreatedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        if (first == null || last == null) return 0L;
        return Math.max(Duration.between(first, last).toMillis(), 0L);
    }
}