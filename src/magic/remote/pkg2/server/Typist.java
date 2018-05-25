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
public class Typist {
    static String arch, wow64Arch, realArch, ahkPath, scriptPath;
    static {
        arch = System.getenv("PROCESSOR_ARCHITECTURE");
        wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

        realArch = arch != null && arch.endsWith("64")
                || wow64Arch != null && wow64Arch.endsWith("64")
                ? "64" : "32";
        
        String directory = System.getProperty("user.home") + java.io.File.separator; //"C://Temp/";
        if(realArch.equals("64")) ahkPath = directory + "AutoHotkeyU64.exe";
        else ahkPath = directory + "AutoHotkeyU32.exe";
        
        scriptPath = directory + "a.ahk";
    }
    
    public static void type(String data) {
        try {
            Runtime.getRuntime().exec(new String[] { ahkPath, scriptPath, data} );
        } catch (IOException ex) {
            Logger.getLogger(Typist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
