package com.krsxh.conexa;

import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.*;
import com.krsxh.conexa.utils.PermissionUtils;
import com.krsxh.conexa.utils.PreferencesManager;

public class ContactinfoActivity extends BaseActivity {

    private ImageButton backBtn, editBtn, shareBtn, starBtn;
    private ImageView detailAvatarImage;
    private TextView detailAvatarInitial, detailName, detailSubtitle, blockBtn;
    private LinearLayout callAction, messageAction, videoAction, shareAction, phoneListContainer;
    private ProgressBar detailLoadingIndicator;

    private ContactModel contact;
    private PreferencesManager prefsManager;
    private long contactId;
    private String pendingCallNumber;
    private HandlerThread contactLoaderThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contactinfo);
        prefsManager = new PreferencesManager(this);
        contactId = getIntent().getLongExtra("contact_id", -1);
        bindViews();
        setupListeners();
        setContactActionsEnabled(false);
        loadContactAsync();
    }

    private void bindViews() {
        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        shareBtn = findViewById(R.id.shareBtn);
        starBtn = findViewById(R.id.starBtn);
        detailAvatarImage = findViewById(R.id.detailAvatarImage);
        detailAvatarInitial = findViewById(R.id.detailAvatarInitial);
        detailName = findViewById(R.id.detailName);
        detailSubtitle = findViewById(R.id.detailSubtitle);
        blockBtn = findViewById(R.id.blockBtn);
        callAction = findViewById(R.id.callAction);
        messageAction = findViewById(R.id.messageAction);
        videoAction = findViewById(R.id.videoAction);
        shareAction = findViewById(R.id.shareAction);
        phoneListContainer = findViewById(R.id.phoneListContainer);
        detailLoadingIndicator = findViewById(R.id.detailLoadingIndicator);
    }

    private void loadContactAsync() {
        if (!PermissionUtils.hasContactsPermission(this)) {
            Toast.makeText(this, R.string.permission_rationale_desc, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        contactLoaderThread = new HandlerThread("ContactDetailLoader");
        contactLoaderThread.start();
        new Handler(contactLoaderThread.getLooper()).post(() -> {
            ContactModel loaded = null;
            for (ContactModel candidate : ContactUtils.loadAllContacts(this)) {
                if (candidate.id == contactId) {
                    loaded = candidate;
                    break;
                }
            }
            ContactModel result = loaded;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                detailLoadingIndicator.setVisibility(View.GONE);
                if (result == null) {
                    Toast.makeText(this, R.string.contact_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                contact = result;
                setContactActionsEnabled(true);
                renderContact();
            });
        });
    }

    private void setContactActionsEnabled(boolean enabled) {
        editBtn.setEnabled(enabled);
        shareBtn.setEnabled(enabled);
        starBtn.setEnabled(enabled);
        callAction.setEnabled(enabled);
        messageAction.setEnabled(enabled);
        videoAction.setEnabled(enabled);
        shareAction.setEnabled(enabled);
        blockBtn.setEnabled(enabled);
    }

    private void renderContact() {
        detailName.setText(contact.name);
        String subtitle = contact.jobTitle;
        if (contact.organization != null && !contact.organization.trim().isEmpty()) {
            subtitle = subtitle == null || subtitle.trim().isEmpty()
                ? contact.organization
                : getString(R.string.organization_format, subtitle, contact.organization);
        }
        if (subtitle == null || subtitle.trim().isEmpty()) {
            detailSubtitle.setVisibility(View.GONE);
        } else {
            detailSubtitle.setText(subtitle);
            detailSubtitle.setVisibility(View.VISIBLE);
        }

        if (contact.photoUri != null) {
            detailAvatarInitial.setVisibility(View.GONE);
            detailAvatarImage.setClipToOutline(true);
            detailAvatarImage.setImageURI(Uri.parse(contact.photoUri));
        } else {
            detailAvatarImage.setBackgroundResource(ContactUtils.avatarDrawableRes(contact.name, this));
            detailAvatarImage.setClipToOutline(true);
            detailAvatarInitial.setVisibility(View.VISIBLE);
            detailAvatarInitial.setText(contact.getInitial());
        }

        starBtn.setImageResource(contact.starred ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

        boolean blocked = prefsManager.isBlocked(contact.id);
        blockBtn.setText(blocked ? "Unblock Contact" : "Block Contact");
        blockBtn.setTextColor(getResources().getColor(blocked ? R.color.accent_success : R.color.accent_error));

        phoneListContainer.removeAllViews();
        addSectionHeading(R.string.contact_details);
        if (contact.phones.isEmpty() && contact.emails.isEmpty() && contact.addresses.isEmpty()) {
            addEmptyDetails(R.string.no_contact_details);
            return;
        }

        if (contact.phones.isEmpty()) {
            addEmptyDetails(R.string.no_phone_number);
        } else {
            addSectionHeading(R.string.phone_numbers);
            for (final ContactModel.PhoneEntry phone : contact.phones) {
                View row = getLayoutInflater().inflate(R.layout.phone_row_item, phoneListContainer, false);
                TextView numberText = row.findViewById(R.id.phoneNumberText);
                TextView labelText = row.findViewById(R.id.phoneLabelText);
                ImageButton videoBtn = row.findViewById(R.id.rowVideoBtn);
                ImageButton messageBtn = row.findViewById(R.id.rowMessageBtn);

                numberText.setText(phone.number);
                labelText.setText(phone.label);

                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeCall(phone.number);
                    }
                });
                row.setOnLongClickListener(v -> {
                    copyNumber(phone.number);
                    return true;
                });
                videoBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startVideoCall(phone.number);
                    }
                });
                messageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage(phone.number);
                    }
                });

                phoneListContainer.addView(row);
            }
        }

        if (!contact.emails.isEmpty()) {
            addSectionHeading(R.string.email_addresses);
            for (final ContactModel.EmailEntry email : contact.emails) {
                addDataRow(email.address, email.label, R.drawable.ic_email, () -> sendEmail(email.address));
            }
        }

        if (!contact.addresses.isEmpty()) {
            addSectionHeading(R.string.addresses);
            for (final ContactModel.AddressEntry address : contact.addresses) {
                addDataRow(address.address, address.label, R.drawable.ic_profile,
                    () -> openAddress(address.address));
            }
        }

    }

    private void addSectionHeading(int titleRes) {
        TextView heading = new TextView(this);
        heading.setText(titleRes);
        heading.setTextColor(getResources().getColor(R.color.text_secondary));
        heading.setTextSize(13);
        heading.setTypeface(heading.getTypeface(), android.graphics.Typeface.BOLD);
        heading.setPadding(2, 12, 2, 8);
        phoneListContainer.addView(heading);
    }

    private void addEmptyDetails(int messageRes) {
        TextView message = new TextView(this);
        message.setText(messageRes);
        message.setTextColor(getResources().getColor(R.color.text_secondary));
        message.setBackgroundResource(R.drawable.bg_card);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        message.setPadding(padding, padding, padding, padding);
        phoneListContainer.addView(message);
    }

    private void addDataRow(String value, String label, int iconRes, Runnable action) {
        View row = getLayoutInflater().inflate(R.layout.contact_data_row, phoneListContainer, false);
        TextView valueView = row.findViewById(R.id.dataRowValue);
        TextView labelView = row.findViewById(R.id.dataRowLabel);
        ImageView icon = row.findViewById(R.id.dataRowIcon);
        valueView.setText(value);
        labelView.setText(label);
        icon.setImageResource(iconRes);
        row.setContentDescription(getString(
            iconRes == R.drawable.ic_email
                ? R.string.contact_email_description
                : R.string.contact_address_description, value));
        row.setOnClickListener(v -> action.run());
        phoneListContainer.addView(row);
    }

    private void copyNumber(String number) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.phone_numbers), number));
        Toast.makeText(this, R.string.number_copied, Toast.LENGTH_SHORT).show();
    }

    private void sendEmail(String address) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null));
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, R.string.could_not_open_email, Toast.LENGTH_SHORT).show();
        }
    }

    private void openAddress(String address) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(address)));
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, R.string.could_not_open_maps, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri contactUri = ContactsContract.Contacts.getLookupUri(contact.id, contact.lookupKey);
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setDataAndType(contactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                    intent.putExtra("finishActivityOnSaveCompleted", true);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(ContactinfoActivity.this, "Unable to open editor.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContact();
            }
        });
        shareAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContact();
            }
        });

        starBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newState = !contact.starred;
                ContactUtils.setStarred(ContactinfoActivity.this, contact, newState);
                starBtn.setImageResource(newState ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            }
        });

        callAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!contact.phones.isEmpty()) makeCall(contact.getPrimaryPhone());
                else Toast.makeText(ContactinfoActivity.this, "No phone number available", Toast.LENGTH_SHORT).show();
            }
        });

        messageAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!contact.phones.isEmpty()) sendMessage(contact.getPrimaryPhone());
                else Toast.makeText(ContactinfoActivity.this, "No phone number available", Toast.LENGTH_SHORT).show();
            }
        });

        videoAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!contact.phones.isEmpty()) startVideoCall(contact.getPrimaryPhone());
                else Toast.makeText(ContactinfoActivity.this, "No phone number available", Toast.LENGTH_SHORT).show();
            }
        });

        blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmBlockToggle();
            }
        });
    }

    private void makeCall(String number) {
        if (number == null || number.trim().isEmpty()) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!PermissionUtils.hasCallPermission(this)) {
            pendingCallNumber = number;
            PermissionUtils.requestCallPermission(this);
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", number, null));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to place call", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(String number) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open messaging app", Toast.LENGTH_SHORT).show();
        }
    }

    private void startVideoCall(String number) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("tel", number, null));
            intent.putExtra("android.intent.extra.video_call", true);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No video calling app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareContact() {
        try {
            Uri contactUri = ContactsContract.Contacts.getLookupUri(contact.id, contact.lookupKey);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(ContactsContract.Contacts.CONTENT_VCARD_TYPE);
            intent.putExtra(Intent.EXTRA_STREAM, contactUri);
            startActivity(Intent.createChooser(intent, "Share contact"));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to share contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmBlockToggle() {
        final boolean currentlyBlocked = prefsManager.isBlocked(contact.id);
        String action = currentlyBlocked ? "Unblock" : "Block";
        new android.app.AlertDialog.Builder(this)
            .setTitle(action + " " + contact.name + "?")
            .setMessage(currentlyBlocked
                ? "This contact will be able to reach you again."
                : "You will no longer receive calls or messages from this contact.")
            .setPositiveButton(action, new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    prefsManager.setBlocked(contact.id, !currentlyBlocked);
                    renderContact();
                    Toast.makeText(ContactinfoActivity.this, contact.name + (currentlyBlocked ? " unblocked" : " blocked"), Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQ_CALL_PHONE && pendingCallNumber != null) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                makeCall(pendingCallNumber);
            } else {
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
            pendingCallNumber = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (contactLoaderThread != null) contactLoaderThread.quitSafely();
        super.onDestroy();
    }
}