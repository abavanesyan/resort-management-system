package com.resort.managementsystem.controller;

import com.resort.managementsystem.util.*;
import jakarta.servlet.ServletContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/web/analysis")
public class AnalysisController {

    @Autowired
    private ServletContext servletContext;

    @GetMapping
    public String showAnalysisPage(Model model) {
        try {
            // Load datasets for resort pricing and additional data
            Map<String, Double> averagePricesByResort = DataLoader.loadResortPricing("/static/data/resorts_pricing.csv");
            Map<String, Integer> facilityCounts = DataLoader.loadFacilityCounts("static/data/resorts_facilities.csv");
            Map<String, Integer> roomCounts = DataLoader.loadRoomCounts("static/data/resorts_room_types.csv");
            Map<String, String> checkInTimes = DataLoader.loadCheckInTimes("static/data/resorts_housing_rules.csv");

            // Get image output path
            String outputDirPath = servletContext.getRealPath("/images");
            File outputDir = new File(outputDirPath);
            if (!outputDir.exists()) outputDir.mkdirs();

            // Generate all plots
            ResortPricingChartGenerator.generatePricePlot(averagePricesByResort, outputDirPath + "/pricing_plot.png");
            ChartGenerator.generateBarPlot(facilityCounts, outputDirPath + "/facilities_plot.png");
            RoomChartGenerator.generateTop15RoomChart("static/data/resorts_room_types.csv", "static/data/resorts_general_info.csv", outputDirPath + "/room_counts_plot.png");
            ResortOccupancyAnalysis.generateChartImage(outputDirPath + "/occupancy_plot.png");
            CheckInTimeRangeChartGenerator.generateChartImage(checkInTimes, outputDirPath + "/checkin_time_plot.png");

            // Pass image paths to the view
            model.addAttribute("pricingPlotPath", "/images/pricing_plot.png");
            model.addAttribute("facilityPlotPath", "/images/facilities_plot.png");
            model.addAttribute("roomPlotPath", "/images/room_counts_plot.png");
            model.addAttribute("occupancyPlotPath", "/images/occupancy_plot.png");
            model.addAttribute("checkInTimePlotPath", "/images/checkin_time_plot.png");

            // Flags for visibility
            model.addAttribute("showPlot", true);
            model.addAttribute("showRoomPlot", true);
            model.addAttribute("showOccupancyPlot", true);
            model.addAttribute("showCheckInPlot", true);

            // Pass data for reference
            model.addAttribute("averagePrices", averagePricesByResort);
            model.addAttribute("facilityCounts", facilityCounts);
            model.addAttribute("roomCounts", roomCounts);
            model.addAttribute("checkInTimes", checkInTimes);

        } catch (IOException e) {
            model.addAttribute("errorMessage", "Error loading dataset: " + e.getMessage());
            model.addAttribute("showPlot", false);
            model.addAttribute("showRoomPlot", false);
            model.addAttribute("showOccupancyPlot", false);
            model.addAttribute("showCheckInPlot", false);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error during analysis: " + e.getMessage());
            model.addAttribute("showPlot", false);
            model.addAttribute("showRoomPlot", false);
            model.addAttribute("showOccupancyPlot", false);
            model.addAttribute("showCheckInPlot", false);
        }

        return "analysis";
    }
}
