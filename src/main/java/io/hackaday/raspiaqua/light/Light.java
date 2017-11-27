package io.hackaday.raspiaqua.light;

import io.hackaday.raspiaqua.service.Timer;

/**
 *
 * @author svininykh-av
 */
public class Light {

    long beforeSunriseMinutes = 0;
    long afterSunriseMinutes = 0;
    long beforeSunsetMinutes = 0;
    long afterSunsetMinutes = 0;

    Timer.TimerMode dayMode = Timer.TimerMode.OFF;
    Timer.TimerMode nightMode = Timer.TimerMode.ON;

    public Timer.TimerMode getDayMode() {
        return dayMode;
    }

    public void setDayMode(String property) {
        switch (property) {
            case "on":
                this.dayMode = Timer.TimerMode.ON;
                break;
            case "auto":
                this.dayMode = Timer.TimerMode.AUTO;
                break;
            default:
                this.dayMode = Timer.TimerMode.OFF;
                break;
        }
    }

    public Timer.TimerMode getNightMode() {
        return nightMode;
    }

    public void setNightMode(String property) {
        switch (property) {
            case "on":
                this.dayMode = Timer.TimerMode.ON;
                break;
            case "auto":
                this.dayMode = Timer.TimerMode.AUTO;
                break;
            default:
                this.dayMode = Timer.TimerMode.OFF;
                break;
        }
    }

    public long getAfterSunriseMinutes() {
        return afterSunriseMinutes;
    }

    public void setAfterSunriseMinutes(long afterSunriseMinutes) {
        this.afterSunriseMinutes = afterSunriseMinutes;
    }

    public long getAfterSunsetMinutes() {
        return afterSunsetMinutes;
    }

    public void setAfterSunsetMinutes(long afterSunsetMinutes) {
        this.afterSunsetMinutes = afterSunsetMinutes;
    }

    public long getBeforeSunriseMinutes() {
        return beforeSunriseMinutes;
    }

    public void setBeforeSunriseMinutes(long beforeSunriseMinutes) {
        this.beforeSunriseMinutes = beforeSunriseMinutes;
    }

    public long getBeforeSunsetMinutes() {
        return beforeSunsetMinutes;
    }

    public void setBeforeSunsetMinutes(long beforeSunsetMinutes) {
        this.beforeSunsetMinutes = beforeSunsetMinutes;
    }

}
