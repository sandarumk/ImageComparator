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
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageViewer {

    BufferedImage image;
    private Image scaledImage;
    private JPanel panel;
    private int imageWidth = 0;
    private int imageHeight = 0;

    public ImageViewer(BufferedImage bufferedImage, JPanel panel) {
        this.panel = panel;
        this.image = bufferedImage;
        imageWidth = image.getWidth();
        imageHeight = image.getHeight();
        setScaledImage();
        addToPanel();
    }

    private void addToPanel() {
        JLabel jLabel = new JLabel();
        jLabel.setSize(panel.getWidth(), panel.getHeight());
        ImageIcon icon = new ImageIcon(scaledImage);
        jLabel.setIcon(icon);

        panel.removeAll();
        panel.add(jLabel);
    }

    private void setScaledImage() {
        if (image != null) {
            //use floats so division below won't round
            float iw = imageWidth;
            float ih = imageHeight;
            float pw = panel.getWidth();   //panel width
            float ph = panel.getHeight();  //panel height

            if (pw < iw || ph < ih) {

                /* compare some ratios and then decide which side of image to anchor to panel
                 and scale the other side
                 (this is all based on empirical observations and not at all grounded in theory)*/

                //System.out.println("pw/ph=" + pw/ph + ", iw/ih=" + iw/ih);

                if ((pw / ph) > (iw / ih)) {
                    iw = -1;
                    ih = ph;
                } else {
                    iw = pw;
                    ih = -1;
                }

                //prevent errors if panel is 0 wide or high
                if (iw == 0) {
                    iw = -1;
                }
                if (ih == 0) {
                    ih = -1;
                }

                scaledImage = image.getScaledInstance(
                        new Float(iw).intValue(), new Float(ih).intValue(), Image.SCALE_DEFAULT);

            } else {
                scaledImage = image;
            }


        }
    }
//    static public void main(String args[]) throws
//            Exception {
//        JFrame frame = new JFrame("Display image");
//        JPanel panel = new ImageViewer();
//        frame.getContentPane().add(panel);
//        frame.setSize(500, 500);
//        frame.setVisible(true);
//    }
}
