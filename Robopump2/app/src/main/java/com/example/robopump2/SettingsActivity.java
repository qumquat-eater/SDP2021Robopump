package com.example.robopump2;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SettingsActivity extends AppCompatActivity {

    private ImageButton returnButton;
    private Button commitButton;
    private TextView summary;
    public static final String SHARED_PREF = "shared";
    public static final String TEXT = "text";
    public String text;
    private EditText nameInput, emailInput, postcodeInput, cardInput, CVCInput, expiryInput;
    private String name, email, postcode, cardNumber, expiryDate;
    private String CVC;
    private int selectedUser = 0; //holds the id for the currently selected user profile. Will eventually have to be stored to and read from device.
    private int numUsers = 0; //holds the number of saved profiles. Will eventually have to be read from device.
    final private int MAXUSERS = 3; //holds the max number of users supported by the app
    final String fileName = "userInfo.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
          //scroll account summary
//        TextView accountSum = findViewById(R.id.account_summary);
//        accountSum.setMovementMethod(new ScrollingMovementMethod());

        returnButton = (ImageButton) findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOfRecords() < 1) {
                    Toast.makeText(getApplicationContext(), "Must create a user", Toast.LENGTH_SHORT).show();
                } else {
                    returnToMainPage();
                }
            }
        });

        //on app creation, create the database if not already created
        if (!databaseExists()) {
            try {
                createCSVFile();
                //System.out.println("database created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            //System.out.println("database exists");
        }


        //commit changes and output changes into Account Summary

        commitButton = (Button) findViewById(R.id.commit_changes);
        summary = (TextView) findViewById(R.id.account_summary);
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOfRecords() < 1) {
                    Toast.makeText(getApplicationContext(), "Must create a user first", Toast.LENGTH_SHORT).show();
                }
                if (checkUserInfoValid() && numberOfRecords() >= 1){
                    summary.setText("Account Summary:"+ "\n" +
                            "\nName: "+ name+ "\n" +
                            "\nEmail: "+email+ "\n" +
                            "\nPostcode: "+postcode+ "\n" +
                            "\nCard Number: "+cardNumber);

                    //save values in viewText and prevent values disappearing once users click back to main page
                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(TEXT, summary.getText().toString());
                    editor.apply();
                    // testRead(); uncomment to test if updating data correctly
                    updateUserInformation(selectedUser);
                    //testRead();
                }
            }
        });
        update();

        //This block is for restoring the profile buttons state
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        ArrayList<ImageButton> profileButtons = getUserButtons();
        ArrayList<TextView> profileTexts = getUserButtonTexts();

        for(int i =0; i<profileButtons.size();i++){ //set profile button and text visibility
            profileButtons.get(i).setVisibility(sharedPreferences.getInt((i+1)+"Vis", View.INVISIBLE));
            profileTexts.get(i).setVisibility(sharedPreferences.getInt((i+1)+"Vis", View.INVISIBLE));
        }

        for(int i=0; i<profileButtons.size();i++){ //set profile button and text opacity
            profileButtons.get(i).setAlpha(sharedPreferences.getFloat((i+1)+"Opa", (float) 1));
            profileTexts.get(i).setAlpha(sharedPreferences.getFloat((i+1)+"Opa", (float) 1));
        }

        selectedUser = sharedPreferences.getInt("selectedUser", 0);
        numUsers = sharedPreferences.getInt("numUsers", 0);

    }
    //helper function
    private void update() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        text = sharedPreferences.getString(TEXT,"");
        summary.setText(text);
    }

    public void returnToMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        //System.out.println("Settings thinks selected user is: " + selectedUser);
        intent.putExtra("selectedUser", selectedUser);
        startActivity(intent);
    }

    // Method to read and parse user input into variables
    public void parseUserInput(){
        // Read text input
        nameInput = (EditText) findViewById(R.id.name);
        emailInput = (EditText) findViewById(R.id.email);
        postcodeInput = (EditText) findViewById(R.id.postcode);
        cardInput = (EditText) findViewById(R.id.card_number);
        CVCInput = (EditText) findViewById(R.id.CVC);
        expiryInput = (EditText) findViewById(R.id.expiry_date);

        // Parse user input into variables
        name = nameInput.getText().toString();
        email = emailInput.getText().toString();
        postcode = postcodeInput.getText().toString();
        cardNumber = cardInput.getText().toString();
        CVC = CVCInput.getText().toString();
        expiryDate = expiryInput.getText().toString();
    }

    // Method to validate user input
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean checkUserInfoValid() {
        parseUserInput();

        if (name.length() == 0 || name.length() > 40){
            nameInput.requestFocus();
            nameInput.setError("Name must be between 1 and 40 characters");
            return false;
        }
        if (email.length() == 0 || email.length() > 40){
            emailInput.requestFocus();
            emailInput.setError("Email must be between 1 and 40 characters");
            return false;
        }
        else if(email.contains(",")) {
            emailInput.requestFocus();
            emailInput.setError("Email must not contain any commas");
            return false;
        }
        else if(!email.contains("@")) {
            emailInput.requestFocus();
            emailInput.setError("Email must contain @");
            return false;
        }
        if (postcode.length() == 0 || postcode.length() > 40){
            postcodeInput.requestFocus();
            postcodeInput.setError("Postcode must be between 1 and 40 characters");
            return false;
        }
        if (cardNumber.length() == 0 || cardNumber.length() > 20 || cardNumber.length() < 8 //check constraints
                || (!cardNumber.matches("[0-9]+"))) {
            cardInput.requestFocus();
            cardInput.setError("Card Number must be between 8 and 20 characters");
            return false;
        }
        if (expiryDate.length() == 0 || !expiryDate.matches("(?:0[1-9]|1[0-2])/[0-9]{2}")){
            expiryInput.requestFocus();
            expiryInput.setError("Expiry date must be in the form MM/YY");
            return false;
        }

        else if (YearMonth.now(ZoneId.systemDefault()).isAfter(YearMonth.parse(expiryDate, DateTimeFormatter.ofPattern("MM/uu")))) {
            expiryInput.requestFocus();
            expiryInput.setError("Card is expired, please enter a valid expiry date");
            return false;
        }
        if (CVC.length() != 3) {
            CVCInput.requestFocus();
            CVCInput.setError("CVC must be 3 digits long");
            return false;
        }

        return true;
    }
    // Method to create a user record from the users input
    public UserInformation addUser(){
        // Create a new instance of card information from the user input
        CardInformation cardInfo = new CardInformation(cardNumber, expiryDate, CVC);
        // Create a new use from the inputted information
        UserInformation user = new UserInformation(name,email,postcode, cardInfo);

        return user;
    }

    public void addUserClick(View view) throws IOException { //function called when add user button is pressed
        if(numUsers<MAXUSERS && checkUserInfoValid()){
            numUsers++; //increment number of user profiles by 1
            selectedUser = numUsers; //id switches to new button
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);

            sharedPreferences.edit().putInt("numUsers", numUsers).commit();
            sharedPreferences.edit().putInt("selectedUser", selectedUser).commit();


            ArrayList<ImageButton> buttons = getUserButtons();
            ArrayList<TextView> texts = getUserButtonTexts();

            buttons.get(numUsers-1).setVisibility(View.VISIBLE); //unhide new button
            texts.get(numUsers-1).setVisibility(View.VISIBLE); //unhide that buttons text

            String key = numUsers + "Vis";

            sharedPreferences.edit().putInt(key, View.VISIBLE).commit(); //store new visibility

            System.out.println("Write record selected user: " + selectedUser);
            UserInformation newUser = addUser(); //get inputted user info
            String[] userInfoArray = getStringArrayFromUser(newUser);
            writeUserRecord(userInfoArray);

            switchUser((View) buttons.get(selectedUser-1));

            //testRead();
        }
        // Display error message if max users reached
        else if (numUsers == MAXUSERS){
            Toast.makeText(this, "Max Users Reached", Toast.LENGTH_SHORT).show();
        }

    }

    public void switchUser(View view){//function called when a profile button is pressed
        ArrayList<ImageButton> buttons = getUserButtons();
        ArrayList<TextView> texts = getUserButtonTexts();
        ImageButton clickedButton = (ImageButton) view;

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);

        // Update selected user here
        for(int i=0; i<buttons.size();i++){ //set opacity low for all
            buttons.get(i).setAlpha((float) 0.4);
            texts.get(i).setAlpha((float) 0.4);

            if(buttons.get(i).equals(clickedButton)){ //get the id for the clicked button
                selectedUser = i+1;
            }

            sharedPreferences.edit().putFloat((i+1) + "Opa", (float) 0.4).apply(); //store new opacity

        }

        clickedButton.setAlpha((float) 1);
        texts.get(selectedUser-1).setAlpha((float) 1);

        sharedPreferences.edit().putFloat(selectedUser + "Opa", (float) 1).commit(); //store opacity for selected button
        sharedPreferences.edit().putInt("selectedUser", selectedUser).commit(); //store newly selected user

        System.out.println("Update summary selected user: " + selectedUser);
        updateSummaryFromRecord(selectedUser);
    }

    private ArrayList<ImageButton> getUserButtons(){ //function for getting the profile buttons
        ArrayList<ImageButton> userButtons = new ArrayList<ImageButton>();
        userButtons.add((ImageButton)findViewById(R.id.user1Button));
        userButtons.add((ImageButton)findViewById(R.id.user2Button));
        userButtons.add((ImageButton)findViewById(R.id.user3Button));

        return userButtons;
    }

    private ArrayList<TextView> getUserButtonTexts(){ //function for getting the profile buttons text labels
        ArrayList<TextView> userTexts = new ArrayList<TextView>();
        userTexts.add((TextView) findViewById(R.id.user1Text));
        userTexts.add((TextView) findViewById(R.id.user2Text));
        userTexts.add((TextView) findViewById(R.id.user3Text));
        return userTexts;
    }

    // Creates a new csv file with the appropriate column names
    public void createCSVFile() throws IOException {
        FileOutputStream pw = null;
        // Build String with column names
        StringBuilder builder = new StringBuilder();
        String columnNamesList = "Name, Email, Postcode, Card Number, Card Expiry Date, Card CVC";
        builder.append(columnNamesList);
        try {
            // Creates a new csv file with the correct column names
            pw = openFileOutput(fileName, MODE_PRIVATE);
            pw.write(builder.toString().getBytes());
            //System.out.println("File made");
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

    // Parses a String into a UserInformation object
    public UserInformation getUserFromString(String userDetails){
        String[] row = userDetails.split(",");
        CardInformation card = new CardInformation(row[3], row[4], row[5]);
        UserInformation user = new UserInformation(row[0], row[1], row[2], card);
        return user;
    }

    public String[] getStringArrayFromUser(UserInformation user) {
        // Parse UserInformation to a String array
        String name = user.getUserName();
        String email = user.getEmail();
        String postcode = user.getPostcode();
        CardInformation cardInfo = user.getDetails();
        String cardNo = cardInfo.getCardNumber();
        String CVC = cardInfo.getCvcNumber();
        String expiryDate = cardInfo.getExpiryDate();

        String[] userInfoArray = new String[] {name, email, postcode, cardNo, expiryDate, CVC};
        return userInfoArray;
    }

    //adds a record to the last line of the csv file
    public void writeUserRecord(String[] userInfo) throws IOException {
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
            pw = openFileOutput(fileName, MODE_APPEND);
            pw.write(builder.toString().getBytes());
            System.out.println(builder.toString());
            Toast.makeText(this, "User Added", Toast.LENGTH_SHORT).show();
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

    // returns the number of lines(records) in the csv file
    // NOTE: this also counts the first line which are the column titles
    public int numberOfRecords() {
        FileInputStream pw = null;
        int count =0;
        try {
            pw = openFileInput(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(pw));
            // Reads each line and increments counter while line is not null
            String line;
            while((line = br.readLine()) != null){
                count++;
            }
            //to account for first row
            count--;
            //Toast.makeText(this, "Number of users: " + count, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    // test method to see if can read whole contents of file and print them
    public void testRead(){
        FileInputStream pw = null;
        String info = "";
        try {
            pw = openFileInput(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(pw));
            StringBuffer sb = new StringBuffer();
            // Reads each line of file and adds it to a string buffer
            String line;
            while((line = br.readLine()) != null){
                sb.append(line + "\n");
            }
            //System.out.println("Details recovered: " + sb.toString());
            //Toast.makeText(this, "User info read", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // returns the user record which is on the line whichRecord
    public UserInformation readUserRecord(int whichUser) {
        UserInformation user = null;
        // First check if passed in arg is within range
        if (numberOfRecords() < whichUser){
            Toast.makeText(this, "That user doesn't exist", Toast.LENGTH_SHORT).show();
            return user;
        }
        FileInputStream pw = null;
        String record = "";
        try {
            pw = openFileInput(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(pw));
            // Loops through all previous lines before requested line
            for(int i = 0; i < whichUser; i++) {
                br.readLine();
            }
            record = br.readLine();
            System.out.println("Record: " + selectedUser + " is: " + record);
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

    // checks if database has already been created
    // this possibly replaces need for numberOfRecords?
    public boolean databaseExists(){
        File f = new File(getFilesDir(), fileName);
        return f.exists();
    }

    // updates the user record on the specified line
    public void updateUserInformation(int whichUser) {
        // First check if passed in arg is within range
        if (numberOfRecords() < whichUser){
            Toast.makeText(this, "That user doesn't exist", Toast.LENGTH_SHORT).show();
        }
        FileInputStream pw = null;
        try {
            pw = openFileInput(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(pw));
            StringBuffer sb = new StringBuffer();
            String line;
            parseUserInput();
            int count = 0; // to keep track of what reader is on
            String updatedInfo = (name + "," + email + "," + postcode + "," + cardNumber
                    + "," + expiryDate + "," + CVC );
            // add newline if all users ahave been added and trying to edit user1/2
            if (numUsers == 3 && (whichUser == 1 || whichUser == 2)){
                updatedInfo += "\n";
            }
            // Loops through all lines in file
            while((line = br.readLine()) != null) {
                // if reached line we want to update fill in with new string
                if (count == whichUser) {
                    sb.append(updatedInfo);
                }
                // if not line we want just append to sb normally
                else {
                    sb.append(line + "\n");
                }
                count++;
            }
            System.out.println(sb.toString());
            // Now write whole string with updated line to file
            FileOutputStream fos = null;
            fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(sb.toString().getBytes());
            fos.close();

            //System.out.println("Record " + whichUser + " updated");
            Toast.makeText(this, "Updated user:" + whichUser, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void updateSummaryFromRecord(int id){
        UserInformation user = readUserRecord(id); // the user record which is on the line of selected user in CSV file
        String[] userInfo = getStringArrayFromUser(user); // Parse UserInformation to a String array
        summary = (TextView) findViewById(R.id.account_summary);
        summary.setText("Account Summary:"+ "\n" +
                "\nName: "+userInfo[0]+ "\n" +
                "\nEmail: "+userInfo[1]+ "\n" +
                "\nPostcode: "+userInfo[2]+ "\n" +
                "\nCard Number: "+userInfo[3]);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEXT, summary.getText().toString());
        editor.apply();
        update();
    }


}