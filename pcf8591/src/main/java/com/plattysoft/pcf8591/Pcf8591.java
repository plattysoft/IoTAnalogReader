/*
 * Copyright 2017 Dave McKelvie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.plattysoft.pcf8591;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;

/**
 * Android Things driver for the PCF8591 Analog to Digital Converter
 * http://www.nxp.com/documents/data_sheet/PCF8591.pdf
 */
public class Pcf8591 implements AutoCloseable {

    /**
     * Device Control byte values
     */
    public static final int ANALOG_OUTPUT_ENABLE = 0x40;
    public static final int MODE_FOUR_SINGLE_ENDED = 0x00;
    public static final int MODE_THREE_DIFFERENTIAL = 0x10;
    public static final int MODE_TWO_SINGLE_ONE_DIFFERENTIAL = 0x20;
    public static final int MODE_TWO_DIFFERENTIAL = 0x30;
    public static final int AUTO_INCREMENT = 0x04;

    /**
     * Device base address
     */
    private static final int BASE_ADDRESS = 0x48;

    /**
     * Control byte to be written to Pcf8591 to configure features
     */
    private int mControl;

    protected final I2cDevice mI2cDevice;

    protected Pcf8591(I2cDevice device) {
        this.mI2cDevice = device;
    }

    /**
     * Create a Pcf8591 with the default address on the
     * default I2C bus.
     *
     * @return new Pcf8591
     */
    public static Pcf8591 open() throws IOException {
        return open(0, getBus());
    }

    /**
     * Create a Pcf8591 with the given bus on the
     * default address.
     *
     * @param bus     the I2C bus the mI2cDevice is on
     * @return new Pcf8591
     */
    public static Pcf8591 open(String bus) throws IOException {
        return open(0, getBus());
    }

    /**
     * Create a Pcf8591 with the given address on the
     * default I2C bus.
     *
     * @param address value of A0-A2 for your Pcf8591
     * @return new Pcf8591
     */
    public static Pcf8591 open(int address) throws IOException {
        return open(address, getBus());
    }

    /**
     * Create a Pcf8591 with the given address on the
     * given bus.
     *
     * @param address value of A0-A2 for your Pcf8591
     * @param bus     the I2C bus the mI2cDevice is on
     * @return new Pcf8591
     */
    public static Pcf8591 open(int address, String bus) throws IOException {
        int fullAddress = BASE_ADDRESS + address;

        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        I2cDevice device = peripheralManagerService.openI2cDevice(bus, fullAddress);

        return new Pcf8591(device);
    }

    /**
     * set the config value that will be written to the PCF8591
     *
     * @param configuration mI2cDevice configuration, refer datasheet
     */
    public void configure(int configuration) {
        mControl = configuration;
    }

    protected static String getBus() {
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        List<String> deviceList = peripheralManagerService.getI2cBusList();
        if (deviceList.isEmpty()) {
            return "I2C1";
        } else {
            return deviceList.get(0);
        }
    }

    public void close() throws IOException {
        if (mI2cDevice != null) {
            mI2cDevice.close();
        }
    }

    /**
     * read a single ADC channel
     *
     * @param channel to read [0:3]
     * @return ADC result
     */
    public int readValue(int channel) throws IOException {
        if (channel < 0 || channel > 3) {
            return -1;
        }

        byte[] config = {(byte) ((channel | mControl) & 0xFF)};
        byte[] buffer = new byte[2];

        mI2cDevice.write(config, 1);
        mI2cDevice.read(buffer, buffer.length);

        return (buffer[1] & 0xFF);
    }

    public int setAnalogOutput(int value) throws IOException {
        if (value < 0 || value > 255) {
            return -1;
        }
        byte[] data = {ANALOG_OUTPUT_ENABLE, (byte) (value)};
        mI2cDevice.write(data, 2);
        return 0;
    }

    /**
     * Read all ADC channels
     *
     * @return values for channels 0 - 3
     */
    public int[] readAllValues() throws IOException {
        byte[] config = {(byte) (mControl | AUTO_INCREMENT)};
        byte[] buffer = new byte[5];

        mI2cDevice.write(config, 1);
        mI2cDevice.read(buffer, buffer.length);

        return new int[]{buffer[1] & 0xFF, buffer[2] & 0xFF, buffer[3] & 0xFF, buffer[4] & 0xFF};
    }
}