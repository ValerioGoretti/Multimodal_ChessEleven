package com.example.dipanshkhandelwal.chess;


import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


class IOHelper {

    private String filename;
    private Context context;
    IOHelper(Context context,String filename) {
        this.filename =filename;
        this.context = context;
    }

    private String stringFromStream(InputStream is) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void writeToFile(String str) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(str.getBytes(), 0, str.length());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String stringFromAsset() {
        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open(filename);
            String result = stringFromStream(is);
            is.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    String stringFromFile(){
        try {
            FileInputStream fis = context.openFileInput(filename);
            String str = stringFromStream(fis);
            fis.close();
            return str;
        }
        catch (IOException e){
            String fromAsset = stringFromAsset();
            writeToFile(fromAsset);
            return fromAsset;
        }
    }
}