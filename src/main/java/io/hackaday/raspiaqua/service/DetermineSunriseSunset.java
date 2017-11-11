package io.hackaday.raspiaqua.service;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 *
 * @author svininykh-av
 */
public class DetermineSunriseSunset {

    Calendar today = Calendar.getInstance();
    Calendar sunrise = Calendar.getInstance();
    Calendar sunset = Calendar.getInstance();
    SunriseSunsetCalculator calculator;

    public DetermineSunriseSunset(Date date, Properties prop) {
        today = Calendar.getInstance(
                TimeZone.getTimeZone(prop.getProperty("place.timezone", "Europe/London")),
                Locale.getDefault()
        );
        calculator = new SunriseSunsetCalculator(
                new Location(prop.getProperty("place.latitude", "0.0"), prop.getProperty("place.longitude", "0.0")),
                today.getTimeZone()
        );
        today.setTime(date);
        sunrise = calculator.getOfficialSunriseCalendarForDate(today);
        sunset = calculator.getOfficialSunsetCalendarForDate(today);
    }

    private Calendar getNextSunrise() {
        Calendar sunriseDay = today;
        if (sunriseDay.after(sunrise)) {
            sunriseDay.add(Calendar.DAY_OF_MONTH, 1);
        }
        return calculator.getAstronomicalSunriseCalendarForDate(sunriseDay);
    }

    private Calendar getNextSunset() {
        Calendar sunsetDay = today;
        if (sunsetDay.after(sunset)) {
            sunsetDay.add(Calendar.DAY_OF_MONTH, 1);
        }
        return calculator.getAstronomicalSunriseCalendarForDate(sunsetDay);
    }

    private boolean isDayNow() {
        return today.after(sunrise) && today.before(sunset);
    }

    public long getDayDurationMinutes() {
        if (!isDayNow()) {
            return 0;
        }
        return today.toInstant().until(sunset.toInstant(), ChronoUnit.MINUTES);
    }

    public long getNightDurationMinutes() {
        if (isDayNow()) {
            return 0;
        }
        if (today.after(sunset)) {
            return today.toInstant().until(getNextSunrise().toInstant(), ChronoUnit.MINUTES);
        }
        return today.toInstant().until(sunrise.toInstant(), ChronoUnit.MINUTES);
    }

    public Calendar getSunrise() {
        return sunrise;
    }

    public Calendar getSunset() {
        return sunset;
    }

    public Calendar getToday() {
        return today;
    }
    
    

}
