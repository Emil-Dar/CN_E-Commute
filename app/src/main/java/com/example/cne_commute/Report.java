package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Report implements Serializable {

    @SerializedName("report_id")
    private String reportId;

    @SerializedName("report_code")
    private String reportCode;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("driver_id")
    private String driverId;

    @SerializedName("driver_name")
    private String driverName;

    @SerializedName("franchise_id")
    private String franchiseId;

    @SerializedName("operator_name")
    private String operatorName;

    @SerializedName("toda")
    private String toda;

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

    @SerializedName("status")
    private String status;

    @SerializedName("remarks")
    private String remarks;

    public Report() {
        // Required default constructor
    }

    public Report(String userId, String driverId, String driverName,
                  String franchiseId, String operatorName, String toda,
                  String commuterName, String commuterContact,
                  String parkingObstruction, String trafficMovement,
                  String driverBehavior, String licensingDocumentation,
                  String attireFare, String imageDescription, String imageUrl,
                  String timestamp, String status, String remarks) {
        this.userId = userId;
        this.driverId = driverId;
        this.driverName = driverName;
        this.franchiseId = franchiseId;
        this.operatorName = operatorName;
        this.toda = toda;
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
        this.status = status;
        this.remarks = remarks;
    }

    // Getters
    public String getReportId() { return reportId; }
    public String getReportCode() { return reportCode; }
    public String getUserId() { return userId; }
    public String getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public String getFranchiseId() { return franchiseId; }
    public String getOperatorName() { return operatorName; }
    public String getToda() { return toda; }
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
    public String getStatus() { return status; }
    public String getRemarks() { return remarks; }

    // Setters
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setReportCode(String reportCode) {
        this.reportCode = reportCode;
    }

    // Convert to Map for Supabase submission
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        map.put("driver_id", driverId);
        map.put("driver_name", driverName);
        map.put("franchise_id", franchiseId);
        map.put("operator_name", operatorName);
        map.put("toda", toda);
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
        map.put("status", status);
        map.put("remarks", remarks);
        map.put("report_code", reportCode); //  Include in submission
        return map;
    }

    // Convert from Map (used in local history)
    public static Report fromMap(String reportId, Map<String, Object> data) {
        Report report = new Report();

        report.reportId = reportId;
        report.reportCode = (String) data.get("report_code"); //  NEW
        report.userId = (String) data.get("user_id");
        report.driverId = (String) data.get("driver_id");
        report.driverName = (String) data.get("driver_name");
        report.franchiseId = (String) data.get("franchise_id");
        report.operatorName = (String) data.get("operator_name");
        report.toda = (String) data.get("toda");
        report.commuterName = (String) data.get("commuter_name");
        report.commuterContact = (String) data.get("commuter_contact");
        report.parkingObstruction = (String) data.get("parking_obstruction_violations");
        report.trafficMovement = (String) data.get("traffic_movement_violations");
        report.driverBehavior = (String) data.get("driver_behavior_violations");
        report.licensingDocumentation = (String) data.get("licensing_documentation_violations");
        report.attireFare = (String) data.get("attire_fare_violations");
        report.imageDescription = (String) data.get("image_description");
        report.imageUrl = (String) data.get("image_url");
        report.timestamp = (String) data.get("timestamp");
        report.status = (String) data.get("status");
        report.remarks = (String) data.get("remarks");

        return report;
    }
}
