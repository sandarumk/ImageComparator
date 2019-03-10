package com.dinu.image.matcher;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;

import java.awt.image.BufferedImage;

public class HistogramCompare {

    public double calculateHistogram(BufferedImage design, BufferedImage actual) {
        MBFImage query = ImageUtilities.createMBFImage(design, true);
        MBFImage target = ImageUtilities.createMBFImage(actual, true);

        HistogramModel model = new HistogramModel(4, 4, 4);

        model.estimateModel(query);
        MultidimensionalHistogram designHistogram = model.histogram.clone();

        model.estimateModel(target);
        MultidimensionalHistogram targetHistogram = model.histogram.clone();

        return targetHistogram.compare(designHistogram, DoubleFVComparison.EUCLIDEAN);
    }
}
