package com.example.dipanshkhandelwal.chess;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    private IOHelper ioHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singn_up);
        Button b=(Button) findViewById(R.id.button3);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    final EditText pw2 = (EditText) findViewById(R.id.editText);
                    final EditText pw = (EditText) findViewById(R.id.editText3);
                    final EditText usname = (EditText) findViewById(R.id.editText4);
                    final EditText email = (EditText) findViewById(R.id.signup_email);
                    ioHelper=new IOHelper(getApplicationContext(),"accounts.json");
                    String stringAccounts=ioHelper.stringFromFile();
                    System.out.println(stringAccounts);
                    JSONArray accountArray=new JSONArray(stringAccounts);


                    boolean alreadyUsed=false;
                    for(int i=0;i<accountArray.length();i++) {
                        if (accountArray.getJSONObject(i).get("username").equals(usname.getText().toString())) {
                            Toast.makeText(getApplicationContext(), "username già in uso",
                                    Toast.LENGTH_LONG).show();
                            alreadyUsed=true;

                        }}

                    if (pw2.getText().toString().equals(pw.getText().toString())&&!(alreadyUsed)){
                        String newAccount=String.format("{\"username\":\"%s\",\"password\":\"%s\",\"pin\":\"%s\"}",usname.getText().toString(),pw.getText().toString(),"123");
                        JSONObject newAccountObj=new JSONObject(newAccount);
                        accountArray.put(newAccountObj);
                        ioHelper.writeToFile(accountArray.toString());
                        Intent intent = new Intent(getBaseContext(), PinSettingActivity.class);
                        intent.putExtra("newUser",usname.getText().toString());
                        intent.putExtra("newEmail",email.getText().toString());
                        intent.putExtra("newPassword",pw.getText().toString());
                        startActivity(intent);

                        Toast.makeText(getApplicationContext(), "Nuovo Utente Registrato!",
                                Toast.LENGTH_LONG).show();

                    }
                        else{ Toast.makeText(getApplicationContext(), "Credenziali non valide",
                                Toast.LENGTH_LONG).show();}

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