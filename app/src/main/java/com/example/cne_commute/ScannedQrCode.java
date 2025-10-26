package com.example.cne_commute;

import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ScannedQrCode implements Serializable {
    private static final String TAG = "ScannedQrCode";

    private String driverId;
    private String franchiseId;
    private String driverName;
    private String driverContactNo;
    private String vehiclePlate;
    private String route;
    private String operatorName;
    private String toda;
    private String scanTimestamp;

    private int scanCountToday = 1;
    private List<String> scanTimestamps = new ArrayList<>();
    private boolean isArchived = false;

    // Full constructor
    public ScannedQrCode(String driverId, String franchiseId, String driverName, String driverContactNo,
                         String vehiclePlate, String route, String operatorName, String toda, String scanTimestamp) {
        this.driverId = driverId;
        this.franchiseId = franchiseId;
        this.driverName = driverName;
        this.driverContactNo = driverContactNo;
        this.vehiclePlate = vehiclePlate;
        this.route = route;
        this.operatorName = operatorName;
        this.toda = toda;
        this.scanTimestamp = scanTimestamp;

        if (scanTimestamps == null) {
            scanTimestamps = new ArrayList<>();
        }

        if (scanTimestamp != null && !scanTimestamp.trim().isEmpty()) {
            scanTimestamps.add(scanTimestamp);
            sortTimestampsDescending();
        }
    }

    // Minimal constructor
    public ScannedQrCode(String driverId, String franchiseId, String driverName, String driverContactNo,
                         String scanTimestamp) {
        this(driverId, franchiseId, driverName, driverContactNo, "", "", "", "", scanTimestamp);
    }

    // Getters
    public String getDriverId() {
        return driverId;
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

    public String getOperatorName() {
        return operatorName;
    }

    public String getToda() {
        return toda;
    }

    public String getScanTimestamp() {
        return scanTimestamp;
    }

    public void setScanTimestamp(String scanTimestamp) {
        this.scanTimestamp = scanTimestamp;
    }

    public int getScanCountToday() {
        return scanCountToday;
    }

    public void setScanCountToday(int scanCountToday) {
        this.scanCountToday = scanCountToday;
    }

    public List<String> getScanTimestamps() {
        return scanTimestamps;
    }

    public void setScanTimestamps(List<String> scanTimestamps) {
        this.scanTimestamps = scanTimestamps;
        sortTimestampsDescending();
    }

    public void addScanTimestamp(String timestamp) {
        if (scanTimestamps == null) {
            Log.w(TAG, "scanTimestamps was null — initializing now");
            scanTimestamps = new ArrayList<>();
        }
        if (timestamp != null && !timestamp.trim().isEmpty()) {
            scanTimestamps.add(timestamp);
            sortTimestampsDescending();
            scanCountToday = scanTimestamps.size();
            Log.d(TAG, "Added timestamp: " + timestamp + " | Total today: " + scanCountToday);
        }
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    private void sortTimestampsDescending() {
        scanTimestamps.sort((a, b) -> {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date dateA = format.parse(a);
                Date dateB = format.parse(b);
                return dateB.compareTo(dateA); // Descending
            } catch (Exception e) {
                Log.e(TAG, "Timestamp parse error", e);
                return 0;
            }
        });
    }

    // Object comparison by Driver ID
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ScannedQrCode)) return false;
        ScannedQrCode other = (ScannedQrCode) obj;
        return Objects.equals(driverId, other.driverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverId);
    }

    @Override
    public String toString() {
        return "ScannedQrCode{" +
                "driverId='" + driverId + '\'' +
                ", driverName='" + driverName + '\'' +
                ", driverContactNo='" + driverContactNo + '\'' +
                ", franchiseId='" + franchiseId + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", toda='" + toda + '\'' +
                ", scanTimestamp='" + scanTimestamp + '\'' +
                ", scanCountToday=" + scanCountToday +
                ", scanTimestamps=" + scanTimestamps +
                ", isArchived=" + isArchived +
                '}';
    }
}
