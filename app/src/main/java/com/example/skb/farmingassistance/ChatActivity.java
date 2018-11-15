package com.example.skb.farmingassistance;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {


    private ImageButton imageSend, messageSend;
    private EditText messageInput;
    private String IsAdmin;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private RecyclerView userMessageList;
    private final List<Chat> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private ChatAdapter chatAdapter;
    private String messageKey;
    private static int Gallery_pick = 1;
    private Uri imageUri;
    private StorageReference filepath;
    private StorageReference userImageRef;
    private String RandomNum, downloadUri;

    private String messageSenderID, messageReceiverID,currentUserID, currentDate, currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Initialize();
        messageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        FetchMessages();
        imageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar fordate = Calendar.getInstance();
                SimpleDateFormat currRandomTime = new SimpleDateFormat("yyMMddHHmmssZ");
                String randomTime = currRandomTime.format(fordate.getTime());

                RandomNum = randomTime + currentUserID;

                userImageRef = FirebaseStorage.getInstance().getReference().child("Messages");

                galleryOpen();
            }
        });

    }

    private void galleryOpen() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_pick);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==Gallery_pick && resultCode==RESULT_OK && data!=null){
            imageUri =data.getData();
            assert imageUri != null;
            String imageName = imageUri.getLastPathSegment();


            messageInput.setText(imageName);
//            final InputStream imageStream;
//            try {
//                imageStream = getContentResolver().openInputStream(imageUri);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                imageSend.setImageBitmap(selectedImage);
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }

            filepath = userImageRef.child(imageUri.getLastPathSegment() + RandomNum +".jpg");

        }
    }
    private void FetchMessages() {

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()){
                    Chat messages = dataSnapshot.getValue(Chat.class);
                    messageList.add(messages);
                    chatAdapter.notifyDataSetChanged();
                }
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

    private void Initialize() {
        Toolbar AnnouncementToolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        setSupportActionBar(AnnouncementToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("সরাসরি কথোপকথন");


        imageSend = (ImageButton) findViewById(R.id.chat_image_send);
        messageSend = (ImageButton) findViewById(R.id.chat_send);
        messageInput= (EditText) findViewById(R.id.chat_text);

        RootRef= FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid().toString();


        IsAdmin = getIntent().getExtras().get("isAdmin").toString();
        if (IsAdmin.equals("Yes")){
            messageKey = getIntent().getExtras().get("messageKey").toString();

            messageReceiverID = messageKey;
            messageSenderID = currentUserID;
        }else {
            messageKey = getIntent().getExtras().get("messageKey").toString();

            messageReceiverID = messageKey;
            messageSenderID = currentUserID;

        }
        chatAdapter = new ChatAdapter(messageList);
        userMessageList = (RecyclerView) findViewById(R.id.user_message_list);

        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(chatAdapter);

    }


    private void sendMessage() {
        String messageText = messageInput.getText().toString();
        if (imageUri == null){
            if (TextUtils.isEmpty(messageText)){
                Toast.makeText(ChatActivity.this, "আপনার বার্তা লিখুন", Toast.LENGTH_SHORT).show();

            }else {

                String message_sender_ref = "Messages"+ "/" + messageSenderID + "/"+ messageReceiverID;
                String message_receiver_ref = "Messages" + "/" +  messageReceiverID + "/"+ messageSenderID;

                DatabaseReference user_messages_key = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();
                String message_push_id = user_messages_key.getKey();


                Calendar fordate = Calendar.getInstance();
                SimpleDateFormat currDate = new SimpleDateFormat("dd-MM-yyyy");
                currentDate = currDate.format(fordate.getTime());

                Calendar fortime = Calendar.getInstance();
                SimpleDateFormat currTime = new SimpleDateFormat("HH-mm aa");
                currentTime = currTime.format(fortime.getTime());

                Map messageMap = new HashMap();
                messageMap.put("message", messageText);
                messageMap.put("date", currentDate);
                messageMap.put("time", currentTime);
                messageMap.put("type", "text");
                messageMap.put("from", currentUserID);
                messageMap.put("isadmin", IsAdmin);

                Map messagePath = new HashMap();
                messagePath.put(message_sender_ref + "/" + message_push_id , messageMap);
                messagePath.put(message_receiver_ref + "/" + message_push_id , messageMap);

                RootRef.updateChildren(messagePath).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            messageInput.getText().clear();
                            Toast.makeText(ChatActivity.this, "সফলভাবেব বার্তা যোগ করা হয়েছে", Toast.LENGTH_SHORT).show();
                        }else {
                            String m = task.getException().getMessage().toString();
                            Toast.makeText(ChatActivity.this, "আবার চেষ্টা করুন!! Error:" + m, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        }else {
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){

                        downloadUri = task.getResult().getDownloadUrl().toString();

                        //Toast.makeText(ChatActivity.this,"Success to store image", Toast.LENGTH_SHORT).show();

                        saveFileToDatabase(downloadUri);

                    }else {
                        String m = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this,"পিকচার আপলোড করতে ব্যর্থ !! আবার চেষ্টা করুন!! Error:"+ m, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void saveFileToDatabase(String loadUri) {
        String message_sender_ref = "Messages"+ "/" + messageSenderID + "/"+ messageReceiverID;
        String message_receiver_ref = "Messages" + "/" +  messageReceiverID + "/"+ messageSenderID;

        DatabaseReference user_messages_key = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();
        String message_push_id = user_messages_key.getKey();


        Calendar fordate = Calendar.getInstance();
        SimpleDateFormat currDate = new SimpleDateFormat("dd-MM-yyyy");
        currentDate = currDate.format(fordate.getTime());

        Calendar fortime = Calendar.getInstance();
        SimpleDateFormat currTime = new SimpleDateFormat("HH-mm aa");
        currentTime = currTime.format(fortime.getTime());

        Map messageMap = new HashMap();
        messageMap.put("message", loadUri);
        messageMap.put("date", currentDate);
        messageMap.put("time", currentTime);
        messageMap.put("type", "image");
        messageMap.put("from", currentUserID);
        messageMap.put("isadmin", IsAdmin);

        Map messagePath = new HashMap();
        messagePath.put(message_sender_ref + "/" + message_push_id , messageMap);
        messagePath.put(message_receiver_ref + "/" + message_push_id , messageMap);

        RootRef.updateChildren(messagePath).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    messageInput.getText().clear();
                    imageUri = null;
                    Toast.makeText(ChatActivity.this, "সফলভাবেব পিকচার আপলোড করা হয়েছে", Toast.LENGTH_SHORT).show();
                }else {
                    String m = task.getException().getMessage().toString();
                    Toast.makeText(ChatActivity.this, "আবার চেষ্টা করুন!! Error:" + m, Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

}
