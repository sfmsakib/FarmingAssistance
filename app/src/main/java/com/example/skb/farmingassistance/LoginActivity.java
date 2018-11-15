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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {


    private EditText loginEmail, loginPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private Toolbar login_toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        TextView createNewAccount = findViewById(R.id.register_link);
        TextView forgotPassword = findViewById(R.id.login_forgot_password);
        loginEmail =  findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        Button loginSubmit =  findViewById(R.id.login_submit);
        loadingBar = new ProgressDialog(this);
        login_toolbar = (Toolbar) findViewById(R.id.toolbar_login);

        setSupportActionBar(login_toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("ব্যবহারকারী লগইন");


        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResetPasswordActivity();
            }
        });



        loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();



                if (TextUtils.isEmpty(email)){
                    Toast.makeText(LoginActivity.this,"ইমেইল দিতে হবে!!", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this,"পাসওয়ার্ড দিতে হবে!!", Toast.LENGTH_SHORT).show();
                }else {
                    loadingBar.setTitle("লগিং ইন");
                    loadingBar.setMessage("দয়া করে অপেক্ষা করুন ...");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(true);



                    mAuth.signInWithEmailAndPassword(email,password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        sendUserToMainActivity();
                                        Toast.makeText(LoginActivity.this,"সফলভাবে লগিং হয়েছে।", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }else {
                                        String msg = new String();
                                        if (task.getException() != null){
                                            msg = task.getException().getMessage();
                                        }
                                        Toast.makeText(LoginActivity.this, "আবার চেষ্টা করুন !!  Error :" + msg, Toast.LENGTH_LONG).show();
                                        loadingBar.dismiss();

                                    }
                                }
                            });
                }

            }
        });
//
//        setSupportActionBar(login_toolbar);
//        getSupportActionBar().setTitle("Login");

        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });



    }

    private void sendResetPasswordActivity() {
        Intent resetActivity = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        startActivity(resetActivity);


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
        Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);
        finish();
    }

    private void sendUserToRegisterActivity() {
        Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerActivity);
    }
}
