/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.image;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author Dinu
 */
public class EdgeDetection {

//    public static int[][] maskSobelX = {
//        {-1, 0, 1},
//        {-2, 0, 2},
//        {-1, 0, 1}};
//    public static int[][] maskSobelY = {
//        {1, 2, 1},
//        {0, 0, 0},
//        {-1, -2, -1}};
    
    public static int[][] maskSobelX = {
        {1, 0, -1},
        {2, 0, -2},
        {1, 0, -1}};
    public static int[][] maskSobelY = {
        {1, 2, 1},
        {0, 0, 0},
        {-1, -2, -1}};
    public static int[][] maskLaplacian = {
        {0, 1, 0},
        {1, -4, 1},
        {0, 1, 0}};
    public static int[][][] maskKirsh = {
        {{-3, -3, 5},
            {-3, 0, 5},
            {-3, -3, 5}},
        {{-3, 5, 5},
            {-3, 0, 5},
            {-3, -3, -3}},
        {{5, 5, 5},
            {-3, 0, -3},
            {-3, -3, -3}},
        {{5, 5, -3},
            {5, 0, -3},
            {-3, -3, -3}},
        {{5, -3, -3},
            {5, 0, -3},
            {5, -3, -3}},
        {{-3, -3, -3},
            {5, 0, -3},
            {5, 5, -3}},
        {{-3, -3, -3},
            {-3, 0, -3},
            {5, 5, 5}},
        {{-3, -3, -3},
            {-3, 0, 5},
            {-3, 5, 5}},};

    // Apply sobel edge detection
    public static BufferedImage applyEdgeDetection(BufferedImage bi, int[][] mask) {
        // Get intenstiry matrix for all channels

        bi = PointOperation.averageGrayscale(bi);

        int[][] intensities = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.RED);

        // apply mask for each channel
        int[][] modifiedIntensties = applyMask(intensities, mask);
        System.out.println("finished insentities");

        // create new image with filtered intensity values
        BufferedImage modified = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        for (int i = 0; i < modified.getHeight(); i++) {
            for (int j = 0; j < modified.getWidth(); j++) {
                modified.setRGB(j, i, ImageUtil.colorToRGB(new Color(bi.getRGB(j, i)).getAlpha(),
                        modifiedIntensties[i][j],
                        modifiedIntensties[i][j],
                        modifiedIntensties[i][j]));
            }
        }
        System.out.println("applied edge detection");
        return modified;
    }

    // Apply sobel edge detection
    public static BufferedImage applySobelBoth(BufferedImage bi) {
        // Get intenstiry matrix for all channels

        bi = PointOperation.averageGrayscale(bi);

        int[][] intensities = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.RED);

        // apply mask for each channel
        int[][] modifiedIntenstiesX = applyMask(intensities, maskSobelX);
        int[][] modifiedIntenstiesY = applyMask(intensities, maskSobelY);
        System.out.println("finished insentities");

        // create new image with filtered intensity values
        BufferedImage modified = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        for (int i = 0; i < modified.getHeight(); i++) {
            for (int j = 0; j < modified.getWidth(); j++) {
                int newIntensity = (int) Math.sqrt(modifiedIntenstiesX[i][j] * modifiedIntenstiesX[i][j] + modifiedIntenstiesY[i][j] * modifiedIntenstiesY[i][j]);
                modified.setRGB(j, i, ImageUtil.colorToRGB(new Color(bi.getRGB(j, i)).getAlpha(),
                        newIntensity,
                        newIntensity,
                        newIntensity));
            }
        }
        System.out.println("applied Sobel");
        return modified;
    }

    public static BufferedImage applyThreshold(BufferedImage bi, int threshold) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        BufferedImage modified = new BufferedImage(w, h, bi.getType());
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Color color = new Color(bi.getRGB(i, j));
                int r = 0, g = 0, b = 0;
                if (color.getRed() > threshold) {
                    r = 255;
                }else{
                    r = 0;
                }
                if (color.getGreen() > threshold) {
                    g = 255;
                }else{
                    g = 0;
                }
                if (color.getBlue() > threshold) {
                    b = 255;
                }else{
                    b = 0;
                }

                modified.setRGB(i, j, ImageUtil.colorToRGB(color.getAlpha(), r, g, b));
            }
        }
        System.out.println("Image threshold applied");
        return modified;


    }

    private static int[][] applyMask(int[][] image, int[][] mask) {
        int height = image.length;
        int width = image[0].length;
        int mid = (int) Math.floor(mask.length / 2);
        int[][] result = new int[height][width];

        int total = 0;
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[0].length; j++) {
                total += mask[i][j];
            }
        }

        for (int i = mid; i < height - mid; i++) {
            for (int j = mid; j < width - mid; j++) {
                int value = 0;
                for (int mi = -mid; mi <= mid; mi++) {
                    for (int mj = -mid; mj <= mid; mj++) {
                        value = value + (image[i + mi][j + mj] * mask[mid + mi][mid + mj]);
                    }
                }
                result[i][j] = Math.abs(value);
            }
        }
        return result;
    }

    // Apply sobel edge detection
    public static BufferedImage applyKirsch(BufferedImage bi) {
        // Get intenstiry matrix for all channels

        bi = PointOperation.averageGrayscale(bi);

        int[][] intensities = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.RED);

        // apply mask for each channel
        int[][][] modifiedIntensties = new int[maskKirsh.length][bi.getHeight()][bi.getWidth()];

        for (int i = 0; i < maskKirsh.length; i++) {
            modifiedIntensties[i] = applyMask(intensities, maskKirsh[i]);
            System.out.println("finished Kirsh template " + (i+1));
        }      

        // create new image with filtered intensity values
        BufferedImage modified = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        for (int i = 0; i < modified.getHeight(); i++) {
            for (int j = 0; j < modified.getWidth(); j++) {
                int max = modifiedIntensties[0][i][j];
                for (int x = 1; x < modifiedIntensties.length; x++) {
                    if (modifiedIntensties[x][i][j] > max) {
                        max = modifiedIntensties[x][i][j];
                    }
                }

                modified.setRGB(j, i, ImageUtil.colorToRGB(new Color(bi.getRGB(j, i)).getAlpha(),
                        max,
                        max,
                        max));
            }
        }
        System.out.println("applied Kirsh edge detection");
        return modified;
    }
}
