package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;

public class Assignment {

    @SerializedName("assignment_id")
    private String assignmentId;

    @SerializedName("driver_id")
    private String driverId;

    @SerializedName("franchise_id")
    private String franchiseId;

    @SerializedName("assigned_at")
    private String assignedAt;

    // constructor with assignmentId
    public Assignment(String assignmentId, String driverId, String franchiseId, String assignedAt) {
        this.assignmentId = assignmentId;
        this.driverId = driverId;
        this.franchiseId = franchiseId;
        this.assignedAt = assignedAt;
    }

    // getters
    public String getAssignmentId() {
        return assignmentId;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getFranchiseId() {
        return franchiseId;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    // setters
    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public void setFranchiseId(String franchiseId) {
        this.franchiseId = franchiseId;
    }

    public void setAssignedAt(String assignedAt) {
        this.assignedAt = assignedAt;
    }
}
