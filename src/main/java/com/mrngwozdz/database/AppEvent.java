package com.mrngwozdz.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_events")
public class AppEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial", nullable = false)
    private UUID serial;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    private String description;

    @Column(name = "event_data")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String eventData;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    public AppEvent(UUID serial, String eventType, String description, String eventData) {
        this.serial = serial;
        this.eventType = eventType;
        this.description = description;
        this.eventData = eventData;
        this.createdAt = Instant.now();
    }

    public AppEvent(UUID serial, String eventType, String description) {
        this(serial, eventType, description, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppEvent appEvent = (AppEvent) o;
        return Objects.equals(id, appEvent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "AppEvent{" +
                "id=" + id +
                ", eventType='" + eventType + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}