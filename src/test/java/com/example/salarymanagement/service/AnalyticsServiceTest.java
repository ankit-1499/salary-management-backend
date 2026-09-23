package com.example.salarymanagement.service;

import com.example.salarymanagement.dto.SalaryAnalyticsDTO;
import com.example.salarymanagement.repository.CompensationRepository;
import com.example.salarymanagement.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private CompensationRepository compensationRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    @DisplayName("Happy Path: Average and median salary calculations across mock dataset")
    void getSummaryAnalytics_HappyPath() {
        when(compensationRepository.getTotalCompanyCost()).thenReturn(new BigDecimal("330000.00"));
        when(compensationRepository.getGlobalAverageBaseSalary()).thenReturn(new BigDecimal("100000.00"));
        when(compensationRepository.getActiveHeadcount()).thenReturn(3L);
        when(compensationRepository.getAllSortedCtcValues()).thenReturn(List.of(
                new BigDecimal("90000.00"),
                new BigDecimal("110000.00"),
                new BigDecimal("130000.00")
        ));

        SalaryAnalyticsDTO summary = analyticsService.getSummaryAnalytics();

        assertNotNull(summary);
        assertEquals(new BigDecimal("330000.00"), summary.totalCompanyCost());
        assertEquals(new BigDecimal("100000.00"), summary.globalAverageSalary());
        assertEquals(new BigDecimal("110000.00"), summary.medianSalary());
        assertEquals(3L, summary.activeHeadcount());
    }

    @Test
    @DisplayName("Edge Case: Even vs odd headcount median calculations")
    void getSummaryAnalytics_EvenHeadcountMedian() {
        when(compensationRepository.getTotalCompanyCost()).thenReturn(new BigDecimal("400000.00"));
        when(compensationRepository.getGlobalAverageBaseSalary()).thenReturn(new BigDecimal("100000.00"));
        when(compensationRepository.getActiveHeadcount()).thenReturn(4L);
        when(compensationRepository.getAllSortedCtcValues()).thenReturn(List.of(
                new BigDecimal("80000.00"),
                new BigDecimal("100000.00"),
                new BigDecimal("120000.00"),
                new BigDecimal("140000.00")
        ));

        SalaryAnalyticsDTO summary = analyticsService.getSummaryAnalytics();

        // (100000 + 120000) / 2 = 110000.00
        assertEquals(new BigDecimal("110000.00"), summary.medianSalary());
    }

    @Test
    @DisplayName("Worst Case: Handling empty dataset (0 employees) gracefully without divide-by-zero")
    void getSummaryAnalytics_EmptyDataset() {
        when(compensationRepository.getTotalCompanyCost()).thenReturn(null);
        when(compensationRepository.getGlobalAverageBaseSalary()).thenReturn(null);
        when(compensationRepository.getActiveHeadcount()).thenReturn(0L);
        when(compensationRepository.getAllSortedCtcValues()).thenReturn(Collections.emptyList());

        SalaryAnalyticsDTO summary = analyticsService.getSummaryAnalytics();

        assertNotNull(summary);
        assertEquals(BigDecimal.ZERO.setScale(2), summary.totalCompanyCost());
        assertEquals(BigDecimal.ZERO, summary.globalAverageSalary());
        assertEquals(BigDecimal.ZERO, summary.medianSalary());
        assertEquals(0L, summary.activeHeadcount());
    }
}
