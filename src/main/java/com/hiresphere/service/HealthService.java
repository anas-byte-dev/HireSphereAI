package com.hiresphere.service;

import com.hiresphere.store.DataStore;
import org.springframework.stereotype.Service;

/**
 * HealthService - Handles the logic for the health check endpoint.
 *
 * In Spring Boot:
 *   @Service  = business logic layer
 *   @Component = general Spring-managed bean
 *
 * The Controller calls the Service.
 * The Service calls the DataStore (our in-memory "database").
 *
 * For now this service just returns a status string.
 * We'll add more methods here as the project grows.
 */
@Service
public class HealthService {

    private final DataStore dataStore;

    // Spring automatically injects DataStore here (Constructor Injection)
    public HealthService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Returns a simple status message confirming the backend is running.
     * Also reports how many users are currently in memory.
     */
    public String getStatus() {
        return "UP";
    }

    public int getTotalUsersInMemory() {
        return dataStore.getUsers().size();
    }
}
