package com.example.robopump2;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class DatabaseReader {
    final String fileName = "userInfo.csv";

    // returns the user record which is on the line whichRecord
    public UserInformation readUserRecord(int whichUser, Context c) {
        UserInformation user = null;
        // First check if passed in arg is within range
        if (numberOfRecords(c) < whichUser){
            Toast.makeText(c, "That user doesn't exist", Toast.LENGTH_SHORT).show();
            return user;
        }
        FileInputStream pw = null;
        String record = "";
        try {
            pw = c.openFileInput(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(pw));
            // Loops through all previous lines before requested line
            for(int i = 0; i < whichUser; i++) {
                br.readLine();
            }
            record = br.readLine();
            System.out.println("Record: " + whichUser + " is: " + record);
            //Toast.makeText(this, "User info read", Toast.LENGTH_SHORT).show();
            user = getUserFromString(record);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    //adds a record to the last line of the csv file
    public void writeUserRecord(String[] userInfo, Context c) throws IOException {
        FileOutputStream pw = null;
        // Builds string from userInfo array
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        for (String str: userInfo) {
            builder.append(str + ",");
        }
        //this line removes the unnecessary final comma of the last record
        builder.setLength(builder.length() - 1);
        try {
            // Adds the user information to the end of an existing file
            pw = c.openFileOutput(fileName, c.MODE_APPEND);
            pw.write(builder.toString().getBytes());
            System.out.println(builder.toString());
            Toast.makeText(c, "User Added", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (pw != null) {
                try {
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

    // checks if database has already been created
    // this possibly replaces need for numberOfRecords?
    public boolean databaseExists(Context c){
        File f = new File(c.getFilesDir(), fileName);
        return f.exists();
    }

    // Creates a new csv file with the appropriate column names
    public void createCSVFile(Context c) throws IOException {
        FileOutputStream pw = null;
        // Build String with column names
        StringBuilder builder = new StringBuilder();
        String columnNamesList = "Name, Email, Postcode, Card Number, Card Expiry Date, Card CVC";
        builder.append(columnNamesList);
        try {
            // Creates a new csv file with the correct column names
            pw = c.openFileOutput(fileName, c.MODE_PRIVATE);
            pw.write(builder.toString().getBytes());
            System.out.println("File made");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                try {
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public UserInformation getUserFromString(String userDetails){
        String[] row = userDetails.split(",");
        CardInformation card = new CardInformation(row[3], row[4], row[5]);
        UserInformation user = new UserInformation(row[0], row[1], row[2], card);
        return user;
    }

}



