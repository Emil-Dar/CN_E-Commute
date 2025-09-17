package com.example.cne_commute;

public class Constants {
    // 🌐 Supabase project URL
    public static final String SUPABASE_URL = "https://rtwrbkrroilftdhggxjc.supabase.co";

    // 🗂️ Supabase storage bucket name
    public static final String SUPABASE_BUCKET = "report-images"; // Confirmed bucket name

    // 🔑 Supabase anon API key (safe for public use with RLS enabled)
    public static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJ0d3Jia3Jyb2lsZnRkaGdneGpjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ4MDg1OTksImV4cCI6MjA3MDM4NDU5OX0.eiCQTeLh9IG4mX3cNqoIe6-cq33pzeO_qSTtONuMnKA";

    // 📤 Supabase storage upload endpoint
    public static final String STORAGE_UPLOAD_URL = SUPABASE_URL + "/storage/v1/object/" + SUPABASE_BUCKET + "/";

    // 🌐 Supabase public access URL for viewing uploaded files
    public static final String STORAGE_PUBLIC_URL = SUPABASE_URL + "/storage/v1/object/public/" + SUPABASE_BUCKET + "/";

    // 📊 Supabase table names
    public static final String REPORT_TABLE = "reports";
    public static final String USER_TABLE = "users";

    // 🪵 Logging tag for debugging
    public static final String TAG = "CNE_Commute";
}
