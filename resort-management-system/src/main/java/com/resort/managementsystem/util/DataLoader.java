package com.resort.managementsystem.util;

import com.opencsv.CSVReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataLoader {

    // Load facility counts from the facilities CSV
    public static Map<String, Integer> loadFacilityCounts(String dataPath) throws Exception {
        Map<String, Integer> facilityCounts = new LinkedHashMap<>();

        ClassPathResource resource = new ClassPathResource(dataPath);
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            String[] line;
            boolean isFirstLine = true;

            while ((line = reader.readNext()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                if (line.length < 2) continue;

                String code = line[0].trim();
                String facilities = line[1].trim();

                if (facilities.isEmpty()) continue;

                int count = (int) Arrays.stream(facilities.split(","))
                        .map(String::trim)
                        .filter(f -> !f.isEmpty())
                        .count();

                facilityCounts.put(code, count);
            }
        }

        return facilityCounts;
    }

    // Load room counts from the room types CSV
    public static Map<String, Integer> loadRoomCounts(String dataPath) throws Exception {
        Map<String, Integer> roomCounts = new LinkedHashMap<>();

        ClassPathResource resource = new ClassPathResource(dataPath);
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            String[] line;
            boolean isFirstLine = true;

            while ((line = reader.readNext()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                if (line.length < 2) continue;

                String code = line[0].trim();
                String roomTypes = line[1].trim();

                if (roomTypes.isEmpty()) continue;

                int count = (int) Arrays.stream(roomTypes.split(","))
                        .map(String::trim)
                        .filter(f -> !f.isEmpty())
                        .count();

                roomCounts.put(code, count);
            }
        }

        return roomCounts;
    }

    // Load check-in time ranges from the housing rules CSV
    public static Map<String, String> loadCheckInTimes(String dataPath) throws Exception {
        Map<String, String> checkInTimes = new LinkedHashMap<>();

        ClassPathResource resource = new ClassPathResource(dataPath);
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            String[] line;
            boolean isFirstLine = true;

            while ((line = reader.readNext()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                if (line.length < 2) continue;

                String code = line[0].trim();
                String checkIn = line[1].trim();

                if (!checkIn.isEmpty()) {
                    checkInTimes.put(code, checkIn);
                }
            }
        }

        return checkInTimes;
    }

    // Load resort pricing data from the CSV file
    public static Map<String, Double> loadResortPricing(String dataPath) throws Exception {
        Map<String, Double> averagePrices = new LinkedHashMap<>();

        ClassPathResource resource = new ClassPathResource(dataPath);
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            String[] line;
            boolean isFirstLine = true;

            while ((line = reader.readNext()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                if (line.length < 2) continue;

                String code = line[0].trim();
                String priceStr = line[1].trim();

                if (!priceStr.isEmpty()) {
                    try {
                        double price = Double.parseDouble(priceStr);
                        averagePrices.put(code, price);
                    } catch (NumberFormatException e) {
                        // Handle the case where the price is not a valid number
                        System.err.println("Invalid price format for code: " + code);
                    }
                }
            }
        }

        return averagePrices;
    }

}
