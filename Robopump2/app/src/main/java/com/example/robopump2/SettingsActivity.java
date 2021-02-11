package com.example.robopump2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton returnButton;
    private Button commitButton;
    private EditText name1;
    private EditText email1;
    private EditText postcode1;
    private EditText card_number1;
    private TextView summary;
    public static final String SHARED_PREF = "shared";
    public static final String TEXT = "text";
    public String text;
    private EditText nameInput, emailInput, postcodeInput, cardInput, CVCInput, expiryInput;
    private String name, email, postcode, cardNumber, expiryDate;
    private int CVC;
    private int selectedUser = 1; //holds the id for the currently selected user profile. Will eventually have to be stored to and read from device.
    private int numUsers = 1; //holds the number of saved profiles. Will eventually have to be read from device.
    final private int MAXUSERS = 3; //holds the max number of users supported by the app

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
        commitButton = (Button)findViewById(R.id.commit_changes);
        name1 = (EditText)findViewById(R.id.name);
        email1 = (EditText)findViewById(R.id.email);
        postcode1 = (EditText)findViewById(R.id.postcode);
        card_number1 = (EditText)findViewById(R.id.card_number);
        summary = (TextView)findViewById(R.id.account_summary);
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name2 = name1.getText().toString();
                String email2 = email1.getText().toString();
                String postcode2 = postcode1.getText().toString();
                String card_number2 = card_number1.getText().toString();
                summary.setText("Account Summary:"+
                                "\n\nName: "+name2+
                                "\nEmail: "+email2+
                                "\nPostcode: "+postcode2+
                                "\nCard Number: "+card_number2);

                //save values in viewText and prevent values disappearing once users click back to main page
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(TEXT,summary.getText().toString());
                editor.apply();

            }
        });

        update();


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

    // Method to create a user record from the users input
    // This will probably be called when add user button is clicked
    public UserInformation addUser() {

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
        CVC = Integer.valueOf(CVCInput.getText().toString());
        expiryDate = expiryInput.getText().toString();

        // Create a new use from the inputted information
        UserInformation user = new UserInformation(name,email,postcode,
                new CardInformation(cardNumber, CVC, expiryDate));

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

