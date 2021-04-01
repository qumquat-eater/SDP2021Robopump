package com.example.robopump2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ImageButton settingsButton, fuellingButton, assitanceButton;
    private TextView current_amount;
    private CheckBox checkFull;
    boolean isChecked = false;
    DatabaseReader x = new DatabaseReader();
    private String[]  orderSummary  = {"","","","","",""}; //holds name, email, card number, fuel type, fuel amount, total cost
    Hashtable<String, Double> fuelPrices = new Hashtable<String,Double>(); //Holds fuel prices with fuel name as the key
    private int selectedUser = 0; //holds the currently selected user profile
    private boolean fuelChosen = false;
    private SeekBar seekBar;
    private boolean errorReceived = false;
    private boolean forceStop = false;
    private boolean finished = false;
    public static final String SHARED_PREF = "shared";
    //private static final String PROGRESS = "SEEKBAR";
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //begin: define local variable of seekbar and amount;
        TextView textView= (TextView)findViewById(R.id.current_amount);
        seekBar= (SeekBar)findViewById(R.id.fuel_amount_slider);
        checkFull = (CheckBox)findViewById(R.id.checkBox);
        current_amount = (TextView)findViewById(R.id.current_amount);
        //end: define local variable of seekbar and amount;
        //define buttons
        settingsButton = (ImageButton) findViewById(R.id.Settings);
        fuellingButton = (ImageButton) findViewById(R.id.start_fuelling);
        assitanceButton = (ImageButton) findViewById(R.id.Assistance);
        //scroll order summary
        TextView orderSum = findViewById(R.id.order_summary);
        orderSum.setMovementMethod(new ScrollingMovementMethod());

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
                if (fuelChosen&&seekBar.getProgress()!=0 || fuelChosen&&checkFull.isChecked()) {
                    errorReceived = false;
                    forceStop = false;
                    finished = false;
                    showPopupWindow(v);
                    SharedPreferences prefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
                    prefs.edit().putBoolean("fuelChosen",fuelChosen).apply();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please choose fuel type and amount first", Toast.LENGTH_SHORT).show();

                }
            }
        });
        SharedPreferences prefs = getSharedPreferences(SHARED_PREF,MODE_PRIVATE);
        fuelChosen= prefs.getBoolean("fuelChosen", false);

        //assistance button onClick
        assitanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAssistanceWindow(v);
            }
        });


        SharedPreferences seekPref = getSharedPreferences(" ", MODE_PRIVATE);
        SharedPreferences.Editor seekEdit = seekPref.edit();
        seekBar.setProgress(seekPref.getInt(SHARED_PREF,0));
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
                seekEdit.putInt(SHARED_PREF, seekBar.getProgress());
                seekEdit.commit();
                if (progressChangedValue>99){
                    textView.setText(String.valueOf("Full"));
                    orderSummary[4] = "Full"; //update selected fuel amount
                    checkFull.setChecked(true);
                }else {
                    textView.setText(String.valueOf(progressChangedValue) + "L");
                    orderSummary[4] = progressChangedValue + ""; //update selected fuel amount
                    checkFull.setChecked(false);
                }
                SharedPreferences sharedPref = getSharedPreferences("mypref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                current_amount = findViewById(R.id.current_amount);
                editor.putBoolean("checked", checkFull.isChecked());
                editor.putString("amount",orderSummary[4]);
                editor.apply();
                updateOrderSummary();
            }
        });
        //end: slide seekbar,and change amount

        //intitialise fuel prices. This approach leans quite heavily on hardcoded values, may need to be changed
        fuelPrices.put("petrol",(double) 2);
        fuelPrices.put("premium petrol",(double) 4);
        fuelPrices.put("diesel",(double) 3);
        fuelPrices.put("premium diesel",(double) 5);

        SharedPreferences sharedPreferences = getSharedPreferences("orderDetails", MODE_PRIVATE);
        selectedUser = sharedPreferences.getInt("selectedUser", selectedUser);


        TextView orderView = (TextView) findViewById(R.id.order_summary);
        orderView.setText(sharedPreferences.getString("orderText", "")); //load stored order summary

        for(int i = 0; i<orderSummary.length; i++){ //load stored orderSummary details
            orderSummary[i] = sharedPreferences.getString(i+"","");
        }

        int newSelectedUser = getIntent().getIntExtra("selectedUser",selectedUser); //replace this 0 with value from setting layout

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

        //on app creation, create the database if not already created
        if (!x.databaseExists(this)) {
            try {
                x.createCSVFile(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // if no user data found automatically switch user to settings page
        if (x.numberOfRecords(getApplicationContext()) < 2){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        SharedPreferences sharedPref = getSharedPreferences("mypref", Context.MODE_PRIVATE);
        current_amount.setText(sharedPref.getString("amount","") + "L");
        isChecked = sharedPref.getBoolean("checked", false);
        if(isChecked){
            checkFull.setChecked(true);
        }else{
            checkFull.setChecked(false);
        }
    }

    //check-box to add full fuel
    public void onCheckboxClicked(View view) {
        SharedPreferences sharedPref = getSharedPreferences("mypref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        isChecked = ((CheckBox) view).isChecked();
        current_amount = findViewById(R.id.current_amount);
        if (view.getId() == R.id.checkBox) {
            if (isChecked) {
                current_amount.setText("Full");
                orderSummary[4] = "Full"; //update selected fuel amount
                updateOrderSummary();
                editor.putBoolean("checked", checkFull.isChecked());
                editor.putString("amount",orderSummary[4]);
                editor.apply();
                seekBar.setProgress(100);

            }else{
                current_amount.setText("0");
                orderSummary[4] = "0"; //update selected fuel amount
                updateOrderSummary();
                editor.putBoolean("checked", checkFull.isChecked());
                editor.putString("amount",orderSummary[4]);
                editor.apply();
                seekBar.setProgress(0);
            }
            SharedPreferences seekPref = getSharedPreferences(" ", MODE_PRIVATE);
            SharedPreferences.Editor seekEdit = seekPref.edit();
            //seekBar.setProgress(seekPref.getInt(SHARED_PREF,0));
            System.out.println(seekBar.getProgress());
            seekEdit.putInt(SHARED_PREF, seekBar.getProgress());
            seekEdit.commit();
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
            fuelButtons.get(i).setAlpha((float) 0.2);
            sharedPreferences.edit().putFloat((String) fuelButtons.get(i).getText(),(float) 0.4).apply();
        }

        clickedButton.setAlpha(1); //highlight clicked fuel button
        sharedPreferences.edit().putFloat((String) clickedButton.getText(),(float) 1).apply();

        orderSummary[3] = ((String) clickedButton.getText()).toLowerCase(); //update fuel selection
        updateOrderSummary();
        fuelChosen = true;
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
                // Change button visibilities
                popupWindow.getContentView().findViewById(R.id.cancel_button1).setVisibility(View.VISIBLE);
                popupWindow.getContentView().findViewById(R.id.liveFuel).setVisibility(View.VISIBLE);
                popupWindow.getContentView().findViewById(R.id.ok_button).setVisibility(View.INVISIBLE);
                popupWindow.getContentView().findViewById(R.id.popup_message).setVisibility(View.INVISIBLE);
                popupWindow.getContentView().findViewById(R.id.warning_sign).setVisibility(View.INVISIBLE);
                popupWindow.getContentView().findViewById(R.id.fuelling_message).setVisibility(View.VISIBLE);
                popupWindow.getContentView().findViewById(R.id.livePrice).setVisibility(View.VISIBLE);
                popupWindow.getContentView().findViewById(R.id.liveType).setVisibility(View.VISIBLE);
                ((TextView) popupWindow.getContentView().findViewById(R.id.liveType)).setText(orderSummary[3]);

                // Run network connection on new thread as Android doesn't allow it on main thread
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        // Send request to server
                        String fuelAmount =String.valueOf(seekBar.getProgress());
                        String fuelType = orderSummary[3];
                        String request = fuelType + "," + orderSummary[4] +"#";
                        String response = AppClient.connect(request);
                        if (response.equalsIgnoreCase("error")){
                            errorReceived = true;
                        }
                        if (response.equalsIgnoreCase("success")){
                            finished = true;
                        }
                    }
                }).start();

                // increment fuel amount by 1l every second
                Handler h = new Handler();
                final Runnable r = new Runnable() {
                    int count = 0;
                    @Override
                    public void run() {
                        if ((orderSummary[4].equals("Full") || count < Integer.parseInt(orderSummary[4])) && !finished && !forceStop && !errorReceived) {
                            count++;
                            ((TextView) popupWindow.getContentView().findViewById(R.id.livePrice)).setText("Price: £ " + fuelPrices.get(orderSummary[3]) * count);
                            ((TextView) popupWindow.getContentView().findViewById(R.id.liveFuel)).setText("Amount: " + count + "L");
                            h.postDelayed(this, 1000); //ms
                        }
                        // change message to complete fuelling when server message success received or when counter reaches fuel amount chosen
                        if (finished || (!orderSummary[4].equals("Full") && count == Integer.parseInt(orderSummary[4]))) {
                            ((TextView) popupWindow.getContentView().findViewById(R.id.liveFuel)).setText("Fuelling Complete!");
                            ((TextView) popupWindow.getContentView().findViewById(R.id.fuelling_message)).setText("Click finish to complete the fuelling process");
                            popupWindow.getContentView().findViewById(R.id.cancel_button1).setVisibility(View.INVISIBLE);
                            popupWindow.getContentView().findViewById(R.id.liveType).setVisibility(View.INVISIBLE);
                            popupWindow.getContentView().findViewById(R.id.finish_button).setVisibility(View.VISIBLE);
                            popupWindow.getContentView().findViewById(R.id.checkbox_email).setVisibility(View.VISIBLE);
                        }
                        if (errorReceived){
                            // showing error message and hide other info
                            popupWindow.getContentView().findViewById(R.id.liveFuel).setVisibility(View.INVISIBLE);
                            popupWindow.getContentView().findViewById(R.id.popup_message).setVisibility(View.INVISIBLE);
                            popupWindow.getContentView().findViewById(R.id.fuelling_message).setVisibility(View.INVISIBLE);
                            popupWindow.getContentView().findViewById(R.id.livePrice).setVisibility(View.INVISIBLE);
                            popupWindow.getContentView().findViewById(R.id.liveType).setVisibility(View.INVISIBLE);
                            popupWindow.getContentView().findViewById(R.id.error_message).setVisibility(View.VISIBLE);
                            popupWindow.getContentView().findViewById(R.id.warning_sign).setVisibility(View.VISIBLE);
                            ((TextView) popupWindow.getContentView().findViewById(R.id.cancel_button1)).setText("Abort");
                        }
                    }
                };
                h.postDelayed(r, 1000); // one second in ms
            }
        });

        // make stop button be used to force stop fuelling at any time
        Button stopButton = (Button)popupView.findViewById(R.id.cancel_button1);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                // send stop message
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        // Send stop request to server
                        String request = "Stop#";
                        String response = AppClient.connect(request);
                    }
                }).start();
                forceStop = true;
                Toast.makeText(getApplicationContext(), "Fuelling Stopped!", Toast.LENGTH_SHORT).show();
                forceStop = false;
                popupWindow.dismiss();
            }
        });

        // make stop button be used to force stop fuelling at any time
        Button finishButton = (Button)popupView.findViewById(R.id.finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                // reset all booleans now that fuelling complete and close popup
                popupWindow.dismiss();
            }
        });
    }

    // displays assistance window
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

        // make ok button close popup- will
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
        DatabaseReader x = new DatabaseReader();
        UserInformation newUser = x.readUserRecord(selectedUser,getApplicationContext());
        orderSummary[0] = newUser.getUserName();
        orderSummary[1] = newUser.getEmail();
        orderSummary[2] = newUser.getDetails().getCardNumber();
        updateOrderSummary();
    }

    private void updateOrderSummary(){ //method for updating the text of the order summary
        Double newPrice = (double) 0;
        if(orderSummary[3]!="" && orderSummary[4]!="" && orderSummary[4] != "Full") {
            newPrice = fuelPrices.get(orderSummary[3]) * Double.parseDouble(orderSummary[4]); //calculate new price from fuel type and fuel amount
        }

        orderSummary[5] = newPrice +"";

        TextView orderView = (TextView) findViewById(R.id.order_summary);

        String fuelAmount;
        if(orderSummary[4].equals("Full")){
            fuelAmount = "Full Tank";
            orderSummary[5] = fuelPrices.get(orderSummary[3]) + "/L Full Tank";
        }else{
            fuelAmount = orderSummary[4] + "L";
        }

        String displayString = "ORDER SUMMARY:\n\n" +
                "User: " + "\n" + orderSummary[0] + "\n" +
                "\nFuel Type: " + "\n" + orderSummary[3] + "\n" +
                "\nFuel Amount: " + "\n" + fuelAmount + "\n" +
                "\nTotal Price:" + "\n" + "£" + orderSummary[5];

        orderView.setText(displayString);

        SharedPreferences sharedPreferences = getSharedPreferences("orderDetails", MODE_PRIVATE);
        sharedPreferences.edit().putString("orderText", displayString).apply();
        for(int i=0; i<orderSummary.length; i++){
            sharedPreferences.edit().putString(i+"", orderSummary[i]).apply(); //store the details
        }
    }
}