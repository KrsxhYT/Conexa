package com.krsxh.conexa;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.*;
import com.krsxh.conexa.models.ContactModel;
import com.krsxh.conexa.utils.ContactUtils;
import com.krsxh.conexa.utils.PermissionUtils;
import com.krsxh.conexa.utils.PreferencesManager;
import java.util.List;

public class ContactinfoActivity extends android.app.Activity {

    private ImageButton backBtn, editBtn, shareBtn, starBtn;
    private ImageView detailAvatarImage;
    private TextView detailAvatarInitial, detailName, blockBtn;
    private LinearLayout callAction, messageAction, videoAction, shareAction, phoneListContainer;

    private ContactModel contact;
    private PreferencesManager prefsManager;
    private long contactId;
    private String pendingCallNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contactinfo);
        prefsManager = new PreferencesManager(this);
        contactId = getIntent().getLongExtra("contact_id", -1);
        bindViews();
        loadContact();
        setupListeners();
    }

    private void bindViews() {
        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        shareBtn = findViewById(R.id.shareBtn);
        starBtn = findViewById(R.id.starBtn);
        detailAvatarImage = findViewById(R.id.detailAvatarImage);
        detailAvatarInitial = findViewById(R.id.detailAvatarInitial);
        detailName = findViewById(R.id.detailName);
        blockBtn = findViewById(R.id.blockBtn);
        callAction = findViewById(R.id.callAction);
        messageAction = findViewById(R.id.messageAction);
        videoAction = findViewById(R.id.videoAction);
        shareAction = findViewById(R.id.shareAction);
        phoneListContainer = findViewById(R.id.phoneListContainer);
    }

    private void loadContact() {
        List<ContactModel> all = ContactUtils.loadAllContacts(this);
        for (ContactModel c : all) {
            if (c.id == contactId) { contact = c; break; }
        }
        if (contact == null) {
            Toast.makeText(this, "Contact not found. It may have been deleted.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        renderContact();
    }

    private void renderContact() {
        detailName.setText(contact.name);

        if (contact.photoUri != null) {
            detailAvatarInitial.setVisibility(View.GONE);
            detailAvatarImage.setImageURI(Uri.parse(contact.photoUri));
        } else {
            detailAvatarImage.setBackgroundResource(ContactUtils.avatarDrawableRes(contact.name, this));
            detailAvatarInitial.setVisibility(View.VISIBLE);
            detailAvatarInitial.setText(contact.getInitial());
        }

        starBtn.setImageResource(contact.starred ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

        boolean blocked = prefsManager.isBlocked(contact.id);
        blockBtn.setText(blocked ? "Unblock Contact" : "Block Contact");
        blockBtn.setTextColor(getResources().getColor(blocked ? R.color.accent_success : R.color.accent_error));

        phoneListContainer.removeAllViews();
        if (contact.phones.isEmpty()) {
            TextView noPhone = new TextView(this);
            noPhone.setText("No phone number available");
            noPhone.setTextColor(getResources().getColor(R.color.text_secondary));
            noPhone.setPadding(0, 10, 0, 10);
            phoneListContainer.addView(noPhone);
        } else {
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
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to place call", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(String number) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open messaging app", Toast.LENGTH_SHORT).show();
        }
    }

    private void startVideoCall(String number) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + number));
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
}