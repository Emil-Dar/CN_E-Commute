package com.example.cne_commute;

public class ScannedQrCode {
    private String operatorName;
    private String age;
    private String homeAddress;
    private String trPlateNumber;
    private String contactNo;

    public ScannedQrCode(String operatorName, String age, String homeAddress, String trPlateNumber, String contactNo) {
        this.operatorName = operatorName;
        this.age = age;
        this.homeAddress = homeAddress;
        this.trPlateNumber = trPlateNumber;
        this.contactNo = contactNo;
    }

    // Getters
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
}
