package com.hiresphere.realtime;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * RealtimeController - Exposes SSE stream for real-time live database updates.
 */
@RestController
@RequestMapping("/api/realtime")
@Tag(name = "Real-Time Engine", description = "Server-Sent Events and live database event streaming")
public class RealtimeController {

    private final RealtimeEventService realtimeEventService;

    public RealtimeController(RealtimeEventService realtimeEventService) {
        this.realtimeEventService = realtimeEventService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to live real-time events", description = "Subscribes the client to real-time updates for jobs, applications, interviews, and notifications.")
    public SseEmitter streamEvents() {
        return realtimeEventService.subscribe();
    }

    @GetMapping("/status")
    @Operation(summary = "Get real-time connection status", description = "Returns active subscriber count and database sync status.")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "ACTIVE");
        status.put("activeSubscribers", realtimeEventService.getActiveSubscriberCount());
        status.put("protocol", "Server-Sent Events (SSE)");
        status.put("databaseSync", "REALTIME_PERSISTENT");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/ping")
    @Operation(summary = "Broadcast a manual ping test", description = "Emits a test ping to all connected real-time clients.")
    public ResponseEntity<Map<String, Object>> ping(@RequestParam(defaultValue = "Ping from HireSphere AI") String message) {
        realtimeEventService.broadcast("PING", "SYSTEM", 0, message, null);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Ping broadcasted to " + realtimeEventService.getActiveSubscriberCount() + " clients.");
        return ResponseEntity.ok(res);
    }
}
