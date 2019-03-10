package com.dinu.image;

import com.dinu.image.matcher.CoordinationDistanceMatcher;
import com.dinu.image.matcher.PcaMatcher;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGColourSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.engine.Engine;
import org.openimaj.image.feature.local.engine.asift.ASIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.text.extraction.swt.LineCandidate;
import org.openimaj.image.text.extraction.swt.SWTTextDetector;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.Pair;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author Dinu
 */
public class FeatureCompare {

    // getOverallMatchingScore(Bufferedimage, Bufferedimage)
    // getLayoutMatchingScore()
    // getGSIFTMatchingScore()
    // get ASIFTMatchingScore()
    // get CSIFTMatchingScore()

    public double getSIFTMatchingScore(BufferedImage design, BufferedImage actual) {
        MBFImage query = ImageUtilities.createMBFImage(design, true);
        MBFImage target = ImageUtilities.createMBFImage(actual, true);
        DoGSIFTEngine engine = new DoGSIFTEngine();

        LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
        LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());

        LocalFeatureMatcher<Keypoint> matcher = new CoordinationDistanceMatcher<>(8);

        matcher.setModelFeatures(queryKeypoints);
        matcher.findMatches(targetKeypoints);
        List<Pair<Keypoint>> matches = matcher.getMatches();

        displayMatches(query, target, queryKeypoints, matcher.getMatches(), RGBColour.RED, "SIFT");

        return calculateMatchesScore(design, queryKeypoints.size(), matches);
    }

    public double getPcaSIFTMatchingScore(BufferedImage design, BufferedImage actual) {
        MBFImage query = ImageUtilities.createMBFImage(design, true);
        MBFImage target = ImageUtilities.createMBFImage(actual, true);
        DoGSIFTEngine engine = new DoGSIFTEngine();

        LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
        LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());

        LocalFeatureMatcher<Keypoint> matcher = new PcaMatcher<>();

        matcher.setModelFeatures(queryKeypoints);
        matcher.findMatches(targetKeypoints);
        List<Pair<Keypoint>> matches = matcher.getMatches();

        displayMatches(query, target, queryKeypoints, matcher.getMatches(), RGBColour.CYAN, "PCA-SIFT");

        return calculateMatchesScore(design, queryKeypoints.size(), matches);
    }


    public double getASIFTMatchingScore(BufferedImage design, BufferedImage actual) {
        MBFImage query = ImageUtilities.createMBFImage(design, true);
        MBFImage target = ImageUtilities.createMBFImage(actual, true);
        ASIFTEngine engine = new ASIFTEngine();

        LocalFeatureList<Keypoint> queryKeypoints = engine.findKeypoints(query.flatten());
        LocalFeatureList<Keypoint> targetKeypoints = engine.findKeypoints(target.flatten());

        LocalFeatureMatcher<Keypoint> matcher = new CoordinationDistanceMatcher<>(8);

        matcher.setModelFeatures(queryKeypoints);
        matcher.findMatches(targetKeypoints);
        List<Pair<Keypoint>> matches = matcher.getMatches();

        displayMatches(query, target, queryKeypoints, matcher.getMatches(), RGBColour.GREEN, "ASIFT");

        return calculateMatchesScore(design, queryKeypoints.size(), matches);
    }

    public double getCSIFTMatchingScore(BufferedImage design, BufferedImage actual) {
        MBFImage query = ImageUtilities.createMBFImage(design, true);
        MBFImage target = ImageUtilities.createMBFImage(actual, true);
        DoGColourSIFTEngine engine = new DoGColourSIFTEngine();

        LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query);
        LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target);

        LocalFeatureMatcher<Keypoint> matcher = new CoordinationDistanceMatcher<>(8);

        matcher.setModelFeatures(queryKeypoints);
        matcher.findMatches(targetKeypoints);
        List<Pair<Keypoint>> matches = matcher.getMatches();

        displayMatches(query, target, queryKeypoints, matcher.getMatches(), RGBColour.BLUE, "CSIFT");

        return calculateMatchesScore(design, queryKeypoints.size(), matches);
    }

    private void displayMatches(MBFImage query, MBFImage target, LocalFeatureList<Keypoint> queryKeypoints, List<Pair<Keypoint>> matches, Float[] color, String title) {
        System.out.println("query points:" + queryKeypoints.size());
        System.out.println("matches:" + matches.size());

        query.drawPoints(queryKeypoints, RGBColour.MAGENTA, 5);
        MBFImage consistentMatches = MatchingUtilities.drawMatches(query, target, matches, color);
        DisplayUtilities.display(consistentMatches, title);
    }


    private double calculateMatchesScore(BufferedImage design, int designPointCount, List<Pair<Keypoint>> matches) {
        if (designPointCount == 0) {
            return 0;
        }
        int matchesCount = matches.size();
        double sum = 0;
        for (Pair<Keypoint> pair : matches) {
            sum += calculateDiff(pair);
        }
        double maxDiff = maxDiff(design.getWidth(), design.getHeight());
        sum += (designPointCount - matchesCount) * maxDiff;

        return 100.0 - ((sum * 100 / designPointCount) / maxDiff);
    }

    private double calculateMatchesScore(BufferedImage design, List<Pair<Keypoint>> matches) {
        int count = 0;
        double sum = 0;
        for (Pair<Keypoint> pair : matches) {
            sum += calculateDiff(pair);
            ++count;
        }
        if (count == 0) {
            return 0;
        } else {
            return 100.0 - ((sum * 100 / count) / maxDiff(design.getWidth(), design.getHeight()));
        }
    }

    private double calculateDiff(Pair<Keypoint> pair) {
        Keypoint p1 = pair.getFirstObject();
        Keypoint p2 = pair.getSecondObject();
        return Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2)
                + Math.pow((p1.scale - p2.scale), 2) + Math.pow((p1.ori - p2.ori), 2));
    }

    private double maxDiff(int width, int height) {
        Keypoint p1 = new Keypoint(0, 0, 0, 0, null);
        Keypoint p2 = new Keypoint(width, height, (float) (Math.PI * 2), 0, null);//scale neglected
        return calculateDiff(new Pair<>(p1, p2));
    }


    // I also need a very clearn set of code lines in each matcher. This should be something like the new method i have done
    // at the end of the pixel matcher

    public enum FeatureEngine {
        GSIFT, GColourSIFT, ASIFT;
    }

    public enum FeatureMatcher {
        BasicMatcher, ConsistentLocalFeatureMatcher2d;
    }

    public double compareFeatures(BufferedImage design, BufferedImage actual, FeatureEngine featureEngine, FeatureMatcher featureMatcher, boolean showFeatures) {
        MBFImage query = ImageUtilities.createMBFImage(design, true);
        MBFImage target = ImageUtilities.createMBFImage(actual, true);

        //initialize feature extraction engine
        Engine engine;
        switch (featureEngine) {
            case GSIFT:
                engine = new DoGSIFTEngine();
                break;
            case GColourSIFT:
                engine = new DoGColourSIFTEngine();
                break;
            case ASIFT:
                engine = new ASIFTEngine();
                break;
            default:
                engine = new DoGSIFTEngine();
        }

        LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
        LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());

        //initialize LocalFeature matcher
        LocalFeatureMatcher<Keypoint> matcher;
        switch (featureMatcher) {
            case BasicMatcher:
                matcher = new BasicMatcher<>(10);
                break;
            case ConsistentLocalFeatureMatcher2d:
                RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(5.0, 1500,
                        new RANSAC.PercentageInliersStoppingCondition(0.5));
                matcher = new ConsistentLocalFeatureMatcher2d<>(
                        new FastBasicKeypointMatcher<>(8), modelFitter);
                break;
            default:
                matcher = new BasicMatcher<>(10);
                break;

        }

        matcher.setModelFeatures(queryKeypoints);
        matcher.findMatches(targetKeypoints);

        List<Pair<Keypoint>> matches = matcher.getMatches();
        if (showFeatures) {
            displayMatches(query, target, queryKeypoints, matches, RGBColour.GREEN, featureEngine.name());
        }

        //calculate difference
        return calculateMatchesScore(design, matches);

    }

    public double compareFeatures(BufferedImage design, BufferedImage actual) {
        MBFImage query = ImageUtilities.createMBFImage(design, true);
        MBFImage target = ImageUtilities.createMBFImage(actual, true);

//        MBFImage query = removeTexts("query.png");
//        MBFImage target = removeTexts("target.png");
        DoGSIFTEngine engine = new DoGSIFTEngine();
        LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
        LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());


//        DoGColourSIFTEngine engine = new DoGColourSIFTEngine();
//        LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query);
//        System.out.printf(queryKeypoints.toString());
//        LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target);
//        System.out.printf(targetKeypoints.toString());


        LocalFeatureMatcher<Keypoint> matcher = new BasicMatcher<Keypoint>(10);
        matcher.setModelFeatures(queryKeypoints);
        matcher.findMatches(targetKeypoints);

        //basic
        displayMatches(query, target, queryKeypoints, matcher.getMatches(), RGBColour.CYAN, "DoGSIFT");

        RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(5.0, 1500,
                new RANSAC.PercentageInliersStoppingCondition(0.5));
        matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
                new FastBasicKeypointMatcher<Keypoint>(8), modelFitter);

        matcher.setModelFeatures(queryKeypoints);
        matcher.findMatches(targetKeypoints);

        MBFImage consistentMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(),
                RGBColour.GREEN);

        List<Pair<Keypoint>> matches = matcher.getMatches();
        int count = 0;
        double sum = 0;
        for (Pair<Keypoint> pair : matches) {
            sum += calculateDiff(pair);
            ++count;
        }

        DisplayUtilities.display(consistentMatches);

        if (count == 0) {
            return 100.0;
        } else {
            return (sum * 100 / count) / maxDiff(design.getWidth(), design.getHeight());
        }

    }

    public static BufferedImage removeTexts(BufferedImage bi) {
        final SWTTextDetector detector = new SWTTextDetector();
        detector.getOptions().direction = SWTTextDetector.Direction.Both;
        detector.getOptions().intensityThreshold = 0.12F;

        final MBFImage image = ImageUtilities.createMBFImage(bi, false);
        detector.analyseImage(image.flatten());
        for (final LineCandidate line : detector.getLines()) {
            image.drawShapeFilled(line.getRegularBoundingBox(), RGBColour.BLUE);
        }
        return ImageUtilities.createBufferedImageForDisplay(image);
    }


}
