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

public class RegisterActivity extends AppCompatActivity {


    private EditText registerEmail, registerPassword, registerConfirmPassword;
    private Button registerSubmit;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private Toolbar register_toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mAuth = FirebaseAuth.getInstance();


        registerEmail = (EditText) findViewById(R.id.setup_name);
        registerPassword = (EditText) findViewById(R.id.register_password);
        registerConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        registerSubmit = (Button) findViewById(R.id.setup_submit);
        loadingBar = new ProgressDialog(this);

        register_toolbar = (Toolbar) findViewById(R.id.toolbar_register);

        setSupportActionBar(register_toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("ব্যবহারকারী রেজিস্টার");

        registerSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();
                String confirmPassword = registerConfirmPassword.getText().toString();

                if (TextUtils.isEmpty(email)){
                    Toast.makeText(RegisterActivity.this, "ইমেইল দিতে হবে!!", Toast.LENGTH_LONG).show();
                }else if (TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this, "পাসওয়ার্ড দিতে হবে!!", Toast.LENGTH_LONG).show();
                }else if (!password.equals(confirmPassword)){
                    Toast.makeText(RegisterActivity.this, "কনফার্ম পাসওয়ার্ড দিতে হবে!!", Toast.LENGTH_LONG).show();
                }else {

                    loadingBar.setTitle("একাউন্ট তৈরি হচ্ছে");
                    loadingBar.setMessage("দয়া করে অপেক্ষা করুন ...");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(true);

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){

                                        sendUserToSetupActivity();
                                        Toast.makeText(RegisterActivity.this, "সফলভাবে একাউন্ট তৈরি হয়েছে ",Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                    }else {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(RegisterActivity.this,"আবার চেষ্টা করুন !!  Error :"+ message,Toast.LENGTH_SHORT).show();
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
        Intent mainActivity = new Intent(RegisterActivity.this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);
        finish();
    }

    private void sendUserToSetupActivity() {
        Intent setupActivity = new Intent(RegisterActivity.this, SetupActivity.class);
        setupActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupActivity);
        finish();
    }


}
