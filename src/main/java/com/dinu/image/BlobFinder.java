/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dinu.image;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Dinu
 */
public class BlobFinder {

    private byte[][] COLOUR_ARRAY = {
            {(byte) 103, (byte) 121, (byte) 255},
            {(byte) 249, (byte) 255, (byte) 139},
            {(byte) 140, (byte) 255, (byte) 127},
            {(byte) 167, (byte) 254, (byte) 255},
            {(byte) 255, (byte) 111, (byte) 71}
    };

    private int width;
    private int height;

    private int[] labelBuffer;

    private int[] labelTable;
    private int[] xMinTable;
    private int[] xMaxTable;
    private int[] yMinTable;
    private int[] yMaxTable;
    private int[] massTable;

    private BufferedImage srcImage;
    private BufferedImage dstImage;

    public static class Blob {

        public int xMin;
        public int xMax;
        public int yMin;
        public int yMax;
        public int mass;

        public Blob(int xMin, int xMax, int yMin, int yMax, int mass) {
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
            this.mass = mass;
        }
    }

    public BlobFinder(int width, int height) {
        this.width = width;
        this.height = height;

        labelBuffer = new int[width * height];

        // The maximum number of blobs is given by an image filled with equally spaced single pixel
        // blobs. For images with less blobs, memory will be wasted, but this approach is simpler and
        // probably quicker than dynamically resizing arrays
        int tableSize = width * height / 4;

        labelTable = new int[tableSize];
        xMinTable = new int[tableSize];
        xMaxTable = new int[tableSize];
        yMinTable = new int[tableSize];
        yMaxTable = new int[tableSize];
        massTable = new int[tableSize];
    }

    public List<Blob> detectBlobs(BufferedImage srcImage, int minBlobMass, int maxBlobMass, byte matchVal, List<Blob> blobList) {

        this.srcImage = srcImage;
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();

        // Get raw image data
        Raster raster = srcImage.getData();
        DataBuffer buffer = raster.getDataBuffer();

        int type = buffer.getDataType();
        if (type != DataBuffer.TYPE_BYTE) {
            System.err.println("Wrong image data type");
            return blobList;
        }
        if (buffer.getNumBanks() != 1) {
            System.err.println("Wrong image data format");
            return blobList;
        }

        DataBufferByte byteBuffer = (DataBufferByte) buffer;
        byte[] srcData = byteBuffer.getData(0);

        // Sanity check image
        if (width * height * 3 != srcData.length) {
            System.err.println("Unexpected image data size. Should be RGB image");
            return blobList;
        }

        // Output Image info
        System.out.printf("Loaded image: '%s', width: %d, height: %d, num bytes: %d\n", "", width, height, srcData.length);

        // Create Monochrome version - using basic threshold technique


        byte[] monochromeData = new byte[width * height];
        int srcPtr = 0;
        int monoPtr = 0;

        while (srcPtr < srcData.length) {
            int val = ((srcData[srcPtr] & 0xFF) + (srcData[srcPtr + 1] & 0xFF) + (srcData[srcPtr + 2] & 0xFF)) / 3;
            monochromeData[monoPtr] = (val > 128) ? (byte) 0xFF : 0;

            srcPtr += 3;
            monoPtr += 1;
        }

        byte[] dstData = new byte[srcData.length];
        srcData = monochromeData;

        if (dstData != null && dstData.length != srcData.length * 3) {
            throw new IllegalArgumentException("Bad array lengths: srcData 1 byte/pixel (mono), dstData 3 bytes/pixel (RGB)");
        }

        // This is the neighbouring pixel pattern. For position X, A, B, C & D are checked
        // A B C
        // D X
        srcPtr = 0;
        int aPtr = -width - 1;
        int bPtr = -width;
        int cPtr = -width + 1;
        int dPtr = -1;

        int label = 1;

        // Iterate through pixels looking for connected regions. Assigning labels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                labelBuffer[srcPtr] = 0;

                // Check if on foreground pixel
                if (srcData[srcPtr] == matchVal) {
                    // Find label for neighbours (0 if out of range)
                    int aLabel = (x > 0 && y > 0) ? labelTable[labelBuffer[aPtr]] : 0;
                    int bLabel = (y > 0) ? labelTable[labelBuffer[bPtr]] : 0;
                    int cLabel = (x < width - 1 && y > 0) ? labelTable[labelBuffer[cPtr]] : 0;
                    int dLabel = (x > 0) ? labelTable[labelBuffer[dPtr]] : 0;

                    // Look for label with least value
                    int min = Integer.MAX_VALUE;
                    if (aLabel != 0 && aLabel < min) {
                        min = aLabel;
                    }
                    if (bLabel != 0 && bLabel < min) {
                        min = bLabel;
                    }
                    if (cLabel != 0 && cLabel < min) {
                        min = cLabel;
                    }
                    if (dLabel != 0 && dLabel < min) {
                        min = dLabel;
                    }

                    // If no neighbours in foreground
                    if (min == Integer.MAX_VALUE) {
                        labelBuffer[srcPtr] = label;
                        labelTable[label] = label;

                        // Initialise min/max x,y for label
                        yMinTable[label] = y;
                        yMaxTable[label] = y;
                        xMinTable[label] = x;
                        xMaxTable[label] = x;
                        massTable[label] = 1;

                        label++;
                    } // Neighbour found
                    else {
                        // Label pixel with lowest label from neighbours
                        labelBuffer[srcPtr] = min;

                        // Update min/max x,y for label
                        yMaxTable[min] = y;
                        massTable[min]++;
                        if (x < xMinTable[min]) {
                            xMinTable[min] = x;
                        }
                        if (x > xMaxTable[min]) {
                            xMaxTable[min] = x;
                        }

                        if (aLabel != 0) {
                            labelTable[aLabel] = min;
                        }
                        if (bLabel != 0) {
                            labelTable[bLabel] = min;
                        }
                        if (cLabel != 0) {
                            labelTable[cLabel] = min;
                        }
                        if (dLabel != 0) {
                            labelTable[dLabel] = min;
                        }
                    }
                }

                srcPtr++;
                aPtr++;
                bPtr++;
                cPtr++;
                dPtr++;
            }
        }

        // Iterate through labels pushing min/max x,y values towards minimum label
        if (blobList == null) {
            blobList = new ArrayList<Blob>();
        }

        for (int i = label - 1; i > 0; i--) {
            if (labelTable[i] != i) {
                if (xMaxTable[i] > xMaxTable[labelTable[i]]) {
                    xMaxTable[labelTable[i]] = xMaxTable[i];
                }
                if (xMinTable[i] < xMinTable[labelTable[i]]) {
                    xMinTable[labelTable[i]] = xMinTable[i];
                }
                if (yMaxTable[i] > yMaxTable[labelTable[i]]) {
                    yMaxTable[labelTable[i]] = yMaxTable[i];
                }
                if (yMinTable[i] < yMinTable[labelTable[i]]) {
                    yMinTable[labelTable[i]] = yMinTable[i];
                }
                massTable[labelTable[i]] += massTable[i];

                int l = i;
                while (l != labelTable[l]) {
                    l = labelTable[l];
                }
                labelTable[i] = l;
            } else {
                // Ignore blobs that butt against corners
                if (i == labelBuffer[0]) {
                    continue;                                    // Top Left
                }
                if (i == labelBuffer[width]) {
                    continue;                                // Top Right
                }
                if (i == labelBuffer[(width * height) - width + 1]) {
                    continue;    // Bottom Left
                }
                if (i == labelBuffer[(width * height) - 1]) {
                    continue;            // Bottom Right
                }
                if (massTable[i] >= minBlobMass && (massTable[i] <= maxBlobMass || maxBlobMass == -1)) {
                    Blob blob = new Blob(xMinTable[i], xMaxTable[i], yMinTable[i], yMaxTable[i], massTable[i]);
                    blobList.add(blob);
                }
            }
        }

        // If dst buffer provided, fill with coloured blobs
        if (dstData != null) {
            for (int i = label - 1; i > 0; i--) {
                if (labelTable[i] != i) {
                    int l = i;
                    while (l != labelTable[l]) {
                        l = labelTable[l];
                    }
                    labelTable[i] = l;
                }
            }

            // Renumber lables into sequential numbers, starting with 0
            int newLabel = 0;
            for (int i = 1; i < label; i++) {
                if (labelTable[i] == i) {
                    labelTable[i] = newLabel++;
                } else {
                    labelTable[i] = labelTable[labelTable[i]];
                }
            }

            srcPtr = 0;
            int dstPtr = 0;
            while (srcPtr < srcData.length) {
                if (srcData[srcPtr] == matchVal) {
                    int c = labelTable[labelBuffer[srcPtr]] % COLOUR_ARRAY.length;
                    dstData[dstPtr] = COLOUR_ARRAY[c][0];
                    dstData[dstPtr + 1] = COLOUR_ARRAY[c][1];
                    dstData[dstPtr + 2] = COLOUR_ARRAY[c][2];
                } else {
                    dstData[dstPtr] = 0;
                    dstData[dstPtr + 1] = 0;
                    dstData[dstPtr + 2] = 0;
                }

                srcPtr++;
                dstPtr += 3;
            }

            {
                DataBufferByte dataBuffer = new DataBufferByte(dstData, dstData.length);
                int[] colOrder = new int[]{2, 1, 0};
                PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, 3, 3 * width, colOrder);

                ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                ColorModel colourModel = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);

                WritableRaster raster2 = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));

                dstImage = new BufferedImage(colourModel, raster2, false, null);
            }
        }

        return blobList;
    }

    public BufferedImage getDstImage() {
        return dstImage;
    }

}
