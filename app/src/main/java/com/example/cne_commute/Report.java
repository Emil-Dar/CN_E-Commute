package com.example.cne_commute;

import java.io.Serializable;

public class Report implements Serializable {
    private String violation;
    private String description;
    private String imagePath;

    public Report(String violation, String description, String imagePath) {
        this.violation = violation;
        this.description = description;
        this.imagePath = imagePath;
    }

    public String getViolation() {
        return violation;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }
}
