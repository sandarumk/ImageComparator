package com.dinu.vision;

import com.dinu.image.BlobFinder;
import com.dinu.image.BlobUtil;

import static org.junit.Assert.*;

/**
 * @author Dinu
 */
public class CompareFrameTest {

    @org.junit.Test
    public void meanShiftDistance() {
//        ---
//                X:  350 ->  717, Y: 1698 -> 1731, mass:   9737
//        X:   82 -> 1021, Y: 1641 -> 1671, mass:  29140
//                --- dis=251.98677213959732 class=3
//
//                ---
//                X:   62 -> 1014, Y: 1647 -> 1682, mass:  33841
//        X:  161 ->  228, Y: 1816 -> 1851, mass:   2448
//                --- dis=488.5734335798458 class=4

        BlobFinder.Blob blob1 = new BlobFinder.Blob(350, 717, 1698, 1731, 9737);
        BlobFinder.Blob blob2 = new BlobFinder.Blob(82, 1021, 1641, 1671, 29140);
        BlobFinder.Blob blob3 = new BlobFinder.Blob(62, 1014, 1647, 1682, 33841);
        BlobFinder.Blob blob4 = new BlobFinder.Blob(161, 228, 1816, 1851, 2448);

        System.out.println(String.format("b1,b2: %f", BlobUtil.meanShiftDistance(blob1, blob2)));
        System.out.println(String.format("b2,b4: %f", BlobUtil.meanShiftDistance(blob2, blob4)));
        System.out.println(String.format("b2,b3: %f", BlobUtil.meanShiftDistance(blob2, blob3)));
    }
}