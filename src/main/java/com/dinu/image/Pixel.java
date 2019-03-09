/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.image;

/**
 *
 * @author Dinu
 */
public class Pixel {

    private int x, y;
    private int d;

    public Pixel(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    public Pixel(int x, int y, int d) {
        super();
        this.x = x;
        this.y = y;
        this.d = d;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pixel) {
            Pixel newObj = (Pixel) obj;
//            System.out.println("x,y=" + x + "," + y);
//            System.out.println("x,y'=" + newObj.getX() + "," + newObj.getY());

            if (x == newObj.getX() && y == newObj.getY()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
