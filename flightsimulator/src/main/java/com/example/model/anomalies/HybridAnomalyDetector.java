package com.example.model.anomalies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.model.anomalies.ZScoreAnomalyDetector;
import com.example.model.anomalies.SimpleAnomalyDetector;

public class HybridAnomalyDetector implements TimeSeriesAnomalyDetector {

	public static float high_correlation = 0.95f;
	public static float low_correlation = 0.5f;

	@Override
	public void learnNormal(TimeSeries ts) 
	{
		HashMap<String, float[]> map = ts.map;
		String[] features = ts.features;
		int firstFeatureIndex, secondFeatureIndex, pair_X_Index = -1, pair_Y_Index = -1;

		// Run for each feature
		for (firstFeatureIndex = 0; firstFeatureIndex < features.length - 1; firstFeatureIndex++) {
			float maxCorrelation = 0;
			// firstFeature = features[firstFeatureIndex];

			// Find the highest correlation for feature in index (firstFeatureIndex)
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

			if(maxCorrelation <= low_correlation)
			{
				// We want to handle this feature with linear regrisseon
			}
			else if (maxCorrelation >= high_correlation)
			{
				// we want to handle this feature with ZScore
			}
			else
			{
				// We want to handle this feature with the third option 
			}
		}
	}

	@Override
	public List<AnomalyReport> detect(TimeSeries ts) 
	{
		return null;
	}
}
