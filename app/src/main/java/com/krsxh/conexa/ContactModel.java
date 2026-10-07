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
    public String email;

    public static class PhoneEntry {
        public String number;
        public String label;
        public PhoneEntry(String number, String label) {
            this.number = number;
            this.label = label;
        }
    }

    public String getPrimaryPhone() {
        return phones.isEmpty() ? "" : phones.get(0).number;
    }

    public String getInitial() {
        if (name == null || name.isEmpty()) return "#";
        return String.valueOf(Character.toUpperCase(name.charAt(0)));
    }
}