package com.krsxh.conexa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.krsxh.conexa.adapters.ContactAdapter;
import com.krsxh.conexa.models.ContactModel;
import com.krsxh.conexa.utils.ContactUtils;
import com.krsxh.conexa.utils.PermissionUtils;
import java.util.*;

public class FavouritesActivity extends android.app.Activity {

    private RecyclerView favoritesRecyclerView;
    private LinearLayout favEmptyState;
    private LinearLayout navHome, navFavorites, navSettings, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        favEmptyState = findViewById(R.id.favEmptyState);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        navHome = findViewById(R.id.navHome);
        navFavorites = findViewById(R.id.navFavorites);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);
        highlightTab();
        setupNav();
        loadFavorites();
    }

    private void highlightTab() {
        ImageView icon = findViewById(R.id.navFavoritesIcon);
        TextView text = findViewById(R.id.navFavoritesText);
        icon.setColorFilter(getResources().getColor(R.color.accent_primary));
        text.setTextColor(getResources().getColor(R.color.accent_primary));
    }

    private void setupNav() {
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FavouritesActivity.this, MainActivity.class));
                finish();
            }
        });
        navFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {}
        });
        navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FavouritesActivity.this, SettingsActivity.class));
            }
        });
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FavouritesActivity.this, ProfileActivity.class));
            }
        });
    }

    private void loadFavorites() {
        if (!PermissionUtils.hasContactsPermission(this)) return;
        List<ContactModel> all = ContactUtils.loadAllContacts(this);
        List<ContactModel> favs = new ArrayList<ContactModel>();
        for (ContactModel c : all) if (c.starred) favs.add(c);

        if (favs.isEmpty()) {
            favEmptyState.setVisibility(View.VISIBLE);
            favoritesRecyclerView.setVisibility(View.GONE);
        } else {
            favEmptyState.setVisibility(View.GONE);
            favoritesRecyclerView.setVisibility(View.VISIBLE);
            favoritesRecyclerView.setAdapter(new ContactAdapter(this, favs));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }
}