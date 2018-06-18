package com.cyberx.shmuel.cyberx.controller;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by User on 15/06/2018.
 */

public class ReadWriteToFile {
    public static void write(String fileName, String fileContent, boolean erase, Context context) {
        FileOutputStream outputStream;


        if (erase) {
            try {
                FileOutputStream writer = new FileOutputStream(fileName);
                writer.write(("").getBytes());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                    outputStream.write(fileContent.getBytes());
                    outputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        else {
            try {
                outputStream = context.openFileOutput(fileName,Context.MODE_PRIVATE | Context.MODE_APPEND);
                outputStream.write(fileContent.getBytes());
                outputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }


    }

    public static String read(String fileName, Context context) {

        String everything=null;

        StringBuilder sb = null;
        try {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sb!=null) {
            everything = sb.toString();
        }
        return everything;
    }
}
