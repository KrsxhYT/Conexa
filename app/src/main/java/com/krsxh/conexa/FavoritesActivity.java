package com.krsxh.conexa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.krsxh.conexa.utils.PermissionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoritesActivity extends BaseActivity {

    private EditText searchInput;
    private ImageButton clearSearchBtn;
    private RecyclerView favoritesRecyclerView;
    private LinearLayout favEmptyState;
    private TextView emptyTitle, emptyDescription, favoritesCount;
    private List<ContactModel> allFavorites = new ArrayList<>();
    private ContactAdapter adapter;
    private HandlerThread bgThread;
    private Handler bgHandler;
    private boolean permissionRequired;
    private boolean permissionRequestMade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);

        searchInput = findViewById(R.id.favoritesSearchInput);
        clearSearchBtn = findViewById(R.id.favoritesClearSearchBtn);
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        favEmptyState = findViewById(R.id.favEmptyState);
        emptyTitle = findViewById(R.id.favEmptyTitle);
        emptyDescription = findViewById(R.id.favEmptyDescription);
        favoritesCount = findViewById(R.id.favoritesCount);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setNestedScrollingEnabled(false);

        setupSearch();
        setupNavigation();
        highlightTab();
        favEmptyState.setOnClickListener(v -> {
            if (permissionRequired) {
                permissionRequestMade = true;
                PermissionUtils.requestContactsPermission(this);
            }
        });

        bgThread = new HandlerThread("FavoriteLoader");
        bgThread.start();
        bgHandler = new Handler(bgThread.getLooper());
    }

    private void setupSearch() {
        clearSearchBtn.setOnClickListener(v -> searchInput.setText(""));
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearSearchBtn.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                renderFavorites(filterFavorites(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.navFavorites).setOnClickListener(v -> {});
        findViewById(R.id.navSettings).setOnClickListener(v ->
            startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v ->
            startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void highlightTab() {
        ImageView icon = findViewById(R.id.navFavoritesIcon);
        TextView text = findViewById(R.id.navFavoritesText);
        findViewById(R.id.navFavorites).setSelected(true);
        icon.setColorFilter(getResources().getColor(R.color.accent_primary));
        text.setTextColor(getResources().getColor(R.color.accent_primary));
    }

    private void loadFavorites() {
        if (!PermissionUtils.hasContactsPermission(this)) {
            permissionRequired = true;
            favoritesCount.setText("");
            renderFavorites(new ArrayList<>());
            if (!permissionRequestMade) {
                permissionRequestMade = true;
                PermissionUtils.requestContactsPermission(this);
            }
            return;
        }

        permissionRequired = false;
        bgHandler.post(() -> {
            List<ContactModel> loaded = new ArrayList<>();
            for (ContactModel contact : ContactUtils.loadAllContacts(this)) {
                if (contact.starred) loaded.add(contact);
            }
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                allFavorites = loaded;
                renderFavorites(filterFavorites(searchInput.getText().toString()));
            });
        });
    }

    private List<ContactModel> filterFavorites(String query) {
        String normalizedQuery = query.toLowerCase(Locale.getDefault()).trim();
        if (normalizedQuery.isEmpty()) return new ArrayList<>(allFavorites);

        String digits = normalizedQuery.replaceAll("[^0-9]", "");
        List<ContactModel> filtered = new ArrayList<>();
        for (ContactModel contact : allFavorites) {
            boolean nameMatches = contact.name.toLowerCase(Locale.getDefault()).contains(normalizedQuery);
            boolean phoneMatches = false;
            if (!digits.isEmpty()) {
                for (ContactModel.PhoneEntry phone : contact.phones) {
                    if (phone.number.replaceAll("[^0-9]", "").contains(digits)) {
                        phoneMatches = true;
                        break;
                    }
                }
            }
            boolean emailMatches = false;
            for (ContactModel.EmailEntry email : contact.emails) {
                if (email.address.toLowerCase(Locale.getDefault()).contains(normalizedQuery)) {
                    emailMatches = true;
                    break;
                }
            }
            if (nameMatches || phoneMatches || emailMatches) filtered.add(contact);
        }
        return filtered;
    }

    private void renderFavorites(List<ContactModel> contacts) {
        int count = contacts.size();
        favoritesCount.setText(permissionRequired ? "" : getResources().getQuantityString(
            R.plurals.favorite_count, count, count));

        if (contacts.isEmpty()) {
            favoritesRecyclerView.setVisibility(View.GONE);
            favEmptyState.setVisibility(View.VISIBLE);
            boolean searching = searchInput != null && searchInput.length() > 0;
            if (permissionRequired) {
                emptyTitle.setText(R.string.permission_rationale_title);
                emptyDescription.setText(R.string.favorites_permission_desc);
            } else {
                emptyTitle.setText(searching ? R.string.no_favorites_match_title : R.string.no_favorites_title);
                emptyDescription.setText(searching ? R.string.no_favorites_match_desc : R.string.no_favorites_desc);
            }
            return;
        }

        favEmptyState.setVisibility(View.GONE);
        favoritesRecyclerView.setVisibility(View.VISIBLE);
        if (adapter == null) {
            adapter = new ContactAdapter(this, contacts);
            favoritesRecyclerView.setAdapter(adapter);
        } else {
            adapter.updateContacts(contacts);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bgHandler != null) loadFavorites();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_READ_CONTACTS) {
            if (PermissionUtils.hasContactsPermission(this)) {
                permissionRequired = false;
                loadFavorites();
            } else {
                permissionRequired = true;
                renderFavorites(new ArrayList<>());
                Toast.makeText(this, R.string.permission_rationale_desc, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (bgThread != null) bgThread.quitSafely();
        super.onDestroy();
    }
}
