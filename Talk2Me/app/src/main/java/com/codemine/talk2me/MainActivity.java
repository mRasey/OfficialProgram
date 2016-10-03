package com.codemine.talk2me;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Contact> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        initContacts(); // 初始化联系人列表

        ContactsAdapter contactsAdapter = new ContactsAdapter(MainActivity.this, R.layout.contact_item, contacts);
        final ListView contractsList = (ListView) findViewById(R.id.contactsList);
        contractsList.setAdapter(contactsAdapter);

        contractsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Contact contact = contacts.get(position);
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("contactName", contact.name);
                startActivity(intent);
            }
        });
    }

    public void initContacts() {
        contacts.add(new Contact("billy", "hahaha", "now", R.drawable.head));
    }
}
