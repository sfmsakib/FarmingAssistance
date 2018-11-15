package com.example.skb.farmingassistance;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, adminRef, postRef, likeRef, postDelRef;
    private ImageView navProfileImage;
    private TextView navProfileName;
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    String profileImage ;
    String profileName;
    private RecyclerView postList;
    Boolean likeChecker = false;
    Boolean AdminStatus = false;
    private String IsAdmin = " ";
    private String currentUserId;
    private StorageReference userImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        adminRef = FirebaseDatabase.getInstance().getReference().child("Admin");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        userImageRef = FirebaseStorage.getInstance().getReference().child("Post Images");



        postList = (RecyclerView) findViewById(R.id.post_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);






        if (currentUser != null){
            currentUserId = currentUser.getUid();
        }


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
//                Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
//                chatIntent.putExtra("isAdmin", IsAdmin);
//                startActivity(chatIntent);
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        navProfileImage = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.navImageView);
        navProfileName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.navTextView);


        DisplayAllUsersPost();

    }

    private void DisplayAllUsersPost() {

        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postRef, Post.class)
                .build();

        FirebaseRecyclerAdapter<Post,PostViewHolder> adapter =new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder holder, int position, @NonNull final Post model) {

                final String imageName = model.getPostimagename();


                final String postkey = getRef(position).getKey();
                //String pUrl = getItem(position).getProfileimage();
                final String uid = model.getUid();



                holder.fullname.setText(model.getFullname());
                holder.date.setText(model.getDate());
                holder.time.setText(model.getTime());
                holder.description.setText(model.getDescription());
                if (!model.getProfileimage().equals(" ")){
                    Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.profile).into(holder.profileimage);
                }
                if (!model.getPostimage().equals(" ")){
                    Picasso.get().load(model.getPostimage()).into(holder.postimage);
                }
//                if (!model.getPostimage().equals(" ")){
//                    Transformation blurTransformation = new Transformation() {
//                        @Override
//                        public Bitmap transform(Bitmap source) {
//                            Bitmap blurred = Blur.fastblur(MainActivity.this, source, 10);
//                            source.recycle();
//                            return blurred;
//                        }
//
//                        @Override
//                        public String key() {
//                            return "blur()";
//                        }
//                    };
//                    Picasso.get()
//                            .load(R.drawable.placeholder) // thumbnail url goes here
//                            .transform(blurTransformation)
//                            .resize(640, 480)
//                            .into(holder.postimage, new Callback() {
//                                @Override
//                                public void onSuccess() {
//                                    Picasso.get()
//                                            .load(model.getPostimage()) // image url goes here
//                                            .placeholder(R.drawable.placeholder)
//                                            .into(holder.postimage);
//                                }
//
//                                @Override
//                                public void onError(Exception e) {
//                                }
//                            });
//                }


               // Picasso.get().load(imageName).into(holder.postimage);


                holder.setLikeStatus(postkey);

                holder.likePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        likeChecker = true;
                        likeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (likeChecker.equals(true)){
                                    if (dataSnapshot.child(postkey).hasChild(currentUserId)){
                                        likeRef.child(postkey).child(currentUserId).removeValue();
                                        likeChecker = false;
                                    }else {
                                        likeRef.child(postkey).child(currentUserId).setValue(true);
                                        likeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });


                holder.commentPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commentIntent.putExtra("postkey",postkey);
                        startActivity(commentIntent);
                    }
                });

                holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(MainActivity.this, holder.buttonViewOption);
                        //inflating menu from xml resource
                        if (IsAdmin.equals("Yes")) {
                            popup.inflate(R.menu.option_menu);
                        }else {
                            if (uid.equals(currentUserId)){
                                popup.inflate(R.menu.option_menu);
                            }
                        }
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.action_delete:
                                        StorageReference postImage = userImageRef.child(imageName);
                                        postImage.delete();
                                        postDelRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postkey);
                                        postDelRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(MainActivity.this, "সফলভাবে পোস্ট মুছে ফেলা হয়েছে",Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Toast.makeText(MainActivity.this, "পোস্ট মুছে ফেলা হয় নি, আবার চেষ্টা করুন !!",Toast.LENGTH_SHORT).show();

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
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_design, parent, false);
                PostViewHolder viewHolder = new PostViewHolder(view);
                return viewHolder;

            }
        };
        adapter.startListening();
        postList.setAdapter(adapter);

    }

    public static class PostViewHolder extends RecyclerView.ViewHolder
    {


        TextView date, time, fullname, description;
        ImageButton buttonViewOption;
        ImageView postimage;
        CircleImageView profileimage;


        ImageButton likePost, commentPost;
        TextView likesShow;
        int likeCount;

        DatabaseReference likeRef;
        String currentUserId;
        FirebaseAuth mAuth;

        public PostViewHolder(View itemView) {
            super(itemView);

            date= itemView.findViewById(R.id.post_update_date);
            time= itemView.findViewById(R.id.post_update_time);
            fullname= itemView.findViewById(R.id.post_update_profile_name);
            description= itemView.findViewById(R.id.post_update_description);
            profileimage =itemView.findViewById(R.id.post_update_profile_image);
            postimage =itemView.findViewById(R.id.post_update_image);

            likePost =itemView.findViewById(R.id.like_button);
            commentPost =itemView.findViewById(R.id.comment_button);
            likesShow =itemView.findViewById(R.id.like_count);
            buttonViewOption =itemView.findViewById(R.id.textViewOptions);


            likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            currentUserId = mAuth.getCurrentUser().getUid();

        }

        public void setLikeStatus(final String PostKey){

            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if (dataSnapshot.child(PostKey).hasChild(currentUserId)){
                            likeCount = (int) dataSnapshot.child(PostKey).getChildrenCount();
                            likePost.setImageResource(R.drawable.ic_thumb_up_like_24dp);
                            likesShow.setText((Integer.toString(likeCount) + (" Likes")));
                        }else {
                            likeCount = (int) dataSnapshot.child(PostKey).getChildrenCount();
                            likePost.setImageResource(R.drawable.ic_thumb_up_unlike_24dp);
                            likesShow.setText((Integer.toString(likeCount) + (" Likes")));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        if (currentUser == null){
            sendUserToWelcomeActivity();

        }else {
            checkUserExistence();
        }
    }

    private void checkUserExistence() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(currentUserId)){
                    adminRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild(currentUserId)){
                                sendUserToSetupActivity();
                            }else {
                                adminRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            AdminStatus = true;
                                            IsAdmin = "Yes";
                                            if(dataSnapshot.hasChild("profileimage")){
                                                profileImage = dataSnapshot.child("profileimage").getValue(String.class);
                                                Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(navProfileImage);

                                            }
                                            if(dataSnapshot.hasChild("fullname")){
                                                profileName = dataSnapshot.child("fullname").getValue(String.class);
                                                navProfileName.setText(profileName);
                                            }
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

                }else {
                    userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                IsAdmin = "No";
                                if(dataSnapshot.hasChild("profileimage")){
                                    profileImage = dataSnapshot.child("profileimage").getValue(String.class);
                                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(navProfileImage);

                                }
                                if(dataSnapshot.hasChild("fullname")){
                                    profileName = dataSnapshot.child("fullname").getValue(String.class);
                                    navProfileName.setText(profileName);
                                }
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

    private void sendUserToSetupActivity() {
        Intent setupActivity = new Intent(MainActivity.this, SetupActivity.class);
        setupActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupActivity);
        finish();
    }

    private void sendUserToWelcomeActivity() {
        Intent welcomeIntent = new Intent(MainActivity.this,WelcomeActivity.class);
        startActivity(welcomeIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }








    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_profile) {
            sendUserToProfileActivity();
            return true;
        }
        else if(id == R.id.action_logout){
            mAuth.signOut();
            sendUserToWelcomeActivity();
            return true;
        }else if(id == R.id.action_add_post){
            sendUserToPostActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendUserToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        profileIntent.putExtra("isAdmin",IsAdmin);
        startActivity(profileIntent);
    }

    private void sendUserToPostActivity() {
        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        postIntent.putExtra("isAdmin",IsAdmin);
        startActivity(postIntent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_weather) {
            Intent weatherIntent = new Intent(MainActivity.this, WeatherActivity.class);
            //weatherIntent.putExtra("isAdmin",IsAdmin);
            startActivity(weatherIntent);

        } else if (id == R.id.nav_announcement) {
            Intent announcementIntent = new Intent(MainActivity.this, AnnouncementActivity.class);
            startActivity(announcementIntent);

        }else if (id == R.id.nav_profile) {
            Intent NewProfileIntent = new Intent(MainActivity.this, NewProfile.class);
            NewProfileIntent.putExtra("isAdmin",IsAdmin);
            startActivity(NewProfileIntent);

        } else if (id == R.id.nav_live_chat) {

            if (IsAdmin=="Yes"){
                Intent messageIntent = new Intent(MainActivity.this, MessagesActivity.class);
                messageIntent.putExtra("isAdmin", IsAdmin);
                startActivity(messageIntent);
            }else {
                Intent chatIntent = new Intent(MainActivity.this, SelectAdminActivity.class);
                startActivity(chatIntent);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
