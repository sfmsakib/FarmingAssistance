package com.example.skb.farmingassistance;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> userMessages;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;
    public ChatAdapter(List<Chat> userMessages){
        this.userMessages = userMessages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View V = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_design, parent, false);

        mAuth = FirebaseAuth.getInstance();
        return new ChatViewHolder(V);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatViewHolder holder, int position) {
            String messageSenderID = mAuth.getCurrentUser().getUid();
            final Chat messages = userMessages.get(position);

            String fromUserID = messages.getFrom();
            String messageType = messages.getType();
            String IsAdmin = messages.getIsadmin();

            if (IsAdmin.equals("Yes")){
                userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(fromUserID);
            }else {
                userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
            }

            userDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if (dataSnapshot.hasChild("profileimage")) {
                            String image = dataSnapshot.child("profileimage").getValue().toString();
                            Picasso.get().load(image).placeholder(R.drawable.profile).into(holder.receiverProfileImage);
                        }else {
                            Picasso.get().load(R.drawable.profile).into(holder.receiverProfileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if (messageType.equals("text")){
                holder.receiverMessge.setVisibility(View.INVISIBLE);
                holder.senderMessage.setVisibility(View.INVISIBLE);
                holder.receiverProfileImage.setVisibility(View.INVISIBLE);
                holder.senderImage.setVisibility(View.INVISIBLE);
                holder.receiverImage.setVisibility(View.INVISIBLE);



                if (fromUserID.equals(messageSenderID)){
                    holder.senderMessage.setVisibility(View.VISIBLE);
                    holder.senderMessage.setBackgroundResource(R.drawable.sender_message);
                    holder.senderMessage.setTextColor(Color.BLACK);
                    holder.senderMessage.setGravity(Gravity.RIGHT);
                    holder.senderMessage.setText(messages.getMessage());
                }else {
                    holder.senderMessage.setVisibility(View.INVISIBLE);
                    holder.receiverMessge.setVisibility(View.VISIBLE);
                    holder.receiverProfileImage.setVisibility(View.VISIBLE);

                    holder.receiverMessge.setBackgroundResource(R.drawable.reciever_message);
                    holder.receiverMessge.setTextColor(Color.WHITE);
                    holder.receiverMessge.setGravity(Gravity.LEFT);
                    holder.receiverMessge.setText(messages.getMessage());

                }
            }else if (messageType.equals("image")){
                holder.receiverMessge.setVisibility(View.INVISIBLE);
                holder.senderMessage.setVisibility(View.INVISIBLE);
                holder.receiverProfileImage.setVisibility(View.INVISIBLE);
                holder.senderImage.setVisibility(View.INVISIBLE);
                holder.receiverImage.setVisibility(View.INVISIBLE);



                if (fromUserID.equals(messageSenderID)){
                    holder.senderImage.setVisibility(View.VISIBLE);
                    //holder.senderImage.setGravity(Gravity.RIGHT);
                    Picasso.get()
                            .load(messages.getMessage())
                            .into(holder.senderImage);
                    //holder.senderMessage.setText(messages.getMessage());
                }else {
                    holder.senderImage.setVisibility(View.INVISIBLE);
                    holder.receiverImage.setVisibility(View.VISIBLE);
                    holder.receiverProfileImage.setVisibility(View.VISIBLE);

                    Picasso.get()
                            .load(messages.getMessage())
                            .into(holder.receiverImage);

                    //holder.receiverMessge.setGravity(Gravity.LEFT);
                    //holder.receiverMessge.setText(messages.getMessage());

                }
            }
    }

    @Override
    public int getItemCount() {
        return userMessages.size();
    }


    public class ChatViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessage, receiverMessge;
        public CircleImageView receiverProfileImage;
        public ImageView senderImage, receiverImage;
        public ChatViewHolder(View itemView) {
            super(itemView);

            senderMessage = (TextView) itemView.findViewById(R.id.chat_sender_message);
            receiverMessge = (TextView) itemView.findViewById(R.id.chat_receiver_message);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.chat_profileImage);

            senderImage = (ImageView) itemView.findViewById(R.id.chat_sender_image);
            receiverImage = (ImageView) itemView.findViewById(R.id.chat_receiver_image);

        }
    }




}
