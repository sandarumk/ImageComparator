package com.dinu.image;

import com.dinu.model.Matches;

import java.util.*;

/**
 * @author Dinu
 */
public class BlobUtil {

    public static ArrayList<Matches> processMeanShift(ArrayList<BlobFinder.Blob> designBlobList, ArrayList<BlobFinder.Blob> actualBlobList) {
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
                    double distance = meanShiftDistance(blob1, blob2);
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

    public static double meanShiftDistance(BlobFinder.Blob blob1, BlobFinder.Blob blob2) {
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

}
