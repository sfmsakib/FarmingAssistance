package com.example.skb.farmingassistance;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
    String PostKey, currentUserId;
    private RecyclerView commentsList;
    private EditText commentsText;
    private ImageButton commentsSend;
    private DatabaseReference commentRef, userRef, adminRef;
    private FirebaseAuth mAuth;
    private String currentTime, currentDate, RandomNum;
    private Toolbar comment_toolbar;
    private String full_Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        PostKey = getIntent().getExtras().get("postkey").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid().toString();
        commentRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("Comments");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        adminRef = FirebaseDatabase.getInstance().getReference().child("Admin");

        comment_toolbar = (Toolbar) findViewById(R.id.toolbar_comment);

        setSupportActionBar(comment_toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("মন্তব্য");

        commentsList = (RecyclerView) findViewById(R.id.comments_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        commentsList.setLayoutManager(linearLayoutManager);

        commentsText = (EditText) findViewById(R.id.comments_text);
        commentsSend = (ImageButton) findViewById(R.id.comments_send);

        DisplayAllUsersComments();
        getInfo();

        commentsSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!full_Name.isEmpty()){
                    putComments();
                }else {
                    Toast.makeText(CommentsActivity.this, " আপনার প্রোফাইল সম্পূর্ণ korun!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getInfo() {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    full_Name = dataSnapshot.child("fullname").getValue().toString();
                }else {
                    adminRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                full_Name = dataSnapshot.child("fullname").getValue().toString();
                            }else {
                                Toast.makeText(CommentsActivity.this, " আপনার প্রোফাইল সম্পূর্ণ না!!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void DisplayAllUsersComments() {

        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(commentRef, Comments.class)
                .build();

        FirebaseRecyclerAdapter<Comments,CommentsActivity.CommentViewHolder> adapter = new FirebaseRecyclerAdapter<Comments, CommentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comments model) {

                holder.nameField.setText(model.getFullname());
                holder.commDate.setText(model.getDate());
                holder.commTime.setText(model.getTime());
                holder.desc.setText(model.getComment());

            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments, parent, false);
                return new CommentViewHolder(view);

            }
        };

        adapter.startListening();
        commentsList.setAdapter(adapter);

    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder
    {

        TextView nameField, commDate, commTime, desc;
        public CommentViewHolder(View itemView) {
            super(itemView);

            nameField = itemView.findViewById(R.id.comments_name);
            commDate = itemView.findViewById(R.id.comments_date);
            commTime = itemView.findViewById(R.id.comments_time);
            desc = itemView.findViewById(R.id.comments_desc);

        }
    }

    private void putComments() {
        String textCom = commentsText.getText().toString().trim();
        if (TextUtils.isEmpty(textCom)){
            Toast.makeText(CommentsActivity.this, "আপনার মন্তব্য লিখুন ", Toast.LENGTH_SHORT).show();
        }else {

            Calendar fordate = Calendar.getInstance();
            SimpleDateFormat currDate = new SimpleDateFormat("dd/MM/yyyy");
            currentDate = currDate.format(fordate.getTime());

            Calendar fortime = Calendar.getInstance();
            SimpleDateFormat currTime = new SimpleDateFormat("h:mm a");
            currentTime = currTime.format(fortime.getTime());


            SimpleDateFormat currRandomTime = new SimpleDateFormat("yyMMddHHmmssZ");
            String randomTime = currRandomTime.format(fortime.getTime());

            RandomNum = randomTime + currentUserId;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", currentUserId);
            commentsMap.put("fullname", full_Name);
            commentsMap.put("date", currentDate);
            commentsMap.put("time", currentTime);
            commentsMap.put("comment", textCom);

            commentRef.child(RandomNum).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        commentsText.getText().clear();
                        Toast.makeText(CommentsActivity.this, "সফলভাবে মন্তব্য যোগ করা হয়েছে", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(CommentsActivity.this, "আবার চেষ্টা করুন!! Error:", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

}
