package com.hiresphere.model;

/**
 * Company - Represents a company posted by a recruiter.
 *
 * A Company is created once by a Recruiter.
 * Multiple recruiters can belong to the same company in a real app,
 * but for simplicity here each recruiter has their own Company object.
 */
public class Company {

    private int id;
    private String name;            // e.g., "TechCorp Solutions"
    private String industry;        // e.g., "IT / Software"
    private String website;         // e.g., "https://techcorp.com"
    private String location;        // e.g., "Bangalore, India"
    private String description;     // Short description of the company
    private int size;               // Approximate number of employees
    private String contactEmail;    // e.g., "hr@techcorp.com"
    private String contactPhone;    // e.g., "+91-9876543210"

    // ─── Constructors ─────────────────────────────────────────────────────────

    public Company() {}

    public Company(String name, String industry, String location) {
        this.name = name;
        this.industry = industry;
        this.location = location;
    }

    // ─── Getters and Setters ──────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}
