package com.hiresphere.dto;

/**
 * CompanyRequest - Data sent by the client to create/update company information.
 *
 * Used for: PUT /api/recruiters/{userId}/company
 *
 * Example JSON:
 * {
 *   "name": "TechCorp Solutions",
 *   "description": "Leading IT services company based in Bangalore",
 *   "website": "https://techcorp.com",
 *   "location": "Bangalore, India",
 *   "industry": "IT / Software",
 *   "size": 500,
 *   "contactEmail": "hr@techcorp.com",
 *   "contactPhone": "+91-8012345678"
 * }
 */
public class CompanyRequest {

    private String name;
    private String description;
    private String website;
    private String location;
    private String industry;
    private int size;              // 0 means "not specified" — we skip update if 0
    private String contactEmail;
    private String contactPhone;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public CompanyRequest() {}

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}
