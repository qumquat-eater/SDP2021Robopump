package com.example.robopump2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class MainActivity extends AppCompatActivity {

    private ImageButton settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView= (TextView)findViewById(R.id.current_amount);
        SeekBar seekBar= (SeekBar)findViewById(R.id.fuel_amount_slider);
        settingsButton = (ImageButton) findViewById(R.id.Settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsPage();
            }
        });

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

    }

    public void openSettingsPage() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}