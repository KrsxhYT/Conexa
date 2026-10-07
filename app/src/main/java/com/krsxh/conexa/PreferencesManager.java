package com.krsxh.conexa.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class PreferencesManager {
    private static final String PREFS = "conexa_prefs";
    private SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isBlocked(long contactId) {
        return getBlockedSet().contains(String.valueOf(contactId));
    }

    public void setBlocked(long contactId, boolean blocked) {
        Set<String> set = getBlockedSet();
        if (blocked) set.add(String.valueOf(contactId));
        else set.remove(String.valueOf(contactId));
        prefs.edit().putStringSet("blocked_ids", set).apply();
    }

    public Set<String> getBlockedSet() {
        Set<String> defaultSet = new HashSet<String>();
        return new HashSet<String>(prefs.getStringSet("blocked_ids", defaultSet));
    }

    public String getThemeMode() {
        return prefs.getString("theme_mode", "system");
    }

    public void setThemeMode(String mode) {
        prefs.edit().putString("theme_mode", mode).apply();
    }

    public String getSortOrder() {
        return prefs.getString("sort_order", "first_name");
    }

    public void setSortOrder(String order) {
        prefs.edit().putString("sort_order", order).apply();
    }
}