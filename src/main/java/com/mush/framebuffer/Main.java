/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.framebuffer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cic
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("preparing..." + args.length);

//        String fbFile = "/dev/fb0";
        String fbFile = args.length == 0 ? "test.out" : args[0];
        int width = 1824;
        int height = 984;

        byte[] buffer1 = new byte[width * height * 4];
//        byte[] buffer2 = new byte[width * height * 4];

        long time00 = System.currentTimeMillis();

        BufferedImage image = createImage(width, height);
        byte[] buffer2 = getImageByteBuffer(image);

        long time0 = System.currentTimeMillis();

        for (int i = 0; i < buffer1.length; i++) {
            buffer1[i] = i % 4 == 0 ? (byte) 0xFF : 0x30;
            //buffer2[i] = i % 4 == 1 ? (byte) 0xFF : 0x30;
        }

        long time1 = System.currentTimeMillis();

        useStream(fbFile, buffer1);

        long time2 = System.currentTimeMillis();

        useFile(fbFile, buffer1);

        long time3 = System.currentTimeMillis();

        int count = useFileRefresh(fbFile, buffer1, buffer2);

        long time4 = System.currentTimeMillis();

        System.out.println("drawing image    : " + (time0 - time00) + " ms");
        System.out.println("drawing loop     : " + (time1 - time0) + " ms");
        System.out.println("writing stream   : " + (time2 - time1) + " ms");
        System.out.println("writing file     : " + (time3 - time2) + " ms");
        System.out.println("writing file " + count + "x : " + (time4 - time3) + " ms " + ((time4 - time3) / count) + " ms/f");
    }

    private static BufferedImage createImage(int width, int height) {
        int samplesPerPixel = 4;
        int[] bandOffsets = {2, 1, 0, 3}; // BGRA order

        byte[] bgraPixelData = new byte[width * height * samplesPerPixel];

        DataBuffer buffer = new DataBufferByte(bgraPixelData, bgraPixelData.length);
        WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, samplesPerPixel * width, samplesPerPixel, bandOffsets, null);

        ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

        BufferedImage image = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);

        Graphics2D g = image.createGraphics();

        g.setBackground(Color.GRAY);
        g.clearRect(0, 0, width/2, height);

        g.setColor(Color.RED);
        g.drawRect(100, 100, 100, 100);

        g.setColor(Color.GREEN);
        g.drawRect(150, 120, 100, 100);

        g.setColor(Color.BLUE);
        g.drawRect(170, 150, 100, 100);

        return image;
    }

    private static byte[] getImageByteBuffer(BufferedImage image) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ImageIO.write(originalImage, "jpg", baos);
//        baos.flush();
//        byte[] imageInByte = baos.toByteArray();
//        baos.close();
//        return imageInByte;

        return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    }

    private static void useStream(String filename, byte[] buffer) {
        try (FileOutputStream fos = new FileOutputStream(filename);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(buffer);

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void useFile(String filename, byte[] buffer) {
        try (RandomAccessFile raf = new RandomAccessFile(filename, "rw")) {
            raf.write(buffer);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static int useFileRefresh(String filename, byte[] buffer1, byte[] buffer2) {
        int c = 0;
        try (RandomAccessFile raf = new RandomAccessFile(filename, "rw")) {
            for (int i = 0; i < 10; i++) {
                raf.seek(0);
                raf.write(buffer1);
                c++;
                raf.seek(0);
                raf.write(buffer2);
                c++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return c;
    }

}
