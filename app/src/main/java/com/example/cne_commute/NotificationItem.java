package com.example.cne_commute;

public class NotificationItem {

    // -------------------- FIELDS --------------------
    private String type;           // "report" or "appointment"
    private String title;
    private String message;
    private String timestamp;
    private String id;             // reportId or appointmentId

    // Additional report-related fields
    private String driverId;
    private String driverName;
    private String franchiseId;
    private String operatorName;
    private String toda;
    private String violation;
    private String status;
    private String imageUrl;

    private boolean isSelected = false;

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { this.isSelected = selected; }


    // -------------------- CONSTRUCTORS --------------------

    // Full constructor (for report notifications)
    public NotificationItem(
            String type,
            String title,
            String message,
            String timestamp,
            String id,
            String driverId,
            String driverName,
            String franchiseId,
            String operatorName,
            String toda,
            String violation,
            String status,
            String imageUrl
    ) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.id = id;
        this.driverId = driverId;
        this.driverName = driverName;
        this.franchiseId = franchiseId;
        this.operatorName = operatorName;
        this.toda = toda;
        this.violation = violation;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    // Minimal constructor (for appointment or simple notifications)
    public NotificationItem(String type, String title, String message, String timestamp, String id) {
        this(type, title, message, timestamp, id, null, null, null, null, null, null, null, null);
    }

    // -------------------- GETTERS --------------------

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getFranchiseId() {
        return franchiseId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getToda() {
        return toda;
    }

    public String getViolation() {
        return violation;
    }

    public String getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // -------------------- SETTERS (Optional, if you plan to modify objects later) --------------------
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setFranchiseId(String franchiseId) {
        this.franchiseId = franchiseId;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public void setToda(String toda) {
        this.toda = toda;
    }

    public void setViolation(String violation) {
        this.violation = violation;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
