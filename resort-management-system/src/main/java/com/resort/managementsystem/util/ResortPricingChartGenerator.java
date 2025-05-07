package com.resort.managementsystem.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ResortPricingChartGenerator {

    public static void generatePricePlot(Map<String, Double> averagePrices, String outputFilePath) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Double> entry : averagePrices.entrySet()) {
            dataset.addValue(entry.getValue(), "Price", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Resort Pricing Analysis",   // Title of the chart
                "Resort Code",               // X-axis label
                "Average Price",             // Y-axis label
                dataset,                     // Dataset
                PlotOrientation.VERTICAL,    // Plot orientation
                true,                        // Include legend
                true,                        // Tooltips
                false                        // URLs
        );

        // Save the chart as an image
        File chartFile = new File(outputFilePath);
        ChartUtils.saveChartAsPNG(chartFile, chart, 800, 600);
    }
}
