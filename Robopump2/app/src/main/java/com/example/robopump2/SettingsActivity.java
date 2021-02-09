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

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageButton returnButton;
    private Button commitButton;
    private EditText name;
    private EditText email;
    private EditText postcode;
    private EditText card_number;
    private TextView summary;
    public static final String SHARED_PREF = "shared";
    public static final String TEXT = "text";
    public String text;

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
        name = (EditText)findViewById(R.id.name);
        email = (EditText)findViewById(R.id.email);
        postcode = (EditText)findViewById(R.id.postcode);
        card_number = (EditText)findViewById(R.id.card_number);
        summary = (TextView)findViewById(R.id.account_summary);
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name1 = name.getText().toString();
                String email1 = email.getText().toString();
                String postcode1 = postcode.getText().toString();
                String card_number1 = card_number.getText().toString();
                summary.setText("Account Summary:"+
                                "\n\nName: "+name1+
                                "\nEmail: "+email1+
                                "\nPostcode: "+postcode1+
                                "\nCard Number: "+card_number1);

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
}

