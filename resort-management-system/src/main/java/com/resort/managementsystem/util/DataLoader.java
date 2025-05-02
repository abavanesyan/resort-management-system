package com.resort.managementsystem.util;

import com.opencsv.CSVReader;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataLoader {

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

                // Use OpenCSV's correct parsing and count facilities
                int count = (int) Arrays.stream(facilities.split(","))
                        .map(String::trim)
                        .filter(f -> !f.isEmpty())
                        .count();

                facilityCounts.put(code, count);
            }
        }

        return facilityCounts;
    }
}
