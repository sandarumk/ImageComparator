/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.model;

import com.dinu.image.BlobFinder;

/**
 *
 * @author dinu
 */
public class Matches {

    BlobFinder.Blob blob1;
    BlobFinder.Blob blob2;

    public BlobFinder.Blob getBlob1() {
        return blob1;
    }

    public BlobFinder.Blob getBlob2() {
        return blob2;
    }

    public void setBlob1(BlobFinder.Blob blob1) {
        this.blob1 = blob1;
    }

    public void setBlob2(BlobFinder.Blob blob2) {
        this.blob2 = blob2;
    }

    public Matches(BlobFinder.Blob blob1, BlobFinder.Blob blob2) {
        this.blob1 = blob1;
        this.blob2 = blob2;
    }

    public Matches() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n---\n");
        sb.append(blob1.toString());
        sb.append("\n");
        sb.append(blob2.toString());
        sb.append("\n---");
        return sb.toString();
    }
}
