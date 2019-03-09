/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dinu
 */
public class Segmentation {

    public static int THRESHOLD = 120;
    public static int NEW_VALUE = 255;

    public static BufferedImage segmentContour(BufferedImage bi) {
        
        bi = PointOperation.averageGrayscale(bi);

        int[][] intensities = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.RED);

        // apply mask for each channel
        List<Pixel> listModified = applyContourTracking(intensities);
        
        for (int i = 0; i < listModified.size(); i++) {
            Pixel p = listModified.get(i);
//            bi.setRGB(p.getY(), p.getX(), ImageUtil.colorToRGB(255, NEW_VALUE, 0, 0));
             bi.setRGB(p.getY(), p.getX(), Color.RED.getRGB());
        }
        
        System.out.println("applied contour tracking");

        return bi;
    }

    public static List<Pixel> applyContourTracking(int[][] pixels) {
        int height = pixels.length;
        int width = pixels[0].length;

        List<Pixel> listPixels = new ArrayList<Pixel>();

        outerloop:
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                if (pixels[i][j] >= THRESHOLD) {
                    listPixels.add(new Pixel(i, j, 1));
                    break outerloop;
                }
            }
        }

        if (listPixels.size() > 0) {
            Pixel first = listPixels.get(0);
//            System.out.println("first pixel(i,j)=" + first.getX() + "," + first.getY());
            Pixel neighbour = null;
            do {
                neighbour = getNeighbour(listPixels.get(listPixels.size() - 1), pixels);
                listPixels.add(neighbour);

            } while (!neighbour.equals(first));
        }

//        for (int i = 0; i < listPixels.size(); i++) {
//            Pixel p = listPixels.get(i);
//            pixels[p.getX()][p.getY()] = NEW_VALUE;
//        }

        return listPixels;
    }

    private static Pixel getNeighbour(Pixel pixel, int[][] pixels) {
        int i = pixel.getX();
        int j = pixel.getY();
        int d = pixel.getD();

        int m = (d - 2) % 8;
        if (d == 1) {
            m = 7;
        } else if (d == 0) {
            m = 6;
        }
        for (int x = 0; x < 8; x++) {
            if (pixels[i][j + 1] > THRESHOLD && m == 1) { // 1
                j = j + 1;
                d = 1;
                break;
            } else if (pixels[i + 1][j + 1] > THRESHOLD && m == 2) { // 2
                i = i + 1;
                j = j + 1;
                d = 2;
                break;
            } else if (pixels[i + 1][j] > THRESHOLD && m == 3) { // 3
                i = i + 1;
                d = 3;
                break;
            } else if (pixels[i + 1][j - 1] > THRESHOLD && m == 4) { // 4
                i = i + 1;
                j = j - 1;
                d = 4;
                break;
            } else if (pixels[i][j - 1] > THRESHOLD && m == 5) { // 5
                j = j - 1;
                d = 5;
                break;
            } else if (pixels[i - 1][j - 1] > THRESHOLD && m == 6) { // 6
                i = i - 1;
                j = j - 1;
                d = 6;
                break;
            } else if (pixels[i - 1][j] > THRESHOLD && m == 7) { // 7
                i = i - 1;
                d = 7;
                break;
            } else if (pixels[i - 1][j + 1] > THRESHOLD && m == 0) { // 8
                i = i - 1;
                j = j + 1;
                d = 0;
                break;
            }

            m = (m + 1) % 8;
        }
//        System.out.println("new pixel(i,j)=" + i + "," + j);
        return new Pixel(i, j, d);
    }
}


