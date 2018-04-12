/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.framebuffer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cic
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("preparing...");

        int width = 1824;
        int height = 984;

        byte[] buffer = new byte[width * height * 4];
        
        long time0 = System.currentTimeMillis();
        
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = i % 4 == 0 ? (byte)0xFF : 0x30;
        }
        
//        byte[] bytesLine = new byte[width];
//        for (int i = 0; i < width / 4; i++) {
//            bytesLine[i * 4 + 0] = (byte) 0xff;
//            bytesLine[i * 4 + 1] = 0x00;
//            bytesLine[i * 4 + 2] = 0x00;
//            bytesLine[i * 4 + 3] = 0x00;
//        }

        long time1 = System.currentTimeMillis();

        try (FileOutputStream fos = new FileOutputStream("/dev/fb0");
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {

//            byte[] bytesA = new byte[]{(byte) 0xFF, 0x00, 0x00, 0x00};
//            byte[] bytesB = new byte[]{0x00, (byte) 0xFF, 0x00, 0x00};
//            byte[] bytesC = new byte[]{0x00, 0x00, (byte) 0xFF, 0x00};
//            byte[] bytesD = new byte[]{0x00, 0x00, 0x00, (byte) 0xFF};
//
//            int len1 = (height / 4) /2;
//            
//            for (int j = 0; j < len1; j++) {
//
//                for (int i = 0; i < width; i++) {
//                    bos.write(bytesA);
//                }
//
//                for (int i = 0; i < width; i++) {
//                    bos.write(bytesB);
//                }
//
//                for (int i = 0; i < width; i++) {
//                    bos.write(bytesC);
//                }
//
//                for (int i = 0; i < width; i++) {
//                    bos.write(bytesD);
//                }
//            }
//            
//            int len2 = (height ) - 50;
//            
//            for (int j = 0; j < len2; j++) {
//                bos.write(bytesLine);
//            }
            bos.write(buffer);
            


//            bos.flush();
//            fos.flush();

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        long time2 = System.currentTimeMillis();
        
        System.out.println("drawing: " + (time1 - time0) + " ms");
        System.out.println("writing: " + (time2 - time1) + " ms");
    }

}
