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

        Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            new String[]{
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.TYPE
            }, null, null, null);
        if (emailCursor != null) {
            while (emailCursor.moveToNext()) {
                long contactId = emailCursor.getLong(emailCursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID));
                String address = emailCursor.getString(emailCursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Email.ADDRESS));
                int type = emailCursor.getInt(emailCursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Email.TYPE));
                ContactModel contact = map.get(contactId);
                if (contact != null && address != null && !address.trim().isEmpty()) {
                    contact.emails.add(new ContactModel.EmailEntry(address, emailTypeLabel(type)));
                }
            }
            emailCursor.close();
        }

        Cursor addressCursor = cr.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
            new String[]{
                ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID,
                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE
            }, null, null, null);
        if (addressCursor != null) {
            while (addressCursor.moveToNext()) {
                long contactId = addressCursor.getLong(addressCursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID));
                String address = addressCursor.getString(addressCursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
                int type = addressCursor.getInt(addressCursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
                ContactModel contact = map.get(contactId);
                if (contact != null && address != null && !address.trim().isEmpty()) {
                    contact.addresses.add(new ContactModel.AddressEntry(address, postalTypeLabel(type)));
                }
            }
            addressCursor.close();
        }

        Cursor organizationCursor = cr.query(ContactsContract.Data.CONTENT_URI,
            new String[]{
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.CommonDataKinds.Organization.COMPANY,
                ContactsContract.CommonDataKinds.Organization.TITLE
            }, ContactsContract.Data.MIMETYPE + "=?",
            new String[]{ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE}, null);
        if (organizationCursor != null) {
            while (organizationCursor.moveToNext()) {
                long contactId = organizationCursor.getLong(organizationCursor.getColumnIndexOrThrow(
                    ContactsContract.Data.CONTACT_ID));
                ContactModel contact = map.get(contactId);
                if (contact != null && contact.organization == null) {
                    contact.organization = organizationCursor.getString(organizationCursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Organization.COMPANY));
                    contact.jobTitle = organizationCursor.getString(organizationCursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Organization.TITLE));
                }
            }
            organizationCursor.close();
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

    private static String emailTypeLabel(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Email.TYPE_WORK: return "Work";
            case ContactsContract.CommonDataKinds.Email.TYPE_HOME: return "Home";
            case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE: return "Mobile";
            default: return "Email";
        }
    }

    private static String postalTypeLabel(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK: return "Work";
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME: return "Home";
            default: return "Address";
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
        return Math.floorMod(name.hashCode(), 5);
    }

    public static int avatarDrawableRes(String name, Context context) {
        String[] names = {"bg_avatar_1", "bg_avatar_2", "bg_avatar_3", "bg_avatar_4", "bg_avatar_5"};
        int idx = avatarColorFor(name);
        return context.getResources().getIdentifier(names[idx], "drawable", context.getPackageName());
    }
}