package com.example.cne_commute;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class ReportMapper {

    // Convert raw Supabase map to Report object
    public static Report fromMap(String reportId, Map<String, Object> data) {
        return new Report(
                (String) data.get("user_id"),
                (String) data.get("driver_id"),
                (String) data.get("driver_name"),
                (String) data.get("franchise_id"),
                (String) data.get("operator_name"),
                (String) data.get("toda"),
                (String) data.get("commuter_name"),
                (String) data.get("commuter_contact"),
                (String) data.get("parking_obstruction_violations"),
                (String) data.get("traffic_movement_violations"),
                (String) data.get("driver_behavior_violations"),
                (String) data.get("licensing_documentation_violations"),
                (String) data.get("attire_fare_violations"),
                (String) data.get("image_description"),
                (String) data.get("image_url"),
                (String) data.get("timestamp"),
                (String) data.get("status"),
                (String) data.get("remarks")
        );
    }

    // Convert Report list to ReportData list for UI
    public static List<ReportData> toReportDataList(List<Report> reports) {
        List<ReportData> mapped = new ArrayList<>();
        for (Report report : reports) {
            ReportData data = new ReportData(
                    report.getReportId(),
                    report.getReportCode(), //  Added this line
                    report.getUserId(),
                    report.getDriverId(),
                    report.getDriverName(),
                    report.getFranchiseId(),
                    report.getOperatorName(),
                    report.getToda(),
                    report.getCommuterName(),
                    report.getCommuterContact(),
                    report.getParkingObstruction(),
                    report.getTrafficMovement(),
                    report.getDriverBehavior(),
                    report.getLicensingDocumentation(),
                    report.getAttireFare(),
                    report.getImageDescription(),
                    report.getImageUrl(),
                    report.getStatus(),
                    report.getRemarks(),
                    report.getTimestamp()
            );

            mapped.add(data);
        }
        return mapped;
    }
}
