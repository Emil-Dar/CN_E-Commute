package com.example.cne_commute;

public class SupabaseAuthManager {

    private static SupabaseAuthManager instance;
    private String currentUserId;

    private SupabaseAuthManager() {
        // TODO: Replace this with actual Supabase Auth logic
        currentUserId = "JQaQykeDbrTpG12NNJEMwj8viB02"; // Example user ID
    }

    public static SupabaseAuthManager getInstance() {
        if (instance == null) {
            instance = new SupabaseAuthManager();
        }
        return instance;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }
}
