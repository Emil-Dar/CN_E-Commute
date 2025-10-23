package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;

public class Driver {
    @SerializedName("driver_id")
    public String driverId;

    @SerializedName("password")
    public String password;

    @SerializedName("driver_name_clean")
    private String fullName;

    @SerializedName("first_name")
    public String firstName;

    @SerializedName("middle_name")
    public String middleName;

    @SerializedName("last_name")
    public String lastName;

    @SerializedName("suffix")
    public String suffix;

    @SerializedName("address")
    public String address;

    @SerializedName("contact_num")
    public String contactNum;

    @SerializedName("license_num")
    public String licenseNum;

    @SerializedName("license_expiration")
    public String licenseExpiration;

    @SerializedName("license_restriction")
    public String licenseRestriction;

    public String getDriverId() {
        return driverId;
    }

    public String getFullName() {
        return fullName;
    }

}
