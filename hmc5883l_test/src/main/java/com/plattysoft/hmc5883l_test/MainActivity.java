package com.plattysoft.hmc5883l_test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.plattysoft.hmc5883l_test.other.HMC5883L;

import java.io.IOException;

import static com.plattysoft.hmc5883l_test.Hmc5883l.GAIN_1090;
import static com.plattysoft.hmc5883l_test.other.HMC5883L.MEASUREMENT_NORMAL;
import static com.plattysoft.hmc5883l_test.other.HMC5883L.OPERATION_MODE_CONT;
import static com.plattysoft.hmc5883l_test.other.HMC5883L.OUTPUT_RATE_4;
import static com.plattysoft.hmc5883l_test.other.HMC5883L.SAMPLES_AVARAGE_8;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        firstVersion();
        secondVersion();
    }

    private void secondVersion() {
        HMC5883L hmcl5883l;

        try {
            hmcl5883l = new HMC5883L("I2C1");
            hmcl5883l.setSamplesAvarage(SAMPLES_AVARAGE_8);
            hmcl5883l.setOutputRate(OUTPUT_RATE_4);
            hmcl5883l.setMeasurementMode(MEASUREMENT_NORMAL);
            hmcl5883l.setMeasurementGain(HMC5883L.GAIN_1090);
            hmcl5883l.setOperationMode(OPERATION_MODE_CONT);
            while (true) {
                float[] b = hmcl5883l.getMagnitudes();
                Log.i(TAG, "X: " + b[0] + " mG, Y: " + b[1] + " mG, Z: " + b[2] + " mG");
                Thread.sleep(1_000);
            }
        } catch (IOException e) {
            // error.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void firstVersion() {
        try {
            Hmc5883l hmc5883l = new Hmc5883l();
            hmc5883l.setAverage(Hmc5883l.AVERAGE_8);
            hmc5883l.setMode(Hmc5883l.MODE_NORMAL);
            hmc5883l.setGain(GAIN_1090);

            for (;;) {
                // Perform single measurement
                hmc5883l.setOperatingMode(Hmc5883l.OP_SINGLE);
                // Wait for measurement to complete
                while (!hmc5883l.getRdy())
                    ;
                // Read all three axes
                double[] b = hmc5883l.getXYZ();
                Log.i(TAG, "X: " + b[0] + " mG, Y: " + b[1] + " mG, Z: " + b[2] + " mG");

                Thread.sleep(1_000);
            }
        } catch (IOException |InterruptedException|Hmc5883l.RangeOverflowException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
