/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 *
 * @author Dinu
 */
public class Representation {

    public static int NEW_VALUE = 255;
    public static String chainCode;

    public static BufferedImage chainCodes(BufferedImage bi) {
        bi = PointOperation.averageGrayscale(bi);

        int[][] intensities = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.RED);

        // apply mask for each channel
        List<Pixel> listModified = Segmentation.applyContourTracking(intensities);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < listModified.size(); i++) {
            Pixel p = listModified.get(i);
            bi.setRGB(p.getY(), p.getX(), ImageUtil.colorToRGB(255, NEW_VALUE, 0, 0));
            int d = p.getD();
            d = 9 - d;
            if (d == 9) {
                d = 1;
            } else if (d == 8) {
                d = 0;
            }
            sb.append(d);
        }

        chainCode = sb.toString();

        System.out.println("applied contour tracking");
        return bi;
    }

    public static String normalizeStartingPoint(String chainCode) {
        ArrayList<Character> a = new ArrayList<Character>(chainCode.length());

        char[] chars = chainCode.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            a.add(chars[i]);
        }
        String smallest = getString(a);
        for (int i = 0; i < a.size(); i++) {
            a.add(a.remove(0));

            double val1 = Double.valueOf(smallest);
            String s = getString(a);
            double val2 = Double.valueOf(s);

            if (val2 < val1) {
                smallest = s;
            }

        }


        return smallest;
    }

    private static String getString(ArrayList<Character> list) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            result.append(list.get(i));
        }

        return result.toString();
    }

    public static String normalizeRotation(String chainCode) {
        if (chainCode.length() >= 2) {
            ArrayList<String> a = new ArrayList<String>(chainCode.length());

            char[] chars = chainCode.toCharArray();

            for (int i = 0; i < chars.length; i++) {
                a.add(String.valueOf(chars[i]));
            }

            ArrayList<String> b = new ArrayList<String>(a);
            b.add(0, b.remove(b.size() - 1));


            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < a.size(); i++) {
                int val = Integer.valueOf(a.get(i)) - Integer.valueOf(b.get(i));
                if (val < 0) {
                    val += 8;
                }
                sb.append(String.valueOf(val));
            }

            return sb.toString();
        } else {
            return chainCode;
        }
    }

    public static double calculatePerimeter(String chainCode) {

        double result = 0.0;

        for (int i = 0; i < chainCode.length(); i++) {
            int val = Integer.valueOf(chainCode.substring(i, i + 1));

            if (val == 0 || val == 2 || val == 4 || val == 6) {
                result += 1.0;
            } else if (val == 1 || val == 3 || val == 5 || val == 7) {
                result += Math.sqrt(2.0);
            }

        }

        return result;
    }

    public static double calculateArea(String chainCode) {
        double dB = 0;
        double dA = 0;
        double B = 0;
        double A = 0;
        double C = 0;

        for (int i = 0; i < chainCode.length(); i++) {
            int val = Integer.valueOf(chainCode.substring(i, i + 1));

            if (val == 0) {
                dB = 0;
                C = 1;
            } else if (val == 1) {
                dB = 1;
                C = 1;
            } else if (val == 7) {
                dB = -1;
                C = 1;
            } else if (val == 3) {
                dB = 1;
                C = -1;
            } else if (val == 4) {
                dB = 0;
                C = -1;
            } else if (val == 5) {
                dB = -1;
                C = -1;
            } else if (val == 2) {
                dB = 1;
                C = 0;
            } else if (val == 6) {
                dB = -1;
                C = 0;
            }

            dA = C * (B + (dB / 2));
            B += dB;
            A += dA;
        }

        return A;
    }

    public static double calculateCompactness(double perimeter, double area) {
        return perimeter * perimeter / (4 * Math.PI * area);
    }

    public static void main(String[] args) {
        String s1 = "076454221";
        String s2 = normalizeRotation(s1);
        System.out.println(s1);
        System.out.println(s2);

        System.out.println("Perimeter=" + calculatePerimeter(s1));
        System.out.println("Area=" + calculateArea(s1));

    }

    /**
     * Applies run code
     *
     * @param bi Buffered image to run codes
     * @return area of the run codes
     */
    public static long applyRunCodes(BufferedImage bi) {
        final int threshold = 100;
        final int maxIntensity = 255;

        int width = bi.getWidth();
        int height = bi.getHeight();

        int[][] intensities = ImageUtil.convertToMatrix(bi, ImageUtil.IntensityModel.RED);

        long area = 0;
        for (int y = 0; y < height; y++) {
            int startX = -1;
            int endX = -1;
            for (int x = 0; x < width; x++) {
                if (intensities[y][x] > threshold) {
                    if (startX < 0) {
                        startX = x;
                    } else {
                        endX = x;
                    }
                } else {
                    if (startX > 0 && endX > 0) {
                        for (int i = startX + 1; i < endX; i++) {

                            bi.setRGB(i, y, Color.RED.getRGB());
                            ++area;
                        }
                        bi.setRGB(startX, y, Color.BLUE.getRGB());
                        bi.setRGB(endX, y, Color.BLUE.getRGB());
                        
                        startX = -1;
                        endX = -1;
                    }
                }

            }
        }

        System.out.println("area=" + area);
        return area;
    }
}
