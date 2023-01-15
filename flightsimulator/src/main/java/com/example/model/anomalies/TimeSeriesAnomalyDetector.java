package com.example.model.anomalies;

import java.util.List;
import com.example.util.*;

public interface TimeSeriesAnomalyDetector {
	void learnNormal(TimeSeries ts);

	List<AnomalyReport> detect(TimeSeries ts);

	Line GetCorrelatedLine(String attr1, String attr2);

	
}
