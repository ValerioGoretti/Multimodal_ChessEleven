package com.example.dipanshkhandelwal.chess;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private IOHelper ioHelper;
    private Button registrati_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        registrati_button = (Button) findViewById(R.id.Registrati_button_login);
        registrati_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), SignUp.class);
                startActivity(intent);
            }
        });

        Button login=(Button)findViewById(R.id.button2);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final EditText usname=(EditText)findViewById(R.id.login_us) ;
                final EditText pw=(EditText)findViewById(R.id.login_pw) ;
                try {

                    ioHelper=new IOHelper(getApplicationContext(),"accounts.json");
                    String stringAccounts=ioHelper.stringFromFile();
                    System.out.println(stringAccounts);
                    JSONArray accountArray=new JSONArray(stringAccounts);


                    boolean logged=false;
                    for(int i=0;i<accountArray.length();i++) {
                        JSONObject jsonAccount= accountArray.getJSONObject(i);
                        if (jsonAccount.get("username").equals(usname.getText().toString())||jsonAccount.get("password").equals(pw.getText().toString())) {
                            logged=true;
                            Intent intent = new Intent(getBaseContext(), PinLoginActivity.class);
                            intent.putExtra("User",usname.getText().toString());
                            intent.putExtra("Password",pw.getText().toString());
                            String pin=(String)jsonAccount.get("pin");
                            intent.putExtra("Pin",pin);
                            startActivity(intent);
                            finish();
                            break;
                        }}



                }
                catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Service unavailable, try later",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }
}