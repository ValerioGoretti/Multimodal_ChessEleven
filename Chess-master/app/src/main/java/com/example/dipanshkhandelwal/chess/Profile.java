package com.example.dipanshkhandelwal.chess;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class Profile extends AppCompatActivity {
    public TextView username;
    public TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Intent i = getIntent();
        String user = i.getStringExtra("user");
        Log.i("Profile", "USERNAME -->  " + user);
        username=findViewById(R.id.usename);
        email=findViewById(R.id.email);

        SessionManager sessionManager=new SessionManager(getApplicationContext());
        String usern= user.split("@")[0];
        username.setText(usern);
        email.setText(user);
    }
}