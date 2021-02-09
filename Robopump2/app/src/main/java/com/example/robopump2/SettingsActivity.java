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
    private EditText name1;
    private EditText email1;
    private EditText postcode1;
    private EditText card_number1;
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
}

