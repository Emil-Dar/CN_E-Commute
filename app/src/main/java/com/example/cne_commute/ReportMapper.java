package com.example.cne_commute;

import java.util.Map;

public class ReportMapper {
    public static Report fromMap(Map<String, Object> data) {
        return new Report(
                (String) data.get("user_id"),
                (String) data.get("driver_id"),
                (String) data.get("driver_name"),
                (String) data.get("driver_contact"),
                (String) data.get("franchise_id"),
                (String) data.get("commuter_name"),
                (String) data.get("commuter_contact"),
                (String) data.get("parking_obstruction_violations"),
                (String) data.get("traffic_movement_violations"),
                (String) data.get("driver_behavior_violations"),
                (String) data.get("licensing_documentation_violations"),
                (String) data.get("attire_fare_violations"),
                (String) data.get("image_description"),
                (String) data.get("image_url"),
                (String) data.get("timestamp")
        );
    }
}
