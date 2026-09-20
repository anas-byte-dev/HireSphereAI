package com.hiresphere.realtime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RealtimeEvent - Represents a real-time event pushed to connected frontend clients
 * over Server-Sent Events (SSE).
 */
public class RealtimeEvent {

    private String eventType;
    private String entityType;
    private int entityId;
    private String message;
    private String timestamp;
    private Object payload;

    public RealtimeEvent() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public RealtimeEvent(String eventType, String entityType, int entityId, String message, Object payload) {
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.message = message;
        this.payload = payload;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public int getEntityId() { return entityId; }
    public void setEntityId(int entityId) { this.entityId = entityId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
}
