package com.example.cne_commute;

import java.util.Objects;

public class ScannedQrCode {
    private String id; // Unique ID for each QR code entry
    private String operatorName;
    private String age;
    private String homeAddress;
    private String trPlateNumber;
    private String contactNo;

    public ScannedQrCode(String id, String operatorName, String age, String homeAddress, String trPlateNumber, String contactNo) {
        this.id = id;
        this.operatorName = operatorName;
        this.age = age;
        this.homeAddress = homeAddress;
        this.trPlateNumber = trPlateNumber;
        this.contactNo = contactNo;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getAge() {
        return age;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public String getTrPlateNumber() {
        return trPlateNumber;
    }

    public String getContactNo() {
        return contactNo;
    }

    // Optional but recommended: equals() and hashCode() for object comparison by ID
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
}
