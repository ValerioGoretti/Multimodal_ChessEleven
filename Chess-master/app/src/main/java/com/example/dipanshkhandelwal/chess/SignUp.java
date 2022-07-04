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

public class SignUp extends AppCompatActivity {
    private IOHelper ioHelper;
    private Button Accedi_button_reg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singn_up);
        Button b=(Button) findViewById(R.id.button3);
        Accedi_button_reg = (Button) findViewById(R.id.Accedi_button_reg);
        Accedi_button_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

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
                       // ioHelper.writeToFile(accountArray.toString());
                        Intent intent = new Intent(getBaseContext(), PinSettingActivity.class);
                        intent.putExtra("newUser",usname.getText().toString());
                        intent.putExtra("newEmail",email.getText().toString());
                        intent.putExtra("newPassword",pw.getText().toString());
                        intent.putExtra("newAccount",newAccount);
                        startActivity(intent);
                        finish();


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