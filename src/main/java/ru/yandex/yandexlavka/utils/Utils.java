package ru.yandex.yandexlavka.utils;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

public class Utils {
    public static TimeWindow convertStringIntervalToSecondsInterval(String interval) {
        final int secondsInHour = 3600;
        final int secondsInMinute = 60;

        String[] intervalParts = interval.split("-");
        String[] intervalPartStart = intervalParts[0].split(":");
        int secondsStart = Integer.parseInt(intervalPartStart[0].trim()) * secondsInHour +
                Integer.parseInt(intervalPartStart[1].trim()) * secondsInMinute;
        String[] intervalPartEnd = intervalParts[1].split(":");
        int secondsEnd = Integer.parseInt(intervalPartEnd[0].trim()) * secondsInHour +
                Integer.parseInt(intervalPartEnd[1].trim()) * secondsInMinute;

        return TimeWindow.newInstance(secondsStart, secondsEnd);

    }
}
