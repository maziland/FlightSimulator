package com.example.model.anomalies;

import java.util.List;

public interface TimeSeriesAnomalyDetector {
	void learnNormal(TimeSeries ts);

	List<AnomalyReport> detect(TimeSeries ts);
}
