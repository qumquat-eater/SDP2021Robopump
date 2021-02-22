package com.example.robopump2;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;


public class DatabaseReader {
    final String fileName = "userInfo.csv";

    public UserInformation readUserRecord(int whichUser, Context context) {
        UserInformation user = null;
        // First check if passed in arg is within range
        if (numberOfRecords(context) < whichUser){
            return user;
        }
        FileInputStream pw = null;
        String record = "";
        try {
            pw = context.openFileInput(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(pw));
            // Loops through all previous lines before requested line
            for(int i = 0; i < whichUser; ++i) {
                br.readLine();
            }
            record = br.readLine();
            System.out.println("Record: " + whichUser + " is: " + record);
            user = getUserFromString(record);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    public int numberOfRecords(Context context) {
        FileInputStream pw = null;
        int count = 0;
        try {
            pw = context.openFileInput(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(pw));
            // Reads each line and increments counter while line is not null
            String line;
            while((line = br.readLine()) != null){
                count++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    public UserInformation getUserFromString(String userDetails){
        String[] row = userDetails.split(",");
        CardInformation card = new CardInformation(row[3], row[4], row[5]);
        UserInformation user = new UserInformation(row[0], row[1], row[2], card);
        return user;
    }
}
