package com.krsxh.conexa;

import java.util.ArrayList;
import java.util.List;

public class ContactModel {
    public long id;
    public String lookupKey;
    public String name;
    public String photoUri;
    public boolean starred;
    public List<PhoneEntry> phones = new ArrayList<>();
    public List<EmailEntry> emails = new ArrayList<>();
    public List<AddressEntry> addresses = new ArrayList<>();
    public String organization;
    public String jobTitle;

    public static class PhoneEntry {
        public String number;
        public String label;
        public PhoneEntry(String number, String label) {
            this.number = number;
            this.label = label;
        }
    }

    public static class EmailEntry {
        public String address;
        public String label;

        public EmailEntry(String address, String label) {
            this.address = address;
            this.label = label;
        }
    }

    public static class AddressEntry {
        public String address;
        public String label;

        public AddressEntry(String address, String label) {
            this.address = address;
            this.label = label;
        }
    }

    public String getPrimaryPhone() {
        return phones.isEmpty() ? "" : phones.get(0).number;
    }

    public String getPrimaryEmail() {
        return emails.isEmpty() ? "" : emails.get(0).address;
    }

    public String getInitial() {
        if (name == null || name.isEmpty()) return "#";
        return String.valueOf(Character.toUpperCase(name.charAt(0)));
    }
}