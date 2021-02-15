package com.example.robopump2;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


import androidx.appcompat.app.AppCompatActivity;

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
    private int selectedUser = 1; //holds the id for the currently selected user profile. Will eventually have to be stored to and read from device.
    private int numUsers = 1; //holds the number of saved profiles. Will eventually have to be read from device.
    final private int MAXUSERS = 3; //holds the max number of users supported by the app
    private ImageButton addUser;
    private ImageButton user1Button;
    private TextView t_user1Button;
    private ImageButton user2Button;
    private TextView t_user2Button;
    private ImageButton user3Button;
    private TextView t_user3Button;
    private int clickcount=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
        returnButton = (ImageButton) findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToMainPage();

            }
        });

        //commit changes and output changes into Account Summary

        commitButton = (Button) findViewById(R.id.commit_changes);
        summary = (TextView) findViewById(R.id.account_summary);
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseUserInput();
                summary.setText("Account Summary:"+
                                "\n\nName: "+name+
                                "\nEmail: "+email+
                                "\nPostcode: "+postcode+
                                "\nCard Number: "+cardNumber);

                //save values in viewText and prevent values disappearing once users click back to main page
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(TEXT, summary.getText().toString());
                editor.apply();

            }
        });
        update();



        //save the state of users button when click on addUsers button
        user1Button=(ImageButton)findViewById(R.id.user1Button);
        t_user1Button=(TextView)findViewById(R.id.user1Text);
        user2Button=(ImageButton)findViewById(R.id.user2Button);
        t_user2Button=(TextView)findViewById(R.id.user2Text);
        user3Button=(ImageButton)findViewById(R.id.user3Button);
        t_user3Button=(TextView)findViewById(R.id.user3Text);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int isVisible = sharedPreferences.getInt("user2", View.INVISIBLE);
        user2Button.setVisibility(isVisible);
        isVisible = sharedPreferences.getInt("t_user2", View.INVISIBLE);
        t_user2Button.setVisibility(isVisible);
        isVisible = sharedPreferences.getInt("user3", View.INVISIBLE);
        user3Button.setVisibility(isVisible);
        isVisible = sharedPreferences.getInt("t_user3", View.INVISIBLE);
        t_user3Button.setVisibility(isVisible);

        addUser = (ImageButton)findViewById(R.id.Settings);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickcount = clickcount + 1;
                //when click on addUser button first time, user2 become visible
                if (clickcount == 1) {

                    user2Button.setVisibility(View.VISIBLE);
                    t_user2Button.setVisibility(View.VISIBLE);
                    sharedPreferences.edit().putInt("user2", View.VISIBLE).commit();
                    sharedPreferences.edit().putInt("t_user2", View.VISIBLE).commit();
                }
                //when click on addUser button second time, user3 become visible
                if (clickcount == 2) {
                    user3Button.setVisibility(View.VISIBLE);
                    t_user3Button.setVisibility(View.VISIBLE);
                    sharedPreferences.edit().putInt("user3", View.VISIBLE).commit();
                    sharedPreferences.edit().putInt("t_user3", View.VISIBLE).commit();
                }
            }
        });


        SharedPreferences sharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(this);
        float opa= sharedPreferences.getFloat("u1", (float) 1.0);
        user1Button.setAlpha(opa);
        t_user1Button.setAlpha(opa);
        opa= sharedPreferences.getFloat("u2", (float) 1.0);
        user2Button.setAlpha(opa);
        t_user2Button.setAlpha(opa);
        opa= sharedPreferences.getFloat("u3", (float) 1.0);
        user3Button.setAlpha(opa);
        t_user3Button.setAlpha(opa);

        //when click on user1 button ,user2 and user3 button fade
        user1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user1Button.setAlpha((float)1.0);
                t_user1Button.setAlpha((float)1.0);
                user2Button.setAlpha((float)0.4);
                t_user2Button.setAlpha((float)0.4);
                user3Button.setAlpha((float)0.4);
                t_user3Button.setAlpha((float)0.4);
                sharedPreferences.edit().putFloat("u1", (float)1.0).commit();
                sharedPreferences.edit().putFloat("u2", (float)0.4).commit();
                sharedPreferences.edit().putFloat("u3", (float)0.4).commit();
            }
        });

        //when click on user2 button ,user1 and user3 button fade
        user2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user1Button.setAlpha((float)0.4);
                t_user1Button.setAlpha((float)0.4);
                user2Button.setAlpha((float)1.0);
                t_user2Button.setAlpha((float)1.0);
                user3Button.setAlpha((float)0.4);
                t_user3Button.setAlpha((float)0.4);
                sharedPreferences.edit().putFloat("u1", (float)0.4).commit();
                sharedPreferences.edit().putFloat("u2", (float)1.0).commit();
                sharedPreferences.edit().putFloat("u3", (float)0.4).commit();
            }
        });


        //when click on user3 button, user1 and user2 button fade
        user3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user1Button.setAlpha((float)0.4);
                t_user1Button.setAlpha((float)0.4);
                user2Button.setAlpha((float)0.4);
                t_user2Button.setAlpha((float)0.4);
                user3Button.setAlpha((float)1.0);
                t_user3Button.setAlpha((float)1.0);
                sharedPreferences.edit().putFloat("u1", (float)0.4).commit();
                sharedPreferences.edit().putFloat("u2", (float)0.4).commit();
                sharedPreferences.edit().putFloat("u3", (float)1.0).commit();
            }
        });


    }
    //helper function
    private void update() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        text = sharedPreferences.getString(TEXT,"");
        summary.setText(text);
    }

    public void returnToMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
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
        if (postcode.length() == 0 || postcode.length() > 40){
            postcodeInput.requestFocus();
            postcodeInput.setError("Postcode must be between 1 and 40 characters");
            return false;
        }
        if (cardNumber.length() == 0 || cardNumber.length() > 20 || cardNumber.length() < 7 //check constraints
                || (!cardNumber.matches("[0-9]+"))) {
            cardInput.requestFocus();
            cardInput.setError("Card Number must be between 1 and 20 characters");
            return false;
        }
        if (expiryDate.length() == 0 || !expiryDate.matches("(?:0[1-9]|1[0-2])/[0-9]{2}")){
            expiryInput.requestFocus();
            expiryInput.setError("Expiry date must be in the form MM/YY");
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
        CardInformation cardInfo = new CardInformation(cardNumber, CVC, expiryDate);
        // Create a new use from the inputted information
        UserInformation user = new UserInformation(name,email,postcode, cardInfo);

        return user;
    }

    public void addUserClick(View view){ //function called when add user button is pressed
        if(numUsers<MAXUSERS){
            numUsers++; //increment number of user profiles by 1
            selectedUser = numUsers; //id switches to new button

            ArrayList<ImageButton> buttons = getUserButtons();
            ArrayList<TextView> texts = getUserButtonTexts();

            buttons.get(numUsers-1).setVisibility(View.VISIBLE); //unhide new button
            texts.get(numUsers-1).setVisibility(View.VISIBLE); //unhide that buttons text

            //THE TWO FUNCTIONS BELOW ARE NECESSARY BUT COMMENTED OUT DUE TO BUG CAUSED BY addUser() MEANING IF ANY FIELDS ARE EMPTY THE APP CRASHES
            //UserInformation newUser = addUser(); //get inputted user info
            //writeUserRecord(newUser);

            switchUser((View) buttons.get(selectedUser-1));

        }

    }

    public void switchUser(View view){//function called when a profile button is pressed
        ArrayList<ImageButton> buttons = getUserButtons();
        ArrayList<TextView> texts = getUserButtonTexts();
        ImageButton clickedButton = (ImageButton) view;
        //update selected user here

        for(int i=0; i<buttons.size();i++){ //set opacity low for all
            buttons.get(i).setAlpha((float) 0.4);
            texts.get(i).setAlpha((float) 0.4);

            if(buttons.get(i).equals(clickedButton)){ //get the id for the clicked button
                selectedUser = i+1;
            }
        }


        clickedButton.setAlpha((float) 1);
        texts.get(selectedUser-1).setAlpha((float) 1);

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

    private void writeUserRecord(UserInformation user){
        //TODO: This will take a user and write the info to the device
    }

    private void updateSummaryFromRecord(int id){
        //TODO: This will update the order summary from a record on the device
    }


}

