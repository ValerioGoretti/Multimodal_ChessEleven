package com.example.dipanshkhandelwal.chess;

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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

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
                    System.out.println(pw.getText().toString());
                    System.out.println(pw2.getText().toString());

                    if (pw.getText().toString().equals(pw2.getText().toString())){

                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    String url = "https://chesseleven.oa.r.appspot.com/register";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                System.out.println(response);
                                Log.i("VOLLEY", response);
                                JSONObject jsonObject = new JSONObject(response);
                                System.out.println(jsonObject.toString());
                                if (jsonObject.get("success").equals("true")) {

                                    Toast.makeText(getApplicationContext(), "Registered",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    if (jsonObject.get("reason").equals("email")){

                                    Toast.makeText(getApplicationContext(), "Email already used",Toast.LENGTH_LONG).show();}
                                    if(jsonObject.get("reason").equals("username")){
                                        Toast.makeText(getApplicationContext(), "Username already used",
                                                Toast.LENGTH_LONG).show();

                                    }
                                }
                                //SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                                //editor.putInt("currentSession", respo);
                                // preferences.edit()}
                            } catch (Exception e) {
                                System.out.println(e.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Service unavailable, try later",
                                    Toast.LENGTH_LONG).show();
                            Log.e("VOLLEY", error.toString());
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("username", usname.getText().toString());
                            params.put("password", pw.getText().toString());
                            params.put("email", email.getText().toString());
                            return params;
                        }

                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            String responseString = "";
                            if (response != null) {
                                responseString = String.valueOf(response.statusCode);
                                // can get more details such as response.headers
                            }
                            //return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                            return super.parseNetworkResponse(response);
                        }
                    };

                    queue.add(stringRequest);
                }
                    else{ Toast.makeText(getApplicationContext(), "The two password do not match",
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