package com.juliazluo.www.simpleweather;

/**
 * Created by julia on 2017-03-10.
 */

public class DayOfWeek {

    private String day;
    private String minTemp, maxTemp;

    public String getDay() {
        return day;
    }

    public String getMinTemp() {
        return minTemp;
    }

    public String getMaxTemp() {
        return maxTemp;
    }

    public DayOfWeek(String day, String minTemp, String maxTemp) {
        this.day = day;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;

    }
}
