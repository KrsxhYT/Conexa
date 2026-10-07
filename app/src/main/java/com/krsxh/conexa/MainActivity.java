package com.krsxh.conexa;

import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.krsxh.conexa.ContactAdapter;
import com.krsxh.conexa.ContactModel;
import com.krsxh.conexa.ContactUtils;
import com.krsxh.conexa.utils.PermissionUtils;
import java.util.*;

public class MainActivity extends android.app.Activity {

    private EditText searchInput;
    private ImageButton clearSearchBtn, profileBtn;
    private RecyclerView contactsRecyclerView;
    private LinearLayout favoritesRow, favoritesSection, emptyStateLayout, permissionLayout;
    private TextView emptyStateText, grantPermissionBtn;
    private ProgressBar loadingSpinner;
    private LinearLayout navHome, navFavorites, navSettings, navProfile;

    private List<ContactModel> allContacts = new ArrayList<ContactModel>();
    private ContactAdapter adapter;
    private Handler bgHandler;
    private HandlerThread bgThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        bindViews();
        setupListeners();
        highlightNavTab();
        checkPermissionAndLoad();
    }

    private void bindViews() {
        searchInput = findViewById(R.id.searchInput);
        clearSearchBtn = findViewById(R.id.clearSearchBtn);
        profileBtn = findViewById(R.id.profileBtn);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        favoritesRow = findViewById(R.id.favoritesRow);
        favoritesSection = findViewById(R.id.favoritesSection);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        emptyStateText = findViewById(R.id.emptyStateText);
        permissionLayout = findViewById(R.id.permissionLayout);
        grantPermissionBtn = findViewById(R.id.grantPermissionBtn);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setNestedScrollingEnabled(false);

        navHome = findViewById(R.id.navHome);
        navFavorites = findViewById(R.id.navFavorites);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupListeners() {
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
        clearSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInput.setText("");
            }
        });
        grantPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtils.requestContactsPermission(MainActivity.this);
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                clearSearchBtn.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                filterContacts(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {}
        });
        navFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
            }
        });
        navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
    }

    private void highlightNavTab() {
        ImageView icon = findViewById(R.id.navHomeIcon);
        TextView text = findViewById(R.id.navHomeText);
        icon.setColorFilter(getResources().getColor(R.color.accent_primary));
        text.setTextColor(getResources().getColor(R.color.accent_primary));
    }

    private void checkPermissionAndLoad() {
        if (PermissionUtils.hasContactsPermission(this)) {
            permissionLayout.setVisibility(View.GONE);
            loadContactsAsync();
        } else {
            permissionLayout.setVisibility(View.VISIBLE);
            PermissionUtils.requestContactsPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLayout.setVisibility(View.GONE);
                loadContactsAsync();
            } else {
                permissionLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadContactsAsync() {
        loadingSpinner.setVisibility(View.VISIBLE);
        bgThread = new HandlerThread("ContactLoader");
        bgThread.start();
        bgHandler = new Handler(bgThread.getLooper());

        bgHandler.post(new Runnable() {
            @Override
            public void run() {
                final List<ContactModel> loaded = ContactUtils.loadAllContacts(MainActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        allContacts = loaded;
                        loadingSpinner.setVisibility(View.GONE);
                        renderFavorites();
                        renderContactList(allContacts);
                        bgThread.quitSafely();
                    }
                });
            }
        });
    }

    private void renderFavorites() {
        favoritesRow.removeAllViews();
        List<ContactModel> favs = new ArrayList<ContactModel>();
        for (ContactModel c : allContacts) if (c.starred) favs.add(c);

        favoritesSection.setVisibility(favs.isEmpty() ? View.GONE : View.VISIBLE);

        for (final ContactModel c : favs) {
            View item = getLayoutInflater().inflate(R.layout.favorite_avatar_item, favoritesRow, false);
            ImageView avatarImage = item.findViewById(R.id.favAvatarImage);
            TextView avatarInitial = item.findViewById(R.id.favAvatarInitial);
            TextView favName = item.findViewById(R.id.favName);

            favName.setText(c.name);
            if (c.photoUri != null) {
                avatarInitial.setVisibility(View.GONE);
                avatarImage.setImageURI(Uri.parse(c.photoUri));
            } else {
                avatarImage.setBackgroundResource(ContactUtils.avatarDrawableRes(c.name, this));
                avatarInitial.setVisibility(View.VISIBLE);
                avatarInitial.setText(c.getInitial());
            }

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ContactinfoActivity.class);
                    intent.putExtra("contact_id", c.id);
                    intent.putExtra("lookup_key", c.lookupKey);
                    startActivity(intent);
                }
            });
            favoritesRow.addView(item);
        }
    }

    private void renderContactList(List<ContactModel> contacts) {
        if (contacts.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            contactsRecyclerView.setVisibility(View.GONE);
            emptyStateText.setText(searchInput.getText().length() > 0
                ? getString(R.string.no_contacts_found) : "No contacts on this device");
            return;
        }
        emptyStateLayout.setVisibility(View.GONE);
        contactsRecyclerView.setVisibility(View.VISIBLE);

        if (adapter == null) {
            adapter = new ContactAdapter(this, contacts);
            contactsRecyclerView.setAdapter(adapter);
        } else {
            adapter.updateContacts(contacts);
        }
    }

    private void filterContacts(String query) {
        String q = query.toLowerCase(Locale.US).trim();
        if (q.isEmpty()) {
            renderContactList(allContacts);
            return;
        }
        List<ContactModel> filtered = new ArrayList<ContactModel>();
        for (ContactModel c : allContacts) {
            boolean nameMatch = c.name.toLowerCase(Locale.US).contains(q);
            boolean phoneMatch = false;
            for (ContactModel.PhoneEntry p : c.phones) {
                if (p.number.replaceAll("[^0-9]", "").contains(q.replaceAll("[^0-9]", "")) && !q.replaceAll("[^0-9]","").isEmpty()) {
                    phoneMatch = true; break;
                }
            }
            if (nameMatch || phoneMatch) filtered.add(c);
        }
        renderContactList(filtered);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionUtils.hasContactsPermission(this) && !allContacts.isEmpty()) {
            loadContactsAsync();
        }
    }
}