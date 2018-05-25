/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package magic.remote.pkg2.server;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

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
        stage.setTitle("Ekushey Remote");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/icon2small.png")));
        stage.setResizable(false);
        stage.show();

        stage.setOnCloseRequest(e -> {
            BluetoothConnectionManager bluetoothConnection = BluetoothConnectionManager.getInstance();
            if (bluetoothConnection != null) {
                bluetoothConnection.stopConnection();
            }
            controller.setIsShowing(false);
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    extractExeFiles(System.getProperty("user.home"));
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }).start();

        launch(args);
    }

    /**
     * Gets running jar file path.
     *
     * @return running jar file path.
     */
    private static File getCurrentJarFilePath() {
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        while (path.contains("%20")) {
            path = path.replace("%20", " ");
        }
        return new File(path);

    }

    /**
     * Extracts all exe files to the destination directory.
     *
     * @param destDir destination directory.
     * @throws IOException if there's an i/o problem.
     */
    private static void extractExeFiles(String destDir) throws IOException {
        java.util.jar.JarFile jar = new java.util.jar.JarFile(getCurrentJarFilePath());
        java.util.Enumeration enumEntries = jar.entries();
        String entryName;
        while (enumEntries.hasMoreElements()) {
            java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
            entryName = file.getName();
            if ((entryName != null) && ((entryName.endsWith(".exe")) || (entryName.endsWith(".ahk")))) {
                java.io.File f = new java.io.File(destDir + java.io.File.separator + entryName);
                if (file.isDirectory()) { // if its a directory, create it
                    f.mkdir();
                    continue;
                }
                java.io.InputStream is = jar.getInputStream(file); // get the input stream
                java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                while (is.available() > 0) {  // write contents of 'is' to 'fos'
                    fos.write(is.read());
                }

                fos.close();
                is.close();
            }
        }
    }
}
