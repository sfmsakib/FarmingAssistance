package com.example.skb.farmingassistance;

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
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);

        Toolbar reset_toolbar = findViewById(R.id.toolbar_reset);
        setSupportActionBar(reset_toolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("পাসওয়ার্ড পরিবর্তন");

        email = findViewById(R.id.reset_email);
        Button submit = findViewById(R.id.reset_submit);
        firebaseAuth = FirebaseAuth.getInstance();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResetEmail();
            }
        });
    }

    private void sendResetEmail() {
        String eml = email.getText().toString().trim();
        if (TextUtils.isEmpty(eml)){
            Toast.makeText(this,"write your Email", Toast.LENGTH_SHORT).show();
        }else {
            firebaseAuth.sendPasswordResetEmail(eml).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ResetPasswordActivity.this,"পাসওয়ার্ড পরিবর্তনের ইমেল পাঠানো হয়েছে। আপনার ইনবক্স চেক করেন।", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                        finish();
                    }else {
                        Toast.makeText(ResetPasswordActivity.this,"নেটওয়ার্ক এর সমস্যা । আবার চেষ্টা করুন!!!", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }


    }
}
