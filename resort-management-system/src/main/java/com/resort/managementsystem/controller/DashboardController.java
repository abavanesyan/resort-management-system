// src/main/java/com/resort/managementsystem/controller/DashboardController.java
package com.resort.managementsystem.controller;

import com.resort.managementsystem.service.GuestService;
import com.resort.managementsystem.service.ReservationService;
import com.resort.managementsystem.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/web/dashboard")
public class DashboardController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private GuestService guestService;

    @GetMapping
    public String dashboard(Model model) {
        // Key Metrics
        int totalBookings = reservationService.getTotalBookings();
        double totalRevenue = reservationService.getTotalRevenue();
        double occupancyRate = reservationService.getOccupancyRate();
        double customerSatisfaction = 4.5; // mock

        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("occupancyRate", Math.round(occupancyRate * 10.0) / 10.0);
        model.addAttribute("customerSatisfaction", customerSatisfaction);

        // Trend Charts (last 5 months)
        List<String> trendLabels = Arrays.asList(
                YearMonth.now().minusMonths(4).toString(),
                YearMonth.now().minusMonths(3).toString(),
                YearMonth.now().minusMonths(2).toString(),
                YearMonth.now().minusMonths(1).toString(),
                YearMonth.now().toString()
        );
        model.addAttribute("bookingsOverTimeLabels", trendLabels);
        model.addAttribute("bookingsOverTimeData", reservationService.getBookingsOverTime());
        model.addAttribute("revenueOverTimeLabels", trendLabels);
        model.addAttribute("revenueOverTimeData", reservationService.getRevenueOverTime());
        model.addAttribute("occupancyOverTimeLabels", trendLabels);
        model.addAttribute("occupancyOverTimeData", reservationService.getOccupancyOverTime());

        // Detailed Breakdowns
        Map<String, Double> revenueByRoomType = reservationService.getRevenueByRoomType();
        model.addAttribute("revenueByRoomTypeLabels", revenueByRoomType.keySet());
        model.addAttribute("revenueByRoomTypeData", revenueByRoomType.values());
        model.addAttribute("bookingSourcesLabels", Arrays.asList("Online", "Walk-In", "Agency"));
        model.addAttribute("bookingSourcesData", reservationService.getBookingSources());
        model.addAttribute("topStaff", staffService.getTopPerformingStaff());

        // Top Frequent Guests
        model.addAttribute("frequentGuests", guestService.findFrequentGuests(3));

        return "web/dashboard";
    }
}

