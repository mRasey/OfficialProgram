package com.codemine.talk2me;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_chat);

        ArrayList<String> msg = new ArrayList<>();

        TextView chattingWith = (TextView) findViewById(R.id.chattingWith);
        chattingWith.setText(getIntent().getStringExtra("contactName"));

        TextView backText = (TextView) findViewById(R.id.back_text);
        backText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ListView chatList = (ListView) findViewById(R.id.chattingListView);

        TextView inputMsg = (TextView) findViewById(R.id.inputMsg);
        inputMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
