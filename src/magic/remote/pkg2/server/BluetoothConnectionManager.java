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

    public static BluetoothConnectionManager getInstance() {
        return instance;
    }

    public volatile String connectionStatus = "not connected";
    private Queue<String> writerQueue = new LinkedList<>();
    private RemoteDevice remoteDevice;
    private InputStream inputStream;
    private OutputStream outputStream;
//    private BufferedReader inputReader;
//    private PrintWriter outputWriter;
    private StreamConnectionNotifier notifier;
    private StreamConnection connection;
    private LocalDevice localDevice;

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int screenWidth = (int) screenSize.getWidth();
    int screenHeight = (int) screenSize.getHeight();

    private Thread readThread = new Thread() {
        @Override
        public void run() {
            byte[] buffer = new byte[10240];
            int bytes;

            MouseKeyboardControl controller = new MouseKeyboardControl();

            while (connectionStatus.equals("connected")) {
                try {
                    System.out.println("Trying to read input stream");
                    bytes = inputStream.read(buffer);
                    if (bytes == -1) {
                        connectionStatus = "disconnected";
                        stopConnection();
                        break;
                    }
                    String incomingMessage = new String(buffer, 0, bytes);
                    System.out.println("Input Stream: " + incomingMessage);

                    //process command here
                    String command = incomingMessage;
                    String[] commandList = incomingMessage.split(Constants.DELIM);

                    String action = commandList[0];
//                    if (action.equals("TYPE_CHARACTER")) {
//                        String textToType = commandList[1];
//                        controller.typeString(textToType);
//                    }

                    //////////
                    switch (action) {
                        case "LEFT_CLICK":
                            controller.leftClick();
                            break;
                        case "RIGHT_CLICK":
                            controller.rightClick();
                            break;
                        case "DOUBLE_CLICK":
                            controller.doubleClick();
                            break;
                        case "MOUSE_WHEEL":
                            int scrollAmount = Integer.parseInt(commandList[1]);
                            controller.mouseWheel(scrollAmount);
                            break;
                        case "MOUSE_MOVE":
                            float x = Float.parseFloat(commandList[1]);
                            float y = Float.parseFloat(commandList[2]);
                            Point point = MouseInfo.getPointerInfo().getLocation();
                            // Get current mouse position
                            float nowx = point.x;
                            float nowy = point.y;
                            controller.mouseMove((int) (nowx + x), (int) (nowy + y));
                            break;
                        case "MOUSE_MOVE_LIVE":
                            // need to adjust coordinates 
                            float xCord = Float.parseFloat(commandList[1]);
                            float yCord = Float.parseFloat(commandList[2]);
                            xCord = xCord * screenWidth;
                            yCord = yCord * screenHeight;
                            controller.mouseMove((int) xCord, (int) yCord);
                            break;
                        case "KEY_PRESS":
                            int keyCode = Integer.parseInt(commandList[1]);
                            controller.keyPress(keyCode);
                            break;
                        case "KEY_RELEASE":
                            keyCode = Integer.parseInt(commandList[1]);
                            controller.keyRelease(keyCode);
                            break;
                        case "CTRL_ALT_T":
                            controller.ctrlAltT();
                            break;
                        case "CTRL_SHIFT_Z":
                            controller.ctrlShiftZ();
                            break;
                        case "ALT_F4":
                            controller.altF4();
                            break;
                        case "TYPE_CHARACTER":
                            //handle StringIndexOutOfBoundsException here when pressing soft enter key
                            String ch = commandList[1];
                            controller.typeString(ch);
                            break;
                        case "TYPE_KEY":
                            keyCode = Integer.parseInt(commandList[1]);
                            controller.typeCharacter(keyCode);
                            break;
                        case "LEFT_ARROW_KEY":
                            controller.pressLeftArrowKey();
                            break;
                        case "DOWN_ARROW_KEY":
                            controller.pressDownArrowKey();
                            break;
                        case "RIGHT_ARROW_KEY":
                            controller.pressRightArrowKey();
                            break;
                        case "UP_ARROW_KEY":
                            controller.pressUpArrowKey();
                            break;
                        case "F5_KEY":
                            controller.pressF5Key();
                            break;
                    }
                    /////////

                } catch (IOException ex) {
                    Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("Connection Closed");
        }

    };

    private Thread writeThread = new Thread() {
        @Override
        public void run() {

            while (connectionStatus.equals("connected")) {
                if (writerQueue.size() > 0) {
                    String message = writerQueue.peek();
                    if (message != null) {
                        System.out.println("Sending Message: " + message);
                        //The following line actually sends the data - RME
//                        outputWriter.write(message);
                        try {
                            outputStream.write(message.getBytes());
                            outputStream.flush();

                        } catch (IOException ex) {
                            Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        writerQueue.poll();
                    }
                }
            }
        }

    };

    private Thread heartbeatThread = new Thread() {
        @Override
        public void run() {
            while (connectionStatus.equals("connected")) {
                try {
                    write("Heartbeat");
                    sleep(10000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    };

    public RemoteDevice getRemoteDevice() {
        return remoteDevice;
    }

    private BluetoothConnectionManager() {
    }

    public void startConnection() {
        Thread startConnectionThread = new Thread() {
            @Override
            public void run() {
                try {
                    UUID uuid = new UUID(Constants.UUID.replace("-", ""), false); //UUID.fromString(Constants.UUID);
                    String connectionString = "btspp://localhost:" + uuid.toString() + ";name=" + Constants.APP_NAME + " Server";
                    //String connectionString = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";

                    connectionStatus = "connecting";

                    notifier = (StreamConnectionNotifier) Connector.open(connectionString);;
                    System.out.println("Starting Connection...");

                    try {
                        connection = notifier.acceptAndOpen();
                        System.out.println("Connection established");

                        writerQueue.clear();

                        remoteDevice = RemoteDevice.getRemoteDevice(connection);
                        inputStream = connection.openInputStream();
                        outputStream = connection.openOutputStream();

//                        inputReader = new BufferedReader(new InputStreamReader(inputStream));
//                        outputWriter = new PrintWriter(new OutputStreamWriter(outputStream));
                        connectionStatus = "connected";

                        readThread.start();
                        writeThread.start();
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
                System.out.println("CONNECTION CLOSE 2");
                notifier.close();
            }
            if (connection != null) {
                System.out.println("CONNECTION CLOSE 1");
                connection.close();
            }
            
//            if (outputWriter != null) {
//                System.out.println("CONNECTION CLOSE 3");
//                outputWriter.close();
//            }
//            if (inputReader != null) {
//                System.out.println("CONNECTION CLOSE 4");
//                inputReader.close();
//            }
            if (inputStream != null) {
                System.out.println("CONNECTION CLOSE 5");
                inputStream.close();
            }
            if (outputStream != null) {
                System.out.println("CONNECTION CLOSE 6");
                outputStream.close();
            }
            if (remoteDevice != null) {
                System.out.println("CONNECTION CLOSE 7");
                remoteDevice = null;
            }

            if (writerQueue != null) {
                System.out.println("CONNECTION CLOSE 8");
                writerQueue.clear();
            }
        } catch (IOException ex) {
            Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(String message) {
        writerQueue.add(message);
    }

}
