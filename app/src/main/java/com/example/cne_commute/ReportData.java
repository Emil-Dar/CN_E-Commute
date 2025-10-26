package com.example.cne_commute;

import android.util.Log;
import com.google.gson.annotations.SerializedName;

public class ReportData {

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

    @SerializedName("report_id")
    private String reportId;

    @SerializedName("report_code")
    private String reportCode;

    @SerializedName("status")
    private String status;

    @SerializedName("remarks")
    private String remarks;

    @SerializedName("timestamp")
    private String timestamp;

    public ReportData() {
        // Required for deserialization
    }

    public ReportData(
            String reportId,
            String reportCode,
            String userId,
            String driverId,
            String driverName,
            String franchiseId,
            String operatorName,
            String toda,
            String commuterName,
            String commuterContact,
            String parkingObstructionViolations,
            String trafficMovementViolations,
            String driverBehaviorViolations,
            String licensingDocumentationViolations,
            String attireFareViolations,
            String imageDescription,
            String imageUrl,
            String status,
            String remarks,
            String timestamp) {
        this.reportId = reportId;
        this.reportCode = reportCode;
        this.userId = userId;
        this.driverId = driverId;
        this.driverName = driverName;
        this.franchiseId = franchiseId;
        this.operatorName = operatorName;
        this.toda = toda;
        this.commuterName = commuterName;
        this.commuterContact = commuterContact;
        this.parkingObstructionViolations = parkingObstructionViolations;
        this.trafficMovementViolations = trafficMovementViolations;
        this.driverBehaviorViolations = driverBehaviorViolations;
        this.licensingDocumentationViolations = licensingDocumentationViolations;
        this.attireFareViolations = attireFareViolations;
        this.imageDescription = imageDescription;
        this.imageUrl = imageUrl;
        this.status = status;
        this.remarks = remarks;
        this.timestamp = timestamp;
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
    public String getParkingObstructionViolations() { return parkingObstructionViolations; }
    public String getTrafficMovementViolations() { return trafficMovementViolations; }
    public String getDriverBehaviorViolations() { return driverBehaviorViolations; }
    public String getLicensingDocumentationViolations() { return licensingDocumentationViolations; }
    public String getAttireFareViolations() { return attireFareViolations; }
    public String getImageDescription() { return imageDescription; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public String getRemarks() { return remarks; }
    public String getTimestamp() { return timestamp; }

    // Setters
    public void setReportId(String reportId) { this.reportId = reportId; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public void setFranchiseId(String franchiseId) { this.franchiseId = franchiseId; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public void setToda(String toda) { this.toda = toda; }
    public void setCommuterName(String commuterName) { this.commuterName = commuterName; }
    public void setCommuterContact(String commuterContact) { this.commuterContact = commuterContact; }
    public void setParkingObstructionViolations(String parkingObstructionViolations) { this.parkingObstructionViolations = parkingObstructionViolations; }
    public void setTrafficMovementViolations(String trafficMovementViolations) { this.trafficMovementViolations = trafficMovementViolations; }
    public void setDriverBehaviorViolations(String driverBehaviorViolations) { this.driverBehaviorViolations = driverBehaviorViolations; }
    public void setLicensingDocumentationViolations(String licensingDocumentationViolations) { this.licensingDocumentationViolations = licensingDocumentationViolations; }
    public void setAttireFareViolations(String attireFareViolations) { this.attireFareViolations = attireFareViolations; }
    public void setImageDescription(String imageDescription) { this.imageDescription = imageDescription; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setStatus(String status) { this.status = status; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    // Debugging helper
    @Override
    public String toString() {
        return "ReportData{" +
                "reportId='" + reportId + '\'' +
                ", reportCode='" + reportCode + '\'' +
                ", userId='" + userId + '\'' +
                ", status='" + status + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
