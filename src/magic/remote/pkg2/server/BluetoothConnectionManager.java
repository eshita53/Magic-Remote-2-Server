/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package magic.remote.pkg2.server;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

/**
 *
 * @author romana
 */
public class BluetoothConnectionManager {

    private static BluetoothConnectionManager instance = new BluetoothConnectionManager();

    protected volatile String connectionStatus = "not connected";

    protected Queue<String> writerQueue = new LinkedList<>();
    protected Queue<String> processingQueue = new LinkedList<>();

    protected StreamConnectionNotifier notifier;
    protected StreamConnection connection;

    protected InputStream inputStream;
    protected OutputStream outputStream;

    protected RemoteDevice remoteDevice;
    protected LocalDevice localDevice;

    protected Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    protected int screenWidth = (int) screenSize.getWidth();
    protected int screenHeight = (int) screenSize.getHeight();

    protected Thread readThread;
    protected Thread writeThread;
    protected Thread heartbeatThread;
    protected Thread processingThread;
    
    private BluetoothConnectionManager() {
        try {
            localDevice = LocalDevice.getLocalDevice();
        } catch (BluetoothStateException ex) {
            Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public static BluetoothConnectionManager getInstance() {
        return instance;
    }

    public Dimension getScreenSize() {
        return screenSize;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public RemoteDevice getRemoteDevice() {
        return remoteDevice;
    }

    public LocalDevice getLocalDevice() {
        return localDevice;
    }

    
    
    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public Queue<String> getWriterQueue() {
        return writerQueue;
    }

    public Queue<String> getProcessingQueue() {
        return processingQueue;
    }

    public void write(String message) {
        writerQueue.add(message);
    }

    public void sendData(String data) throws IOException {
        if (outputStream != null) {
            outputStream.write(data.getBytes());
            outputStream.flush();
        }
    }

    public void startConnection() {
        Thread startConnectionThread = new Thread() {
            @Override
            public void run() {
                try {

                    LocalDevice device = LocalDevice.getLocalDevice();
                    if (device.getDiscoverable() != DiscoveryAgent.GIAC) {
                        device.setDiscoverable(DiscoveryAgent.GIAC);
                    }

                    UUID uuid = new UUID(Constants.UUID.replace("-", ""), false); //UUID.fromString(Constants.UUID);
                    System.out.println("My UUID: "+ uuid);
                    
                    java.util.UUID uuid2 = java.util.UUID.fromString(Constants.UUID);
                    System.out.println("My UUID 2: "+ uuid2);
                    
                    String connectionString = "btspp://localhost:" + uuid.toString() + ";name=" + Constants.APP_NAME + " Server";
                    //String connectionString = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";

                    connectionStatus = "connecting";

                    notifier = (StreamConnectionNotifier) Connector.open(connectionString);
                    System.out.println("Starting Connection...");

                    try {
                        connection = notifier.acceptAndOpen();
                        System.out.println("Connection established");

                        writerQueue.clear();
                        processingQueue.clear();

                        remoteDevice = RemoteDevice.getRemoteDevice(connection);
                        inputStream = connection.openInputStream();
                        outputStream = connection.openOutputStream();

                        connectionStatus = "connected";
                        
                        processingThread = new Thread(new ProcessingRunner());
                        processingThread.start();

                        readThread = new Thread(new ReadRunner());
                        readThread.start();

                        writeThread = new Thread(new WriteRunner());
                        writeThread.start();

                        heartbeatThread = new Thread(new HeartBeatRunner());
                        heartbeatThread.start();
                    } catch (InterruptedIOException e) {
                        System.out.println("Connection aborted");
                    }

                } catch (IOException ex) {
                    Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        startConnectionThread.start();
    }

    public void stopConnection() {
        try {
            connectionStatus = "disconnected";

            if (notifier != null) {
                System.out.println("CONNECTION CLOSE 1");
                notifier.close();
            }
            if (connection != null) {
                System.out.println("CONNECTION CLOSE 2");
                connection.close();
            }

            if (inputStream != null) {
                System.out.println("CONNECTION CLOSE 3");
                inputStream.close();
            }
            if (outputStream != null) {
                System.out.println("CONNECTION CLOSE 4");
                outputStream.close();
            }
            if (remoteDevice != null) {
                System.out.println("CONNECTION CLOSE 5");
                remoteDevice = null;
            }

            if (writerQueue != null) {
                System.out.println("CONNECTION CLOSE 6");
                writerQueue.clear();
            }
            
            if(processingQueue!=null) {
                System.out.println("CONNECTION CLOSE 7");
                processingQueue.clear();
            }
        } catch (IOException ex) {
            Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
