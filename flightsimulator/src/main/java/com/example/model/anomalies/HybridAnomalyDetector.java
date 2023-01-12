package com.example.model.anomalies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.model.anomalies.ZScoreAnomalyDetector;
import com.example.model.anomalies.SimpleAnomalyDetector;

public class HybridAnomalyDetector implements TimeSeriesAnomalyDetector {

	public static float high_correlation = 0.95f;
	public static float low_correlation = 0.5f;
	List<CorrelatedFeatures> correlatedFeatures = new ArrayList<CorrelatedFeatures>();
	HashMap<String, Float> zscore_map = new HashMap();

	@Override
	public void learnNormal(TimeSeries ts) 
	{
		HashMap<String, float[]> map = ts.map;
		String[] features = ts.features;
		int firstFeatureIndex, secondFeatureIndex, pair_X_Index = -1, pair_Y_Index = -1;

		// Run for each feature
		for (firstFeatureIndex = 0; firstFeatureIndex < features.length - 1; firstFeatureIndex++) {
			float maxCorrelation = 0;

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
			
			// Handle with ZScore algorithm 
			if(maxCorrelation <= low_correlation)
			{
				// Handle feature array 
				float max_zscore = 0;

				// Run for each value in the feature array
				for(int j = 1; j < map.get(features[firstFeatureIndex]).length - 1;j++)
				{
					float feature_avg = StatLib.avg(ZScoreAnomalyDetector.getSlice(map.get(features[firstFeatureIndex]), 0, j));
					float feature_var = StatLib.var(ZScoreAnomalyDetector.getSlice(map.get(features[firstFeatureIndex]), 0, j));

					// Saves the current value to variable
					float current_value = map.get(features[firstFeatureIndex])[j];

					// Calculate Zscore
					
					float feature_zscore = ZScoreAnomalyDetector.GetAbsValue(current_value - feature_avg) / feature_var;

					// Saves the maximum Zscore to variable
					if (feature_zscore > max_zscore)
						max_zscore = feature_zscore;
				}
				zscore_map.put(features[firstFeatureIndex], max_zscore);
			}
			else if (maxCorrelation >= high_correlation)
			{
				Point[] points = StatLib.createPointsArray(map.get(features[pair_X_Index]),
						map.get(features[pair_Y_Index]));
				Line lin_reg = StatLib.linear_reg(points);
				float maxOffset = SimpleAnomalyDetector.getMaxOffset(points, lin_reg);
				CorrelatedFeatures cf = new CorrelatedFeatures(features[pair_X_Index], features[pair_Y_Index],
						maxCorrelation, lin_reg, maxOffset * 1.1f);
				correlatedFeatures.add(cf);
				// we want to handle this feature with Linear regression
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
