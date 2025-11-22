package com.example.cne_commute.driver_fragments;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cne_commute.R;
import com.example.cne_commute.SupabaseApiClient;
import com.example.cne_commute.SupabaseService;

import java.text.*;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverComplaints extends Fragment {

    private LinearLayout activeComplaintsContainer;
    private LinearLayout historyComplaintsContainer;
    private TextView activeHeader;
    private TextView historyHeader;
    private ProgressBar progressBar;
    private TextView emptyText;
    private ScrollView scrollView;
    private EditText searchBar;
    private Spinner dateFilter;

    private String driverId = "";
    private String highlightReportId = "";
    private boolean keepHighlightFromNotification = false; // if true, keep highlight until user clears it

    // master list of reports fetched from backend (filtered client-side)
    private List<Map<String, Object>> allReports = new ArrayList<>();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static DriverComplaints newInstance(String driverId, String highlightReportId) {
        DriverComplaints fragment = new DriverComplaints();
        Bundle args = new Bundle();
        args.putString("driver_id", driverId);
        args.putString("highlight_report_id", highlightReportId);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_complaints, container, false);

        activeComplaintsContainer = view.findViewById(R.id.activeComplaintsContainer);
        historyComplaintsContainer = view.findViewById(R.id.historyComplaintsContainer);
        activeHeader = view.findViewById(R.id.activeHeader);
        historyHeader = view.findViewById(R.id.historyHeader);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        scrollView = view.findViewById(R.id.scrollView);
        searchBar = view.findViewById(R.id.searchBar);
        dateFilter = view.findViewById(R.id.dateFilter);

        if (getArguments() != null) {
            driverId = toDriverCode(getArguments().getString("driver_id", ""));
            highlightReportId = getArguments().getString("highlight_report_id", "");
            keepHighlightFromNotification = highlightReportId != null && !highlightReportId.trim().isEmpty();
        }

        setupFilters();
        fetchComplaints();

        return view;
    }

    // ---------- FETCH DATA ----------
    private void fetchComplaints() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        SupabaseService service = SupabaseApiClient.getRetrofitInstance()
                .create(SupabaseService.class);

        service.getReportsByDriverId(
                SupabaseApiClient.SUPABASE_API_KEY,
                "Bearer " + SupabaseApiClient.SUPABASE_API_KEY,
                "eq." + driverId, // keep original driverId
                "created_at.desc",
                "*"
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                clearContainers();

                if (response.isSuccessful() && response.body() != null) {
                    allReports = new ArrayList<>(response.body());

                    // ---------- DEBUG: log fetched reports ----------
                    for (Map<String, Object> r : allReports) {
                        Log.d("DriverComplaints", "Fetched ReportID: " + r.get("report_id") +
                                " DriverID: " + r.get("driver_id"));
                    }

                    // ---------- FIXED DRIVER FILTER ----------
                    allReports.removeIf(r -> {
                        Object d = r.get("driver_id");
                        if (d == null) return true;

                        // just trim and compare case-insensitive
                        String reportDriverId = d.toString().trim();
                        String normalizedDriverId = driverId.trim();
                        boolean match = reportDriverId.equalsIgnoreCase(normalizedDriverId);

                        Log.d("DriverComplaints", "Comparing reportDriverId='" + reportDriverId +
                                "' vs driverId='" + normalizedDriverId + "' -> " + match);

                        return !match;
                    });

                    Log.d("DriverComplaints", "After filtering, " + allReports.size() + " reports remain");

                    if (allReports.isEmpty()) {
                        showEmpty("No complaints found.");
                        return;
                    }

                    displayComplaints(allReports);

                    if (keepHighlightFromNotification && !highlightReportId.isEmpty()) {
                        mainHandler.postDelayed(() -> highlightComplaint(highlightReportId), 500);
                    }

                } else {
                    showEmpty("Failed to load complaints.");
                    Log.w("DriverComplaints", "Failed fetching: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                showEmpty("Error loading complaints.");
                Log.e("DriverComplaints", "Network error fetching reports", t);
            }
        });
    }




    // ---------- DISPLAY ----------
    private void displayComplaints(List<Map<String, Object>> reports) {
        clearContainers();
        if (emptyText != null) emptyText.setVisibility(View.GONE);

        if (reports == null || reports.isEmpty()) {
            showEmpty("No complaints found.");
            return;
        }

        List<Map<String, Object>> active = new ArrayList<>();
        List<Map<String, Object>> history = new ArrayList<>();

        // Separate active vs history complaints — use status explicitly
        for (Map<String, Object> r : reports) {
            if (r == null) continue;
            String stat = str(r.get("status")).trim();
            String remarks = str(r.get("remarks")).trim();

            // ✅ Use exact Supabase values, no guessing
            if (stat.isEmpty() && remarks.isEmpty()) {
                stat = "Pending";
            }

            // Normalize capitalization for display only
            r.put("status", capitalize(stat));
            r.put("remarks", capitalize(remarks));

            // ✅ Separate based on status only
            if ("Resolved".equalsIgnoreCase(stat) || "Unresolved".equalsIgnoreCase(stat)) {
                history.add(r);
            } else {
                active.add(r);
            }
        }


        Log.d("DriverComplaints", "Active: " + active.size() + ", History: " + history.size());

        // Sort newest first
        sortReports(active);
        sortReports(history);

        // Display Active Complaints
        if (!active.isEmpty()) {
            if (activeHeader != null) activeHeader.setVisibility(View.VISIBLE);
            for (Map<String, Object> r : active) {
                addComplaintCard(r, activeComplaintsContainer);
            }
        }

        // Display Complaint History (Resolved & Unresolved)
        if (!history.isEmpty()) {
            if (historyHeader != null) historyHeader.setVisibility(View.VISIBLE);
            for (Map<String, Object> r : history) {
                addComplaintCard(r, historyComplaintsContainer);
            }
        }

        // If both are empty, show empty message
        if (active.isEmpty() && history.isEmpty()) {
            showEmpty("No complaints found.");
        }
    }

    private String toDriverCode(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";
        raw = raw.replace("DR", "").replace("dr", "").trim();

        try {
            int num = Integer.parseInt(raw);
            return String.format("DR%04d", num);
        } catch (Exception e) {
            return raw; // already DR0001 format
        }
    }



    private void sortReports(List<Map<String, Object>> list) {
        Collections.sort(list, (a, b) -> {
            try {
                Date da = parseManila(str(a.get("created_at")));
                Date db = parseManila(str(b.get("created_at")));
                // newest first
                return db.compareTo(da);
            } catch (Exception e) {
                return 0;
            }
        });
    }

    // ---------- CARD ----------
    private void addComplaintCard(Map<String, Object> report, LinearLayout container) {
        if (container == null || report == null) return;

        View cardView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_complaint_card, container, false);

        // ---------------- FIND VIEWS ----------------
        TextView reportId = safeFind(cardView, R.id.reportId);
        TextView status = safeFind(cardView, R.id.status);
        TextView remarks = safeFind(cardView, R.id.remarks);
        TextView violations = safeFind(cardView, R.id.violations);
        TextView message = safeFind(cardView, R.id.message);
        TextView appointment = safeFind(cardView, R.id.appointment);
        TextView date = safeFind(cardView, R.id.date);
        CardView cardRoot = safeFind(cardView, R.id.cardRoot);

        // ---------------- FETCH DATA ----------------
        String id = str(report.get("report_id"));
        String stat = str(report.get("status")).trim();
        if (stat.isEmpty()) stat = "Pending";
        String desc = str(report.get("description"));
        String created = str(report.get("created_at"));
        String reportRemarks = str(report.get("remarks"));
        String resolvedAt = str(report.get("resolved_at"));

        // ---------------- BUILD VIOLATIONS ----------------
        StringBuilder vio = new StringBuilder();
        List<String> violationFields = Arrays.asList(
                "parking_obstruction_violations",
                "traffic_movement_violations",
                "driver_behavior_violations",
                "licensing_documentation_violations",
                "attire_fare_violations"
        );
        for (String field : violationFields) {
            Object value = report.get(field);
            if (value != null && !"none".equalsIgnoreCase(String.valueOf(value))) {
                String label = capitalize(field.replace("_violations", "").replace("_", " "));
                vio.append("• ").append(label).append(": ").append(value).append("\n");
            }
        }

        // ---------------- SET DATA TO VIEWS ----------------
        if (reportId != null) reportId.setText("Report ID: " + id);
        if (status != null) status.setText(stat);
        if (violations != null) violations.setText(vio.toString().trim());
        if (message != null) message.setText(desc.isEmpty() ? "Complaint filed by commuter." : desc);

        if (date != null) {
            String filedOn = "Filed on: " + (created.isEmpty() ? "N/A" : formatDate(created));
            if ("Resolved".equalsIgnoreCase(stat) && !resolvedAt.isEmpty()) {
                filedOn += "\nResolved on: " + formatDate(resolvedAt);
            }
            date.setText(filedOn);
        }

        // ---------------- REMARKS (always visible) ----------------
        if (remarks != null) {
            remarks.setVisibility(View.VISIBLE);
            String r = reportRemarks.isEmpty() ? "N/A" : reportRemarks;
            remarks.setText("Remarks: " + r);
        }

        // ---------------- STATUS-SPECIFIC HANDLING ----------------
        // Ensure appointment view defaults to GONE
        if (appointment != null) appointment.setVisibility(View.GONE);

        if ("Pending".equalsIgnoreCase(stat)) {
            if (message != null)
                message.setText("A commuter reported a complaint. The PSTMU office is reviewing it.");
            // remarks already visible (may show N/A)
        } else if ("Accepted".equalsIgnoreCase(stat)) {
            if (message != null) message.setText("The complaint has been verified and accepted.");
            // Show remarks already
            fetchAppointmentForReport(id, appointment); // some accepted complaints may have appointment
        } else if ("Scheduled".equalsIgnoreCase(stat)) {
            if (message != null) message.setText(desc.isEmpty() ? "Complaint scheduled for appointment." : desc);
            // Remarks shown already
            // fetch scheduled date/time from appointments table
            fetchAppointmentForReport(id, appointment);
        } else if ("Resolved".equalsIgnoreCase(stat)) {
            if (message != null)
                message.setText(desc.isEmpty() ? "Complaint resolved." : desc);
            // remarks shown
        } else if ("Unresolved".equalsIgnoreCase(stat)) {
            if (message != null)
                message.setText(desc.isEmpty() ? "Complaint unresolved." : desc);
            // remarks shown
        } else {
            // fallback for other statuses
            if (message != null) message.setText(desc.isEmpty() ? "Complaint filed by commuter." : desc);
        }

        // ---------------- STATUS BADGE COLOR ----------------
        int badgeColor = ContextCompat.getColor(requireContext(), R.color.dark_gray);
        String displayStatus = stat;

        try {
            String lowerRemarks = reportRemarks.toLowerCase(Locale.ROOT);
            String lowerStatus = stat.toLowerCase(Locale.ROOT);

            // ✅ Only consider remarks if they say "resolved" but NOT "unresolved"
            boolean remarksIndicateResolved =
                    lowerRemarks.contains("resolved") && !lowerRemarks.contains("unresolved");

            // ✅ Determine final status for display
            if (remarksIndicateResolved && !"unresolved".equalsIgnoreCase(stat)) {
                displayStatus = "Resolved";
                badgeColor = ContextCompat.getColor(requireContext(), R.color.status_resolved_green);
            } else {
                switch (lowerStatus) {
                    case "pending":
                        badgeColor = ContextCompat.getColor(requireContext(), R.color.status_pending_red);
                        break;
                    case "accepted":
                        badgeColor = ContextCompat.getColor(requireContext(), R.color.status_accepted_yellow);
                        break;
                    case "scheduled":
                        badgeColor = ContextCompat.getColor(requireContext(), R.color.status_scheduled_blue);
                        break;
                    case "resolved":
                        badgeColor = ContextCompat.getColor(requireContext(), R.color.status_resolved_green);
                        break;
                    case "unresolved":
                        badgeColor = ContextCompat.getColor(requireContext(), R.color.status_unresolved_gray);
                        break;
                    default:
                        badgeColor = ContextCompat.getColor(requireContext(), R.color.dark_gray);
                        break;
                }
            }
        } catch (Exception e) {
            Log.w("DriverComplaints", "Badge color fallback: " + e.getMessage());
        }

        if (status != null) {
            status.setText(displayStatus);
            Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.status_badge_bg);
            if (drawable instanceof GradientDrawable) {
                GradientDrawable bgShape = (GradientDrawable) drawable.mutate();
                try {
                    bgShape.setColor(badgeColor);
                    status.setBackground(bgShape);
                } catch (Exception e) {
                    status.setBackgroundColor(badgeColor);
                }
            } else {
                status.setBackgroundColor(badgeColor);
            }
        }

        // ---------------- HIGHLIGHT CLICKED NOTIFICATION ----------------
        if (!highlightReportId.isEmpty() && highlightReportId.equals(id) && cardRoot != null) {
            Drawable unviewedBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_notification_unviewed);
            Drawable viewedBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_notification_viewed);

            cardRoot.setBackground(unviewedBg);

            if (scrollView != null) {
                scrollView.post(() -> scrollView.smoothScrollTo(0, cardView.getTop()));
            }

            // fade after viewing
            if (!keepHighlightFromNotification) {
                mainHandler.postDelayed(() -> {
                    if (isAdded()) cardRoot.setBackground(viewedBg);
                }, 4000);
            }

            // tap to clear
            cardRoot.setOnClickListener(v -> {
                cardRoot.setBackground(viewedBg);
                highlightReportId = "";
                keepHighlightFromNotification = false;
            });
        } else {
            // Normal viewed state
            cardRoot.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_notification_viewed));
            cardRoot.setOnClickListener(null);
        }

        // ---------------- ADD TO CONTAINER ----------------
        container.addView(cardView);
    }

    // ---------- DATE UTILITIES ----------
    private Date parseManila(String iso) throws ParseException {
        if (iso == null || iso.trim().isEmpty()) throw new ParseException("empty", 0);
        List<String> patterns = Arrays.asList(
                "yyyy-MM-dd HH:mm:ss.SSSSSS",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
        );
        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                return sdf.parse(iso);
            } catch (Exception ignored) {
            }
        }
        // fallback to current time
        return new Date();
    }

    private String formatDate(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "N/A";
        try {
            List<String> patterns = Arrays.asList(
                    "yyyy-MM-dd HH:mm:ss.SSSSSS",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd"
            );
            Date parsed = null;
            for (String p : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                    parsed = sdf.parse(timestamp);
                    if (parsed != null) break;
                } catch (Exception ignored) {
                }
            }
            if (parsed == null) return timestamp;
            SimpleDateFormat out = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            out.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
            return out.format(parsed);
        } catch (Exception e) {
            return timestamp;
        }
    }

    private String formatDateTimeFriendly(String dateStr, String timeStr) {
        // dateStr expected "yyyy-MM-dd" or similar; timeStr expected "HH:mm:ss" or "HH:mm"
        if ((dateStr == null || dateStr.isEmpty()) && (timeStr == null || timeStr.isEmpty()))
            return "N/A";

        String combined = (dateStr != null ? dateStr : "") + " " + (timeStr != null ? timeStr : "");
        try {
            // try common patterns
            List<String> patterns = Arrays.asList(
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd HH:mm",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm",
                    "yyyy-MM-dd"
            );
            Date parsed = null;
            for (String p : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                    parsed = sdf.parse(combined.trim());
                    if (parsed != null) break;
                } catch (Exception ignored) {
                }
            }
            if (parsed == null) {
                // try parsing date-only then append time
                if (dateStr != null && !dateStr.isEmpty()) {
                    Date d = parseManila(dateStr);
                    if (timeStr != null && !timeStr.isEmpty()) {
                        // try to parse time and combine hours/minutes
                        try {
                            SimpleDateFormat timeParse = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                            Date t = timeParse.parse(timeStr);
                            Calendar cd = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
                            cd.setTime(d);
                            Calendar ct = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
                            ct.setTime(t);
                            cd.set(Calendar.HOUR_OF_DAY, ct.get(Calendar.HOUR_OF_DAY));
                            cd.set(Calendar.MINUTE, ct.get(Calendar.MINUTE));
                            parsed = cd.getTime();
                        } catch (Exception ignored) {
                            parsed = d;
                        }
                    } else parsed = d;
                }
            }
            if (parsed == null) return combined.trim();
            SimpleDateFormat out = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            out.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
            return out.format(parsed);
        } catch (Exception e) {
            return combined.trim();
        }
    }

    // ---------- FILTERS ----------
    private void setupFilters() {
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterComplaints(s.toString(),
                            dateFilter != null && dateFilter.getSelectedItem() != null ?
                                    dateFilter.getSelectedItem().toString() : "");
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (dateFilter != null) {
            dateFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    filterComplaints(searchBar != null ? searchBar.getText().toString() : "",
                            parent.getSelectedItem() != null ? parent.getSelectedItem().toString() : "");
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void filterComplaints(String query, String dateRange) {
        if (allReports == null || allReports.isEmpty()) {
            showEmpty("No complaints found.");
            return;
        }

        List<Map<String, Object>> filtered = new ArrayList<>();
        Date now = new Date();

        String qLower = query == null ? "" : query.toLowerCase(Locale.getDefault()).trim();

        for (Map<String, Object> r : allReports) {
            String violations = collectViolations(r).toLowerCase(Locale.getDefault());
            String remarks = str(r.get("remarks")).toLowerCase(Locale.getDefault());
            String desc = str(r.get("description")).toLowerCase(Locale.getDefault());

            boolean matchesSearch = qLower.isEmpty() ||
                    violations.contains(qLower) ||
                    remarks.contains(qLower) ||
                    desc.contains(qLower);

            boolean matchesDate = true;
            if (dateRange != null && !dateRange.trim().isEmpty()) {
                try {
                    Date created = parseManila(str(r.get("created_at")));
                    Calendar calNow = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
                    calNow.setTime(now);

                    if ("Today".equalsIgnoreCase(dateRange)) {
                        Calendar c2 = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
                        c2.setTime(created);
                        matchesDate = calNow.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                                && calNow.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
                    } else if ("This Week".equalsIgnoreCase(dateRange)) {
                        Calendar start = (Calendar) calNow.clone();
                        start.set(Calendar.DAY_OF_WEEK, calNow.getFirstDayOfWeek());
                        start.set(Calendar.HOUR_OF_DAY, 0);
                        start.set(Calendar.MINUTE, 0);
                        start.set(Calendar.SECOND, 0);
                        Calendar end = (Calendar) start.clone();
                        end.add(Calendar.DAY_OF_YEAR, 7);
                        matchesDate = !created.before(start.getTime()) && created.before(end.getTime());
                    } else if ("This Month".equalsIgnoreCase(dateRange)) {
                        Calendar c2 = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
                        c2.setTime(created);
                        matchesDate = calNow.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                                && calNow.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
                    }
                } catch (Exception ignored) {}
            }

            if (matchesSearch && matchesDate) filtered.add(r);
        }

        displayComplaints(filtered);
    }

    private String collectViolations(Map<String, Object> report) {
        StringBuilder vio = new StringBuilder();
        for (String field : Arrays.asList(
                "parking_obstruction_violations",
                "traffic_movement_violations",
                "driver_behavior_violations",
                "licensing_documentation_violations",
                "attire_fare_violations")) {
            if (report.containsKey(field) && report.get(field) != null
                    && !"none".equalsIgnoreCase(report.get(field).toString())) {
                vio.append(report.get(field)).append(" ");
            }
        }
        return vio.toString();
    }

    // ---------- HIGHLIGHT ----------
    private void highlightComplaint(String id) {
        if (id == null || id.isEmpty()) return;

        mainHandler.postDelayed(() -> {
            boolean found = highlightInContainer(activeComplaintsContainer, id);
            if (!found) highlightInContainer(historyComplaintsContainer, id);
        }, 300);
    }


    private boolean highlightInContainer(LinearLayout container, String reportId) {
        if (container == null) return false;

        for (int i = 0; i < container.getChildCount(); i++) {
            View cardView = container.getChildAt(i);
            TextView reportIdView = cardView.findViewById(R.id.reportId);

            if (reportIdView != null && reportIdView.getText().toString().contains(reportId)) {
                CardView card = cardView.findViewById(R.id.cardRoot);
                if (card != null) {
                    Drawable unviewedBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_notification_unviewed);
                    Drawable viewedBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_notification_viewed);

                    card.setBackground(unviewedBg);

                    if (scrollView != null)
                        scrollView.post(() -> scrollView.smoothScrollTo(0, cardView.getTop()));

                    if (!keepHighlightFromNotification) {
                        mainHandler.postDelayed(() -> {
                            if (isAdded()) card.setBackground(viewedBg);
                        }, 4000);
                    } else {
                        // Keep highlighted until user switches tab or clicks
                        card.setOnClickListener(v -> {
                            card.setBackground(viewedBg);
                            highlightReportId = "";
                            keepHighlightFromNotification = false;
                        });
                    }
                }
                return true;
            }
        }
        return false;
    }


    // ---------- HELPERS ----------
    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        String[] parts = s.split("\\s+");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.length() > 0) {
                out.append(p.substring(0, 1).toUpperCase()).append(p.substring(1).toLowerCase());
            }
            if (i < parts.length - 1) out.append(" ");
        }
        return out.toString();
    }

    private void clearContainers() {
        if (activeComplaintsContainer != null) activeComplaintsContainer.removeAllViews();
        if (historyComplaintsContainer != null) historyComplaintsContainer.removeAllViews();
        if (activeHeader != null) activeHeader.setVisibility(View.GONE);
        if (historyHeader != null) historyHeader.setVisibility(View.GONE);
        if (emptyText != null) emptyText.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        clearContainers();
        if (emptyText != null) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(message);
        }
    }

    private void fetchAppointmentForReport(String reportId, TextView appointmentView) {
        if (reportId == null || reportId.isEmpty() || appointmentView == null) return;

        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        // adjust parameters according to your SupabaseService
        Call<List<Map<String, Object>>> call = service.getAppointmentsByReportId(
                SupabaseApiClient.SUPABASE_API_KEY,
                "Bearer " + SupabaseApiClient.SUPABASE_API_KEY,
                "eq." + reportId
        );

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> appointment = response.body().get(0);
                    String date = str(appointment.get("scheduled_date"));
                    String time = str(appointment.get("scheduled_time"));
                    String display = "Meeting scheduled on: " + formatDateTimeFriendly(date, time);

                    appointmentView.setVisibility(View.VISIBLE);
                    appointmentView.setText(display);
                    Log.d("Supabase", "Meeting scheduled on " + date + " at " + time);
                } else {
                    appointmentView.setVisibility(View.GONE);
                    Log.e("Supabase", "No appointments found or error: " + (response != null ? response.code() : "null"));
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                if (!isAdded()) return;
                appointmentView.setVisibility(View.GONE);
                Log.w("DriverComplaints", "Failed to fetch appointment for report " + reportId, t);
            }
        });
    }

    // safe find helper — returns casted View or null
    @SuppressWarnings("unchecked")
    private <T extends View> T safeFind(View root, int id) {
        try {
            return (T) root.findViewById(id);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // When leaving the tab, clear any active highlight
        keepHighlightFromNotification = false;
        highlightReportId = "";
    }

}
