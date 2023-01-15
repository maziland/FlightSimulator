package com.example.model.anomalies;

import com.example.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {

	public List<CorrelatedFeatures> correlatedFeatures = new ArrayList<CorrelatedFeatures>();
	public static float threshold = 0.90f;

	public static float getMaxOffset(Point[] points, Line lin_reg) {
		float maxOffset = 0;
		for (int point = 0; point < points.length; point++) {
			float offset = StatLib.dev(points[point], lin_reg);
			if (offset > maxOffset) {
				// this.anomaly_row = point + 1;
				maxOffset = offset;
			}
		}
		return maxOffset;
	}

	public static ArrayList<Integer> getRowsWithHigherOffsets(Point[] points, Line lin_reg, float max_offset) {
		ArrayList<Integer> anomalyRows = new ArrayList<Integer>();

		for (int point = 0; point < points.length; point++) {
			float offset = StatLib.dev(points[point], lin_reg);
			if (offset > max_offset) {
				anomalyRows.add(point + 1);
			}
		}
		return anomalyRows;
	}

	public void learnNormal(TimeSeries ts) {

		HashMap<String, float[]> map = ts.map;
		String[] features = ts.features;
		// String firstFeature, secondFeature = null;
		int firstFeatureIndex, secondFeatureIndex, pair_X_Index = -1, pair_Y_Index = -1;

		for (firstFeatureIndex = 0; firstFeatureIndex < features.length - 1; firstFeatureIndex++) {
			float maxCorrelation = 0;
			// firstFeature = features[firstFeatureIndex];

			for (secondFeatureIndex = firstFeatureIndex
					+ 1; secondFeatureIndex < features.length; secondFeatureIndex++) {

				// secondFeature = features[secondFeatureIndex];
				float correlation = StatLib.pearson(map.get(features[firstFeatureIndex]),
						map.get(features[secondFeatureIndex]));

				if (Math.abs(correlation) > maxCorrelation) {
					maxCorrelation = Math.abs(correlation);
					pair_X_Index = firstFeatureIndex;
					pair_Y_Index = secondFeatureIndex;
				}

			}

			if (maxCorrelation > threshold) {
				Point[] points = StatLib.createPointsArray(map.get(features[pair_X_Index]),
						map.get(features[pair_Y_Index]));
				Line lin_reg = StatLib.linear_reg(points);
				float maxOffset = getMaxOffset(points, lin_reg);
				CorrelatedFeatures cf = new CorrelatedFeatures(features[pair_X_Index], features[pair_Y_Index],
						maxCorrelation, lin_reg, maxOffset * 1.1f);
				correlatedFeatures.add(cf);
			}
		}
	}

	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		HashMap<String, float[]> map = ts.map;
		List<AnomalyReport> anomalies = new ArrayList<AnomalyReport>();
		ArrayList<Integer> anomalyRows;
		for (CorrelatedFeatures cf : correlatedFeatures) {
			Point[] testPoints = StatLib.createPointsArray(map.get(cf.feature1), map.get(cf.feature2));
			// Line line = StatLib.linear_reg(testPoints);
			anomalyRows = getRowsWithHigherOffsets(testPoints, cf.lin_reg, cf.threshold);

			for (int row : anomalyRows) {
				AnomalyReport anom = new AnomalyReport(cf.feature1 + "-" + cf.feature2, row);
				anomalies.add(anom);
			}
		}
		return anomalies;

	}

	public List<CorrelatedFeatures> getNormalModel() {
		return this.correlatedFeatures;
	}
}
