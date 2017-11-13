package io.hackaday.raspiaqua.service;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author svininykh-av
 */
public class DetermineSunriseSunset {

    Logger logger = LoggerFactory.getLogger(DetermineSunriseSunset.class);

    Calendar today = Calendar.getInstance();
    Calendar sunrise = Calendar.getInstance();
    Calendar sunset = Calendar.getInstance();
    SunriseSunsetCalculator calculator;
    SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

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
        logger.info("Today: {} {}", formatDate.format(today.getTime()), formatTime.format(today.getTime()));
        logger.info("Sunrise: {}, Sunset: {}", formatTime.format(sunrise.getTime()), formatTime.format(sunset.getTime()));
    }

    private Calendar getNextSunrise() {
        Calendar nextSunrise = today;
        if (nextSunrise.after(sunrise)) {
            nextSunrise.add(Calendar.DAY_OF_MONTH, 1);
        }
        nextSunrise=calculator.getOfficialSunriseCalendarForDate(nextSunrise);
        logger.debug("NextSunrise: {} {}", formatDate.format(nextSunrise.getTime()), formatTime.format(nextSunrise.getTime()));
        return nextSunrise;
    }

    public boolean isDayNow() {
        return today.after(sunrise) && today.before(sunset);
    }

    public long getDayDurationMinutes() {
        long lDayDurationMinutes = 1;
        if (!isDayNow()) {
            lDayDurationMinutes = 0;
        } else {
            lDayDurationMinutes += today.toInstant().until(sunset.toInstant(), ChronoUnit.MINUTES);
        }
        logger.debug("DayDuration: {} minutes", lDayDurationMinutes);
        return lDayDurationMinutes;
    }

    public long getAfterSunriseMinutes() {
        long lAfterSunriseMinutes = 1;
        if (!isDayNow()) {
            lAfterSunriseMinutes = 0;
        } else {
            lAfterSunriseMinutes += sunrise.toInstant().until(today.toInstant(), ChronoUnit.MINUTES);
        }
        logger.debug("AfterSunrise: {} minutes", lAfterSunriseMinutes);
        return lAfterSunriseMinutes;
    }

    public long getNightDurationMinutes() {
        long lNightDurationMinutes = 1;
        if (isDayNow()) {
            lNightDurationMinutes = 0;
        } else if (today.after(sunset)) {
            lNightDurationMinutes += today.toInstant().until(getNextSunrise().toInstant(), ChronoUnit.MINUTES);
        } else {
            lNightDurationMinutes += today.toInstant().until(sunrise.toInstant(), ChronoUnit.MINUTES);
        }
        logger.debug("NightDuration: {} minutes", lNightDurationMinutes);
        return lNightDurationMinutes;
    }

    public long getAfterSunsetMinutes() {
        long lAfterSunsetMinutes = 1;
        if (isDayNow()) {
            lAfterSunsetMinutes = 0;
        } else {
            lAfterSunsetMinutes += sunset.toInstant().until(today.toInstant(), ChronoUnit.MINUTES);
        }
        logger.debug("AfterSunset: {} minutes", lAfterSunsetMinutes);
        return lAfterSunsetMinutes;
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
