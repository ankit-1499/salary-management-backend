package com.example.salarymanagement.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "countries")
public class Country {

    @Id
    @Column(name = "country_code", length = 3, nullable = false)
    private String countryCode;

    @Column(name = "country_name", nullable = false)
    private String countryName;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    public Country() {}

    public Country(String countryCode, String countryName, String currencyCode) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.currencyCode = currencyCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return Objects.equals(countryCode, country.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode);
    }
}
