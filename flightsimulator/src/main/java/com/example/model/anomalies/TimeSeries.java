package com.example.model.anomalies;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TimeSeries {
	// This class represents a time-series table of events
	// The events are parsed from a csv file and enables
	// other classes to manipulate the data and read it
	String[] features;
	public HashMap<String, float[]> map;
	public HashMap<String, Correlation> correlatedFeatures;
	String csvFileName;
	int numOfLines;

	public class Correlation {
		public String correlatedFeature;
		public float correlation;

		public Correlation(String correlatedFeature, float correlation) {
			this.correlatedFeature = correlatedFeature;
			this.correlation = correlation;
		}
	}

	public TimeSeries(String csvFileName) {
		try {
			String row;
			BufferedReader csvReader = new BufferedReader(new FileReader(csvFileName));
			this.csvFileName = csvFileName;
			numOfLines();
			// Read the first line - the header
			if ((row = csvReader.readLine()) == null) {
				csvReader.close();
				throw new Exception("CSV file seems to be empty");
			}

			this.features = row.split(",");
			int num_of_features = this.features.length;

			this.map = BuildHashmaps(this.features, num_of_features, csvReader);
			calculateCorrelatedFeatures();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void calculateCorrelatedFeatures() {
		int firstFeatureIndex, secondFeatureIndex;
		this.correlatedFeatures = new HashMap<>();

		// Traverse on all features and get the most correlated for each feature
		for (firstFeatureIndex = 0; firstFeatureIndex < this.features.length - 1; firstFeatureIndex++) {
			float maxCorrelation = 0;
			int correlativeFeatureIndex = -1;

			// Nested loop for the second feature
			for (secondFeatureIndex = firstFeatureIndex
					+ 1; secondFeatureIndex < this.features.length; secondFeatureIndex++) {

				float correlation = StatLib.pearson(this.map.get(this.features[firstFeatureIndex]),
						this.map.get(this.features[secondFeatureIndex]));

				if (Math.abs(correlation) > maxCorrelation) {
					maxCorrelation = Math.abs(correlation);
					correlativeFeatureIndex = secondFeatureIndex;
				}
			}

			if (correlativeFeatureIndex != -1) {
				addCorrelation(firstFeatureIndex, correlativeFeatureIndex, maxCorrelation);
			}
		}
	}

	private void addCorrelation(int firstFeatureIndex1, int correlativeFeatureIndex, float maxCorrelation) {
		/*
		 * In case there was a feature with a correlation, insert both ways
		 * e.g `aileron -- rudder`, insert like so:
		 * {'aileron':'rudder','rudder':aileron}
		 */

		Correlation corr = this.correlatedFeatures.get(this.features[firstFeatureIndex1]);

		// Check if there's a bigger correlation for firstFeature, if so put new
		// correlation

		if (corr == null || corr.correlation <= maxCorrelation) {
			this.correlatedFeatures.put(this.features[firstFeatureIndex1],
					new Correlation(this.features[correlativeFeatureIndex], maxCorrelation));
		}

		// the same but opposite way
		corr = this.correlatedFeatures.get(this.features[correlativeFeatureIndex]);
		if (corr == null || corr.correlation <= maxCorrelation) {
			this.correlatedFeatures.put(this.features[correlativeFeatureIndex],
					new Correlation(this.features[firstFeatureIndex1], maxCorrelation));
		}
	}

	public HashMap<String, float[]> BuildHashmaps(String[] headers, int features_number, BufferedReader csvReader) {

		HashMap<String, float[]> map = new HashMap<String, float[]>();
		String row;

		// First, generate a 2 dimmensional String array for the csv
		ArrayList<String[]> lines = new ArrayList<String[]>();
		int count = 0;

		try {
			while ((row = csvReader.readLine()) != null) {
				lines.add(row.split(","));
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int header = 0; header < headers.length; header++) {
			float[] arr = new float[count];
			for (int line = 0; line < count; line++) {
				arr[line] = (float) Float.parseFloat(lines.get(line)[header]);
			}
			map.put(headers[header], arr);
		}
		return map;
	}

	public void numOfLines() {
		try (BufferedReader csvReader = new BufferedReader(new FileReader(this.csvFileName))) {
			int count = 0;
			while ((csvReader.readLine()) != null) {
				count++;
			}
			this.numOfLines = count - 1;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
