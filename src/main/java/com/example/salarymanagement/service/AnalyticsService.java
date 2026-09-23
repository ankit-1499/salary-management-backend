package com.example.salarymanagement.service;

import com.example.salarymanagement.dto.CountrySummaryDTO;
import com.example.salarymanagement.dto.DepartmentSummaryDTO;
import com.example.salarymanagement.dto.SalaryAnalyticsDTO;
import com.example.salarymanagement.dto.TopEarnerDTO;

import java.util.List;

public interface AnalyticsService {
    SalaryAnalyticsDTO getSummaryAnalytics();
    List<CountrySummaryDTO> getCountryBreakdown();
    List<DepartmentSummaryDTO> getDepartmentBreakdown();
    List<TopEarnerDTO> getTopEarners(String countryCode, int limit);
}
