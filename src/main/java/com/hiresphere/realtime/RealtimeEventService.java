package com.hiresphere.realtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * RealtimeEventService - Manages real-time client connections and broadcasts
 * live database changes to all connected frontends via Server-Sent Events (SSE).
 * 100% Free, zero external infrastructure required.
 */
@Service
public class RealtimeEventService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeEventService.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Registers a new client connection for real-time updates.
     * Timeout set to 30 minutes with heartbeat.
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(1800000L); // 30 mins

        emitters.add(emitter);
        log.info("New real-time client connected. Active subscribers: {}", emitters.size());

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("Client disconnected normally. Active subscribers: {}", emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
            log.info("Client connection timed out. Active subscribers: {}", emitters.size());
        });

        emitter.onError(e -> {
            emitters.remove(emitter);
            log.warn("Real-time client connection error: {}", e.getMessage());
        });

        // Send initial handshake event
        try {
            RealtimeEvent handshake = new RealtimeEvent(
                    "CONNECTED",
                    "SYSTEM",
                    0,
                    "Connected to HireSphere AI Real-Time Stream",
                    null
            );
            emitter.send(SseEmitter.event()
                    .name("hire-sphere-realtime")
                    .data(handshake));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    /**
     * Broadcasts a real-time event to all active clients.
     */
    public void broadcast(String eventType, String entityType, int entityId, String message, Object payload) {
        RealtimeEvent event = new RealtimeEvent(eventType, entityType, entityId, message, payload);
        broadcast(event);
    }

    /**
     * Broadcasts a pre-constructed RealtimeEvent.
     */
    public void broadcast(RealtimeEvent event) {
        log.info("Broadcasting real-time event: {} for {} #{}", event.getEventType(), event.getEntityType(), event.getEntityId());

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("hire-sphere-realtime")
                        .data(event));
            } catch (Exception ex) {
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            log.debug("Removed {} dead subscribers. Active subscribers: {}", deadEmitters.size(), emitters.size());
        }
    }

    public int getActiveSubscriberCount() {
        return emitters.size();
    }
}
