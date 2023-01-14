package com.example.model.anomalies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ZScoreAnomalyDetector implements TimeSeriesAnomalyDetector {

	HashMap<String, Float> zscore_map = new HashMap<>();

	public static float threshold = 0.90f;

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
		// array
		for (int i = 0; i < slicedArray.length; i++) {
			slicedArray[i] = array[startIndex + i];
		}
		// returns the slice of an array
		return slicedArray;
	}

	@Override
	public void learnNormal(TimeSeries ts) {
		HashMap<String, float[]> map = ts.map;
		String[] features = ts.features;

		// Run for each feature and calculate the
		for (int i = 0; i < features.length; i++) {

			// Handle feature array
			float max_zscore = 0;

			// Run for each value in the feature array
			for (int j = 1; j < map.get(features[i]).length; j++) {
				float feature_avg = StatLib.avg(getSlice(map.get(features[i]), 0, j));
				float feature_var = StatLib.var(getSlice(map.get(features[i]), 0, j));

				// Saves the current value to variable
				float current_value = map.get(features[i])[j];

				// Calculate Zscore
				float feature_zscore = GetAbsValue(current_value - feature_avg) / feature_var;

				// Saves the maximum Zscore to variable
				if (feature_zscore > max_zscore)
					max_zscore = feature_zscore;
			}
			zscore_map.put(features[i], max_zscore);
		}
	}

	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		HashMap<String, float[]> map = ts.map;
		String[] features = ts.features;
		List<AnomalyReport> anomalies = new ArrayList<AnomalyReport>();

		// Run for each feature and calculate the
		for (int i = 0; i < features.length; i++) {
			// Run for each value in the feature array
			for (int j = 1; j < map.get(features[i]).length; j++) {
				float feature_avg = StatLib.avg(getSlice(map.get(features[i]), 0, j));
				float feature_var = StatLib.var(getSlice(map.get(features[i]), 0, j));

				// Saves the current value to variable
				float current_value = map.get(features[i])[j];

				// Calculate Zscore
				float feature_zscore = GetAbsValue(current_value - feature_avg) / feature_var;

				// In case we detect anomaly
				if (feature_zscore > zscore_map.get(features[i])) {
					AnomalyReport anom = new AnomalyReport(features[i], j);
					anomalies.add(anom);
				}
			}
		}
		return anomalies;
	}
}