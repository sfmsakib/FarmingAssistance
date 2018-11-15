package com.example.skb.farmingassistance;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnnouncementActivity extends AppCompatActivity {
        private String IsAdmin;
        private Boolean AdminStatus;
        private DatabaseReference postDelRef;

        private DatabaseReference userRef, adminRef;
        private FirebaseAuth mAuth = FirebaseAuth.getInstance();
        private String currentUserId;
        private FirebaseUser currentUser;

        private DatabaseReference annRef;
        private RecyclerView announcementList;
        private StorageReference AnnouncementImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);

        currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            currentUserId = mAuth.getCurrentUser().getUid().toString();
            userRef = FirebaseDatabase.getInstance().getReference().child("Users");
            adminRef = FirebaseDatabase.getInstance().getReference().child("Admin");
            checkAdminExistence();
        }else {
            IsAdmin = "No";
        }

        Toolbar AnnouncementToolbar = (Toolbar) findViewById(R.id.toolbar_Announcement);
        setSupportActionBar(AnnouncementToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("কৃষি ঘোষনা");




        announcementList = (RecyclerView) findViewById(R.id.announcement_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        announcementList.setLayoutManager(linearLayoutManager);

        AnnouncementImageRef = FirebaseStorage.getInstance().getReference().child("Announcement Images");


        annRef = FirebaseDatabase.getInstance().getReference().child("Announcements");



        DisplayAllAnnouncement();

    }

    private void checkAdminExistence() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentUserId)){
                    IsAdmin = "No";
                }else {
                    adminRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(currentUserId)){
                                IsAdmin = "Yes";
                            }else {
                                IsAdmin = "No";
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

    private void DisplayAllAnnouncement() {

        FirebaseRecyclerOptions<Announcement> options = new FirebaseRecyclerOptions.Builder<Announcement>()
                .setQuery(annRef, Announcement.class)
                .build();

        FirebaseRecyclerAdapter<Announcement,AnnouncementActivity.AnnouncementViewHolder> adapter =new FirebaseRecyclerAdapter<Announcement, AnnouncementActivity.AnnouncementViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final AnnouncementActivity.AnnouncementViewHolder holder, int position, @NonNull Announcement model) {

                final String imageName = model.getPostimagename();

                final String postkey = getRef(position).getKey();

                //holder.fullname.setText(model.getFullname());
                holder.date.setText(model.getDate());
                holder.time.setText(model.getTime());
                holder.description.setText(model.getDescription());
                //Picasso.get().load(model.getProfileimage()).into(holder.profileimage);
                if (!model.getPostimage().equals(" ")){
                    Picasso.get().load(model.getPostimage()).into(holder.postimage);

                }

                holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(AnnouncementActivity.this, holder.buttonViewOption);
                        //inflating menu from xml resource
                        if (IsAdmin.equals("Yes")) {
                            popup.inflate(R.menu.option_menu_announcement);
                        }
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.action_delete_ann:
                                        StorageReference postImage = AnnouncementImageRef.child(imageName);
                                        postImage.delete();
                                        postDelRef = FirebaseDatabase.getInstance().getReference().child("Announcements").child(postkey);
                                        postDelRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(AnnouncementActivity.this, "সফলভাবে ঘোষনা মুছে ফেলা হয়েছে",Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Toast.makeText(AnnouncementActivity.this, "ঘোষনা মুছে ফেলা হয় নি, আবার চেষ্টা করুন !!",Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });

                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                        //displaying the popup
                        popup.show();

                    }
                });
            }

            @NonNull
            @Override
            public AnnouncementActivity.AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.announcement_design, parent, false);
                AnnouncementActivity.AnnouncementViewHolder viewHolder = new AnnouncementActivity.AnnouncementViewHolder(view);
                return viewHolder;

            }
        };
        adapter.startListening();
        announcementList.setAdapter(adapter);

    }


    public static class AnnouncementViewHolder extends RecyclerView.ViewHolder
    {


        TextView date, time, fullname, description;
        ImageView postimage;
        CircleImageView profileimage;
        ImageButton buttonViewOption;


        ImageButton likePost, commentPost;
        TextView likesShow;
        int likeCount;

        DatabaseReference likeRef;
        String currentUserId;
        FirebaseAuth mAuth;

        public AnnouncementViewHolder(View itemView) {
            super(itemView);

            date= itemView.findViewById(R.id.announcement_update_date);
            time= itemView.findViewById(R.id.announcement_update_time);
            //fullname= itemView.findViewById(R.id.post_update_profile_name);
            description= itemView.findViewById(R.id.announcement_update_description);
            //profileimage =itemView.findViewById(R.id.post_update_profile_image);
            postimage =itemView.findViewById(R.id.announcement_update_image);
            buttonViewOption =itemView.findViewById(R.id.textViewOptionsannouncement);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

            getMenuInflater().inflate(R.menu.announcement, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_announcement) {
            if (IsAdmin.equals("Yes")){
            sendUserToAddAnnouncementActivity();
            }else {
                Toast.makeText(AnnouncementActivity.this, "শুধূ এডমিন কৃষি ঘোষনা যোগ করতে পারবে!!!",Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendUserToAddAnnouncementActivity() {
        Intent AddAnnIntent = new Intent(AnnouncementActivity.this, AddAnnouncementActivity.class);
        startActivity(AddAnnIntent);
    }



}