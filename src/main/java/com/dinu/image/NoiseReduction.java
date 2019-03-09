/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Dinu
 */
public class NoiseReduction {

    public static int[][] maskMean = {
        {1, 1, 1},
        {1, 1, 1},
        {1, 1, 1}};
    public static int[][] mask3 = {
        {1, 1, 1},
        {1, 2, 1},
        {1, 1, 1}};
    // gaussian mask
    public static int[][] mask5 = {
        {1, 2, 3, 2, 1},
        {2, 7, 11, 7, 2},
        {3, 11, 17, 11, 3},
        {2, 7, 11, 7, 2},
        {1, 2, 3, 2, 1}};
    public static int[][] maskMedianHorizontal = {
        {0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0},
        {5, 5, 5, 5, 5},
        {0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0}};

    public enum MedianModel {

        MIN, MAX, MID;
    }

    // Apply nxn mean filtering. nxn mask is defined at the begining
    public static BufferedImage filterMean(BufferedImage bi, int[][] mask) {
        // Get intenstiry matrix for all channels
        int[][] reds = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.RED);
        int[][] greens = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.GREEN);
        int[][] blues = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.BLUE);

        // apply mask for each channel
        int[][] modifiedReds = applyMask(reds, mask);
        System.out.println("finished red");
        int[][] modifiedGreens = applyMask(greens, mask);
        System.out.println("finished green");
        int[][] modifiedBlues = applyMask(blues, mask);
        System.out.println("finished blue");

        // create new image with filtered intensity values
        BufferedImage modified = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        for (int i = 0; i < modified.getHeight(); i++) {
            for (int j = 0; j < modified.getWidth(); j++) {
                modified.setRGB(j, i, colorToRGB(new Color(bi.getRGB(j, i)).getAlpha(),
                        modifiedReds[i][j],
                        modifiedGreens[i][j],
                        modifiedBlues[i][j]));
            }
        }
        System.out.println("Mean Filtering applied");
        return modified;
    }

    // Apply Thresholding average
    public static BufferedImage filterThreashold(BufferedImage bi, int threshold) {
        // Get intenstiry matrix for all channels
        int[][] reds = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.RED);
        int[][] greens = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.GREEN);
        int[][] blues = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.BLUE);

        // apply mask for each channel
        int[][] modifiedReds = applyMask(reds, maskMean);
        System.out.println("finished red");
        int[][] modifiedGreens = applyMask(greens, maskMean);
        System.out.println("finished green");
        int[][] modifiedBlues = applyMask(blues, maskMean);
        System.out.println("finished blue");

        // create new image with filtered intensity values
        BufferedImage modified = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        for (int i = 0; i < modified.getHeight(); i++) {
            for (int j = 0; j < modified.getWidth(); j++) {
                Color c = new Color(bi.getRGB(j, i));
                if (Math.abs(c.getRed() - modifiedReds[i][j]) < threshold
                        && Math.abs(c.getGreen() - modifiedGreens[i][j]) < threshold
                        && Math.abs(c.getBlue() - modifiedBlues[i][j]) < threshold) {
                    modified.setRGB(j, i, colorToRGB(c.getAlpha(),
                            modifiedReds[i][j],
                            modifiedGreens[i][j],
                            modifiedBlues[i][j]));
                } else {
                    modified.setRGB(j, i, c.getRGB());
                }
            }
        }
        System.out.println("Thresholding average applied");
        return modified;
    }

    // Apply Median filter
    public static BufferedImage filterMedian(BufferedImage bi, int[][] mask) {
        // Get intenstiry matrix for all channels
        int[][] reds = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.RED);
        int[][] greens = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.GREEN);
        int[][] blues = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.BLUE);

        // apply mask for each channel
        int[][] modifiedReds = applyMedian(reds, mask);
        System.out.println("finished red");
        int[][] modifiedGreens = applyMedian(greens, mask);
        System.out.println("finished green");
        int[][] modifiedBlues = applyMedian(blues, mask);
        System.out.println("finished blue");

        // create new image with filtered intensity values
        BufferedImage modified = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        for (int i = 0; i < modified.getHeight(); i++) {
            for (int j = 0; j < modified.getWidth(); j++) {
                modified.setRGB(j, i, colorToRGB(new Color(bi.getRGB(j, i)).getAlpha(),
                        modifiedReds[i][j],
                        modifiedGreens[i][j],
                        modifiedBlues[i][j]));
            }
        }
        System.out.println("Median Filtering applied");
        return modified;
    }

    // Apply Median filter other
    public static BufferedImage filterMedian(BufferedImage bi, int[][] mask, MedianModel model) {
        // Get intenstiry matrix for all channels
        int[][] reds = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.RED);
        int[][] greens = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.GREEN);
        int[][] blues = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.BLUE);

        // apply mask for each channel
        int[][] modifiedReds = applyOther(reds, mask, model);
        System.out.println("finished red");
        int[][] modifiedGreens = applyOther(greens, mask, model);
        System.out.println("finished green");
        int[][] modifiedBlues = applyOther(blues, mask, model);
        System.out.println("finished blue");

        // create new image with filtered intensity values
        BufferedImage modified = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        for (int i = 0; i < modified.getHeight(); i++) {
            for (int j = 0; j < modified.getWidth(); j++) {
                modified.setRGB(j, i, colorToRGB(new Color(bi.getRGB(j, i)).getAlpha(),
                        modifiedReds[i][j],
                        modifiedGreens[i][j],
                        modifiedBlues[i][j]));
            }
        }
        System.out.println("Median " + model + " Filtering applied");
        return modified;
    }

    private static int[][] applyOther(int[][] image, int[][] mask, MedianModel model) {
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
                ArrayList<Integer> list = new ArrayList<Integer>();

                for (int mi = -mid; mi <= mid; mi++) {
                    for (int mj = -mid; mj <= mid; mj++) {
                        for (int x = 0; x < mask[mid + mi][mid + mj]; x++) {
                            list.add(image[i + mi][j + mj]);
                        }
                    }
                }
                Collections.sort(list);
                if (model == MedianModel.MAX) {
                    result[i][j] = list.get(list.size() - 1);
                } else if (model == MedianModel.MIN) {
                    result[i][j] = list.get(0);
                } else if (model == MedianModel.MID) {
                    result[i][j] = (list.get(0) + list.get(list.size() - 1)) / 2;
                } else {
                    result[i][j] = list.get((total + 1) / 2);
                }
            }
        }
        return result;
    }

    // Apply Alpha trimmed
    public static BufferedImage filterAlphaTrimmed(BufferedImage bi, int p) {

        int[][] mask = maskMean;

        // Get intenstiry matrix for all channels
        int[][] reds = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.RED);
        int[][] greens = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.GREEN);
        int[][] blues = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.BLUE);

        // apply mask for each channel
        int[][] modifiedReds = applyAlphaTrimmed(reds, mask, p);
        System.out.println("finished red");
        int[][] modifiedGreens = applyAlphaTrimmed(greens, mask, p);
        System.out.println("finished green");
        int[][] modifiedBlues = applyAlphaTrimmed(blues, mask, p);
        System.out.println("finished blue");

        // create new image with filtered intensity values
        BufferedImage modified = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
        for (int i = 0; i < modified.getHeight(); i++) {
            for (int j = 0; j < modified.getWidth(); j++) {
                modified.setRGB(j, i, colorToRGB(new Color(bi.getRGB(j, i)).getAlpha(),
                        modifiedReds[i][j],
                        modifiedGreens[i][j],
                        modifiedBlues[i][j]));
            }
        }
        System.out.println("Alpha trimmed filtering applied");
        return modified;
    }

    private static int[][] applyAlphaTrimmed(int[][] image, int[][] mask, int p) {
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

        if (p > ((total - 1) / 2)) {
            p = (total - 1) / 2;
        }

        for (int i = mid; i < height - mid; i++) {
            for (int j = mid; j < width - mid; j++) {
                ArrayList<Integer> list = new ArrayList<Integer>();

                for (int mi = -mid; mi <= mid; mi++) {
                    for (int mj = -mid; mj <= mid; mj++) {
                        for (int x = 0; x < mask[mid + mi][mid + mj]; x++) {
                            list.add(image[i + mi][j + mj]);
                        }
                    }
                }
                Collections.sort(list);
                int totalValue = 0;
                for (int x = p; x < total - p; x++) {
                    totalValue += list.get(x);
                }
                result[i][j] = totalValue/(total-(2*p));
            }
        }
        return result;
    }

    private static int[][] applyMedian(int[][] image, int[][] mask) {
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
                ArrayList<Integer> list = new ArrayList<Integer>();

                for (int mi = -mid; mi <= mid; mi++) {
                    for (int mj = -mid; mj <= mid; mj++) {
                        for (int x = 0; x < mask[mid + mi][mid + mj]; x++) {
                            list.add(image[i + mi][j + mj]);
                        }
                    }
                }
                Collections.sort(list);
                result[i][j] = list.get((total + 1) / 2);
            }
        }
        return result;
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
                result[i][j] = value / total;
            }
        }
        return result;
    }

    // Convert R, G, B, Alpha to standard 8 bit
    private static int colorToRGB(int alpha, int red, int green, int blue) {
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
