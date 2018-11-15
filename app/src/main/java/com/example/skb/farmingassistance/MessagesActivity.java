package com.example.skb.farmingassistance;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesActivity extends AppCompatActivity {

    private DatabaseReference messageRef;
    private RecyclerView messagesList;
    private String messageKey;
    private String IsAdmin = "Yes";
    private ArrayList<Object> objectArrayList;
    private MessageListAdapter messageListAdapter;
    private List<Messages> userIdList= new ArrayList<>();
    private List<Object> messages = new ArrayList<>();
    private static final String TAG = "MessagesActivity";
    private String currentAdminID;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_activity);




        Toolbar AnnouncementToolbar = (Toolbar) findViewById(R.id.toolbar_messages);
        setSupportActionBar(AnnouncementToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("বার্তা রিকুয়েস্ট");


        messagesList = (RecyclerView) findViewById(R.id.messages_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        currentAdminID = mAuth.getCurrentUser().getUid().toString();

        messageRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentAdminID);



        //FetchMessageList();
        FetchMessage();




        messageListAdapter = new MessageListAdapter(userIdList);
        messagesList.setAdapter(messageListAdapter);


    }

    private void FetchMessageList() {
        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                GenericTypeIndicator<HashMap<String, Object>> objectsGTypeInd = new GenericTypeIndicator<HashMap<String, Object>>() {};
//                Map<String, Object> objectHashMap = dataSnapshot.getValue(objectsGTypeInd);
//                objectArrayList = new ArrayList<Object>(objectHashMap.values());
//                String user = dataSnapshot.getKey().toString();
//                System.out.println(user);
//


                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Log.v(TAG,""+ childDataSnapshot.getKey());    //gives the value for given keyname
                    userIdList.add(new Messages(childDataSnapshot.getKey()));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void FetchMessage() {
        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String users = dataSnapshot.getKey().toString();
                final Messages messages = new Messages(users);
                userIdList.add(messages);
                messageListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }




    public void sendAdminToChatActivity(){
        Intent chatIntent = new Intent(MessagesActivity.this, ChatActivity.class);
        chatIntent.putExtra("isAdmin", IsAdmin);
        chatIntent.putExtra("messageKey", messageKey);
        startActivity(chatIntent);
    }
}
