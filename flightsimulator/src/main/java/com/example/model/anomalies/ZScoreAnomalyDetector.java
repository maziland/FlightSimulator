package com.example.model.anomalies;

import com.example.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ZScoreAnomalyDetector implements TimeSeriesAnomalyDetector {

	HashMap<String, Float> threshold_map = new HashMap<>();
	HashMap<String, List<Float>> feature_zscore_map = new HashMap<>();

	public List<Float> getZscoresForFeature(String feature) {
		return this.feature_zscore_map.get(feature);
	}

	public static float GetAbsValue(float n) {
		if (n > 0)
			return n;
		else
			return -n;
	}

	public static float[] getSlice(float[] array, int startIndex, int endIndex) {
		// Get the slice of the Array
		float[] slicedArray = new float[endIndex - startIndex];
		// copying array elements from the original array to the newly created sliced
		for (int i = 0; i < slicedArray.length; i++) {
			slicedArray[i] = array[startIndex + i];
		}
		// returns the slice of an array
		return slicedArray;
	}

	@Override
	public Line GetCorrelatedLine(String attr1, String attr2) {
		return null;
	}

	@Override
	public void learnNormal(TimeSeries ts) {
		HashMap<String, float[]> map = ts.map;
		String[] features = ts.features;

		// Run for each feature and calculate the
		for (int i = 0; i < features.length; i++) {

			// Handle feature array
			float max_zscore = 0;
			String feature = features[i];

			// Run for each value in the feature array
			for (int j = 1; j < map.get(feature).length; j++) {
				float feature_avg = StatLib.avg(getSlice(map.get(feature), 0, j));
				float feature_var = StatLib.var(getSlice(map.get(feature), 0, j));
				float feature_standard_dev = (float) Math.sqrt((double) feature_var);

				// Saves the current value to variable
				float current_value = map.get(feature)[j];

				// Calculate Zscore
				float feature_zscore = GetAbsValue(current_value - feature_avg) / feature_standard_dev;
				if (!Float.isFinite(feature_zscore))
				{
					continue;
				}

				// Saves the maximum Zscore to variable
				if (feature_zscore > max_zscore)
					max_zscore = feature_zscore;
			}
			threshold_map.put(features[i], max_zscore);
		}
	}

	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		HashMap<String, float[]> map = ts.map;
		String[] features = ts.features;
		List<AnomalyReport> anomalies = new ArrayList<AnomalyReport>();
		List<Float> zscore_list;

		// Run for each feature and calculate the
		for (int i = 0; i < features.length; i++) {
			zscore_list = new ArrayList<>();
			String feature = features[i];
			// Run for each value in the feature array
			for (int j = 1; j < map.get(feature).length; j++) {
				float feature_avg = StatLib.avg(getSlice(map.get(feature), 0, j));
				float feature_var = StatLib.var(getSlice(map.get(feature), 0, j));

				// Saves the current value to variable
				float current_value = map.get(feature)[j];

				if (feature_var == 0)
				{
					continue;
				}

				// Calculate Zscore
				float feature_zscore = GetAbsValue(current_value - feature_avg) / feature_var;
				zscore_list.add(feature_zscore);

				// In case we detect anomaly
				if (feature_zscore > threshold_map.get(feature)) {
					AnomalyReport anom = new AnomalyReport(feature, j);
					anomalies.add(anom);
				}
			}
			this.feature_zscore_map.put(features[i], zscore_list);
		}
		return anomalies;
	}
}