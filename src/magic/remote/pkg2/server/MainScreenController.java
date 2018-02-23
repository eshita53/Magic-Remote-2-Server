/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package magic.remote.pkg2.server;

import static java.awt.SystemColor.text;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 *
 * @author romana
 */
public class MainScreenController implements Initializable {

    BluetoothConnectionManager connectionManager = BluetoothConnectionManager.getInstance();

    @FXML
    Button connectButton;
    @FXML
    Text connectionStatus;

    @FXML
    private void connectButtonClick(ActionEvent event) {
        if (connectionManager.connectionStatus.equals("disconnected") || connectionManager.connectionStatus.equals("not connected")) {
//            connectButton.setText("Stop waiting");
//            connectionStatus.setText("Waiting for connection...");
//            connectionStatus.setFill(Color.web("#536DFE"));
            connectionManager.startConnection();

        } else {
            connectionManager.stopConnection();
//            connectButton.setText("Connect");
//            connectionStatus.setText("Previous connection aborted. Ready for new connection");
//            connectionStatus.setFill(Color.CRIMSON);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Thread statusManager = new Thread() {
            @Override
            public void run() {
                String prevStatus = "";
                while (true) {
                    try {
                        sleep(150);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    String currentStatus = connectionManager.connectionStatus;

                    if (currentStatus.equals(prevStatus)) {
                        continue;
                    } else {
                        prevStatus = currentStatus;
                    }

                    if (currentStatus.equals("connected")) {
                        Platform.runLater(() -> {
                            connectButton.setText("Disconnect");
                            connectionStatus.setText("Connected to remote device");
                            try {
                                String name = connectionManager.getRemoteDevice().getFriendlyName(true);
                                connectionStatus.setText("Connected to "+name);
                            } catch(Exception e) {
                                connectionStatus.setText("Connected to remote device");
                            }
                            connectionStatus.setFill(Color.GREEN);
                        });

                    } else if (currentStatus.equals("connecting")) {
                        Platform.runLater(() -> {
                            connectButton.setText("Stop waiting");
                            connectionStatus.setText("Waiting for connection...");
                            connectionStatus.setFill(Color.web("#536DFE"));
                        });

                    } else if (currentStatus.equals("disconnected")) {
                        Platform.runLater(() -> {
                            connectButton.setText("Connect");
                            connectionStatus.setText("Previous connection aborted. Ready for new connection");
                            connectionStatus.setFill(Color.CRIMSON);
                        });

                    } else if (currentStatus.equals("not connected")) {
                        Platform.runLater(() -> {
                            connectButton.setText("Connect");
                            connectionStatus.setText("Ready to connect");
                            connectionStatus.setFill(Color.DARKSEAGREEN);
                        });

                    }
                }
            }
        };
        statusManager.start();
    }

}
