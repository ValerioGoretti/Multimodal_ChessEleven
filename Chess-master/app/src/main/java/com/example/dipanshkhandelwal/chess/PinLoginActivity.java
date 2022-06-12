package com.example.dipanshkhandelwal.chess;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class PinLoginActivity extends AppCompatActivity {
    IOHelper ioHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setting);
        ioHelper=new IOHelper(getApplicationContext(),"accounts.json");
        String user=getIntent().getStringExtra("User");
        String password=getIntent().getStringExtra("Password");
        String pin=getIntent().getStringExtra("Pin");
        Toast.makeText(getApplicationContext(), user+password+pin,
                Toast.LENGTH_LONG).show();

    }
}