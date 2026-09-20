package com.hiresphere.dto;

/**
 * StatusUpdateRequest - Request payload for updating active/inactive status.
 */
public class StatusUpdateRequest {

    private Boolean active;

    public StatusUpdateRequest() {}

    public StatusUpdateRequest(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
