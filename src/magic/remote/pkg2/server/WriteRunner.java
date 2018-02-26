/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package magic.remote.pkg2.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tanmoy Krishna Das
 */
public class WriteRunner implements Runnable {
    BluetoothConnectionManager bluetoothConnection = BluetoothConnectionManager.getInstance();
    
    @Override
    public void run() {
        if(bluetoothConnection==null) System.out.println("null found");
        if(bluetoothConnection.connectionStatus==null) System.out.println("Null found 2");
        while (bluetoothConnection.connectionStatus.equals("connected")) {
            if (bluetoothConnection.getWriterQueue().size() > 0) {
                String message = bluetoothConnection.getWriterQueue().peek();
                if (message != null) {
                    System.out.println("Sending Message: " + message);
                    //The following line actually sends the data - RME
//                        outputWriter.write(message);
                    try {
                        bluetoothConnection.sendData(message);

                    } catch (IOException ex) {
                        Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    bluetoothConnection.getWriterQueue().poll();
                }
            }
        }
    }

}
