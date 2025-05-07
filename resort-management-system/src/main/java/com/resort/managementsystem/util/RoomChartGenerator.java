package com.resort.managementsystem.util;

import com.opencsv.exceptions.CsvValidationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.core.io.ClassPathResource;
import com.opencsv.CSVReader;

import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class RoomChartGenerator {

    public static void generateTop15RoomChart(String roomTypesPath, String generalInfoPath, String outputPath) throws IOException {
        Map<String, Integer> roomCounts = new HashMap<>();
        Map<String, String> codeToName = new HashMap<>();

        // Load room types
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource(roomTypesPath).getInputStream()))) {
            String[] line;
            boolean first = true;
            while ((line = reader.readNext()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.length < 2) continue;
                String code = line[0].trim();
                roomCounts.put(code, roomCounts.getOrDefault(code, 0) + 1);
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        // Load general info and map code to name
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource(generalInfoPath).getInputStream()))) {
            String[] line;
            boolean first = true;
            while ((line = reader.readNext()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.length < 2) continue;
                String code = line[0].trim();
                String name = line[1].trim();
                codeToName.put(code, name);
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        // Sort and select top 15
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(roomCounts.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        int limit = Math.min(15, sorted.size());

        // Create dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < limit; i++) {
            String code = sorted.get(i).getKey();
            String name = codeToName.getOrDefault(code, "Unknown");
            int count = sorted.get(i).getValue();
            dataset.addValue(count, "Rooms", name);
        }

        // Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Top 15 Resorts by Room Count",
                "Resort Name",
                "Number of Rooms",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        // Ensure output directory exists
        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();

        // Save chart as PNG
        ChartUtils.saveChartAsPNG(outputFile, chart, 1000, 600);
    }
}
