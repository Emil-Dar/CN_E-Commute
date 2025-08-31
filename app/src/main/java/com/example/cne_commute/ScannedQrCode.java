package com.example.cne_commute;

import java.util.Objects;

public class ScannedQrCode {
    private String id;
    private String franchiseId;
    private String driverName;
    private String driverContactNo;
    private String vehiclePlate;
    private String route;
    private String scanTimestamp; // 🕒 Real-time timestamp passed at scan time


    // Full constructor with timestamp
    public ScannedQrCode(String id, String franchiseId, String driverName, String driverContactNo,
                         String vehiclePlate, String route, String scanTimestamp) {
        this.id = id;
        this.franchiseId = franchiseId;
        this.driverName = driverName;
        this.driverContactNo = driverContactNo;
        this.vehiclePlate = vehiclePlate;
        this.route = route;
        this.scanTimestamp = scanTimestamp;
    }

    // Minimal constructor with timestamp
    public ScannedQrCode(String id, String franchiseId, String driverName, String driverContactNo,
                         String scanTimestamp) {
        this(id, franchiseId, driverName, driverContactNo, "", "", scanTimestamp);
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getFranchiseId() {
        return franchiseId;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverContactNo() {
        return driverContactNo;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public String getRoute() {
        return route;
    }

    public String getScanTimestamp() {
        return scanTimestamp;
    }

    // Object comparison by ID
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ScannedQrCode)) return false;
        ScannedQrCode other = (ScannedQrCode) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ScannedQrCode{" +
                "driverName='" + driverName + '\'' +
                ", driverContactNo='" + driverContactNo + '\'' +
                ", franchiseId='" + franchiseId + '\'' +
                ", scanTimestamp='" + scanTimestamp + '\'' +
                '}';
    }
}
