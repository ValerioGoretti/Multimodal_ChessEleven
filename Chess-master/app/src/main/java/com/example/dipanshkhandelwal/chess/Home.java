package com.example.dipanshkhandelwal.chess;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Home extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Button singlePlayer=(Button) findViewById(R.id.button4);
        Button logoutButton=(Button) findViewById(R.id.button_logout);
        singlePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionManager sessionManager=new SessionManager(getApplicationContext());
                sessionManager.logout();
                Intent intent = new Intent(getBaseContext(), Welcome.class);
                startActivity(intent);
            }
        });



    }
}