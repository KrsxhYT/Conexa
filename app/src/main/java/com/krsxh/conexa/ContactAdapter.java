package com.krsxh.conexa.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.krsxh.conexa.ContactinfoActivity;
import com.krsxh.conexa.R;
import com.krsxh.conexa.models.ContactModel;
import com.krsxh.conexa.utils.ContactUtils;
import java.util.*;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTACT = 1;

    private List<Object> items = new ArrayList<Object>();
    private Context context;

    public ContactAdapter(Context context, List<ContactModel> contacts) {
        this.context = context;
        buildList(contacts);
    }

    public void updateContacts(List<ContactModel> contacts) {
        buildList(contacts);
        notifyDataSetChanged();
    }

    private void buildList(List<ContactModel> contacts) {
        items.clear();
        List<ContactModel> sorted = new ArrayList<ContactModel>(contacts);
        Collections.sort(sorted, new Comparator<ContactModel>() {
            @Override
            public int compare(ContactModel a, ContactModel b) {
                return a.name.compareToIgnoreCase(b.name);
            }
        });
        String currentSection = "";
        for (ContactModel c : sorted) {
            String initial = c.getInitial().toUpperCase(Locale.US);
            if (!initial.matches("[A-Z]")) initial = "#";
            if (!initial.equals(currentSection)) {
                currentSection = initial;
                items.add(currentSection);
            }
            items.add(c);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_CONTACT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.section_header_item, parent, false);
            return new HeaderHolder(v);
        } else {
            View v = inflater.inflate(R.layout.contact_item, parent, false);
            return new ContactHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).text.setText((String) item);
        } else if (holder instanceof ContactHolder) {
            final ContactModel contact = (ContactModel) item;
            ContactHolder h = (ContactHolder) holder;
            h.name.setText(contact.name);
            h.subtext.setText(contact.getPrimaryPhone());
            h.star.setVisibility(contact.starred ? View.VISIBLE : View.GONE);

            if (contact.photoUri != null) {
                h.avatarInitial.setVisibility(View.GONE);
                h.avatarImage.setVisibility(View.VISIBLE);
                h.avatarImage.setImageURI(Uri.parse(contact.photoUri));
            } else {
                h.avatarImage.setVisibility(View.VISIBLE);
                h.avatarImage.setImageDrawable(null);
                h.avatarImage.setBackgroundResource(ContactUtils.avatarDrawableRes(contact.name, context));
                h.avatarInitial.setVisibility(View.VISIBLE);
                h.avatarInitial.setText(contact.getInitial());
            }

            h.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ContactinfoActivity.class);
                    intent.putExtra("contact_id", contact.id);
                    intent.putExtra("lookup_key", contact.lookupKey);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView text;
        HeaderHolder(View v) {
            super(v);
            text = v.findViewById(R.id.textview2);
        }
    }

    static class ContactHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage, star;
        TextView avatarInitial, name, subtext;
        ContactHolder(View v) {
            super(v);
            avatarImage = v.findViewById(R.id.avatarImage);
            avatarInitial = v.findViewById(R.id.avatarInitial);
            name = v.findViewById(R.id.contactName);
            subtext = v.findViewById(R.id.contactSubtext);
            star = v.findViewById(R.id.starIndicator);
        }
    }
}