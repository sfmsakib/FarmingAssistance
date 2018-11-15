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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SetupActivity extends AppCompatActivity {


    private EditText name, address;
    private DatabaseReference userRef;
    private StorageReference userImageRef;
    String currentUserId;
    private ProgressDialog progressDialog;
    private CircleImageView image;
    private Button submit;
    private StorageReference filepath;
    private Uri resultUri;
    private Toolbar setupToolbar;

    final static int galleryPic = 1;
//    private Bitmap bitmap= null;
//    public byte[] byteArray = null;
//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");

        name = (EditText) findViewById(R.id.setup_name);
        address = (EditText) findViewById(R.id.setup_address);
        submit = (Button) findViewById(R.id.setup_submit);
        image = (CircleImageView) findViewById(R.id.setup_image);
        progressDialog = new ProgressDialog(this);


        setupToolbar = (Toolbar) findViewById(R.id.toolbar_setup);

        setSupportActionBar(setupToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("রেজিস্ট্রেশন");

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n = name.getText().toString();
                String a = address.getText().toString();



                if (TextUtils.isEmpty(n)){
                    Toast.makeText(SetupActivity.this, "সম্পূর্ণ নাম দিতে হবে!!", Toast.LENGTH_SHORT).show();

                }else if (TextUtils.isEmpty(a)){

                    Toast.makeText(SetupActivity.this, "িকানা দিতে হবে!!", Toast.LENGTH_SHORT).show();

                }else if (resultUri==null){
                    Toast.makeText(SetupActivity.this, "একটা প্রোফাইল পিকচার দিতে হবে!!", Toast.LENGTH_SHORT).show();
                }else {
                    HashMap hm = new HashMap();
                    hm.put("fullname", n);
                    hm.put("address", a);
                    hm.put("dob", "");
                    hm.put("gender", "");
                    hm.put("division", "");

                    progressDialog.setTitle("আপনার তথ্য যোগ করা হচ্ছে");
                    progressDialog.setMessage("দয়া করে অপেক্ষা করুন ...");
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(true);

                    userRef.updateChildren(hm).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){

                                storeImage();
                                sendUserToMainActivity();
                                //Toast.makeText(SetupActivity.this, "Success",Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }else {
                                String m = null;
                                if (task.getException() != null){
                                    m = task.getException().getMessage();
                                }
                                Toast.makeText(SetupActivity.this, "আবার চেষ্টা করুন !!  Error :"+ m,Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });







        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, galleryPic);
            }
        });
    }

    private void storeImage() {

        filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    String downloadUri = task.getResult().getDownloadUrl().toString();

                    userRef.child("profileimage").setValue(downloadUri)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //Intent self = new Intent(SetupActivity.this, SetupActivity.class);
                                        //startActivity(self);
                                        Toast.makeText(SetupActivity.this, "সফলভাবে একাউন্ট তৈরি হয়েছে ", Toast.LENGTH_SHORT).show();

                                    } else {
                                        String m = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "প্রোফাইল পিকচার দিতে ব্যর্থ, আবার চেষ্টা করুন !!  Error :" + m, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
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

        if (requestCode==galleryPic && resultCode==RESULT_OK && data!=null) {
            resultUri = data.getData();

            Picasso.get()
                    .load(resultUri)
                    .into(image);



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

//            CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setAspectRatio(1,1)
//                    .start(this);
            // }
//        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
//
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//
//            if (resultCode==RESULT_OK){
//                Uri resultUri = result.getUri();

//            final InputStream imageStream;
//            try {
//                imageStream = getContentResolver().openInputStream(resultUri);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                image.setImageBitmap(selectedImage);
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }


            filepath = userImageRef.child(currentUserId + ".jpg");


//            }
//        }
        }

    }



    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
