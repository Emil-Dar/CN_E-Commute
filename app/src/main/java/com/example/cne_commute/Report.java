package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Report implements Serializable {

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
    private String parkingObstruction;

    @SerializedName("traffic_movement_violations")
    private String trafficMovement;

    @SerializedName("driver_behavior_violations")
    private String driverBehavior;

    @SerializedName("licensing_documentation_violations")
    private String licensingDocumentation;

    @SerializedName("attire_fare_violations")
    private String attireFare;

    @SerializedName("image_description")
    private String imageDescription;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("timestamp")
    private String timestamp;

    public Report() {
        // Required default constructor
    }

    public Report(String userId, String driverId, String driverName, String driverContact,
                  String franchiseId, String commuterName, String commuterContact,
                  String parkingObstruction, String trafficMovement,
                  String driverBehavior, String licensingDocumentation,
                  String attireFare, String imageDescription, String imageUrl,
                  String timestamp) {
        this.userId = userId;
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverContact = driverContact;
        this.franchiseId = franchiseId;
        this.commuterName = commuterName;
        this.commuterContact = commuterContact;
        this.parkingObstruction = parkingObstruction;
        this.trafficMovement = trafficMovement;
        this.driverBehavior = driverBehavior;
        this.licensingDocumentation = licensingDocumentation;
        this.attireFare = attireFare;
        this.imageDescription = imageDescription;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // ✅ Getters
    public String getUserId() { return userId; }
    public String getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public String getDriverContact() { return driverContact; }
    public String getFranchiseId() { return franchiseId; }
    public String getCommuterName() { return commuterName; }
    public String getCommuterContact() { return commuterContact; }
    public String getParkingObstruction() { return parkingObstruction; }
    public String getTrafficMovement() { return trafficMovement; }
    public String getDriverBehavior() { return driverBehavior; }
    public String getLicensingDocumentation() { return licensingDocumentation; }
    public String getAttireFare() { return attireFare; }
    public String getImageDescription() { return imageDescription; }
    public String getImageUrl() { return imageUrl; }
    public String getTimestamp() { return timestamp; }

    // ✅ Setter for imageUrl
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // ✅ Convert to Map for Supabase submission
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        map.put("driver_id", driverId);
        map.put("driver_name", driverName);
        map.put("driver_contact", driverContact);
        map.put("franchise_id", franchiseId);
        map.put("commuter_name", commuterName);
        map.put("commuter_contact", commuterContact);
        map.put("parking_obstruction_violations", parkingObstruction);
        map.put("traffic_movement_violations", trafficMovement);
        map.put("driver_behavior_violations", driverBehavior);
        map.put("licensing_documentation_violations", licensingDocumentation);
        map.put("attire_fare_violations", attireFare);
        map.put("image_description", imageDescription);
        map.put("image_url", imageUrl);
        map.put("timestamp", timestamp);
        return map;
    }
}
