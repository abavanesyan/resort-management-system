package com.resort.managementsystem.service;

import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Map;

public class AnalysisService {
    public Map<String, Integer> calculateFacilityCounts(Instances data) {
        Map<String, Integer> facilityCounts = new HashMap<>();

        for (Instance instance : data) {
            String code = instance.stringValue(data.attribute("Code"));
            String facilities = instance.stringValue(data.attribute("General Facilities"));

            if (facilities == null || facilities.trim().isEmpty()) {
                continue;
            }

            // Split and count facilities
            String[] facilitiesArray = facilities.split(",");
            int count = 0;
            for (String item : facilitiesArray) {
                if (!item.trim().isEmpty()) {
                    count++;
                }
            }

            facilityCounts.put(code, count);
        }

        return facilityCounts;
    }
}
