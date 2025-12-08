package com.example.cne_commute;

public class OperatorDriverSummary {

    private String driverId;
    private String driverName;
    private String franchiseId;
    private String assignedDate;
    private int pending;
    private int scheduled;
    private int resolved;
    private int unresolved;

    public OperatorDriverSummary(String driverId, String driverName, String franchiseId,
                                 String assignedDate, int pending, int scheduled,
                                 int resolved, int unresolved) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.franchiseId = franchiseId;
        this.assignedDate = assignedDate;
        this.pending = pending;
        this.scheduled = scheduled;
        this.resolved = resolved;
        this.unresolved = unresolved;
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

    public String getAssignedDate() {
        return assignedDate;
    }

    public int getPending() {
        return pending;
    }

    public int getScheduled() {
        return scheduled;
    }

    public int getResolved() {
        return resolved;
    }

    public int getUnresolved() {
        return unresolved;
    }
}
