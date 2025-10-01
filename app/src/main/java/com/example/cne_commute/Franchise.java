package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Franchise implements Serializable {

    @SerializedName("franchise_id")
    private String franchiseId;

    @SerializedName("registration_date")
    private String registrationDate;

    @SerializedName("toda")
    private String toda;

    @SerializedName("operator_id")
    private String operatorId;

    @SerializedName("renewal_status")
    private String renewalStatus;

    @SerializedName("renewal_date")
    private String renewalDate;

    // --- getters and setters ---
    public String getFranchiseId() {
        return franchiseId;
    }

    public void setFranchiseId(String franchiseId) {
        this.franchiseId = franchiseId;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getToda() {
        return toda;
    }

    public void setToda(String toda) {
        this.toda = toda;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getRenewalStatus() {
        return renewalStatus;
    }

    public void setRenewalStatus(String renewalStatus) {
        this.renewalStatus = renewalStatus;
    }

    public String getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(String renewalDate) {
        this.renewalDate = renewalDate;
    }
}
