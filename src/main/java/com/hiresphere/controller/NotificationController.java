package com.hiresphere.controller;

import com.hiresphere.model.Notification;
import com.hiresphere.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationController - REST API endpoints for user notifications.
 */
@Tag(name = "Notifications", description = "User notifications, interview alerts, and read-status management")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * View notifications, optionally filtered by ?userId={id}.
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestParam(required = false) Integer userId) {
        if (userId != null && userId > 0) {
            List<Notification> list = notificationService.getNotificationsByUser(userId);
            return ResponseEntity.ok(list);
        }
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    /**
     * View notifications for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable int userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    /**
     * View notification by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getNotificationById(@PathVariable int id) {
        Notification notification = notificationService.getNotificationById(id);
        if (notification == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Notification not found with id: " + id));
        }
        return ResponseEntity.ok(notification);
    }

    /**
     * Mark a specific notification as read.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable int id) {
        boolean updated = notificationService.markAsRead(id);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Notification not found with id: " + id));
        }

        Notification notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }

    /**
     * Mark a specific notification as read (standard PUT alias).
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> markAsReadStandard(@PathVariable int id) {
        return markAsRead(id);
    }

    /**
     * Mark all notifications as read for a user.
     */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<?> markAllAsRead(@PathVariable int userId) {
        int count = notificationService.markAllAsRead(userId);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("userId", userId);
        res.put("markedAsReadCount", count);
        res.put("message", "All notifications marked as read.");
        return ResponseEntity.ok(res);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", true);
        body.put("message", message);
        return body;
    }
}
