package com.tnissinen.robotcontrol;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Singleton class for handling bluetooth connection with HC-05 bluetooth adapter
 */
public class BluetoothHandler {

    private static final Character END_CHARACTER = 'x';
    private static final BluetoothHandler ourInstance = new BluetoothHandler();

    private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Set<BluetoothDevice> bondedDevices;
    private String errorMessage;
    private String connectedDevice = "";

    /**
     * Gets the singleton instance of this class
     * @return Singleton instance
     */
    static BluetoothHandler getInstance() {
        return ourInstance;
    }

    /**
     * Private constructor
     */
    private BluetoothHandler() { }

    /**
     * Gets the detailed message of last occured error
     * @return Error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets the name of connected bluetooth bluetoothDevice
     * @return Name of bluetoothDevice. Empty string if not connected.
     */
    public String getConnectedDevice() {

        if(!isConnected())
            connectedDevice = "";

        return connectedDevice;
    }

    /**
     * Is bluetooth support enabled
     * @return Boolean value.
     */
    public Boolean isEnabled(){

        if(bluetoothAdapter == null)
            return false;

        return bluetoothAdapter.isEnabled();
    }

    /**
     * Is bluetooth socket connected
     * @return Boolean value
     */
    public Boolean isConnected(){

        if(socket == null)
            return false;

        return socket.isConnected();
    }

    /**
     * Initializes bluetooth adapter.
     * @return Boolean value of initialization success
     */
    public boolean initialize(){

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            errorMessage = "Device doesnt Support Bluetooth";
            return false;
        }

        return true;
    }

    /**
     * Gets a list of bonded bluetooth devices.
     * @return List of bluetoothDevice names.
     */
    public ArrayList<String> getDevices(){

        ArrayList<String> deviceNames = new ArrayList<>();

        for (BluetoothDevice iterator : bluetoothAdapter.getBondedDevices())
            deviceNames.add(iterator.getName());

        return deviceNames;
    }

    /**
     * Connect to bluetoothDevice with given name.
     * @param deviceName name of bluetoothDevice to connect
     * @return Boolean value of success
     */
    public boolean connect(String deviceName){

        for (BluetoothDevice iterator : bluetoothAdapter.getBondedDevices()) {

            if(iterator.getName().equals(deviceName))
            {
                bluetoothDevice = iterator;

                try {
                    socket = bluetoothDevice.createRfcommSocketToServiceRecord(DEFAULT_UUID);

                    try {
                        socket.connect();
                        Log.e("","Connected");
                    } catch (IOException e) {
                        Log.e("", e.getMessage());
                        try {
                            Log.e("", "Trying fallback...");

                            // another way to connect (may work better in some devices)
                            socket = (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(bluetoothDevice, 1);
                            socket.connect();

                            Log.e("", "Connected by fallback");
                        } catch (Exception e2) {
                            Log.e("", "Couldn't establish Bluetooth connection!");
                            errorMessage = e2.toString();
                            return false;
                        }
                    }

                }catch (IOException e){
                    errorMessage = e.toString();
                    return false;
                }

                break;
            }
        }

        if(socket.isConnected()){

            try {
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                connectedDevice = deviceName;
                return true;

            } catch (IOException e) {
                errorMessage = e.toString();
                return false;
            }
        }

        return false;
    }

    /**
     * Send message to connected bluetooth device.
     * @param message message to send (without special end character)
     * @return Boolean value of success in sending.
     */
    public boolean sendMessage(String message){

        if(isConnected()) {
            try {
                String messageToSend = message + END_CHARACTER;
                outputStream.write(messageToSend.getBytes());
                outputStream.flush();
                return true;

            } catch (IOException e) {
                errorMessage = e.toString();
            }
        }
        else
            errorMessage = "Bluetooth not connected";

        return false;
    }
}

