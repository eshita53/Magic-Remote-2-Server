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
        if (connectionManager.connectionStatus.equals("disconnected")) {
            connectButton.setText("Stop waiting");
            connectionStatus.setText("Waiting for connection...");
            connectionStatus.setFill(Color.web("#536DFE"));
            try {
                connectionManager.startConnection();
            } catch (IOException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            connectionManager.stopConnection();
            connectButton.setText("Connect");
            connectionStatus.setText("Previous connection aborted. Ready for new connection");
            connectionStatus.setFill(Color.CRIMSON);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
