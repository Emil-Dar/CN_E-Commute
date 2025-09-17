package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;

public class ReportData {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("driver_id")
    private String driverId;

    @SerializedName("driver_name")
    private String driverName;

    @SerializedName("driver_contact")
    private String driverContact;

    @SerializedName("franchise_id")
    private String franchiseId;

    @SerializedName("commuter_name")
    private String commuterName;

    @SerializedName("commuter_contact")
    private String commuterContact;

    @SerializedName("parking_obstruction_violations")
    private String parkingObstructionViolations;

    @SerializedName("traffic_movement_violations")
    private String trafficMovementViolations;

    @SerializedName("driver_behavior_violations")
    private String driverBehaviorViolations;

    @SerializedName("licensing_documentation_violations")
    private String licensingDocumentationViolations;

    @SerializedName("attire_fare_violations")
    private String attireFareViolations;

    @SerializedName("image_description")
    private String imageDescription;

    @SerializedName("image_url")
    private String imageUrl;

    // Default constructor for Gson
    public ReportData() {
        // Required for deserialization
    }

    // ✅ Constructor with all required fields
    public ReportData(
            String userId,
            String driverId,
            String driverName,
            String driverContact,
            String franchiseId,
            String commuterName,
            String commuterContact,
            String parkingObstructionViolations,
            String trafficMovementViolations,
            String driverBehaviorViolations,
            String licensingDocumentationViolations,
            String attireFareViolations,
            String imageDescription,
            String imageUrl) {
        this.userId = userId;
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverContact = driverContact;
        this.franchiseId = franchiseId;
        this.commuterName = commuterName;
        this.commuterContact = commuterContact;
        this.parkingObstructionViolations = parkingObstructionViolations;
        this.trafficMovementViolations = trafficMovementViolations;
        this.driverBehaviorViolations = driverBehaviorViolations;
        this.licensingDocumentationViolations = licensingDocumentationViolations;
        this.attireFareViolations = attireFareViolations;
        this.imageDescription = imageDescription;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public String getDriverContact() { return driverContact; }
    public String getFranchiseId() { return franchiseId; }
    public String getCommuterName() { return commuterName; }
    public String getCommuterContact() { return commuterContact; }
    public String getParkingObstructionViolations() { return parkingObstructionViolations; }
    public String getTrafficMovementViolations() { return trafficMovementViolations; }
    public String getDriverBehaviorViolations() { return driverBehaviorViolations; }
    public String getLicensingDocumentationViolations() { return licensingDocumentationViolations; }
    public String getAttireFareViolations() { return attireFareViolations; }
    public String getImageDescription() { return imageDescription; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public void setDriverContact(String driverContact) { this.driverContact = driverContact; }
    public void setFranchiseId(String franchiseId) { this.franchiseId = franchiseId; }
    public void setCommuterName(String commuterName) { this.commuterName = commuterName; }
    public void setCommuterContact(String commuterContact) { this.commuterContact = commuterContact; }
    public void setParkingObstructionViolations(String parkingObstructionViolations) { this.parkingObstructionViolations = parkingObstructionViolations; }
    public void setTrafficMovementViolations(String trafficMovementViolations) { this.trafficMovementViolations = trafficMovementViolations; }
    public void setDriverBehaviorViolations(String driverBehaviorViolations) { this.driverBehaviorViolations = driverBehaviorViolations; }
    public void setLicensingDocumentationViolations(String licensingDocumentationViolations) { this.licensingDocumentationViolations = licensingDocumentationViolations; }
    public void setAttireFareViolations(String attireFareViolations) { this.attireFareViolations = attireFareViolations; }
    public void setImageDescription(String imageDescription) { this.imageDescription = imageDescription; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
