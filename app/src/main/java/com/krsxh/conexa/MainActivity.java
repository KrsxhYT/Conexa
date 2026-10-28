package com.krsxh.conexa;

import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.krsxh.conexa.ContactAdapter;
import com.krsxh.conexa.ContactModel;
import com.krsxh.conexa.ContactUtils;
import com.krsxh.conexa.utils.PermissionUtils;
import java.util.*;

public class MainActivity extends BaseActivity {

    private EditText searchInput;
    private ImageButton clearSearchBtn, profileBtn;
    private View addContactBtn;
    private RecyclerView contactsRecyclerView;
    private LinearLayout favoritesRow, favoritesSection, emptyStateLayout, permissionLayout;
    private TextView emptyStateText, grantPermissionBtn, contactsCount;
    private ProgressBar loadingSpinner;
    private LinearLayout navHome, navFavorites, navSettings, navProfile;

    private List<ContactModel> allContacts = new ArrayList<ContactModel>();
    private ContactAdapter adapter;
    private Handler bgHandler;
    private HandlerThread bgThread;
    private boolean isFirstLoad = true;

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
        addContactBtn = findViewById(R.id.addContactBtn);
        contactsCount = findViewById(R.id.contactsCount);
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
        contactsRecyclerView.setHasFixedSize(false);

        navHome = findViewById(R.id.navHome);
        navFavorites = findViewById(R.id.navFavorites);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupListeners() {
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHaptic(v);
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });

        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHaptic(v);
                Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, R.string.could_not_open_contacts, Toast.LENGTH_SHORT).show();
                }
            }
        });

        clearSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHaptic(v);
                searchInput.setText("");
                searchInput.clearFocus();
            }
        });

        grantPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHaptic(v);
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

        // Navigation
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHaptic(v);
                // Already here
            }
        });
        navFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHaptic(v);
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHaptic(v);
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performHaptic(v);
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void performHaptic(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        } else {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
    }

    private void highlightNavTab() {
        ImageView icon = findViewById(R.id.navHomeIcon);
        TextView text = findViewById(R.id.navHomeText);
        navHome.setSelected(true);
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
        if (isFirstLoad) {
            loadingSpinner.setVisibility(View.VISIBLE);
        }
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
                        isFirstLoad = false;
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

        // Limit to first 12 for performance & clean UI
        int limit = Math.min(favs.size(), 12);
        for (int i = 0; i < limit; i++) {
            final ContactModel c = favs.get(i);
            View item = getLayoutInflater().inflate(R.layout.favorite_avatar_item, favoritesRow, false);
            ImageView avatarImage = item.findViewById(R.id.favAvatarImage);
            TextView avatarInitial = item.findViewById(R.id.favAvatarInitial);
            TextView favName = item.findViewById(R.id.favName);

            favName.setText(c.name);
            if (c.photoUri != null) {
                avatarInitial.setVisibility(View.GONE);
                avatarImage.setClipToOutline(true);
                avatarImage.setImageURI(Uri.parse(c.photoUri));
            } else {
                avatarImage.setBackgroundResource(ContactUtils.avatarDrawableRes(c.name, this));
                avatarImage.setClipToOutline(true);
                avatarInitial.setVisibility(View.VISIBLE);
                avatarInitial.setText(c.getInitial());
            }

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performHaptic(v);
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
        int countString = contacts.size() == 1
            ? R.string.contacts_count_singular : R.string.contacts_count_plural;
        contactsCount.setText(getString(countString, contacts.size()));

        if (contacts.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            contactsRecyclerView.setVisibility(View.GONE);
            String query = searchInput.getText().toString().trim();
            if (query.length() > 0) {
                emptyStateText.setText(getString(R.string.search_no_results, query));
            } else {
                emptyStateText.setText(getString(R.string.empty_contacts_title));
            }
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
            boolean nameMatch = c.name != null && c.name.toLowerCase(Locale.US).contains(q);
            boolean phoneMatch = false;
            if (c.phones != null) {
                for (ContactModel.PhoneEntry p : c.phones) {
                    if (p.number != null) {
                        String cleanNum = p.number.replaceAll("[^0-9]", "");
                        String cleanQ = q.replaceAll("[^0-9]", "");
                        if (!cleanQ.isEmpty() && cleanNum.contains(cleanQ)) {
                            phoneMatch = true;
                            break;
                        }
                    }
                }
            }
            boolean emailMatch = false;
            if (c.emails != null) {
                for (ContactModel.EmailEntry email : c.emails) {
                    if (email.address != null && email.address.toLowerCase(Locale.US).contains(q)) {
                        emailMatch = true;
                        break;
                    }
                }
            }
            if (nameMatch || phoneMatch || emailMatch) filtered.add(c);
        }
        renderContactList(filtered);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionUtils.hasContactsPermission(this)) {
            // Soft refresh on resume (don't show spinner every time)
            loadContactsAsync();
        }
    }
}