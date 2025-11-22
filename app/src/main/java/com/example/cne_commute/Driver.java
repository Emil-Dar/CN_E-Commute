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

    // ✅ Add these missing important fields
    @SerializedName("requested_by")
    public String requestedBy;

    @SerializedName("status")
    public String status;

    @SerializedName("created_at")
    public String createdAt;

    // ======================
    // ✅ Getters
    // ======================
    public String getDriverId() { return driverId; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getSuffix() { return suffix; }
    public String getContactNum() { return contactNum; }
    public String getAddress() { return address; }
    public String getLicenseNum() { return licenseNum; }
    public String getLicenseExpiration() { return licenseExpiration; }
    public String getLicenseRestriction() { return licenseRestriction; }

    // ✅ Needed by notifications
    public String getRequestedBy() { return requestedBy; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    // ======================
    // ✅ Setters
    // ======================
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public void setContactNum(String contactNum) { this.contactNum = contactNum; }
    public void setAddress(String address) { this.address = address; }
    public void setLicenseNum(String licenseNum) { this.licenseNum = licenseNum; }
    public void setLicenseExpiration(String licenseExpiration) { this.licenseExpiration = licenseExpiration; }
    public void setLicenseRestriction(String licenseRestriction) { this.licenseExpiration = licenseRestriction; }

    // ✅ Add setters for new fields if needed
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
