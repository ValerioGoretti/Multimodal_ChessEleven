package com.example.dipanshkhandelwal.chess;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class PinSettingActivity extends AppCompatActivity {
    private IOHelper ioHelper;
    private String newUser;
    private String newEmail;
    private String newPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setting);
        ioHelper=new IOHelper(getApplicationContext(),"accounts.json");
        newUser=getIntent().getStringExtra("newUser");
        newEmail=getIntent().getStringExtra("newEmail");
        newPassword=getIntent().getStringExtra("newPassword");

        Toast.makeText(getApplicationContext(), newUser+newEmail+newPassword,
                Toast.LENGTH_LONG).show();


    }
}