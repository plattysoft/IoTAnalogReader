package com.example.iotanalogreader;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.plattysoft.pcf8591.Pcf8591;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.plattysoft.pcf8591.Pcf8591.MODE_FOUR_SINGLE_ENDED;


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

    private static final String MOTION_SENSOR_PORT = "GPIO6_IO13";
    private Gpio mMotionSensor;

    @Override
    protected void onStart() {
        super.onStart();

        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        try {
            mMotionSensor = peripheralManagerService.openGpio(MOTION_SENSOR_PORT);
            mMotionSensor.setDirection(Gpio.DIRECTION_IN);
            mMotionSensor.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mMotionSensor.registerGpioCallback(
                    new GpioCallback() {
                        @Override
                        public boolean onGpioEdge(Gpio gpio) {
                            try {
                                Log.e("MOTION", gpio.getName()+": "+gpio.getValue());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return true;
                        }
                    }
            );

        } catch (IOException e) {
            e.printStackTrace();
        }

//        setupAlphanumericDisplay();
//        setupAdc();
//        setupLedStrip();
//
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
                try {
                    Log.e("MOTION", "value: "+mMotionSensor.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                readAdcAndDisplayIt();
            }
        }, REFRESH_INTERVAL, REFRESH_INTERVAL);
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
        // Channel 0 : Built-in potentiometer (with Jumper)
        // Channel 1 : Built-in light sensor (with jumper)
        // Channel 2 : Built-in thermistor (with jumper)
        // Channel 3 : Unassigned, used for our potentiometer
        try {
            // Read analog input from channel 2
            int value = mPcf8591.readValue(2);
            Log.d("readAdcAndDisplayIt", "readValue(2): "+value);
            // Set it as the output (which is connected ot input 3)
            mPcf8591.setAnalogOutput(value);
            // Read value from channel 3
            value = mPcf8591.readValue(3);
            Log.d("readAdcAndDisplayIt", "readValue(3): "+value);

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
