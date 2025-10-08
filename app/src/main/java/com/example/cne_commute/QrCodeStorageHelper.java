package com.example.cne_commute;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QrCodeStorageHelper {

    private static final String PREFS_NAME = "ScannedQrCodesPrefs";
    private static final String KEY_QR_LIST = "qrCodeList";
    private static final String KEY_ARCHIVED_LIST = "archivedQrCodeList";

    // Save a single QR code entry with scan tracking
    public static void saveQrCode(Context context, ScannedQrCode newCode) {
        List<ScannedQrCode> qrList = loadQrCodes(context);

        for (ScannedQrCode existing : qrList) {
            if (existing.getDriverId().equals(newCode.getDriverId())) {
                existing.addScanTimestamp(newCode.getScanTimestamp());
                saveQrCodeList(context, qrList);
                Log.d("QrCodeStorageHelper", "Updated scan for: " + existing.getDriverName());
                return;
            }
        }

        qrList.add(0, newCode); // Add to top of list
        saveQrCodeList(context, qrList);
        Log.d("QrCodeStorageHelper", "Added new scan for: " + newCode.getDriverName());
    }

    // Load all saved QR code entries
    public static List<ScannedQrCode> loadQrCodes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_QR_LIST, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ScannedQrCode>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    // Save the entire list of QR codes
    public static void saveQrCodeList(Context context, List<ScannedQrCode> qrList) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(qrList);
        editor.putString(KEY_QR_LIST, json);
        editor.apply();
    }

    // Clear all saved QR codes
    public static void clearQrCodeList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_QR_LIST).apply();
    }

    // Delete a specific QR code by Driver ID
    public static void deleteQrCode(Context context, String driverId) {
        List<ScannedQrCode> qrList = loadQrCodes(context);
        Iterator<ScannedQrCode> iterator = qrList.iterator();

        while (iterator.hasNext()) {
            ScannedQrCode qrCode = iterator.next();
            if (qrCode.getDriverId().equals(driverId)) {
                iterator.remove();
                break;
            }
        }

        saveQrCodeList(context, qrList);
    }

    // Optional: Save a full list (used by adapters or bulk updates)
    public static void saveQrCodes(Context context, List<ScannedQrCode> qrList) {
        saveQrCodeList(context, qrList);
    }

    // Optional: Retrieve a QR code by its unique Driver ID
    public static ScannedQrCode getQrCodeById(Context context, String driverId) {
        for (ScannedQrCode code : loadQrCodes(context)) {
            if (code.getDriverId().equals(driverId)) {
                return code;
            }
        }
        return null;
    }

    // ✅ Save the archived QR code list
    public static void saveArchivedQrCodeList(Context context, List<ScannedQrCode> archivedList) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(archivedList);
        editor.putString(KEY_ARCHIVED_LIST, json);
        editor.apply();
    }

    // ✅ Load the archived QR code list
    public static List<ScannedQrCode> loadArchivedQrCodeList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_ARCHIVED_LIST, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ScannedQrCode>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }
}
