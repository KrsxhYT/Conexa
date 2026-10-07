package com.krsxh.conexa;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.krsxh.conexa.utils.PreferencesManager;

public class SettingsActivity extends android.app.Activity {

    private PreferencesManager prefsManager;
    private TextView themeValueText, sortValueText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        prefsManager = new PreferencesManager(this);

        themeValueText = findViewById(R.id.themeValueText);
        sortValueText = findViewById(R.id.sortValueText);
        updateValues();

        findViewById(R.id.themeRow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeDialog();
            }
        });
        findViewById(R.id.sortOrderRow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortDialog();
            }
        });
        findViewById(R.id.blockedListRow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBlockedInfo();
            }
        });
        findViewById(R.id.permissionRow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppSettings();
            }
        });

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navFavorites = findViewById(R.id.navFavorites);
        LinearLayout navSettings = findViewById(R.id.navSettings);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        ImageView settingsIcon = findViewById(R.id.navSettingsIcon);
        TextView settingsText = findViewById(R.id.navSettingsText);
        settingsIcon.setColorFilter(getResources().getColor(R.color.accent_primary));
        settingsText.setTextColor(getResources().getColor(R.color.accent_primary));

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new android.content.Intent(SettingsActivity.this, MainActivity.class));
                finish();
            }
        });
        navFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new android.content.Intent(SettingsActivity.this, FavouritesActivity.class));
            }
        });
        navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {}
        });
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new android.content.Intent(SettingsActivity.this, ProfileActivity.class));
            }
        });
    }

    private void updateValues() {
        String theme = prefsManager.getThemeMode();
        themeValueText.setText(theme.equals("light") ? "Light" : theme.equals("dark") ? "Dark" : "System Default");
        String sort = prefsManager.getSortOrder();
        sortValueText.setText(sort.equals("last_name") ? "Last name" : "First name");
    }

    private void showThemeDialog() {
        final String[] options = {"Light", "Dark", "System Default"};
        final String[] keys = {"light", "dark", "system"};
        new android.app.AlertDialog.Builder(this)
            .setTitle("Theme")
            .setItems(options, new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    prefsManager.setThemeMode(keys[which]);
                    updateValues();
                }
            }).show();
    }

    private void showSortDialog() {
        final String[] options = {"First name", "Last name"};
        final String[] keys = {"first_name", "last_name"};
        new android.app.AlertDialog.Builder(this)
            .setTitle("Sort contacts")
            .setItems(options, new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    prefsManager.setSortOrder(keys[which]);
                    updateValues();
                }
            }).show();
    }

    private void showBlockedInfo() {
        int count = prefsManager.getBlockedSet().size();
        Toast.makeText(this, count + " blocked contact(s)", Toast.LENGTH_SHORT).show();
    }

    private void openAppSettings() {
        android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(android.net.Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }
}