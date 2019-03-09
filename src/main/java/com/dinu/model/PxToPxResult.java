/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.model;

import java.awt.image.BufferedImage;

/**
 * @author Dinu
 */
public class PxToPxResult {
    public int totalDesign;
    public int totalActual;
    public int similar;
    public int difference;
    public BufferedImage modified;
    private String msg;

    public int getTotalDesign() {
        return totalDesign;
    }

    public void setTotalDesign(int totalDesign) {
        this.totalDesign = totalDesign;
    }

    public int getTotalActual() {
        return totalActual;
    }

    public void setTotalActual(int totalActual) {
        this.totalActual = totalActual;
    }

    public int getSimilar() {
        return similar;
    }

    public void setSimilar(int similar) {
        this.similar = similar;
    }

    public int getDifference() {
        return difference;
    }

    public void setDifference(int difference) {
        this.difference = difference;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    
    
    
}
