package com.example.dipanshkhandelwal.chess;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SessionManager {


        private SharedPreferences prefs;

        public SessionManager(Context cntx) {
            // TODO Auto-generated constructor stub
            prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
        }

        public void login(String usename,String password, String pin) {
            prefs.edit().putString("username", usename).commit();
            prefs.edit().putString("password", password).commit();
            prefs.edit().putString("pin", pin).commit();
            prefs.edit().putBoolean("isloggedIn",true);


        }
        public void logout() {
            prefs.edit().putString("username", "").commit();
            prefs.edit().putString("password", "").commit();
            prefs.edit().putString("pin", "").commit();
            prefs.edit().putBoolean("isloggedIn",false);


        }


        public String getusename() {
            String usename = prefs.getString("usename","");
            return usename;
        }
        public String getPassword() {
            String usename = prefs.getString("password","");
            return usename;
        }
        public String getPin() {
            String usename = prefs.getString("pin","");
            return usename;
        }


    }

