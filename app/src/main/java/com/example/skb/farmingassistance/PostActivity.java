package com.example.skb.farmingassistance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity {
    private String profileimage, fullname;
    private DatabaseReference userAdminRef;
    private String IsAdmin;

    private ImageButton postImage;
    private EditText postText;
    private String postDescription;
    private Button postSubmit;
    private static int Gallery_pick = 1;
    private Uri imageUri;

    private StorageReference userImageRef;
    private DatabaseReference postRef;
    private DatabaseReference userRef, adminRef;
    private StorageReference filepath;
    private FirebaseAuth mAuth;
    private String RandomNum, ImageName;
    private String currentDate;
    private String currentTime, downloadUri, currentUserId;
    private ProgressDialog loadingBar;
//    File compressedImageFile;
//    //private Bitmap bitmap= null;
//    private byte[] byteArray = null;
//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postImage = (ImageButton) findViewById(R.id.addPostImage);
        postText = (EditText) findViewById(R.id.addPostText);
        postSubmit = (Button) findViewById(R.id.addPostSubmit);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef =FirebaseDatabase.getInstance().getReference().child("Users");
        adminRef =FirebaseDatabase.getInstance().getReference().child("Admin");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        userImageRef = FirebaseStorage.getInstance().getReference().child("Post Images");

        loadingBar = new ProgressDialog(this);


        Toolbar postToolbar = (Toolbar) findViewById(R.id.toolbar_post);
        setSupportActionBar(postToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("নতুন পোস্ট যোগ করুন");


        IsAdmin = getIntent().getExtras().get("isAdmin").toString();
        if (IsAdmin.equals("Yes")){
            userAdminRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(currentUserId);
            getInfo();
        }else {
            userAdminRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
            getInfo();
        }





        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openGallery();
            }
        });

        postSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postDescription = postText.getText().toString();


                if (TextUtils.isEmpty(postDescription)){
                    Toast.makeText(PostActivity.this, "পোস্ট লিখুন", Toast.LENGTH_SHORT).show();
                }else if (imageUri==null){
                    //Toast.makeText(PostActivity.this, "পিকচার যোগ করুন", Toast.LENGTH_SHORT).show();
                    Calendar fordate = Calendar.getInstance();
                    SimpleDateFormat currDate = new SimpleDateFormat("dd/MM/yyyy");
                    currentDate = currDate.format(fordate.getTime());

                    Calendar fortime = Calendar.getInstance();
                    SimpleDateFormat currTime = new SimpleDateFormat("h:mm a");
                    currentTime = currTime.format(fortime.getTime());

                    SimpleDateFormat currRandomTime = new SimpleDateFormat("yyMMddHHmmssS");
                    String randomTime = currRandomTime.format(fortime.getTime());

                    RandomNum = randomTime + currentUserId;
                    downloadUri = " ";
                    ImageName  = " ";
                    saveFileToDatabase();
                }else {
                    saveFileToStorage();
                }



            }
        });
    }



    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_pick);
    }
//    public String getPathNew(Uri uri)
//    {
//        String[] projection = { MediaStore.Images.Media.DATA };
//        CursorLoader loader = new CursorLoader(PostActivity.this, uri, projection, null, null, null);
//        Cursor cursor = loader.loadInBackground();
//        if (cursor == null) return null;
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//        String s=cursor.getString(column_index);
//        cursor.close();
//        return s;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==Gallery_pick && resultCode==RESULT_OK && data!=null){
            imageUri =data.getData();


            /////****Image Fun
            //File actualImage = new File(imageUri.getPath());
//            Bitmap bitmap = null;
//
//            try {
//                bitmap = new Compressor(this).compressToBitmap(actualImage);
//                if (bitmap!=null){
//                    Toast.makeText(PostActivity.this, "success to compress" , Toast.LENGTH_SHORT).show();
//                }else {
//                    Toast.makeText(PostActivity.this, "failed to compress" , Toast.LENGTH_SHORT).show();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (bitmap != null) {
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
//            byteArray = byteArrayOutputStream.toByteArray();
//            }




            Transformation blurTransformation = new Transformation() {
                @Override
                public Bitmap transform(Bitmap source) {
                    Bitmap blurred = Blur.fastblur(PostActivity.this, source, 10);
                    source.recycle();
                    return blurred;
                }

                @Override
                public String key() {
                    return "blur()";
                }
            };

            Picasso.get()
                .load(R.drawable.add_post_high2) // thumbnail url goes here
                .placeholder(R.drawable.add_post_high2)
                .transform(blurTransformation)
                .into(postImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Picasso.get()
                                .load(imageUri) // image url goes here
                                .resize(640,480)
                                .placeholder(R.drawable.add_post_high2)
                                .into(postImage);
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });




                Calendar fordate = Calendar.getInstance();
                SimpleDateFormat currDate = new SimpleDateFormat("dd/MM/yyyy");
                currentDate = currDate.format(fordate.getTime());

                Calendar fortime = Calendar.getInstance();
                SimpleDateFormat currTime = new SimpleDateFormat("h:mm a");
                currentTime = currTime.format(fortime.getTime());

                SimpleDateFormat currRandomTime = new SimpleDateFormat("yyMMddHHmmssS");
                String randomTime = currRandomTime.format(fortime.getTime());

                RandomNum = randomTime + currentUserId;
                ImageName = RandomNum +".jpg";

                filepath = userImageRef.child(ImageName);

            }
    }


    private void saveFileToStorage() {


        loadingBar.setTitle("অপেক্ষা করুন");
        loadingBar.setMessage("দয়া করে অপেক্ষা করুন ...");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(false);

        filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){

                    downloadUri = task.getResult().getDownloadUrl().toString();
                    loadingBar.dismiss();

                    //Toast.makeText(PostActivity.this,"Success to store image", Toast.LENGTH_SHORT).show();

                    saveFileToDatabase();

                }else {
                    loadingBar.dismiss();
                    String m = task.getException().getMessage();
                    Toast.makeText(PostActivity.this,"নেটওয়ার্ক সমস্যা, আবার চেষ্টা করুন"+ m, Toast.LENGTH_SHORT).show();

                }
            }
        });

    }




    private void saveFileToDatabase() {

        HashMap postMap = new HashMap();

        postMap.put("uid",currentUserId);
        postMap.put("date",currentDate);
        postMap.put("time",currentTime);
        postMap.put("fullname",fullname);
        postMap.put("profileimage",profileimage);
        postMap.put("postimage",downloadUri);
        postMap.put("postimagename",ImageName);
        postMap.put("description",postDescription);

        postRef.child(RandomNum).updateChildren(postMap).addOnCompleteListener(PostActivity.this, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    postText.getText().clear();
                    postDescription=" ";
                    Toast.makeText(PostActivity.this, "সফলভাবে পোস্ট যোগ করা হয়েছে", Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                }else {
                    String m = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "আবার চেষ্টা করুন!! Error:" + m , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getInfo() {
        userAdminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")){
                        profileimage = dataSnapshot.child("profileimage").getValue(String.class);
                    }else {
                        profileimage = " ";
                    }
                    if(dataSnapshot.hasChild("fullname")){
                        fullname = dataSnapshot.child("fullname").getValue(String.class);
                    } else {
                        fullname = " ";
                    }
                }else {
                    Toast.makeText(PostActivity.this, "Your Profile is not completed!!", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToMainActivity(){
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
