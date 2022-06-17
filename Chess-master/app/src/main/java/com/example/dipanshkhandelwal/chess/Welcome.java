package com.example.dipanshkhandelwal.chess;

import android.content.Intent;
import android.media.AudioManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.View;
import android.widget.Button;

public class Welcome extends AppCompatActivity {
    private AudioManager mAudioManager;
    private int mStreamVolume = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button signInButton=(Button) findViewById(R.id.login_welcome);
        Button signUpButton=(Button) findViewById(R.id.signup_welcome);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SignUp.class);
                startActivity(intent);
            }
        });
    }
}