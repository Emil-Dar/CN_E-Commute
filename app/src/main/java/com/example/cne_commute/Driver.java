package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;

public class Driver {

    @SerializedName("driver_id")
    private String driverId;

    @SerializedName("driver_name_lookup")
    private String driverNameLookup;

    public String getDriverId() {
        return driverId;
    }

    public String getDriverNameLookup() {
        return driverNameLookup;
    }
}
