package com.example.skb.farmingassistance;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

public class SelectAdminActivity extends AppCompatActivity {
    private String IsAdmin = "No";
    private String adminID;
    private boolean checked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_admin);

        Toolbar SelectAdmin = (Toolbar) findViewById(R.id.toolbar_select_admin);
        setSupportActionBar(SelectAdmin);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("আঞ্চলিক কৃষি কর্মকর্তা নির্বাচন করুন");

        Button buttonGo = (Button) findViewById(R.id.admin_go);
        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checked){
                    sendUserToChatActivity();
                }else {
                    Toast.makeText(SelectAdminActivity.this, "আঞ্চলিক কৃষি কর্মকর্তা নির্বাচন করুন", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_dhaka:
                if (checked)
                    adminID= "cIqJHVdankQThfNpdelyNKMEMkw2";
                    break;
            case R.id.radio_chittagong:
                if (checked)
                    adminID= "e4uJ2L0zrgeDqHwNS6EfsY0EViu2";
                    break;
            case R.id.radio_sylhet:
                if (checked)
                    adminID= "oE7xZvUDyGa2PRpwUZVnpc16jI62";
                    break;
            case R.id.radio_barishal:
                if (checked)
                    adminID= "WByMw8VZsCcW3D9Vk5g2RKeox6V2";
                    break;
            case R.id.radio_rajshahi:
                if (checked)
                    adminID= "1971whSuxCUUgyaLQ3nGswQpEcn1";
                    break;
            case R.id.radio_khulna:
                if (checked)
                    adminID= "VOfCvLTiyle0lLvrHOsrXIcULaU2";
                    break;
        }
    }

    public void sendUserToChatActivity(){
        Intent chatIntent = new Intent(SelectAdminActivity.this, ChatActivity.class);
        chatIntent.putExtra("isAdmin", IsAdmin);
        chatIntent.putExtra("messageKey", adminID);
        startActivity(chatIntent);
    }
}
