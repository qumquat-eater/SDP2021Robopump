package com.example.robopump2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageButton settingsButton, fuellingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //begin: define local variable of seekbar and amount;
        TextView textView= (TextView)findViewById(R.id.current_amount);
        SeekBar seekBar= (SeekBar)findViewById(R.id.fuel_amount_slider);
        //end: define local variable of seekbar and amount;
        //define buttons
        settingsButton = (ImageButton) findViewById(R.id.Settings);
        fuellingButton = (ImageButton) findViewById(R.id.start_fuelling);

        //settings button onClick
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsPage();
            }
        });

        //start fuelling button onClick
        fuellingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });

        
        //begin: slide seekbar, and change amount
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText(String.valueOf(progressChangedValue)+"L") ;
            }

        });
        //end: slide seekbar,and change amount
    }

    //opens the settings page
    public void openSettingsPage() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    public void switchFuel(View view) { //this is the method for highlighting the clicked fuel button and unhighlighting the others
        Button clickedButton = (Button) view; //gets the button that was clicked

        ArrayList<Button> fuelButtons = new ArrayList<Button>(); //get all fuel buttons
        fuelButtons.add((Button) findViewById(R.id.premium_diesel));
        fuelButtons.add((Button) findViewById(R.id.diesel));
        fuelButtons.add((Button) findViewById(R.id.premium_petrol2));
        fuelButtons.add((Button) findViewById(R.id.petrol));

        for(int i=0; i<fuelButtons.size();i++){ //fade all fuel buttons
            fuelButtons.get(i).setAlpha((float) 0.4);
        }

        clickedButton.setAlpha(1); //highlight clicked fuel button

        //TODO: Actually have it change fuel selection
    }

    //displays popup window
    public void showPopupWindow(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // make ok button close popup- will later be used to initiate fuelling
        Button okButton = (Button)popupView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
            }
        });

        // make cancel button close popup- will later be used to cancel fuelling
        Button cancelButton = (Button)popupView.findViewById(R.id.cancel_button1);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
            }
        });

        // dismiss the popup window when touched
        /*popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });*/
    }

}