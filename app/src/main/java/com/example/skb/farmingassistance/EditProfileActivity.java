package com.example.skb.farmingassistance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String spinnerDivisionValue, spinnerGenderValue;
    private Spinner spinnerDivision;
    private Spinner spinnerGender;
    private EditText profileName, profileDob, profileAddess;
    private DatabaseReference userAdminRef;
    private String IsAdmin;
    private String currentUserId;
    private ProgressDialog progressDial;
    private Toolbar Edit_toolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Edit_toolbar = (Toolbar) findViewById(R.id.toolbar_edit_profile);

        setSupportActionBar(Edit_toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("তথ্য হালনাগাদ করুন");


        spinnerDivision = (Spinner) findViewById(R.id.edit_spinner_division);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.location_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivision.setAdapter(adapter);
        spinnerDivision.setOnItemSelectedListener(this);

        spinnerGender = (Spinner) findViewById(R.id.edit_gender);
        ArrayAdapter<CharSequence> adapterGender = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapterGender);
        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setGravity(Gravity.CENTER);
                switch (position){
                    case 0:
                        spinnerGenderValue="পুরুষ";
                        break;
                    case 1:
                        spinnerGenderValue="মহিলা";
                        break;
                    case 2:
                        spinnerGenderValue="অন্যান্য";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
               // spinnerGenderValue="Male";

            }
        });


        profileName = (EditText) findViewById(R.id.edit_name);
        profileDob = (EditText) findViewById(R.id.edit_dob);
        profileAddess = (EditText) findViewById(R.id.edit_address);
        progressDial = new ProgressDialog(this);


        Button button = (Button) findViewById(R.id.edit_submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateInfo();
            }
        });



    }

    private void UpdateInfo() {
        String fullname = profileName.getText().toString();
        String dob = profileDob.getText().toString();
        String address = profileAddess.getText().toString();

        if (TextUtils.isEmpty(fullname)){
            Toast.makeText(EditProfileActivity.this, "সম্পূর্ণ নাম দিতে হবে!!", Toast.LENGTH_LONG).show();

        }else if (TextUtils.isEmpty(dob)){

            Toast.makeText(EditProfileActivity.this, "জন্মদিন দিতে হবে!!", Toast.LENGTH_LONG).show();

        }else if (TextUtils.isEmpty(address)){

            Toast.makeText(EditProfileActivity.this, "ঠিকানা দিতে হবে!!", Toast.LENGTH_LONG).show();

        }else if (spinnerGenderValue==null){

            Toast.makeText(EditProfileActivity.this, "জেন্ডার দিতে হবে!!", Toast.LENGTH_LONG).show();

        }else if (spinnerDivisionValue==null){

            Toast.makeText(EditProfileActivity.this, "বিভাগ দিতে হবে!!", Toast.LENGTH_LONG).show();

        }else {


            IsAdmin = getIntent().getExtras().get("isAdmin").toString();
            currentUserId = getIntent().getExtras().get("uid").toString();
            if (IsAdmin.equals("Yes")) {
                userAdminRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(currentUserId);
            } else {
                userAdminRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
            }


            HashMap hm = new HashMap();
            hm.put("fullname", fullname);
            hm.put("address", address);
            hm.put("dob", dob);
            hm.put("gender", spinnerGenderValue);
            hm.put("division", spinnerDivisionValue);

            progressDial.setTitle("আপনার তথ্য হালনাগাদ করা হচ্ছে");
            progressDial.setMessage("দয়া করে অপেক্ষা করুন ...");
            progressDial.show();
            progressDial.setCanceledOnTouchOutside(false);

            userAdminRef.updateChildren(hm).addOnCompleteListener(EditProfileActivity.this, new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                progressDial.dismiss();
                                Toast.makeText(EditProfileActivity.this, "সফলভাবে আপনার তথ্য হালনাগাদ করা হয়েছে",Toast.LENGTH_SHORT).show();
                                sendUserToProfileActivity();
                            }else {
                                progressDial.dismiss();
                                Toast.makeText(EditProfileActivity.this, "আবার চেষ্টা করুন !!  Error :",Toast.LENGTH_SHORT).show();
                            }
                        }
            });
        }

    }

    private void sendUserToProfileActivity() {
        Intent profileIntent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        profileIntent.putExtra("isAdmin",IsAdmin);
        startActivity(profileIntent);
        finish();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ((TextView) parent.getChildAt(0)).setGravity(Gravity.CENTER);
        switch (position) {
            case 0:
                spinnerDivisionValue="ঢাকা";
                //Toast.makeText(AdminRegisterActivity.this, "Selected" + spinnerValue, Toast.LENGTH_LONG).show();

                break;
            case 1:
                spinnerDivisionValue="চট্টগ্রাম";
                //Toast.makeText(AdminRegisterActivity.this, "Selected" + spinnerValue, Toast.LENGTH_LONG).show();
                break;
            case 2:
                spinnerDivisionValue="রাজশাহী";
                break;
            case 3:
                spinnerDivisionValue="সিলেট";
                break;
            case 4:
                spinnerDivisionValue="বরিশাল";
                break;
            case 5:
                spinnerDivisionValue="খুলনা";
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
           // spinnerDivisionValue="Dhaka";

    }
}
