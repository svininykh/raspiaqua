package io.hackaday.raspiaqua.light;

/**
 *
 * @author svininykh-av
 */
public class Light {

    long beforeSunriseMinutes = 0;
    long afterSunriseMinutes = 0;
    long beforeSunsetMinutes = 0;
    long afterSunsetMinutes = 0;

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
