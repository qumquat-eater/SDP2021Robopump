package com.example.robopump2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton returnButton;
    private EditText nameInput, emailInput, postcodeInput, cardInput, CVCInput, expiryInput;
    private String name, email, postcode, cardNumber, expiryDate;
    private int CVC;

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
}

