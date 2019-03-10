/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.vision;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dinu
 */
public class VisionProject {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            CompareFrameNew compareFrame = new CompareFrameNew();
            compareFrame.setVisible(true);
        } catch (Exception ex) {
            Logger.getLogger(VisionProject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
