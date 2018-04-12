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
import java.awt.geom.AffineTransform;
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

    byte[] buffer1;
    byte[] buffer2;
    StringBuilder sb;
    long[] lastTime = null;
    Graphics2D g;

    public static void main(String[] args) {
        new Main(args);
    }

    private long tick(int level) {
        if (lastTime == null) {
            lastTime = new long[5];
            for (int i = 0; i < lastTime.length; i++) {
                lastTime[i] = 0;
            }
        }
        long tock = System.currentTimeMillis();
        long tick = tock - lastTime[level];
        lastTime[level] = tock;
        return tick;
    }

    private Main(String[] args) {
        System.out.println("preparing...");

//        String fbFile = "/dev/fb0";
        String fbFile = args.length == 0 ? "test.out" : args[0];
        System.out.println("Filename: " + fbFile);

        sb = new StringBuilder();
        tick(0);

        int width = 1824;
        int height = 984;

        buffer1 = new byte[width * height * 4];

        sb.append("Creating buffer       : ").append(tick(0)).append("ms\n");

        for (int i = 0; i < buffer1.length; i++) {
            buffer1[i] = i % 4 == 0 ? (byte) 0xFF : 0x30;
        }

        sb.append("Drawing loop to buffer: ").append(tick(0)).append("ms\n");

        BufferedImage image = createImage(width, height);
//        buffer2 = getImageByteBuffer(image);

        sb.append("Creating image        : ").append(tick(0)).append("ms\n");

        useStream(fbFile, buffer1);
        
        sb.append("Writing to stream     : ").append(tick(0)).append("ms\n");

        useFile(fbFile, buffer1);
        
        sb.append("Writing to file       : ").append(tick(0)).append("ms\n");

        int count = useFileRefresh(fbFile/*, buffer1, buffer2*/);
        
        long duration = tick(0);
        sb.append("Writing to file ").append(count).append("x : ").append(duration).append("ms ").append(duration / count).append(" ms/f\n");
        
        System.out.println(sb.toString());
    }

    private BufferedImage createImage(int width, int height) {
        tick(1);
        int samplesPerPixel = 4;
        int[] bandOffsets = {2, 1, 0, 3}; // BGRA order

        byte[] bgraPixelData = new byte[width * height * samplesPerPixel];
        
        sb.append(" Creating img buffer  : ").append(tick(1)).append("ms\n");

        DataBuffer buffer = new DataBufferByte(bgraPixelData, bgraPixelData.length);
        sb.append(" DataBuffer           : ").append(tick(1)).append("ms\n");
        WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, samplesPerPixel * width, samplesPerPixel, bandOffsets, null);
        sb.append(" Raster               : ").append(tick(1)).append("ms\n");

        ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

        BufferedImage image = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
        
        sb.append(" BufferedImage        : ").append(tick(1)).append("ms\n");

        g = image.createGraphics();
        
        sb.append(" image.createGraphics : ").append(tick(1)).append("ms\n");

        g.setBackground(Color.GRAY);
        g.clearRect(0, 0, width / 2, height);
        
        sb.append(" g.clearRect          : ").append(tick(1)).append("ms\n");

        g.setColor(Color.RED);
        g.drawRect(100, 100, 100, 100);

        g.setColor(Color.GREEN);
        g.drawRect(150, 120, 100, 100);

        g.setColor(Color.BLUE);
        g.drawRect(170, 150, 100, 100);
        
        sb.append(" g.drawRect 3x        : ").append(tick(1)).append("ms\n");

        buffer2 = bgraPixelData;

        return image;
    }

    private byte[] getImageByteBuffer(BufferedImage image) {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ImageIO.write(originalImage, "jpg", baos);
//        baos.flush();
//        byte[] imageInByte = baos.toByteArray();
//        baos.close();
//        return imageInByte;

        return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    }

    private void useStream(String filename, byte[] buffer) {
        try (FileOutputStream fos = new FileOutputStream(filename);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(buffer);

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void useFile(String filename, byte[] buffer) {
        try (RandomAccessFile raf = new RandomAccessFile(filename, "rw")) {
            raf.write(buffer);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int useFileRefresh(String filename/*, byte[] buffer1, byte[] buffer2*/) {
        int c = 0;
        Color[] colors = new Color[]{Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.ORANGE, Color.MAGENTA};
        
        AffineTransform t = g.getTransform();
        g.scale(2, 2);
        
        try (RandomAccessFile raf = new RandomAccessFile(filename, "rw")) {
            for (int i = 0; i < 100; i++) {
//                raf.seek(0);
//                raf.write(buffer1);
//                c++;
                int x = 200 + (int)(Math.random() * 500);
                int y = 100 + (int)(Math.random() * 200);
                g.setColor(colors[c % colors.length]);
                g.fillRect(x, y, 100, 100);
                g.setColor(Color.WHITE);
                g.drawRect(x, y, 100, 100);
                
                raf.seek(0);
                raf.write(buffer2);
                c++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        g.setTransform(t);
        
        return c;
    }

}
