package com.resort.managementsystem.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckInTimeRangeChartGenerator {

    public static void generateChartImage(Map<String, String> checkInTimes, String outputFilePath) throws Exception {
        DefaultXYDataset dataset = new DefaultXYDataset();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        LocalDate baseDate = LocalDate.of(2025, 1, 1);  // Base date for all time mapping
        Pattern pattern = Pattern.compile("From (\\d{1,2}:\\d{2} [APM]{2}) to (\\d{1,2}:\\d{2} [APM]{2})");

        int index = 0;
        for (Map.Entry<String, String> entry : checkInTimes.entrySet()) {
            String code = entry.getKey();
            String timeRange = entry.getValue();
            Matcher matcher = pattern.matcher(timeRange);

            if (matcher.matches()) {
                LocalTime start = LocalTime.parse(matcher.group(1), formatter);
                LocalTime end = LocalTime.parse(matcher.group(2), formatter);

                double x = index++;
                double startMillis = baseDate.atTime(start).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                double endMillis = baseDate.atTime(end).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

                dataset.addSeries(code, new double[][]{
                        {x, x},  // X: same resort code index
                        {startMillis, endMillis}  // Y: start and end time in millis
                });
            }
        }

        JFreeChart chart = ChartFactory.createScatterPlot(
                "Check-in Time Ranges by Resort",
                "Resort Index", "Check-in Time",
                dataset
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        plot.setRenderer(renderer);

        DateAxis dateAxis = new DateAxis("Time");
        dateAxis.setDateFormatOverride(new java.text.SimpleDateFormat("hh:mm a"));
        plot.setRangeAxis(dateAxis);

        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16));
        ChartUtils.saveChartAsPNG(new File(outputFilePath), chart, 800, 600);
    }
}
