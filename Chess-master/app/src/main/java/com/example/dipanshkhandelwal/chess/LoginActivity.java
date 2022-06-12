package com.example.dipanshkhandelwal.chess;

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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private IOHelper ioHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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