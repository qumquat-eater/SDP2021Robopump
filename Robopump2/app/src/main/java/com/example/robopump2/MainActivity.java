package com.example.robopump2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ImageButton settingsButton, fuellingButton, assitanceButton;
    private String[]  orderSummary  = {"","","","","",""}; //holds name, email, card number, fuel type, fuel amount, total cost
    Hashtable<String, Double> fuelPrices = new Hashtable<String,Double>(); //Holds fuel prices with fuel name as the key
    private int selectedUser = 0; //holds the currently selected user profile
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
        assitanceButton = (ImageButton) findViewById(R.id.Assistance);

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
        //assistance button onClick
        assitanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAssistanceWindow(v);
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
                orderSummary[4] = progressChangedValue + ""; //update selected fuel amount
                updateOrderSummary();
            }

        });
        //end: slide seekbar,and change amount

        //intitialise fuel prices. This approach leans quite heavily on hardcoded values, may need to be changed
        fuelPrices.put("petrol",(double) 2);
        fuelPrices.put("premium petrol",(double) 4);
        fuelPrices.put("diesel",(double) 3);
        fuelPrices.put("premium diesel",(double) 6);

        SharedPreferences sharedPreferences = getSharedPreferences("orderDetails", MODE_PRIVATE);
        selectedUser = sharedPreferences.getInt("selectedUser", selectedUser);




        TextView orderView = (TextView) findViewById(R.id.order_summary);
        orderView.setText(sharedPreferences.getString("orderText", "")); //load stored order summary

        for(int i = 0; i<orderSummary.length; i++){ //load stored orderSummary details
            orderSummary[i] = sharedPreferences.getString(i+"","");
        }

        int newSelectedUser = getIntent().getIntExtra("selectedUser",selectedUser); //replace this 0 with value from setting layout
        System.out.println("Main activity thinks: " + newSelectedUser);



        if(selectedUser!=newSelectedUser){
            selectedUser = newSelectedUser;
            sharedPreferences.edit().putInt("selectedUser", selectedUser).apply(); //store newly selected user

            updateUserInfo();

        }

        //this block is for setting fuel button opacity
        ArrayList<Button> fuelButtons = new ArrayList<Button>(); //get all fuel buttons
        fuelButtons.add((Button) findViewById(R.id.premium_diesel));
        fuelButtons.add((Button) findViewById(R.id.diesel));
        fuelButtons.add((Button) findViewById(R.id.premium_petrol2));
        fuelButtons.add((Button) findViewById(R.id.petrol));

        for(int i=0; i<fuelButtons.size();i++){
            fuelButtons.get(i).setAlpha(sharedPreferences.getFloat((String) fuelButtons.get(i).getText(),1));
        }

    }

    //opens the settings page
    public void openSettingsPage() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    public void switchFuel(View view) { //this is the method for highlighting the clicked fuel button and unhighlighting the others
        Button clickedButton = (Button) view; //gets the button that was clicked
        SharedPreferences sharedPreferences = getSharedPreferences("orderDetails", MODE_PRIVATE);

        ArrayList<Button> fuelButtons = new ArrayList<Button>(); //get all fuel buttons
        fuelButtons.add((Button) findViewById(R.id.premium_diesel));
        fuelButtons.add((Button) findViewById(R.id.diesel));
        fuelButtons.add((Button) findViewById(R.id.premium_petrol2));
        fuelButtons.add((Button) findViewById(R.id.petrol));

        for(int i=0; i<fuelButtons.size();i++){ //fade all fuel buttons
            fuelButtons.get(i).setAlpha((float) 0.4);
            sharedPreferences.edit().putFloat((String) fuelButtons.get(i).getText(),(float) 0.4).apply();
        }

        clickedButton.setAlpha(1); //highlight clicked fuel button
        sharedPreferences.edit().putFloat((String) clickedButton.getText(),(float) 1).apply();

        //TODO: Actually have it change fuel selection
        orderSummary[3] = ((String) clickedButton.getText()).toLowerCase(); //update fuel selection
        updateOrderSummary();
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

    public void showAssistanceWindow(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.assistance_popup_window, null);

        // create the popup window
        int width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        int height = ConstraintLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // make ok button close popup- will later be used to initiate fuelling
        Button okButton = (Button) popupView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
            }
        });
    }

    private void updateUserInfo(){ //method for updating the user info fields of the orderSummary array
        //SettingsActivity x = new SettingsActivity();
        DatabaseReader x = new DatabaseReader();
        UserInformation newUser = x.readUserRecord(selectedUser,getApplicationContext());
        orderSummary[0] = newUser.getUserName();
        orderSummary[1] = newUser.getEmail();
        orderSummary[2] = newUser.getDetails().getCardNumber();
        updateOrderSummary();
    }

    private void updateOrderSummary(){ //method for updating the text of the order summary
        Double newPrice = (double) 0;
        if(orderSummary[3]!="" && orderSummary[4]!="") {
            newPrice = fuelPrices.get(orderSummary[3]) * Double.parseDouble(orderSummary[4]); //calculate new price from fuel type and fuel amount
        }
        orderSummary[5] = newPrice +"";
        TextView orderView = (TextView) findViewById(R.id.order_summary);

        String cardNum = orderSummary[2];

        if(cardNum.length()>=4){
            cardNum = cardNum.substring(0,4);
            for(int i=0; i < orderSummary[2].length()-4;i++){
                cardNum = cardNum +"*";
            }
        }

        String displayString = "ORDER SUMMARY:\n\n" +
                "Name: " + orderSummary[0] +
                "\nEmail: " + orderSummary[1] +
                "\nCard number: " + cardNum +
                "\nFuel Type: " + orderSummary[3] +
                "\nFuel Amount: " + orderSummary[4] + "L" +
                "\nTotal Price: £" + orderSummary[5];

        orderView.setText(displayString);
        System.out.println(displayString);

        SharedPreferences sharedPreferences = getSharedPreferences("orderDetails", MODE_PRIVATE);
        sharedPreferences.edit().putString("orderText", displayString).apply();
        for(int i=0; i<orderSummary.length; i++){
            sharedPreferences.edit().putString(i+"", orderSummary[i]).apply(); //store the details
        }

    }

}