package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;

public class Appointment {

    @SerializedName("id")
    private String appointmentId;

    @SerializedName("commuter_id")
    private String commuterId;

    @SerializedName("driver_name")
    private String driverName;

    @SerializedName("scheduled_date")
    private String scheduledDate;

    @SerializedName("scheduled_time")
    private String scheduledTime;

    @SerializedName("status")
    private String status;

    public Appointment(String appointmentId, String commuterId, String driverName,
                       String scheduledDate, String scheduledTime, String status) {
        this.appointmentId = appointmentId;
        this.commuterId = commuterId;
        this.driverName = driverName;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.status = status;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getCommuterId() {
        return commuterId;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId='" + appointmentId + '\'' +
                ", commuterId='" + commuterId + '\'' +
                ", driverName='" + driverName + '\'' +
                ", scheduledDate='" + scheduledDate + '\'' +
                ", scheduledTime='" + scheduledTime + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
