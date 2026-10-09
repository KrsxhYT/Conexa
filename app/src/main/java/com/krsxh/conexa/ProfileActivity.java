package com.krsxh.conexa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class ProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        ImageButton backBtn = findViewById(R.id.profileBackBtn);
        TextView userName = findViewById(R.id.profileUserName);
        TextView userPhone = findViewById(R.id.profileUserPhone);
        TextView editProfileBtn = findViewById(R.id.editProfileBtn);
        ImageButton shareBtn = findViewById(R.id.profileShareBtn);
        TextView blockedRow = findViewById(R.id.blockedContactsRow);
        TextView contactSettingsRow = findViewById(R.id.contactSettingsRow);
        TextView importExportRow = findViewById(R.id.importExportRow);
        TextView aboutRow = findViewById(R.id.aboutRow);

        loadOwnProfile(userName, userPhone);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setDataAndType(android.provider.ContactsContract.Profile.CONTENT_URI, "vnd.android.cursor.item/raw_contact");
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Unable to open profile editor", Toast.LENGTH_SHORT).show();
                }
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType(android.provider.ContactsContract.Contacts.CONTENT_VCARD_TYPE);
                    intent.putExtra(Intent.EXTRA_STREAM, android.provider.ContactsContract.Profile.CONTENT_VCARD_URI);
                    startActivity(Intent.createChooser(intent, "Share my info"));
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Unable to share profile", Toast.LENGTH_SHORT).show();
                }
            }
        });

        blockedRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class).putExtra("open_section", "blocked"));
            }
        });
        contactSettingsRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
            }
        });
        importExportRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Import/Export coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        aboutRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Conexa v1.0", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOwnProfile(TextView userName, TextView userPhone) {
        try {
            android.database.Cursor cursor = getContentResolver().query(
                android.provider.ContactsContract.Profile.CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(android.provider.ContactsContract.Profile.DISPLAY_NAME);
                if (nameIdx >= 0) {
                    String name = cursor.getString(nameIdx);
                    userName.setText(name != null ? name : "Me");
                }
                cursor.close();
            } else {
                userName.setText("Set up your profile");
            }
            userPhone.setText("");
        } catch (Exception e) {
            userName.setText("Me");
        }
    }
}