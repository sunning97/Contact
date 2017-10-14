package com.example.giangnguyen.contact;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.giangnguyen.adapter.ContactAdapter;
import com.example.giangnguyen.model.Contact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {
    EditText inputName, inputPhone;
    RadioButton rbtnMale, rbtnFemale;
    Button btnAdd;
    ListView listContact;

    List<Contact> list;
    ContactAdapter adapter;

    String DATABASE_NAME = "dbContact.sqlite";
    String DB_PATH_SUFFIX = "/databases/";

    Dialog dialog;

    SQLiteDatabase database = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        processCopyDatabase();
        checkAndRequestPermission();

        addControll();
        addEvent();

        readDatabase();
    }

    private void readDatabase() {
        database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        Cursor cursor = database.query("Contact", null, null, null, null, null, null);
        list.clear();
        while (cursor.moveToNext()) {
            String name = cursor.getString(1);
            String phone = cursor.getString(2);
            int sex = cursor.getInt(3);
            Contact c = new Contact(name, phone, sex);
            list.add(c);
        }
    }

    private void processCopyDatabase() {
        File dbFile = getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            try {
                copyDatabaseFromAsset();
            } catch (Exception e) {
            }
        }
    }

    private void addEvent() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputName.getText().toString().trim();
                String phone = inputPhone.getText().toString().trim();
                int sex;
                if (rbtnMale.isChecked()) sex = 1;
                else sex = 2;

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                    makeText(MainActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                } else {
                    Contact c = new Contact(name, phone, sex);
                    insertDatabase(c);
                    readDatabase();
                    inputName.setText("");
                    inputPhone.setText("");
                    rbtnMale.setChecked(true);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        listContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDialogg(i);
            }
        });
    }

    private void showDialogg(final int i) {
        final int pos = i;

        dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Contact Detail");
        dialog.setContentView(R.layout.detail_contact_layout);

        final Button btnCall = dialog.findViewById(R.id.btnCall);
        Button btnSMS = dialog.findViewById(R.id.btnSMS);
        Button btnEdit = dialog.findViewById(R.id.btnEdit);
        TextView contactDetailName = dialog.findViewById(R.id.contactDetailName);
        TextView contactDetailPhone = dialog.findViewById(R.id.contactDetailPhone);
        ImageView contactDetailAvatar = dialog.findViewById(R.id.contactDetailAvatar);
        ImageButton ibtnDelete = dialog.findViewById(R.id.ibtnDelete);

        final Contact c = list.get(pos);
        int sex;
        if (c.getSex() == 1) sex = 1;
        else sex = 2;
        if (sex == 1) contactDetailAvatar.setBackgroundResource(R.drawable.male);
        else contactDetailAvatar.setBackgroundResource(R.drawable.female);
        contactDetailName.setText(c.getName());
        contactDetailPhone.setText(c.getPhone());

        ibtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDelete(pos);
                dialog.dismiss();

            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callProcess(pos);
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProcess(pos);
            }
        });

        btnSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                smsProcess(pos);
            }
        });
        dialog.show();
    }

    private void showConfirmDelete(int pos) {
        final int i = pos;
        final Contact c = list.get(i);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Bạn có thực sự muốn xóa: ");

        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDialogg(i);
            }
        });

        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                database.delete("Contact", "phone=?", new String[]{c.getPhone()});
                Toast.makeText(MainActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                readDatabase();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        AlertDialog alertdialog = builder.create();
        alertdialog.show();

    }

    private void smsProcess(int pos) {
        Contact c = list.get(pos);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:"+c.getPhone()));
        startActivity(intent);
    }

    private void editProcess(int pos) {
        int i = pos;
        dialog.dismiss();
        showEditDialog(i);
    }

    private void showEditDialog(final int i) {
        final Contact c = list.get(i);
        int sex;
        if (c.getSex() == 1) sex = 1;
        else sex = 2;

        dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Edit Contact");
        dialog.setContentView(R.layout.contact_edit_layout);

        final EditText editName = dialog.findViewById(R.id.editContactName);
        final EditText editPhone = dialog.findViewById(R.id.editContactPhone);
        final RadioButton editMale = dialog.findViewById(R.id.editMale);
        RadioButton editFemale = dialog.findViewById(R.id.editFemale);
        Button btnOk = dialog.findViewById(R.id.btnComfirmEdit);
        Button btnCancel = dialog.findViewById(R.id.btnComfirmCancel);

        editName.setText(c.getName());
        editPhone.setText(c.getPhone());
        if (sex == 1) editMale.setChecked(true);
        else editFemale.setChecked(true);


        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editName.getText().toString().trim();
                String phone = editPhone.getText().toString().trim();
                int sex;
                if (editMale.isChecked()) sex = 1;
                else sex = 2;

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone))
                    makeText(MainActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                else {
                    ContentValues row = new ContentValues();
                    row.put("name", name);
                    database.update("Contact", row, "phone=?", new String[]{c.getPhone()});

                    row.put("phone", phone);
                    database.update("Contact", row, "name=?", new String[]{name});

                    row.put("sex", sex);
                    database.update("Contact", row, "phone=?", new String[]{phone});

                    readDatabase();
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showDialogg(i);
            }
        });


        dialog.show();
    }

    private void callProcess(int pos) {
        Contact c = list.get(pos);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:"+c.getPhone()));
        startActivity(intent);

    }

    private void insertDatabase(Contact c) {
        ContentValues row = new ContentValues();
        row.put("name", c.getName());
        row.put("phone", c.getPhone());
        row.put("sex", c.getSex());
        long r = database.insert("Contact", null, row);
    }

    private void addControll() {
        inputName = (EditText) findViewById(R.id.inputName);
        inputPhone = (EditText) findViewById(R.id.inputPhone);
        rbtnMale = (RadioButton) findViewById(R.id.contactMale);
        rbtnFemale = (RadioButton) findViewById(R.id.contactFemale);
        listContact = (ListView) findViewById(R.id.ltvContact);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        list = new ArrayList<Contact>();
        adapter = new ContactAdapter(MainActivity.this, R.layout.contact_list_item_layout, list);
        readDatabase();
        listContact.setAdapter(adapter);
    }

    private String getDatabasePath() {
        return getApplicationInfo().dataDir + DB_PATH_SUFFIX + DATABASE_NAME;
    }

    private void copyDatabaseFromAsset() {
        try {

            InputStream myInput;
            myInput = getAssets().open(DATABASE_NAME);
            String outFileName = getDatabasePath();
            File f = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX);
            if (!f.exists()) f.mkdir();

            OutputStream myOutput = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myInput.close();
            myOutput.close();
        } catch (
                Exception e)

        {
        }
    }
    private void checkAndRequestPermission()
    {
        String[] permission = new String[]
                {
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS
                };

        List<String> listPermissionNeeded = new ArrayList<>();
        for(String permissions : permission)
        {
            if(ContextCompat.checkSelfPermission(this,permissions)!=PackageManager.PERMISSION_GRANTED)
                listPermissionNeeded.add(permissions);
        }

        if(!listPermissionNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]),1);
        }
    }
}
