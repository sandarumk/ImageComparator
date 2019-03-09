/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Dinu
 */
public class ImageStorage {

    public static boolean saveImage(BufferedImage bufferedImage,String filepath, String type) {
        try {
            File outputfile = new File(filepath);
            ImageIO.write(bufferedImage, type, outputfile);
            System.out.println("Image saved:" + filepath);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ImageStorage.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public static BufferedImage openImage(String filepath) {
        BufferedImage image = null;
        try {
            String imageName = filepath;
            File input = new File(imageName);
            image = ImageIO.read(input);
            System.out.println("Image opened:" + filepath);
        } catch (IOException ex) {
            Logger.getLogger(ImageStorage.class.getName()).log(Level.SEVERE, null, ex);
        }

        return image;
    }
}
