package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;

public class Operator {

    @SerializedName("operator")
    private String operatorId;

    @SerializedName("password")
    private String password;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("suffix")
    private String suffix;

    @SerializedName("contact_num")
    private String contactNum;

    @SerializedName("address")
    private String address;

    // getters
    public String getOperatorId() { return operatorId; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSuffix() { return suffix; }
    public String getContactNum() { return contactNum; }
    public String getAddress() { return address; }

    // setters (optional, if you want to update locally)
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public void setContactNum(String contactNum) { this.contactNum = contactNum; }
    public void setAddress(String address) { this.address = address; }
}
