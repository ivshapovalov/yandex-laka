package ru.yandex.yandexlavka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "salary.factors")
public class SalaryConfig {

    private Map<String, Integer> earnings;
    private Map<String, Integer> rating;

    public Map<String, Integer> getEarningsFactors() {
        return earnings;
    }

    public void setEarnings(Map<String, Integer> earnings) {
        this.earnings = earnings;
    }

    public Map<String, Integer> getRatingFactors() {
        return rating;
    }

    public void setRating(Map<String, Integer> rating) {
        this.rating = rating;
    }
}


