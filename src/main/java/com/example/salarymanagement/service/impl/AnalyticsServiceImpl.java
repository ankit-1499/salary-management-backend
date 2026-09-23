package com.example.salarymanagement.service.impl;

import com.example.salarymanagement.dto.CountrySummaryDTO;
import com.example.salarymanagement.dto.DepartmentSummaryDTO;
import com.example.salarymanagement.dto.SalaryAnalyticsDTO;
import com.example.salarymanagement.dto.TopEarnerDTO;
import com.example.salarymanagement.repository.CompensationRepository;
import com.example.salarymanagement.service.AnalyticsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final CompensationRepository compensationRepository;

    public AnalyticsServiceImpl(CompensationRepository compensationRepository) {
        this.compensationRepository = compensationRepository;
    }

    @Override
    public SalaryAnalyticsDTO getSummaryAnalytics() {
        BigDecimal totalCompanyCost = compensationRepository.getTotalCompanyCost();
        if (totalCompanyCost == null) {
            totalCompanyCost = BigDecimal.ZERO;
        }

        BigDecimal globalAverageSalary = compensationRepository.getGlobalAverageBaseSalary();
        if (globalAverageSalary == null) {
            globalAverageSalary = BigDecimal.ZERO;
        } else {
            globalAverageSalary = globalAverageSalary.setScale(2, RoundingMode.HALF_UP);
        }

        Long activeHeadcount = compensationRepository.getActiveHeadcount();
        if (activeHeadcount == null) {
            activeHeadcount = 0L;
        }

        List<BigDecimal> ctcValues = compensationRepository.getAllSortedCtcValues();
        BigDecimal medianSalary = calculateMedian(ctcValues);

        return new SalaryAnalyticsDTO(
                totalCompanyCost.setScale(2, RoundingMode.HALF_UP),
                globalAverageSalary,
                medianSalary,
                activeHeadcount
        );
    }

    @Override
    public List<CountrySummaryDTO> getCountryBreakdown() {
        return compensationRepository.getCountryBreakdown();
    }

    @Override
    public List<DepartmentSummaryDTO> getDepartmentBreakdown() {
        return compensationRepository.getDepartmentBreakdown();
    }

    @Override
    public List<TopEarnerDTO> getTopEarners(String countryCode, int limit) {
        int maxLimit = limit > 0 ? limit : 10;
        PageRequest pageable = PageRequest.of(0, maxLimit);

        if (countryCode != null && !countryCode.trim().isEmpty()) {
            return compensationRepository.findTopEarnersByCountry(countryCode.trim(), pageable);
        } else {
            return compensationRepository.findTopEarnersGlobally(pageable);
        }
    }

    private BigDecimal calculateMedian(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int size = values.size();
        if (size % 2 == 1) {
            return values.get(size / 2).setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal mid1 = values.get((size / 2) - 1);
            BigDecimal mid2 = values.get(size / 2);
            return mid1.add(mid2).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }
    }
}
