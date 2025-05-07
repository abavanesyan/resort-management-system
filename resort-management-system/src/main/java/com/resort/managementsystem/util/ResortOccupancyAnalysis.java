package com.resort.managementsystem.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ResortOccupancyAnalysis {

    public static void generateChartImage(String imagePath) throws IOException {
        // Load from classpath instead of hardcoded paths
        InputStream roomTypesStream = ResortOccupancyAnalysis.class.getClassLoader()
                .getResourceAsStream("static/data/resorts_room_types.csv");
        InputStream generalInfoStream = ResortOccupancyAnalysis.class.getClassLoader()
                .getResourceAsStream("static/data/resorts_general_info.csv");

        if (roomTypesStream == null || generalInfoStream == null) {
            throw new IOException("One or both CSV files not found in classpath.");
        }

        Map<String, Integer> resortOccupancyMap = new HashMap<>();

        // Parse room_types CSV
        try (InputStreamReader reader = new InputStreamReader(roomTypesStream);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            for (CSVRecord record : csvParser) {
                String resortCode = record.get("Code");
                String maxOccupancyStr = record.get("Max Occupancy");

                if (maxOccupancyStr != null && !maxOccupancyStr.isEmpty()) {
                    try {
                        int maxOccupancy = Integer.parseInt(maxOccupancyStr);
                        resortOccupancyMap.put(
                                resortCode,
                                Math.max(resortOccupancyMap.getOrDefault(resortCode, 0), maxOccupancy)
                        );
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format for Max Occupancy at resort: " + resortCode);
                    }
                }
            }
        }

        // Create dataset and chart
        DefaultCategoryDataset dataset = createDataset(resortOccupancyMap, generalInfoStream);
        JFreeChart chart = ChartFactory.createBarChart(
                "Top 15 Resorts by Max Occupancy",
                "Resort Name",
                "Max Occupancy",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        ChartUtils.saveChartAsPNG(new File(imagePath), chart, 1000, 600);
    }

    private static DefaultCategoryDataset createDataset(Map<String, Integer> resortOccupancyMap,
                                                        InputStream generalInfoStream) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (InputStreamReader reader = new InputStreamReader(generalInfoStream);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            for (CSVRecord record : csvParser) {
                String resortCode = record.get("Code");
                String resortName = record.get("Name");

                if (resortOccupancyMap.containsKey(resortCode)) {
                    int maxOccupancy = resortOccupancyMap.get(resortCode);
                    dataset.addValue(maxOccupancy, "Max Occupancy", resortName);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading resorts_general_info.csv: " + e.getMessage());
        }

        return dataset;
    }
}
