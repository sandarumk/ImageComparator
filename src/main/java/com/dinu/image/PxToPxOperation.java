/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.image;

import com.dinu.model.PxToPxResult;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author Dinu
 */
public class PxToPxOperation {

    // Rotate left
    public static PxToPxResult apply(BufferedImage design, BufferedImage actual) {
        PxToPxResult result = new PxToPxResult();
        int dw = design.getWidth();
        int dh = design.getHeight();

        int aw = actual.getWidth();
        int ah = actual.getHeight();
        
        result.modified = ImageUtil.duplicateImage(actual);

        if (dw != aw || dh != ah) {
            result.setMsg("Design and Actual images have different image resolutions");
            result.totalDesign = dw * dh;
            result.totalActual = aw * ah;
            result.difference = result.totalDesign;
            return result;
        } else {
            for (int i = 0; i < dw; i++) {
                for (int j = 0; j < dh; j++) {
                    result.totalActual += 1;
                    result.totalDesign += 1;
                    if (design.getRGB(i, j) == actual.getRGB(i, j)) {
                        result.similar += 1;
                    } else {
                        result.difference += 1;
                        result.modified.setRGB(i, j, Color.RED.getRGB());
                    }
                }
            }
        }
        System.out.println("Comparing px to px is successful");
        return result;
    }

    private static int getPixelToPixelScore(BufferedImage design, BufferedImage actual) {
        int score = 0;
        int differences = 0;
        for (int i = 0; i < design.getWidth(); i++) {
            for (int j = 0; j < design.getHeight(); j++) {
                if (design.getRGB(i, j) != actual.getRGB(i, j)) {
                    differences += 1;
                }
            }
        }
        score = differences * 100 / (design.getWidth()*design.getHeight()) ;
        return score;
    }
}
