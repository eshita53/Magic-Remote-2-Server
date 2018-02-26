/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package magic.remote.pkg2.server;

import java.awt.event.WindowEvent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author romana
 */
public class Main extends Application {

    MainScreenController controller = new MainScreenController();

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScreen.fxml"));
        loader.setController(controller);
        root = loader.load();
        loader.setRoot(root);

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Magic Remote");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/icon.png")));
        stage.setResizable(false);
        stage.show();
    
        stage.setOnCloseRequest(e->{
            BluetoothConnectionManager bluetoothConnection = BluetoothConnectionManager.getInstance();
            if(bluetoothConnection!=null) bluetoothConnection.stopConnection();
            controller.setIsShowing(false);
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
