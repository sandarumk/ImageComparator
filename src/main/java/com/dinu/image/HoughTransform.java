/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.image;

/**
 *
 * @author Dinu
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

public class HoughTransform extends Thread {
    
    public static void applyHoughTransform(BufferedImage image){
        HoughTransform h = new HoughTransform(image.getWidth(), image.getHeight());
        h.addPoints(image);
        Vector<HoughLine> lines = h.getLines(30);
        
        for (int j = 0; j < lines.size(); j++) {
            HoughLine line = lines.elementAt(j);
            line.draw(image, Color.RED.getRGB());
        }
        System.out.println("Hough transform applied");
    }

    
    final int neighbourhoodSize = 4;
    final int maxTheta = 180;
    final double thetaStep = Math.PI / maxTheta;
    protected int width, height;
    protected int[][] houghArray;
    protected float centerX, centerY;
    protected int houghHeight;
    protected int doubleHeight;
    protected int numPoints;
    private double[] sinCache;
    private double[] cosCache;

    public HoughTransform(int width, int height) {
        this.width = width;
        this.height = height;
        init();
    }
            

    public void init() {

        // Calculate the maximum height the hough array needs to have 
        houghHeight = (int) (Math.sqrt(2) * Math.max(height, width)) / 2;

        // Double the height of the hough array to cope with negative r values 
        doubleHeight = 2 * houghHeight;

        // Create the hough array 
        houghArray = new int[maxTheta][doubleHeight];

        // Find edge points and vote in array 
        centerX = width / 2;
        centerY = height / 2;

        // Count how many points there are 
        numPoints = 0;

        // cache the values of sin and cos for faster processing 
        sinCache = new double[maxTheta];
        cosCache = sinCache.clone();
        for (int t = 0; t < maxTheta; t++) {
            double realTheta = t * thetaStep;
            sinCache[t] = Math.sin(realTheta);
            cosCache[t] = Math.cos(realTheta);
        }
    }

    /**
     * Adds points from an image. The image is assumed to be greyscale black and
     * white, so all pixels that are not black are counted as edges. The image
     * should have the same dimensions as the one passed to the constructor.
     */
    public void addPoints(BufferedImage image) {

        // Now find edge points and update the hough array 
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                // Find non-black pixels 
                if ((image.getRGB(x, y) & 0x000000ff) != 0) {
                    addPoint(x, y);
                }
            }
        }
    }

    /**
     * Adds a single point to the hough transform. You can use this method
     * directly if your data isn't represented as a buffered image.
     */
    public void addPoint(int x, int y) {

        // Go through each value of theta 
        for (int t = 0; t < maxTheta; t++) {

            //Work out the r values for each theta step 
            int r = (int) (((x - centerX) * cosCache[t]) + ((y - centerY) * sinCache[t]));

            // this copes with negative values of r 
            r += houghHeight;

            if (r < 0 || r >= doubleHeight) {
                continue;
            }

            // Increment the hough array 
            houghArray[t][r]++;

        }

        numPoints++;
    }

    /**
     * Once points have been added in some way this method extracts the lines
     * and returns them as a Vector of HoughLine objects, which can be used to
     * draw on the
     *
     * @param percentageThreshold The percentage threshold above which lines are
     * determined from the hough array
     */
    public Vector<HoughLine> getLines(int threshold) {

        // Initialise the vector of lines that we'll return 
        Vector<HoughLine> lines = new Vector<HoughLine>(20);

        // Only proceed if the hough array is not empty 
        if (numPoints == 0) {
            return lines;
        }

        // Search for local peaks above threshold to draw 
        for (int t = 0; t < maxTheta; t++) {
            loop:
            for (int r = neighbourhoodSize; r < doubleHeight - neighbourhoodSize; r++) {

                // Only consider points above threshold 
                if (houghArray[t][r] > threshold) {

                    int peak = houghArray[t][r];

                    // Check that this peak is indeed the local maxima 
                    for (int dx = -neighbourhoodSize; dx <= neighbourhoodSize; dx++) {
                        for (int dy = -neighbourhoodSize; dy <= neighbourhoodSize; dy++) {
                            int dt = t + dx;
                            int dr = r + dy;
                            if (dt < 0) {
                                dt = dt + maxTheta;
                            } else if (dt >= maxTheta) {
                                dt = dt - maxTheta;
                            }
                            if (houghArray[dt][dr] > peak) {
                                // found a bigger point nearby, skip 
                                continue loop;
                            }
                        }
                    }

                    // calculate the true value of theta 
                    double theta = t * thetaStep;

                    // add the line to the vector 
                    lines.add(new HoughLine(theta, r));

                }
            }
        }

        return lines;
    }

    /**
     * Gets the highest value in the hough array
     */
    public int getHighestValue() {
        int max = 0;
        for (int t = 0; t < maxTheta; t++) {
            for (int r = 0; r < doubleHeight; r++) {
                if (houghArray[t][r] > max) {
                    max = houghArray[t][r];
                }
            }
        }
        return max;
    }

    /**
     * Gets the hough array as an image, in case you want to have a look at it.
     */
    public BufferedImage getHoughArrayImage() {
        int max = getHighestValue();
        BufferedImage image = new BufferedImage(maxTheta, doubleHeight, BufferedImage.TYPE_INT_ARGB);
        for (int t = 0; t < maxTheta; t++) {
            for (int r = 0; r < doubleHeight; r++) {
                double value = 255 * ((double) houghArray[t][r]) / max;
                int v = 255 - (int) value;
                int c = new Color(v, v, v).getRGB();
                image.setRGB(t, r, c);
            }
        }
        return image;
    }

    class HoughLine {

        protected double theta;
        protected double r;

        /**
         * Initialises the hough line
         */
        public HoughLine(double theta, double r) {
            this.theta = theta;
            this.r = r;
        }

        /**
         * Draws the line on the image of your choice with the RGB colour of
         * your choice.
         */
        public void draw(BufferedImage image, int color) {

            int height = image.getHeight();
            int width = image.getWidth();

            // During processing h_h is doubled so that -ve r values 
            int houghHeight = (int) (Math.sqrt(2) * Math.max(height, width)) / 2;

            // Find edge points and vote in array 
            float centerX = width / 2;
            float centerY = height / 2;

            // Draw edges in output array 
            double tsin = Math.sin(theta);
            double tcos = Math.cos(theta);

            if (theta < Math.PI * 0.25 || theta > Math.PI * 0.75) {
                // Draw vertical-ish lines 
                for (int y = 0; y < height; y++) {
                    int x = (int) ((((r - houghHeight) - ((y - centerY) * tsin)) / tcos) + centerX);
                    if (x < width && x >= 0) {
                        image.setRGB(x, y, color);
                    }
                }
            } else {
                // Draw horizontal-sh lines 
                for (int x = 0; x < width; x++) {
                    int y = (int) ((((r - houghHeight) - ((x - centerX) * tcos)) / tsin) + centerY);
                    if (y < height && y >= 0) {
                        image.setRGB(x, y, color);
                    }
                }
            }
        }
    }
}
