package com.resort.managementsystem.controller;

import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.resort.managementsystem.util.ChartGenerator;
import com.resort.managementsystem.util.DataLoader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/web/analysis")
public class AnalysisController {

    @Autowired
    private ServletContext servletContext;

    @GetMapping
    public String showAnalysisPage(Model model) {
        try {
            // Load facility counts directly from CSV (no Weka)
            Map<String, Integer> facilityCounts = DataLoader.loadFacilityCounts("static/data/resorts_facilities.csv");

            // Get the real path of the "images" directory
            String outputDirPath = servletContext.getRealPath("/images");
            File outputDir = new File(outputDirPath);
            if (!outputDir.exists()) {
                outputDir.mkdirs();  // Create directory if it does not exist
            }

            String plotPath = outputDirPath + "/facilities_plot.png";

            // Generate the chart
            ChartGenerator.generateBarPlot(facilityCounts, plotPath);

            // Pass data to template
            model.addAttribute("facilityCounts", facilityCounts);
            model.addAttribute("showPlot", true);
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Error loading dataset: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error during analysis: " + e.getMessage());
        }

        return "analysis";
    }
}
