package com.example.skb.farmingassistance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class NewProfile extends AppCompatActivity {
    private String NewCurrentUserId;
    private ImageView NewProfileImage;
    private  TextView NewProfileName;
    private  TextView NewProfileGender,NewProfileDob, NewProfileAddress, NewProfileDivision;
    private Button NewEditButton;
    private StorageReference NewUserImageRef;
    private ProgressDialog NewProgressDialog;
    private DatabaseReference NewUserAdminRef;
    private String NewIsAdmin;
    private FirebaseAuth NewFirebaseAuth;
    final static int NewGalleryPic = 1;
    private StorageReference NewFilepath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_profile);

        Toolbar NewProfileToolbar = (Toolbar) findViewById(R.id.toolbar_new_profile);
        setSupportActionBar(NewProfileToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("New প্রোফাইল");

        NewProfileImage = (ImageView) findViewById(R.id.new_profile_image);
        NewProfileName = (TextView) findViewById(R.id.new_profile_name);
        NewProfileGender = (TextView) findViewById(R.id.new_profile_gender);
        NewProfileDob = (TextView) findViewById(R.id.new_profile_dob);
        NewProfileAddress = (TextView) findViewById(R.id.new_profile_address);
        NewProfileDivision = (TextView) findViewById(R.id.new_profile_division);
        NewEditButton = (Button) findViewById(R.id.new_button_edit);
        NewUserImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");
        NewProgressDialog = new ProgressDialog(this);

        NewFirebaseAuth = FirebaseAuth.getInstance();
        NewCurrentUserId = NewFirebaseAuth.getCurrentUser().getUid().toString();


        NewIsAdmin = getIntent().getExtras().get("isAdmin").toString();
        if (NewIsAdmin.equals("Yes")){
            NewUserAdminRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(NewCurrentUserId);
            getInfo();
        }else {
            NewUserAdminRef = FirebaseDatabase.getInstance().getReference().child("Users").child(NewCurrentUserId);
            getInfo();
        }




        NewEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToEdit();
            }
        });
        NewProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateImage();
            }
        });




    }
    private void UpdateImage() {
        Intent gallery = new Intent();
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(gallery, NewGalleryPic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==NewGalleryPic && resultCode==RESULT_OK && data!=null){
            Uri changeImageUri = data.getData();
            //Picasso.get().load(R.drawable.profile).into(NewProfileImage);

//            NewProgressDialog.setTitle("তথ্য যোগ করা হচ্ছে");
//            NewProgressDialog.setMessage("অপেক্ষা করুন ...");
//            NewProgressDialog.show();
//            NewProgressDialog.setCanceledOnTouchOutside(false);

            NewFilepath = NewUserImageRef.child(NewCurrentUserId + ".jpg");
            assert changeImageUri != null;
            NewFilepath.putFile(changeImageUri).addOnCompleteListener(NewProfile.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        String down = task.getResult().getDownloadUrl().toString();

                        NewUserAdminRef.child("profileimage").setValue(down);
                    }else {
                        String m = null;
                        if (task.getException() != null){
                            m = task.getException().getMessage();
                        }
                        Toast.makeText(NewProfile.this, "আবার চেষ্টা করুন !!  Error :"+ m,Toast.LENGTH_SHORT).show();
                        NewProgressDialog.dismiss();
                    }
                }
            });

        }




    }

    private void sendToEdit() {
        Intent editIn = new Intent(NewProfile.this, EditProfileActivity.class);
        editIn.putExtra("isAdmin",NewIsAdmin);
        editIn.putExtra("uid",NewCurrentUserId);
        startActivity(editIn);
    }

    private void getInfo() {
        NewUserAdminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profileimage, fullname, dob, address, division, gender;
                if (dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")){
                        profileimage = dataSnapshot.child("profileimage").getValue(String.class);
                        Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(NewProfileImage);
                    }
                    if(dataSnapshot.hasChild("fullname")){
                        fullname = dataSnapshot.child("fullname").getValue(String.class);
                        NewProfileName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("gender")){
                        gender = dataSnapshot.child("gender").getValue(String.class);
                        NewProfileGender.setText(gender);
                    }
                    if(dataSnapshot.hasChild("dob")){
                        dob = dataSnapshot.child("dob").getValue(String.class);
                        NewProfileDob.setText(dob);
                    }
                    if(dataSnapshot.hasChild("address")){
                        address = dataSnapshot.child("address").getValue(String.class);
                        NewProfileAddress.setText(address);
                    }
                    if(dataSnapshot.hasChild("division")){
                        division = dataSnapshot.child("division").getValue(String.class);
                        NewProfileDivision.setText(division);
                    }
                }else {
                    Toast.makeText(NewProfile.this, "You have no information", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
