/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package magic.remote.pkg2.server;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tanmoy Krishna Das
 */
public class ReadRunner implements Runnable {
    BluetoothConnectionManager bluetoothConnection = BluetoothConnectionManager.getInstance();
    
    @Override
    public void run() {
        byte[] buffer = new byte[10240];
        int bytes;

        while (bluetoothConnection.getConnectionStatus().equals("connected")) {
            try {
//                System.out.println("Trying to read input stream");
                bytes = bluetoothConnection.inputStream.read(buffer);
                
                if (bytes == -1) {
                    bluetoothConnection.stopConnection();
                    break;
                }
                
                String incomingMessage = new String(buffer, 0, bytes);
//                System.out.println("Input Stream: " + incomingMessage);

                //process command here
                String command = incomingMessage;
                String[] commandList = incomingMessage.split(Constants.DELIM);

                
                for(String eachCommand: commandList) {
                    if( !eachCommand.equals("") || (eachCommand.length()>0 && eachCommand.charAt(0)=='\b')) {
//                        System.out.println("Command: " + eachCommand);
                        bluetoothConnection.getProcessingQueue().add(eachCommand);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Connection Closed");
    }

}
