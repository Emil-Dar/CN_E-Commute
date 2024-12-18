package com.example.cne_commute;

public class ScannedQrCode {
    private String date;
    private String time;
    private String transactionNumber;

    public ScannedQrCode(String date, String time, String transactionNumber) {
        this.date = date;
        this.time = time;
        this.transactionNumber = transactionNumber;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }
}
