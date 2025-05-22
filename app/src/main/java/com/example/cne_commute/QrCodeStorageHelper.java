package com.example.cne_commute;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QrCodeStorageHelper {

    private static final String PREFS_NAME = "ScannedQrCodesPrefs";
    private static final String KEY_QR_LIST = "qrCodeList";

    public static void saveQrCode(Context context, ScannedQrCode newCode) {
        List<ScannedQrCode> qrList = loadQrCodes(context);
        qrList.add(0, newCode); // newest at top
        saveQrCodeList(context, qrList);
    }

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

    public static void saveQrCodeList(Context context, List<ScannedQrCode> qrList) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(qrList);
        editor.putString(KEY_QR_LIST, json);
        editor.apply();
    }

    public static void clearQrCodeList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_QR_LIST).apply();
    }

    public static void deleteQrCode(Context context, String id) {
        List<ScannedQrCode> qrList = loadQrCodes(context);
        Iterator<ScannedQrCode> iterator = qrList.iterator();

        while (iterator.hasNext()) {
            ScannedQrCode qrCode = iterator.next();
            if (qrCode.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }

        saveQrCodeList(context, qrList);
    }

    // Wrapper to match usage in adapter
    public static void saveQrCodes(Context context, List<ScannedQrCode> qrList) {
        saveQrCodeList(context, qrList);
    }
}
