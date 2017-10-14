package com.example.giangnguyen.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.giangnguyen.contact.R;
import com.example.giangnguyen.model.Contact;

import java.util.List;

/**
 * Created by Giang Nguyen on 12/10/2017.
 */

public class ContactAdapter extends ArrayAdapter {
    private Context context;
    private int res;
    private List<Contact> listContact;

    public ContactAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.res = resource;
        this.context = context;
        this.listContact = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.contact_list_item_layout, parent, false);

            viewHolder.contactName = convertView.findViewById(R.id.contactName);
            viewHolder.contactPhone = convertView.findViewById(R.id.contactPhone);
            viewHolder.contactAvatar = convertView.findViewById(R.id.contactAvatar);

            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();

        Contact contact = listContact.get(position);

        viewHolder.contactName.setText(contact.getName());
        viewHolder.contactPhone.setText(contact.getPhone());

        if (contact.getSex() == 1) viewHolder.contactAvatar.setBackgroundResource(R.drawable.male);
        else viewHolder.contactAvatar.setBackgroundResource(R.drawable.female);

        return convertView;
    }

    private class ViewHolder {
        ImageView contactAvatar;
        TextView contactName, contactPhone;
    }
}
