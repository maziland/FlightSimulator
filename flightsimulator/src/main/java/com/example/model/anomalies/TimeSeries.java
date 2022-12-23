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
	String csvFileName;
	int numOfLines;

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
		} catch (Exception e) {
			e.printStackTrace();
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
