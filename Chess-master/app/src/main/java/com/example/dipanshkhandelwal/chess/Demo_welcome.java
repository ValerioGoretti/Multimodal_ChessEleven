package com.example.dipanshkhandelwal.chess;

import android.content.Intent;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class Demo_welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_welcome);
        new CountDownTimer(4000,1000){
            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {
                Intent intent=new Intent(Demo_welcome.this, Game.class);
                startActivity(intent);
                finish();
            }
        }.start();
    }
}