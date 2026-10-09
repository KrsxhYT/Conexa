package com.krsxh.conexa;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowInsetsControllerCompat;
import com.krsxh.conexa.utils.PreferencesManager;

public abstract class BaseActivity extends AppCompatActivity {

    public static void applyTheme(String themeMode) {
        int mode;
        switch (themeMode) {
            case "light":
                mode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case "dark":
                mode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme(new PreferencesManager(this).getThemeMode());
        super.onCreate(savedInstanceState);
        boolean isDark = (getResources().getConfiguration().uiMode
            & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
            == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat systemBars =
            new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        systemBars.setAppearanceLightStatusBars(!isDark);
        systemBars.setAppearanceLightNavigationBars(!isDark);
    }
}
