package com.hiresphere.service;

import com.hiresphere.model.Notification;
import com.hiresphere.store.DataStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * NotificationService - Manages notification creation, retrieval, and read status.
 */
@Service
public class NotificationService {

    private final DataStore dataStore;

    public NotificationService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Create and store a new notification for a user.
     */
    public Notification createNotification(int userId, String type, String message) {
        if (userId <= 0 || message == null || message.trim().isEmpty()) {
            return null;
        }
        Notification notification = new Notification(userId, type, message.trim());
        dataStore.addNotification(notification);
        return notification;
    }

    /**
     * View notifications for a specific user.
     */
    public List<Notification> getNotificationsByUser(int userId) {
        return dataStore.findNotificationsByUserId(userId);
    }

    /**
     * View all notifications.
     */
    public List<Notification> getAllNotifications() {
        return dataStore.getNotifications();
    }

    /**
     * View single notification by ID.
     */
    public Notification getNotificationById(int id) {
        return dataStore.findNotificationById(id);
    }

    /**
     * Mark a notification as read.
     */
    public boolean markAsRead(int id) {
        return dataStore.markNotificationAsRead(id);
    }

    /**
     * Mark all notifications as read for a user.
     */
    public int markAllAsRead(int userId) {
        return dataStore.markAllNotificationsAsReadForUser(userId);
    }
}
