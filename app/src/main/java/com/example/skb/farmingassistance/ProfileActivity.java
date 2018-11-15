package com.example.skb.farmingassistance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
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
import com.squareup.picasso.Transformation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference userAdminRef;
    private String IsAdmin;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private ImageView profileImage;
    private  TextView profileName;
    private  TextView profileGender,profileDob, profileAddress, profileDivision;
    private Button editButton;
    final static int galleryPic = 1;
    private Uri changeProfileUri;
    private StorageReference filepath;
    private StorageReference userImageRef;
    private ProgressDialog progressDialog;
    private Bitmap bitmap= null;
    public byte[] byteArray = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        Toolbar AnnouncementToolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        setSupportActionBar(AnnouncementToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("প্রোফাইল");

        profileImage = (ImageView) findViewById(R.id.profile_image);
        profileName = (TextView) findViewById(R.id.profile_name);
        profileGender = (TextView) findViewById(R.id.profile_gender);
        profileDob = (TextView) findViewById(R.id.profile_dob);
        profileAddress = (TextView) findViewById(R.id.profile_address);
        profileDivision = (TextView) findViewById(R.id.profile_division);
        editButton = (Button) findViewById(R.id.button_edit);
        userImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid().toString();


        IsAdmin = getIntent().getExtras().get("isAdmin").toString();
        if (IsAdmin.equals("Yes")){
            userAdminRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(currentUserId);
            getInfo();
        }else {
            userAdminRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
            getInfo();
        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToEditActivity();
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateProfileImage();
            }
        });

    }

    private void UpdateProfileImage() {


        Intent gallery = new Intent();
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(gallery, galleryPic);
    }
    public String getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==galleryPic && resultCode==RESULT_OK && data!=null){

            changeProfileUri = data.getData();

//            Transformation blurTransformation = new Transformation() {
//                @Override
//                public Bitmap transform(Bitmap source) {
//                    Bitmap blurred = Blur.fastblur(ProfileActivity.this, source, 10);
//                    source.recycle();
//                    return blurred;
//                }
//
//                @Override
//                public String key() {
//                    return "blur()";
//                }
//            };

            Picasso.get()
                    .load(R.drawable.profile) // thumbnail url goes here
                    //.transform(blurTransformation)
                    .into(profileImage);
//
//            File file = new File(getPath(resultUri));
//            try {
//                bitmap = new Compressor(this)
//                        .setMaxWidth(640)
//                        .setMaxHeight(480)
//                        .setQuality(50)
//                        .compressToBitmap(file);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
//            byteArray = byteArrayOutputStream.toByteArray();
//

//            CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setAspectRatio(1,1)
//                    .start(this);
//        }
//        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
//
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//
//            if (resultCode==RESULT_OK){
//
//                resultUri = result.getUri();

//            final InputStream imageStream;
//            try {
//                imageStream = getContentResolver().openInputStream(resultUri);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                profileImage.setImageBitmap(selectedImage);
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
            progressDialog.setTitle("আপনার তথ্য যোগ করা হচ্ছে");
            progressDialog.setMessage("দয়া করে অপেক্ষা করুন ...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            filepath = userImageRef.child(currentUserId + ".jpg");
            filepath.putFile(changeProfileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        String downloadU = task.getResult().getDownloadUrl().toString();

                        userAdminRef.child("profileimage").setValue(downloadU)
                                .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            Toast.makeText(ProfileActivity.this, "সফল হয়েছে ",Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }else {
                                            String m = task.getException().getMessage();
                                            Toast.makeText(ProfileActivity.this, "প্রোফাইল পিকচার দিতে ব্যর্থ, আবার চেষ্টা করুন !!  Error :" + m, Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                    }else {
                        String m = null;
                        if (task.getException() != null){
                            m = task.getException().getMessage();
                        }
                        Toast.makeText(ProfileActivity.this, "আবার চেষ্টা করুন !!  Error :"+ m,Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }

    }

    private void sendToEditActivity() {
        Intent editIntent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        editIntent.putExtra("isAdmin",IsAdmin);
        editIntent.putExtra("uid",currentUserId);
        startActivity(editIntent);
    }

    private void getInfo() {
        userAdminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profileimage, fullname, dob, address, division, gender;
                if (dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")){
                        profileimage = dataSnapshot.child("profileimage").getValue(String.class);
                        Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(profileImage);

                    }
                    if(dataSnapshot.hasChild("fullname")){
                        fullname = dataSnapshot.child("fullname").getValue(String.class);
                        profileName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("gender")){
                        gender = dataSnapshot.child("gender").getValue(String.class);
                        profileGender.setText(gender);
                    }
                    if(dataSnapshot.hasChild("dob")){
                        dob = dataSnapshot.child("dob").getValue(String.class);
                        profileDob.setText(dob);
                    }
                    if(dataSnapshot.hasChild("address")){
                        address = dataSnapshot.child("address").getValue(String.class);
                        profileAddress.setText(address);
                    }
                    if(dataSnapshot.hasChild("division")){
                        division = dataSnapshot.child("division").getValue(String.class);
                        profileDivision.setText(division);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
