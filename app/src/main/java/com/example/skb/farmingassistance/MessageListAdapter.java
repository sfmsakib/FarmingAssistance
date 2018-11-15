package com.example.skb.farmingassistance;

import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageListViewHolder> {

    private DatabaseReference userDatabaseRef;
    private FirebaseAuth  mAurh = FirebaseAuth.getInstance();
    private String currentAdminID;


    private List<Messages> userMessagesList;
    //private List<Object> messages;
    private DatabaseReference messageRef;
    public int colorWhite, colorGray;



    public MessageListAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
        //this.messages = messages;
    }


    @NonNull
    @Override
    public MessageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View V = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list_design, parent, false);

        return new MessageListViewHolder(V);
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageListViewHolder holder, int position) {

        final Messages messages = userMessagesList.get(position);
        final String userKey = messages.getUser();

        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userKey);
        userDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fullname = dataSnapshot.child("fullname").getValue().toString();
                String image = dataSnapshot.child("profileimage").getValue().toString();
                holder.MessageListText.setText(fullname);
                Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        currentAdminID = mAurh.getCurrentUser().getUid().toString();
        messageRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentAdminID).child(userKey);

        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.hasChild("message")) {
                    String msg;
                    String newMessage = dataSnapshot.child("message").getValue(String.class);
                    String msgType = dataSnapshot.child("type").getValue(String.class);
                    String isAdmin = dataSnapshot.child("isadmin").getValue(String.class);
                    if (msgType.equals("text")){
                         msg = newMessage;
                    }else {
                        msg = "sent an image";
                    }
                    if (isAdmin.equals("Yes")){
                        holder.cardView.setCardBackgroundColor(colorWhite);
                        holder.lastMessage.setText(String.format("You: %s", msg));

                    }else {
                        holder.cardView.setCardBackgroundColor(colorGray);
                        holder.lastMessage.setText(msg);

                    }
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

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(v.getContext(), ChatActivity.class);
                chatIntent.putExtra("isAdmin","Yes");
                chatIntent.putExtra("messageKey",userKey);
                v.getContext().startActivity(chatIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageListViewHolder extends RecyclerView.ViewHolder {
        public TextView MessageListText;
        public TextView lastMessage;
        public CardView cardView;
        public CircleImageView profileImage;


        public MessageListViewHolder(View itemView) {
            super(itemView);
            MessageListText = (TextView) itemView.findViewById(R.id.message_list_design_text);
            lastMessage = (TextView) itemView.findViewById(R.id.message_list_design_last_message);
            cardView = (CardView) itemView.findViewById(R.id.message_list_card_view);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_list_profile_image);

            colorWhite = ContextCompat.getColor(itemView.getContext(), R.color.colorWhite);
            colorGray = ContextCompat.getColor(itemView.getContext(), R.color.graylight);

        }
    }
}
