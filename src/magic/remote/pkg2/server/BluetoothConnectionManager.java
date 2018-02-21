/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package magic.remote.pkg2.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.RemoteDevice;
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

    public volatile String connectionStatus = "disconnected";
    private Queue<String> writerQueue = new LinkedList<>();
    private RemoteDevice remoteDevice;
    private InputStream inputStream;
    private OutputStream outputStream;
    private BufferedReader inputReader;
    private PrintWriter outputWriter;
    private StreamConnectionNotifier notifier;
    private StreamConnection connection;

    private Thread readThread = new Thread() {
        @Override
        public void run() {
            while (connectionStatus.equals("connected")) {
                try {
                    char[] buffer = new char[10240];
                    inputReader.read(buffer);
                    String command = new String(buffer);

                    //process command here
                } catch (IOException ex) {
                    Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    };

    private Thread writeThread = new Thread() {
        @Override
        public void run() {

            while (connectionStatus.equals("connected")) {
                if (writerQueue.size() > 0) {
                    String message = writerQueue.peek();
                    if (message != null) {
                        //The following line actually sends the data - RME
                        outputWriter.write(message);
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

    private BluetoothConnectionManager() {
    }

    public void startConnection() throws IOException {
        Thread startConnectionThread = new Thread() {
            @Override
            public void run() {
                try {
                    UUID uuid = UUID.fromString(Constants.UUID);
                    String connectionString = "btspp://localhost:" + uuid + ";name=" + Constants.APP_NAME + " Server";

                    connectionStatus = "connecting";

                    notifier = (StreamConnectionNotifier) Connector.open(connectionString);
                    System.out.println("Starting Connection...");

                    connection = notifier.acceptAndOpen();
                    System.out.println("Connection established");

                    connectionStatus = "connected";

                    writerQueue.clear();

                    remoteDevice = RemoteDevice.getRemoteDevice(connection);
                    inputStream = connection.openInputStream();
                    outputStream = connection.openOutputStream();

                    inputReader = new BufferedReader(new InputStreamReader(inputStream));
                    outputWriter = new PrintWriter(new OutputStreamWriter(outputStream));

                    readThread.start();
                    writeThread.start();
                    heartbeatThread.start();

                } catch (IOException ex) {
                    Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        startConnectionThread.start();
    }

    public void stopConnection() {
        try {
            if(notifier!=null)notifier.close();
            if(connection!=null)connection.close();
            if(outputWriter!=null)outputWriter.close();
           if(inputReader!=null) inputReader.close();
           if(inputStream!=null) inputStream.close();
           if(outputStream!=null) outputStream.close();
           if(remoteDevice!=null) remoteDevice = null;
           connectionStatus = "disconnected";
           if(writerQueue!=null) writerQueue.clear();
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
