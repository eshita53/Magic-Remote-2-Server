/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package magic.remote.pkg2.server;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import static java.awt.SystemColor.text;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javax.imageio.ImageIO;

/**
 *
 * @author romana
 */
public class MainScreenController implements Initializable {

    BluetoothConnectionManager connectionManager = BluetoothConnectionManager.getInstance();
    private volatile boolean isShowing;

    @FXML
    Button connectButton;
    @FXML
    Text connectionStatus;

    public void setIsShowing(boolean b) {
        isShowing = b;
    }

    @FXML
    private void connectButtonClick(ActionEvent event) {
        if (connectionManager.connectionStatus.equals("disconnected") || connectionManager.connectionStatus.equals("not connected")) {
            connectionManager.startConnection();
        } else {
            connectionManager.stopConnection();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isShowing = true;
        showStatus();
        //startScreenshot();
    }

    boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
        
        int w1=img1.getWidth(), w2=img2.getWidth(), h1=img1.getHeight(), h2=img2.getHeight();
        
        if (w1 == w2 && h1 == h2) {
            for (int x = 0; x < w1; x++) {
                for (int y = 0; y < h1; y++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    private void startScreenshot() {
        new Thread(new Runnable() {
            BufferedImage imgs[], prevImgs[];
            Robot robot;

            @Override
            public void run() {
                try {
                    Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                    robot = new Robot();
                    int columns = 20;
                    int rows = 20;
                    int chunks = rows * columns;
                    int counter = 0;

                    int chunkWidth = ((int) rectangle.getWidth()) / columns;
                    int chunkHeight = ((int) rectangle.getHeight()) / rows;

                    prevImgs = new BufferedImage[chunks];

                    while (isShowing) {
                        BufferedImage screen = robot.createScreenCapture(rectangle);
                        //Image image = SwingFXUtils.toFXImage(screen, null);
//                        ImageIO.write(screen, "jpg", new File("screenshot.jpg"));

                        imgs = new BufferedImage[chunks];
                        int matrix[][] = new int[rows][columns];

                        long strt = Calendar.getInstance().getTimeInMillis();
                        counter = 0;
                        for (int x = 0; x < rows; x++) {
                            for (int y = 0; y < columns; y++) {
                                matrix[x][y] = 0;
                                //Initialize the image array with image chunks
                                imgs[counter] = new BufferedImage(chunkWidth, chunkHeight, screen.getType());

                                // draws the image chunk
                                Graphics2D gr = imgs[counter].createGraphics();
                                gr.drawImage(screen, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                                gr.dispose();

                                if (prevImgs[counter] == null || !bufferedImagesEqual(prevImgs[counter], imgs[counter])) {
                                    matrix[x][y] = 1;
                                }

                                prevImgs[counter] = imgs[counter];

                                counter++;
                            }
                        }
                        long fin = Calendar.getInstance().getTimeInMillis() - strt;
                        System.out.println(fin);

                        System.out.println();
                        for (int i = 0; i < rows; i++) {
                            for (int j = 0; j < columns; j++) {
                                System.out.print(matrix[i][j]);
                            }
                            System.out.println();
                        }
                        
                        System.out.println(fin);
                        
                        sleep(150);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    public void showStatus() {
        Thread statusManager = new Thread() {
            @Override
            public void run() {
                String prevStatus = "";
                while (isShowing == true) {
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
                                connectionStatus.setText("Connected to " + name);
                            } catch (Exception e) {
                                connectionStatus.setText("Connected to remote device");
                            }
                            connectionStatus.setFill(Color.GREEN);
                        });

                    } else if (currentStatus.equals("connecting")) {
                        Platform.runLater(() -> {
                            connectButton.setText("Stop\nwaiting");
                            connectionStatus.setText("Waiting for connection...");

                            try {
                                String localName = connectionManager.getLocalDevice().getFriendlyName();
                                if (localName != null) {
                                    connectionStatus.setText("Waiting for connection\nDevice Name: \"" + localName + "\"");
                                }
                            } catch (Exception e) {
                                System.out.println(e.toString());
                                e.printStackTrace();
                            }

                            connectionStatus.setFill(Color.web("#536DFE"));
                        });

                    } else if (currentStatus.equals("disconnected")) {
                        Platform.runLater(() -> {
                            connectButton.setText("Connect");
                            connectionStatus.setText("Previous connection aborted.\nReady for new connection");
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
