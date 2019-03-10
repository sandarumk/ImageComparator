package com.dinu.image;

import com.dinu.model.Matches;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.hershey.HersheyFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

/**
 * @author Dinu
 */
public class BlobUtil {

    public static MatchingResult getBlobMatching(BufferedImage designImage, BufferedImage actualImage) {
        MatchingResult matchingResult = new MatchingResult();
        ArrayList<BlobFinder.Blob> designBlobList = new ArrayList<>();
        ArrayList<BlobFinder.Blob> actualBlobList = new ArrayList<>();

        matchingResult.setDesignImageBlob(identifyBlobs(designImage, designBlobList));
        matchingResult.setActualImageBlob(identifyBlobs(actualImage, actualBlobList));


        ArrayList<BlobFinder.Blob> designDiffs;
        ArrayList<Matches> matchesList;
        if (!designBlobList.isEmpty()) {
            designDiffs = new ArrayList<>(designBlobList);
            matchesList = BlobUtil.matchBlobs(designDiffs, new ArrayList<>(actualBlobList));

            System.out.println("Diffs List:");
            for (BlobFinder.Blob blob : designDiffs) {
                showRegion(matchingResult.getDesignImageBlob(), blob, Color.RED.getRGB(), -1);
                System.out.println(blob);
            }

            System.out.println("Matched List:");
            int count = 0;
            for (Matches match : matchesList) {
                ++count;
                double dist = BlobUtil.calculateDistance(match.getBlob1(), match.getBlob2());
                System.out.println(match + " dis=" + dist + " class=" + count);
                Random generator = new Random();
                int color = new Color(generator.nextInt(255), generator.nextInt(255), generator.nextInt(255)).getRGB();
                showRegion(designImage, match.getBlob1(), color, count);
                showRegion(actualImage, match.getBlob2(), color, count);
            }

            if (count > 0) {
                matchingResult.setHasResults(true);
            }

            double sumDiff = 0.0;
            int sumMass = 0;
            double maxDiff = 0;
            {
                int width = designImage.getWidth();
                int height = designImage.getHeight();

                //calculate maxDiff using  blob1 as (0,0) and blob2 as (w,h). mass=0
                BlobFinder.Blob blob1 = new BlobFinder.Blob(0, 0, 0, 0, 0);
                BlobFinder.Blob blob2 = new BlobFinder.Blob(width, width, height, height, 0);
                maxDiff = BlobUtil.calculateDistance(blob1, blob2);
            }

            for (Matches match : matchesList) {
                double distanceDifference = BlobUtil.calculateDistance(match.getBlob1(), match.getBlob2());
                int mass = match.getBlob1().mass;
                sumDiff += distanceDifference * mass;
                sumMass += mass;
            }

            for (BlobFinder.Blob blob : designDiffs) {
                int mass = blob.mass;
                sumDiff += maxDiff * mass;
                sumMass += mass;
            }

            double finalPercentage = sumDiff * 100 / (sumMass * maxDiff);
            matchingResult.setSimilarity(100 - finalPercentage);
//            labelBlobScore.setText("" + Math.round(finalPercentage * 100) / 100.0);
            // labelMeanShiftUnmatch.setText("" + Math.round((designBlobList.size() - matchesList.size()) * 10000 / designBlobList.size()) / 100.0);
            // labelMeanShiftMatched.setText("" + Math.round((diffPer) * 100) / 100.0);
        } else {
            System.out.println("No blobs found in design image");
            matchingResult.setHasResults(false);
        }

        return matchingResult;
    }

    public static ArrayList<Matches> matchBlobs(ArrayList<BlobFinder.Blob> designBlobList, ArrayList<BlobFinder.Blob> actualBlobList) {
        double threshold = 5000.0;
//        double massThreshold = 200.0;
        ArrayList<Matches> matchesList = new ArrayList<>();
        if (designBlobList != null && actualBlobList != null) {

            Map<Double, List<Matches>> distanceTable = new HashMap<>();

            for (Iterator<BlobFinder.Blob> iter = designBlobList.iterator(); iter.hasNext(); ) {
                BlobFinder.Blob blob1 = iter.next();
//                if (blob1.mass < massThreshold) {
//                    continue;
//                }

                boolean found = false;
                BlobFinder.Blob closestBlob = null;

                for (BlobFinder.Blob blob2 : actualBlobList) {
                    if (blobEqual(blob1, blob2)) {
                        matchesList.add(new Matches(blob1, blob2));
                        closestBlob = blob2;
                        found = true;
                        break;
                    }
                }
                if (found) {
                    iter.remove();
                    actualBlobList.remove(closestBlob);
                    continue;
                }

                for (BlobFinder.Blob blob2 : actualBlobList) {
//                    if (blob2.mass < massThreshold) {
//                        continue;
//                    }
                    double distance = calculateDistance(blob1, blob2);
                    List<Matches> matches = distanceTable.computeIfAbsent(distance, k -> new ArrayList<>());
                    matches.add(new Matches(blob1, blob2));
                }
            }

            List<Double> distances = new ArrayList<>(distanceTable.keySet());
            Collections.sort(distances);

            for (Double distance : distances) {
                if (distance > threshold) {
                    break;
                }
                List<Matches> matches = distanceTable.get(distance);
                for (Matches match : matches) {
                    if (designBlobList.contains(match.getBlob1()) && actualBlobList.contains(match.getBlob2())) {
                        matchesList.add(match);
                        designBlobList.remove(match.getBlob1());
                        actualBlobList.remove(match.getBlob2());
                    }
                }
            }
        }

        return matchesList;
    }

    private static BufferedImage identifyBlobs(BufferedImage bufImage, ArrayList<BlobFinder.Blob> blobList) {
        BlobFinder blobFinder = new BlobFinder(bufImage.getWidth(), bufImage.getHeight());
        blobFinder.detectBlobs(bufImage, 0, -1, (byte) 0, blobList);
        System.out.println("blob list size:" + blobList.size());
//        for (BlobFinder.Blob blob : blobList) {
//            System.out.println(blob);
//        }
        if (blobList.size() > 0) {
            return blobFinder.getDstImage();
        } else {
            return bufImage;
        }
    }

    private static double euclideanDistance(BlobFinder.Blob blob1, BlobFinder.Blob blob2) {
        return Math.sqrt(Math.pow((blob1.xMax - blob1.xMin) - (blob2.xMax - blob2.xMin), 2)
                + Math.pow((blob1.yMax - blob1.yMin) - (blob2.yMax - blob2.yMin), 2)
                + Math.pow(blob1.mass - blob2.mass, 2));
    }

    private static double meanDistance(BlobFinder.Blob blob1, BlobFinder.Blob blob2) {
        int factorX = 2;
        int factorY = 2;
        int factorM = 1;
        return Math.sqrt((factorX * Math.pow(((blob1.xMax + blob1.xMin) / 2.0) - ((blob2.xMax + blob2.xMin) / 2.0), 2.0)
                + factorY * Math.pow(((blob1.yMax + blob1.yMin) / 2.0) - ((blob2.yMax + blob2.yMin) / 2.0), 2.0)
                + factorM * Math.abs(Math.pow(blob1.mass - blob2.mass, 1))) / (factorX + factorY + factorM));
    }

    private static double meanDistance2(BlobFinder.Blob blob1, BlobFinder.Blob blob2) {
        int factorX = 2;
        int factorY = 2;
        int factorM = 2;
        return Math.sqrt(
                (0
                        + factorX * (Math.pow((blob1.xMin - blob2.xMin), 2) + Math.pow((blob1.xMax - blob2.xMax), 2))
                        + factorY * (Math.pow((blob1.yMin - blob2.yMin), 2) + Math.pow((blob1.yMax - blob2.yMax), 2))
                        + factorM * Math.abs(Math.pow(blob1.mass - blob2.mass, 1))
                ) / (factorX + factorY + factorM));
    }

    public static double calculateDistance(BlobFinder.Blob blob1, BlobFinder.Blob blob2) {
        return meanDistance2(blob1, blob2);
    }

    private static boolean blobEqual(BlobFinder.Blob blob1, BlobFinder.Blob blob2) {
        return blob1.xMin == blob2.xMin && blob1.xMax == blob2.xMax
                && blob1.yMin == blob2.yMin && blob1.yMax == blob2.yMax
                && blob1.mass == blob2.mass;
    }

    public static double layoutDifference(BlobFinder.Blob blob1, BlobFinder.Blob blob2) {
        return Math.sqrt(Math.pow((((blob1.xMin + blob1.xMax) / 2) - ((blob2.xMin + blob2.xMax) / 2)), 2)
                + Math.pow((((blob1.yMin + blob1.yMax) / 2) - ((blob2.yMin + blob2.yMax) / 2)), 2));

//        return Math.sqrt(
//                ((Math.pow((blob1.xMin - blob2.xMin), 2) + Math.pow((blob1.xMax - blob2.xMax), 2))
//                        + (Math.pow((blob1.yMin - blob2.yMin), 2) + Math.pow((blob1.yMax - blob2.yMax), 2))
//                ));
    }

    public static double getEucledeanDistance(BlobFinder.Blob blob1, BlobFinder.Blob blob2) {
        return Math.sqrt(
                Math.pow((blob1.xMin - blob2.xMin), 2) +
                        Math.pow((blob1.xMax - blob2.xMax), 2) +
                        Math.pow((blob1.yMin - blob2.yMin), 2) +
                        Math.pow((blob1.yMax - blob2.yMax), 2) +
                        Math.pow((blob1.mass - blob2.mass), 2)
        );
    }

    private static void showRegion(BufferedImage design, BlobFinder.Blob blob, int color, int index) {
        for (int i = blob.xMin; i <= blob.xMax; i++) {
            for (int j = blob.yMin; j <= blob.yMax; j++) {
                design.setRGB(i, j, color);
            }
        }
        final MBFImage image = ImageUtilities.createMBFImage(design, false);
        image.drawText(String.valueOf(index), blob.xMin, blob.yMax, HersheyFont.CURSIVE, 15, RGBColour.WHITE);
        image.drawText(String.valueOf(index), (blob.xMax + blob.xMin) / 2, blob.yMax, HersheyFont.CURSIVE, 15, RGBColour.BLACK);
//        DisplayUtilities.display(image);
        BufferedImage newDesign = ImageUtilities.createBufferedImageForDisplay(image);
        for (int i = blob.xMin; i <= blob.xMax; i++) {
            for (int j = blob.yMin; j <= blob.yMax; j++) {
                design.setRGB(i, j, newDesign.getRGB(i, j));
            }
        }
    }

    public static class MatchingResult {
        private BufferedImage designImageBlob;
        private BufferedImage actualImageBlob;
        private double similarity;
        private boolean hasResults;

        public BufferedImage getDesignImageBlob() {
            return designImageBlob;
        }

        public void setDesignImageBlob(BufferedImage designImageBlob) {
            this.designImageBlob = designImageBlob;
        }

        public BufferedImage getActualImageBlob() {
            return actualImageBlob;
        }

        public void setActualImageBlob(BufferedImage actualImageBlob) {
            this.actualImageBlob = actualImageBlob;
        }

        public double getSimilarity() {
            return similarity;
        }

        public void setSimilarity(double similarity) {
            this.similarity = similarity;
        }

        public boolean isHasResults() {
            return hasResults;
        }

        public void setHasResults(boolean hasResults) {
            this.hasResults = hasResults;
        }
    }

}
