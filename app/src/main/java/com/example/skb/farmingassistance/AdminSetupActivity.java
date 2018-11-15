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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class AdminSetupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText Admin_name, Admin_address;
    private DatabaseReference adminRef;
    private StorageReference userImageRef;
    String currentUserId;
    private ProgressDialog progressDialog;
    private CircleImageView Admin_image;
    private Button Admin_submit;


    private StorageReference filepath;
    final static int galleryPic = 1;
    private Uri resultUri;

    private String spinnerValue;
    private Spinner spinner;
    private Toolbar adminSetupToolbar;
//    private Bitmap bitmap= null;
//    public byte[] byteArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_setup);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }
        userImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");

        Admin_name = (EditText) findViewById(R.id.admin_setup_name);
        Admin_address = (EditText) findViewById(R.id.admin_setup_address);
        Admin_submit = (Button) findViewById(R.id.admin_setup_submit);
        Admin_image = (CircleImageView) findViewById(R.id.admin_setup_image);
        progressDialog = new ProgressDialog(this);

        adminSetupToolbar = (Toolbar) findViewById(R.id.toolbar_admin_setup);

        setSupportActionBar(adminSetupToolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle("এডমিন রেজিস্টার");

        spinner = (Spinner) findViewById(R.id.admin_setup_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.location_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);



        Admin_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n = Admin_name.getText().toString().trim();
                String a = Admin_address.getText().toString().trim();

                if (TextUtils.isEmpty(n)){
                    Toast.makeText(AdminSetupActivity.this, "সম্পূর্ণ নাম দিতে হবে!! ", Toast.LENGTH_LONG).show();

                }else if (TextUtils.isEmpty(a)){

                    Toast.makeText(AdminSetupActivity.this, "ঠিকানা দিতে হবে!!", Toast.LENGTH_LONG).show();

                }else if (resultUri==null){

                    Toast.makeText(AdminSetupActivity.this, "একটা প্রোফাইল পিকচার দিতে হবে!!", Toast.LENGTH_LONG).show();

                }else if (spinnerValue==null){

                    Toast.makeText(AdminSetupActivity.this, "বিভাগ দিতে হবে!!", Toast.LENGTH_LONG).show();

                }else {

                    adminRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(currentUserId);


                    HashMap hm = new HashMap();
                    hm.put("fullname", n);
                    hm.put("address", a);
                    hm.put("dob", "");
                    hm.put("gender", "");
                    hm.put("division", spinnerValue);

                    progressDialog.setTitle("আপনার তথ্য যোগ করা হচ্ছে");
                    progressDialog.setMessage("দয়া করে অপেক্ষা করুন ...");
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(true);

                    adminRef.updateChildren(hm).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()){
                                            String downloadUri = task.getResult().getDownloadUrl().toString();

                                            adminRef.child("profileimage").setValue(downloadUri)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                //Intent self = new Intent(SetupActivity.this, SetupActivity.class);
                                                                //startActivity(self);

                                                                sendUserToMainActivity();
                                                                Toast.makeText(AdminSetupActivity.this, "সফলভাবে একাউন্ট তৈরি হয়েছে ",Toast.LENGTH_LONG).show();
                                                                progressDialog.dismiss();
                                                            }else {
                                                                String m = task.getException().getMessage();
                                                                Toast.makeText(AdminSetupActivity.this, "প্রোফাইল পিকচার দিতে ব্যর্থ, আবার চেষ্টা করুন !!  Error :" + m, Toast.LENGTH_SHORT).show();
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });

                            }else {
                                String m = null;
                                if (task.getException() != null){
                                    m = task.getException().getMessage();
                                }
                                Toast.makeText(AdminSetupActivity.this, "আবার চেষ্টা করুন !!  Error :"+ m,Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });


        Admin_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, galleryPic);
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

        if (requestCode==galleryPic && resultCode==RESULT_OK && data!=null){

            resultUri = data.getData();

            Picasso.get()
                    .load(resultUri)
                    .into(Admin_image);
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
//                Admin_image.setImageBitmap(selectedImage);
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }

            filepath = userImageRef.child(currentUserId + ".jpg");

        }

    }



    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(AdminSetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                spinnerValue="Dhaka";
                //Toast.makeText(AdminRegisterActivity.this, "Selected" + spinnerValue, Toast.LENGTH_LONG).show();

                break;
            case 1:
                spinnerValue="Chittagong";
                //Toast.makeText(AdminRegisterActivity.this, "Selected" + spinnerValue, Toast.LENGTH_LONG).show();
                break;
            case 2:
                spinnerValue="Rajshahi";
                break;
            case 3:
                spinnerValue="Sylhet";
                break;
            case 4:
                spinnerValue="Barishal";
                break;
            case 5:
                spinnerValue="Khulna";
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinnerValue="Dhaka";

    }
}
