package com.example.cne_commute;

import android.util.Log;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SupabaseRealtimeListener {

    private static final String TAG = "SupabaseRealtimeListener";
    private WebSocket webSocket;

    public interface ReportUpdateCallback {
        void onReportAccepted(String reportId);
    }

    public void startListening(ReportUpdateCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("wss://rtwrbkrroilftdhggxjc.supabase.co/realtime/v1/websocket?apikey=YOUR_PUBLIC_ANON_KEY&vsn=1.0.0")

                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Realtime message received: " + text);

                try {
                    JSONObject json = new JSONObject(text);
                    JSONObject payload = json.optJSONObject("payload");
                    if (payload != null) {
                        JSONObject newRecord = payload.optJSONObject("record");
                        if (newRecord != null && "accepted".equals(newRecord.optString("status"))) {
                            String reportId = newRecord.optString("report_id");
                            callback.onReportAccepted(reportId);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse realtime message", e);
                }
            }
        });
    }

    public void stopListening() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing");
        }
    }
}
