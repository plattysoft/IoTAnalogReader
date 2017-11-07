package com.example.iotanalogreader;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.iotanalogreader.Pcf8591.MODE_FOUR_SINGLE_ENDED;

public class MainActivity extends Activity {

    public static final int REFRESH = 200;
    private static final int LED_RED = Color.RED;
    private static final int LED_OFF = Color.TRANSPARENT;

    private static final long REFRESH_INTERVAL = 200;

    private Pcf8591 mPcf8591;
    private AlphanumericDisplay mSegment;
    private Apa102 mLedStrip;

    int[] mColors = new int[RainbowHat.LEDSTRIP_LENGTH];
    private Timer mTimer;

    @Override
    protected void onStart() {
        super.onStart();

        setupAlphanumericDisplay();
        setupAdc();
        setupLedStrip();

        startTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        mTimer.purge();
        try {
            mPcf8591.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mSegment.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mLedStrip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                readAdcAndDisplayIt();
            }
        }, REFRESH_INTERVAL);
    }

    private void setupLedStrip() {
        try {
            mLedStrip = RainbowHat.openLedStrip();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupAdc() {
        try {
            mPcf8591 = Pcf8591.open(0, "I2C1");
            mPcf8591.configure(MODE_FOUR_SINGLE_ENDED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAdcAndDisplayIt() {
        // Channel 0 : Built-in potenciometer (with Jumper)
        // Channel 1 : Built-in light sensor (with jumper)
        // Channel 2 : Built-in thermistor (with jumper)
        // Channel 3 : Unassigned, used for our potenciometer
        try {
            int value = mPcf8591.readValue(3);
            mSegment.display(value);
            // Display the new value on the led strip
            int numLeds = value * (RainbowHat.LEDSTRIP_LENGTH + 1) / 256 - 1;
            for (int i=0; i<RainbowHat.LEDSTRIP_LENGTH; i++) {
                if (i <= numLeds) {
                    mColors[i] = LED_RED;
                }
                else {
                    mColors[i] = LED_OFF;
                }

            }
            mLedStrip.write(mColors);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupAlphanumericDisplay() {
        try {
            mSegment = RainbowHat.openDisplay();
            mSegment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
            mSegment.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
