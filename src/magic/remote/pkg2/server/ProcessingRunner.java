/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package magic.remote.pkg2.server;

import java.awt.MouseInfo;
import java.awt.Point;
import static java.lang.Thread.sleep;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tanmoy Krishna Das
 */
public class ProcessingRunner implements Runnable {

    BluetoothConnectionManager bluetoothConnection = BluetoothConnectionManager.getInstance();
    int sleepTime = 40;
    int longSleepTime = 100;
    int counter = 1;

    @Override
    public void run() {
        Queue<String> processingQueue = bluetoothConnection.getProcessingQueue();
        MouseKeyboardControl controller = new MouseKeyboardControl();

        while (bluetoothConnection.getConnectionStatus().equals("connected")) {

            String action = processingQueue.poll();

            if (action == null) {
                try {
                    sleep(longSleepTime);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ProcessingRunner.class.getName()).log(Level.SEVERE, null, ex);
                }
                continue;
            }

            try {
                //System.out.println("Action: " + counter++ + ": " + action);

                //////////
                switch (action) {
                    case "LEFT_MOUSE_PRESS":
                        controller.leftMouseDown();
                        break;
                    case "LEFT_MOUSE_RELEASE":
                        controller.leftMouseRelease();
                        
                    case "LEFT_CLICK":
                        controller.leftClick();
                        break;
                    case "RIGHT_CLICK":
                        controller.rightClick();
                        break;
                    case "MIDDLE_CLICK":
                        controller.middleClick();
                        break;
                    case "DOUBLE_CLICK":
                        controller.doubleClick();
                        break;
                    case "MOUSE_WHEEL":
                        String amountStr = processingQueue.poll();
                        while (amountStr == null) {
                            try {
                                sleep(sleepTime);
                                amountStr = processingQueue.poll();
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ProcessingRunner.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        try {
                            System.out.println("Scroll: " + amountStr);
                            int scrollAmount = Integer.parseInt(amountStr);
                            if(scrollAmount>0) {
                                controller.mouseWheel(1);
                            } else if(scrollAmount<0) {
                                controller.mouseWheel(-1);
                            }
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                            //System.out.println("Error: " + e.getMessage() + "\nCause: " + e.getCause());
                        }

                        break;
                    case "MOUSE_MOVE":
                        try {
                            String xStr = processingQueue.poll();
                            while (xStr == null) {
                                sleep(sleepTime);
                                xStr = processingQueue.poll();
                            }

                            String yStr = processingQueue.poll();
                            while (yStr == null) {
                                sleep(sleepTime);
                                yStr = processingQueue.poll();
                            }

                            float x = Float.parseFloat(xStr);
                            float y = Float.parseFloat(yStr);
                            
                            //System.out.println("Move amount: " +  x +", " + y);
                            Point point = MouseInfo.getPointerInfo().getLocation();
                            // Get current mouse position
                            float nowx = point.x;
                            float nowy = point.y;
                            controller.mouseMove(nowx, nowy, (nowx + x), (nowy + y));
                        } catch (Exception e) {
                            e.printStackTrace();
                            //System.out.println("Error: " + e.getMessage() + "\nCause: " + e.getCause());
                        }
                        break;
                    case "MOUSE_MOVE_LIVE":
                        // need to adjust coordinates 
                        try {
                            String xStr = processingQueue.poll();
                            while (xStr == null) {
                                sleep(sleepTime);
                                xStr = processingQueue.poll();
                            }

                            String yStr = processingQueue.poll();
                            while (yStr == null) {
                                sleep(sleepTime);
                                yStr = processingQueue.poll();
                            }

                            float xCord = Float.parseFloat(xStr);
                            float yCord = Float.parseFloat(yStr);
                            xCord = xCord * bluetoothConnection.getScreenWidth();
                            yCord = yCord * bluetoothConnection.getScreenHeight();
                            controller.mouseMove((int) xCord, (int) yCord);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case "KEY_PRESS":
                        try {
                            String data = processingQueue.poll();
                            while (data == null) {
                                sleep(sleepTime);
                                data = processingQueue.poll();
                            }

                            int keyCode = Integer.parseInt(data);
                            controller.keyPress(keyCode);
                        } catch (Exception e) {
                            e.printStackTrace();
                            //System.out.println("Error: " + e.getMessage() + "\nCause: " + e.getCause());
                        }
                        break;
                    case "KEY_RELEASE":
                        try {
                            String data = processingQueue.poll();
                            while (data == null) {
                                sleep(sleepTime);
                                data = processingQueue.poll();
                            }

                            int keyCode = Integer.parseInt(data);
                            controller.keyRelease(keyCode);
                        } catch (Exception e) {
                            e.printStackTrace();
                            //System.out.println("Error: " + e.getMessage() + "\nCause: " + e.getCause());
                        }
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
                        try {
                            String data = processingQueue.poll();
                            while (data == null) {
                                sleep(sleepTime);
                                data = processingQueue.poll();
                            }

                            //System.out.println("Typing: " + data);
                            controller.typeString(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                            //System.out.println("Error: " + e.getMessage() + "\nCause: " + e.getCause());
                        }

                        break;
                    case "TYPE_KEY":
                        try {
                            String data = processingQueue.poll();
                            while (data == null) {
                                sleep(sleepTime);
                                data = processingQueue.poll();
                            }

                            int keyCode = Integer.parseInt(data);
                            controller.typeCharacter(keyCode);
                        } catch (Exception e) {
                            e.printStackTrace();
                            //System.out.println("Error: " + e.getMessage() + "\nCause: " + e.getCause());
                        }
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
                ///////
            } finally {
                controller.resetRobot();
            }

        }
        //System.out.println("proccessing completed");
    }
}
