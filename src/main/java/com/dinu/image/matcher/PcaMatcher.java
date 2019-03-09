package com.dinu.image.matcher;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.matcher.BasicMatcher;

import java.util.List;

public class PcaMatcher<T extends LocalFeature<?, ?>> extends BasicMatcher<T> {
    private final static int DEFAULT_THRESHOLD = 8;
    private final static double DEFAULT_PCA_MULTI = 0.6;
    private double pcaMultiply;

    public PcaMatcher() {
        this(DEFAULT_PCA_MULTI);
    }


    public PcaMatcher(double pcaMultiply) {
        super(DEFAULT_THRESHOLD);
        this.pcaMultiply = pcaMultiply;
    }

    @Override
    protected T checkForMatch(T query, List<T> features) {
        double distsq1 = Double.MAX_VALUE, distsq2 = Double.MAX_VALUE;
        T minkey = null;

        // find two closest matches
        for (final T target : features) {
            final double dsq = target.getFeatureVector().asDoubleFV()
                    .compare(query.getFeatureVector().asDoubleFV(), DoubleFVComparison.SUM_SQUARE);

            if (dsq < distsq1) {
                distsq2 = distsq1;
                distsq1 = dsq;
                minkey = target;
            } else if (dsq < distsq2) {
                distsq2 = dsq;
            }
        }

        // check the distance against PCA multiplication
        if (distsq1 < pcaMultiply * distsq2) {
            return minkey;
        } else
            return null;

    }
}
