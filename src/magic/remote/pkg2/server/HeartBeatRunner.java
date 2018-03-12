/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package magic.remote.pkg2.server;

import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tanmoy Krishna Das
 */
public class HeartBeatRunner implements Runnable {
    BluetoothConnectionManager bluetoothConnection = BluetoothConnectionManager.getInstance();
    
    @Override
    public void run() {
        int counter = 1;
        while (bluetoothConnection.connectionStatus.equals("connected")) {
            try {
                bluetoothConnection.write("Heartbeat " + counter++);
                sleep(4000);
            } catch (InterruptedException ex) {
                Logger.getLogger(BluetoothConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
