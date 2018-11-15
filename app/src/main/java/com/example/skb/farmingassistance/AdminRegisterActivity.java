package com.example.skb.farmingassistance;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminRegisterActivity extends AppCompatActivity{


    private EditText regEmail, regPassword, regConfirmPassword, regSecretPassword;
    private Button regSubmit;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private Toolbar adminRegisterToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        mAuth = FirebaseAuth.getInstance();


        regSecretPassword = (EditText) findViewById(R.id.reg_secret_password);
        regEmail = (EditText) findViewById(R.id.reg_name);
        regPassword = (EditText) findViewById(R.id.reg_password);
        regConfirmPassword = (EditText) findViewById(R.id.reg_confirm_password);
        regSubmit = (Button) findViewById(R.id.reg_submit);
        loadingBar = new ProgressDialog(this);

        adminRegisterToolbar = (Toolbar) findViewById(R.id.toolbar_admin_register);

        setSupportActionBar(adminRegisterToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("এডমিন রেজিস্টার");

        regSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = regEmail.getText().toString();
                String password = regPassword.getText().toString();
                String secretPassword = regSecretPassword.getText().toString();
                String confirmPassword = regConfirmPassword.getText().toString();

                if (!secretPassword.equals("112233")) {
                    Toast.makeText(AdminRegisterActivity.this, "আপনার গোপন পিন দিতে হবে!!", Toast.LENGTH_LONG).show();
                }else if (TextUtils.isEmpty(email)) {
                    Toast.makeText(AdminRegisterActivity.this, "ইমেইল দিতে হবে!! ", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(AdminRegisterActivity.this, "পাসওয়ার্ড দিতে হবে!! ", Toast.LENGTH_LONG).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(AdminRegisterActivity.this, "কনফার্ম পাসওয়ার্ড দিতে হবে!! ", Toast.LENGTH_LONG).show();
                } else {

                    loadingBar.setTitle("একাউন্ট তৈরি হচ্ছে ");
                    loadingBar.setMessage("দয়া করে অপেক্ষা করুন ...");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(true);

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        sendUserToAdminSetupActivity();
                                        Toast.makeText(AdminRegisterActivity.this, "সফলভাবে একাউন্ট তৈরি হয়েছে ", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    } else {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(AdminRegisterActivity.this, "আবার চেষ্টা করুন !!  Error :" + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();


                                    }
                                }
                            });
                }

            }
        });


    }



    @Override
    protected void onStart() {
        super.onStart();


        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            sendUserToMainActivity();

        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(AdminRegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void sendUserToAdminSetupActivity() {
        Intent setupActivity = new Intent(AdminRegisterActivity.this, AdminSetupActivity.class);
        setupActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupActivity);
        finish();
    }
}
