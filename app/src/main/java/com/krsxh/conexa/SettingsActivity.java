package com.krsxh.conexa;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.krsxh.conexa.utils.PreferencesManager;

public class SettingsActivity extends BaseActivity {

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
        navSettings.setSelected(true);
        settingsIcon.setColorFilter(getResources().getColor(R.color.accent_primary));
        settingsText.setTextColor(getResources().getColor(R.color.accent_primary));

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new android.content.Intent(SettingsActivity.this, MainActivity.class)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }
        });
        navFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new android.content.Intent(SettingsActivity.this, FavoritesActivity.class));
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
        themeValueText.setText(theme.equals("light") ? R.string.theme_light
            : theme.equals("dark") ? R.string.theme_dark : R.string.theme_system);
        String sort = prefsManager.getSortOrder();
        sortValueText.setText(sort.equals("last_name") ? R.string.sort_last_name : R.string.sort_first_name);
    }

    private void showThemeDialog() {
        final String[] options = {
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        };
        final String[] keys = {"light", "dark", "system"};
        int selected = prefsManager.getThemeMode().equals("light") ? 0
            : prefsManager.getThemeMode().equals("dark") ? 1 : 2;
        new android.app.AlertDialog.Builder(this)
            .setTitle(R.string.theme)
            .setSingleChoiceItems(options, selected, new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    prefsManager.setThemeMode(keys[which]);
                    BaseActivity.applyTheme(keys[which]);
                    dialog.dismiss();
                }
            }).show();
    }

    private void showSortDialog() {
        final String[] options = {getString(R.string.sort_first_name), getString(R.string.sort_last_name)};
        final String[] keys = {"first_name", "last_name"};
        int selected = prefsManager.getSortOrder().equals("last_name") ? 1 : 0;
        new android.app.AlertDialog.Builder(this)
            .setTitle(R.string.sort_contacts)
            .setSingleChoiceItems(options, selected, new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    prefsManager.setSortOrder(keys[which]);
                    updateValues();
                    dialog.dismiss();
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