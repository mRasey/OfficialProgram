package com.codemine.talk2me;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    ArrayList<ChattingInfo> chattingInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_chat);

        initChattingInfo();

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
        ChattingAdapter chattingAdapter = new ChattingAdapter(ChatActivity.this, R.layout.chatting_item, chattingInfos);
        chatList.setAdapter(chattingAdapter);

        TextView inputMsg = (TextView) findViewById(R.id.inputMsg);
        inputMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void initChattingInfo() {
        chattingInfos.add(new ChattingInfo(R.id.other_layout, R.id.own_layout, R.drawable.head,
                R.drawable.head, "hellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohellohello", "", MsgType.OTHER));
        chattingInfos.add(new ChattingInfo(R.id.other_layout, R.id.own_layout, R.drawable.head,
                R.drawable.head, "", "worldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworldworld", MsgType.OWN));
    }
}