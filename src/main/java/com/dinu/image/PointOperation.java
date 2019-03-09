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
public class PointOperation {

    // Rotate left
    public static BufferedImage transpose(BufferedImage bi) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        BufferedImage modified = new BufferedImage(h, w, bi.getType());
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                modified.setRGB(i, w - 1 - j, bi.getRGB(j, i));
            }
        }
        System.out.println("Image transpose successful");
        return modified;
    }

    // Flip vertically
    public static BufferedImage flipVertical(BufferedImage bi) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        BufferedImage modified = new BufferedImage(w, h, bi.getType());
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                modified.setRGB(i, h - 1 - j, bi.getRGB(i, j));
            }
        }
        System.out.println("Image flip vertical successful");
        return modified;
    }

    // Crop from fixed starting point with given width and height
    public static BufferedImage crop(BufferedImage bi, int x, int y, int width, int height) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        BufferedImage modified = new BufferedImage(width, height, bi.getType());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                modified.setRGB(i, j, bi.getRGB(x + i, y + j));
            }
        }
        System.out.println("Image crop successful");
        return modified;
    }

    // digital image negative
    public static BufferedImage negative(BufferedImage bi) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        BufferedImage modified = new BufferedImage(w, h, bi.getType());
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Color color = getNegative(new Color(bi.getRGB(i, j)));
                modified.setRGB(i, j, color.getRGB());
            }
        }
        System.out.println("Image negative successful");
        return modified;
    }

    private static Color getNegative(Color color) {
        return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
    }

    // Normalize the image (i-min)*255/(max-min)
    public static BufferedImage normalize(BufferedImage bi) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        BufferedImage modified = new BufferedImage(w, h, bi.getType());
        int[][] minMax = getMinMax(bi);

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Color color = getNormalize(new Color(bi.getRGB(i, j)), minMax);
                modified.setRGB(i, j, color.getRGB());
            }
        }
        System.out.println("Image normalize successful");
        return modified;
    }

    private static Color getNormalize(Color color, int[][] minmax) {
        return new Color(
                (color.getRed() - minmax[0][0]) * 255 / (minmax[0][1] - minmax[0][0]),
                (color.getGreen() - minmax[1][0]) * 255 / (minmax[1][1] - minmax[1][0]),
                (color.getBlue() - minmax[2][0]) * 255 / (minmax[2][1] - minmax[2][0]),
                color.getAlpha());
    }

    // calculate overall minimum and maximum of the image in each chanel
    private static int[][] getMinMax(BufferedImage bi) {
        // [R,G,B][min,max]
        int[][] minMax = new int[3][2];

        for (int i = 0; i < 3; i++) {
            minMax[i][0] = 255;
            minMax[i][1] = 0;
        }
        for (int i = 0; i < bi.getWidth(); i++) {
            for (int j = 0; j < bi.getHeight(); j++) {
                Color color = new Color(bi.getRGB(i, j));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

                // calculate minimum values
                if (r < minMax[0][0]) {
                    minMax[0][0] = r;
                }
                if (g < minMax[1][0]) {
                    minMax[1][0] = g;
                }
                if (b < minMax[2][0]) {
                    minMax[2][0] = b;
                }

                // calculate maximum values
                if (r > minMax[0][1]) {
                    minMax[0][1] = r;
                }
                if (g > minMax[1][1]) {
                    minMax[1][1] = g;
                }
                if (b > minMax[2][1]) {
                    minMax[2][1] = b;
                }
            }
        }
        return minMax;
    }

    // The average grayscale method
    public static BufferedImage averageGrayscale(BufferedImage original) {

        int alpha, red, green, blue;
        int newPixel;

        BufferedImage avg_gray = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        // pre caluculation for faster averaging
        int[] avgLUT = new int[766];
        for (int i = 0; i < avgLUT.length; i++) {
            avgLUT[i] = (int) (i / 3);
        }

        for (int i = 0; i < original.getWidth(); i++) {
            for (int j = 0; j < original.getHeight(); j++) {

                // Get pixels by R, G, B
                alpha = new Color(original.getRGB(i, j)).getAlpha();
                red = new Color(original.getRGB(i, j)).getRed();
                green = new Color(original.getRGB(i, j)).getGreen();
                blue = new Color(original.getRGB(i, j)).getBlue();

                newPixel = red + green + blue;
                newPixel = avgLUT[newPixel];
                // Return back to original format
                newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);

                // Write pixels into image
                avg_gray.setRGB(i, j, newPixel);

            }
        }
        System.out.println("Image grayscale successful");
        return avg_gray;
    }

    // Convert R, G, B, Alpha to standard 8 bit
    public static int colorToRGB(int alpha, int red, int green, int blue) {
        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;
        return newPixel;
    }
}
