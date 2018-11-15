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
import android.text.TextUtils;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import id.zelory.compressor.Compressor;

public class AddAnnouncementActivity extends AppCompatActivity {
    private ImageButton AnnouncementImage;
    private EditText AnnouncementText;
    private String AnnouncementDescription;
    private Button AnnouncementSubmit;
    private static int Gallery_pick = 1;
    private Uri imageUri;

    private StorageReference AnnouncementImageRef;
    private DatabaseReference AnnouncementRef;
    private DatabaseReference adminRef;
    private StorageReference filepath;
    private FirebaseAuth mAuth;
    private String RandomNum, ImageName;
    private String currentDate;
    private String currentTime, downloadUri, currentUserId;
    private String IsAdmin = "Yes";
    private ProgressDialog loadingBar;
//    private Bitmap bitmap= null;
//    public byte[] byteArray = null;
//
private String profileimage, fullname;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_announcement);

        AnnouncementImage = (ImageButton) findViewById(R.id.addAnnouncementImage);
        AnnouncementText = (EditText) findViewById(R.id.addAnnouncementText);
        AnnouncementSubmit = (Button) findViewById(R.id.addAnnouncementSubmit);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid().toString();
        adminRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(currentUserId);
        getInfo();
        AnnouncementRef = FirebaseDatabase.getInstance().getReference().child("Announcements");
        AnnouncementImageRef = FirebaseStorage.getInstance().getReference().child("Announcement Images");

        Toolbar AnnouncementToolbar = (Toolbar) findViewById(R.id.toolbar_Add_Announcement);
        setSupportActionBar(AnnouncementToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("নতুন ঘোষণা যোগ করুন");



        AnnouncementImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openGallery();
            }
        });

        AnnouncementSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                AnnouncementDescription = AnnouncementText.getText().toString().trim();


                if (TextUtils.isEmpty(AnnouncementDescription)){
                    Toast.makeText(AddAnnouncementActivity.this, "ঘোষোনা লিখুন ", Toast.LENGTH_SHORT).show();
                }else if (imageUri==null){
                    //Toast.makeText(AddAnnouncementActivity.this, "পিকচার যোগ করুন", Toast.LENGTH_SHORT).show();
                    Calendar fordate = Calendar.getInstance();
                    SimpleDateFormat currDate = new SimpleDateFormat("dd/MM/yyyy");
                    currentDate = currDate.format(fordate.getTime());

                    Calendar fortime = Calendar.getInstance();
                    SimpleDateFormat currTime = new SimpleDateFormat("h:mm a");
                    currentTime = currTime.format(fortime.getTime());

                    SimpleDateFormat currRandomTime = new SimpleDateFormat("yyMMddHHmmssZ");
                    String randomTime = currRandomTime.format(fortime.getTime());

                    RandomNum = randomTime + currentUserId;
                    downloadUri = " ";
                    ImageName = " ";
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

        if (requestCode==Gallery_pick && resultCode==RESULT_OK && data!=null){
            imageUri =data.getData();
//
//            File file = new File(getPath(imageUri));
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


            Picasso.get().load(imageUri).into(AnnouncementImage);


            Calendar fordate = Calendar.getInstance();
            SimpleDateFormat currDate = new SimpleDateFormat("dd/MM/yyyy");
            currentDate = currDate.format(fordate.getTime());

            Calendar fortime = Calendar.getInstance();
            SimpleDateFormat currTime = new SimpleDateFormat("h:mm a");
            currentTime = currTime.format(fortime.getTime());

            SimpleDateFormat currRandomTime = new SimpleDateFormat("yyMMddHHmmssZ");
            String randomTime = currRandomTime.format(fortime.getTime());

            RandomNum = randomTime + currentUserId;
            ImageName = RandomNum +".jpg";



            filepath = AnnouncementImageRef.child(ImageName);

        }
    }


    private void saveFileToStorage() {
        loadingBar.setTitle("নতুন ঘোষণা যোগ করা হচ্ছে");
        loadingBar.setMessage("দয়া করে অপেক্ষা করুন ...");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
        filepath.putFile(imageUri).addOnCompleteListener(AddAnnouncementActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){

                    downloadUri = task.getResult().getDownloadUrl().toString();

                    //Toast.makeText(AddAnnouncementActivity.this,"Success to store image", Toast.LENGTH_SHORT).show();
//
//                    HashMap AnnounceMap = new HashMap();
//                    AnnounceMap.put("postimage",downloadUri);
//                    AnnounceMap.put("postimagename",ImageName);
//
//
//                    AnnouncementRef.child(RandomNum).updateChildren(AnnounceMap);
                    saveFileToDatabase();

                }else {
                    String m = task.getException().getMessage();
                    Toast.makeText(AddAnnouncementActivity.this,"নেটওয়ার্ক সমস্যা, আবার চেষ্টা করুন"+ m, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

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
        postMap.put("description",AnnouncementDescription);
        postMap.put("postimage",downloadUri);
        postMap.put("postimagename",ImageName);

        AnnouncementRef.child(RandomNum).updateChildren(postMap).addOnCompleteListener(AddAnnouncementActivity.this, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    Toast.makeText(AddAnnouncementActivity.this, "সফলভাবে ঘোষনা যোগ করা হয়েছে", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                    sendAdminToAnnouncementActivity();
                }else {
                    String m = task.getException().getMessage();
                    Toast.makeText(AddAnnouncementActivity.this, "আবার চেষ্টা করুন!! Error:" + m, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                    sendAdminToAnnouncementActivity();
                }
            }
        });
    }
    private void getInfo() {
        adminRef.addValueEventListener(new ValueEventListener() {
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
                    Toast.makeText(AddAnnouncementActivity.this, "Your Profile is not completed!!", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void sendAdminToAnnouncementActivity() {
        Intent AnnouncementIntent = new Intent(AddAnnouncementActivity.this, AnnouncementActivity.class);
        startActivity(AnnouncementIntent);
    }
}
