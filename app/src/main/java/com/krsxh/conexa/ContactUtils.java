package com.krsxh.conexa;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import java.util.*;

public class ContactUtils {

    public static List<ContactModel> loadAllContacts(Context context) {
        List<ContactModel> result = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        Map<Long, ContactModel> map = new LinkedHashMap<>();

        String[] projection = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.STARRED
        };

        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                ContactModel c = new ContactModel();
                c.id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                c.lookupKey = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY));
                c.name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                c.photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI));
                c.starred = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.STARRED)) == 1;
                if (c.name == null || c.name.trim().isEmpty()) c.name = "Unknown";
                map.put(c.id, c);
            }
            cursor.close();
        }

        // Load phone numbers
        Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE
            }, null, null, null);

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                long contactId = phoneCursor.getLong(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                String number = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int type = phoneCursor.getInt(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE));
                String label = phoneTypeLabel(type);
                ContactModel c = map.get(contactId);
                if (c != null && number != null) {
                    c.phones.add(new ContactModel.PhoneEntry(number, label));
                }
            }
            phoneCursor.close();
        }

        result.addAll(map.values());
        return result;
    }

    private static String phoneTypeLabel(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE: return "Mobile";
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME: return "Home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK: return "Work";
            default: return "Other";
        }
    }

    public static void setStarred(Context context, ContactModel contact, boolean starred) {
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(ContactsContract.Contacts.STARRED, starred ? 1 : 0);
        context.getContentResolver().update(
            ContactsContract.Contacts.CONTENT_URI, values,
            ContactsContract.Contacts._ID + "=?", new String[]{String.valueOf(contact.id)});
        contact.starred = starred;
    }

    public static int avatarColorFor(String name) {
        if (name == null || name.isEmpty()) return 0;
        int hash = Math.abs(name.hashCode());
        return hash % 5;
    }

    public static int avatarDrawableRes(String name, Context context) {
        String[] names = {"bg_avatar_1", "bg_avatar_2", "bg_avatar_3", "bg_avatar_4", "bg_avatar_5"};
        int idx = avatarColorFor(name);
        return context.getResources().getIdentifier(names[idx], "drawable", context.getPackageName());
    }
}