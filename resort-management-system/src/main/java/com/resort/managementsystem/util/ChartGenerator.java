package com.resort.managementsystem.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ChartGenerator {
    public static void generateBarPlot(Map<String, Integer> facilityCounts, String outputPath) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Populate dataset
        for (Map.Entry<String, Integer> entry : facilityCounts.entrySet()) {
            dataset.addValue(entry.getValue(), "Facilities", entry.getKey());
        }

        // Create the chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Number of General Facilities per Resort Code", // Chart title
                "Resort Code",                                // X-axis label
                "Number of Facilities",                       // Y-axis label
                dataset,                                      // Dataset
                PlotOrientation.VERTICAL,                     // Plot orientation
                false,                                        // Include legend
                true,                                         // Tooltips
                false                                         // URLs
        );

        // Ensure the directory exists before saving the image
        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs(); // Create parent directories if they don't exist

        // Save the chart as a PNG file
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
    }
}
