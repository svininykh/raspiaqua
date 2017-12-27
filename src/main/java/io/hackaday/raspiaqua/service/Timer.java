package io.hackaday.raspiaqua.service;

import io.hackaday.raspiaqua.aeration.Aerate;
import io.hackaday.raspiaqua.light.Light;
import io.hackaday.raspiaqua.proto.Aquarium.AquaDevice.Condition;
import io.hackaday.raspiaqua.proto.Aquarium.AquaDevice.Condition.Status;

/**
 *
 * @author svininykh-av
 */
public class Timer {

    public enum TimerMode {
        ON,
        AUTO,
        OFF
    }

    DetermineSunriseSunset dss;
    Condition lightCondition = Condition.getDefaultInstance();
    Condition aerateCondition = Condition.getDefaultInstance();

    public Timer(DetermineSunriseSunset dss) {
        this.dss = dss;
    }

    public Condition getLightCondition() {
        return lightCondition;
    }

    public void setLightCondition(Light light) {
        if (dss.isDayNow()) {
            switch (light.getDayMode()) {
                case ON:
                    lightCondition = getAquariumCondition(Status.ON,
                            dss.getDayDurationMinutes());
                    break;
                case AUTO:
                    if (light.getAfterSunriseMinutes() > 0 && light.getAfterSunriseMinutes() > dss.getAfterSunriseMinutes()) {
                        lightCondition = getAquariumCondition(Status.ON,
                                light.getAfterSunriseMinutes() - dss.getAfterSunriseMinutes());
                    } else if (light.getBeforeSunsetMinutes() > 0 && light.getBeforeSunsetMinutes() > dss.getDayDurationMinutes()) {
                        lightCondition = getAquariumCondition(Status.ON,
                                dss.getDayDurationMinutes());
                    } else {
                        lightCondition = getAquariumCondition(Status.OFF,
                                dss.getDayDurationMinutes() - light.getBeforeSunsetMinutes());
                    }
                    break;
                default:
                    lightCondition = getAquariumCondition(Status.OFF,
                            dss.getDayDurationMinutes());
                    break;
            }
        } else {
            switch (light.getNightMode()) {
                case ON:
                    lightCondition = getAquariumCondition(Status.ON,
                            dss.getNightDurationMinutes());
                    break;
                case AUTO:
                    if (light.getAfterSunsetMinutes() > 0 && light.getAfterSunsetMinutes() > dss.getAfterSunsetMinutes()) {
                        lightCondition = getAquariumCondition(Status.ON,
                                light.getAfterSunsetMinutes() - dss.getAfterSunsetMinutes());
                    } else if (light.getBeforeSunriseMinutes() > 0 && light.getBeforeSunriseMinutes() > dss.getNightDurationMinutes()) {
                        lightCondition = getAquariumCondition(Status.ON,
                                dss.getNightDurationMinutes());
                    } else {
                        lightCondition = getAquariumCondition(Status.OFF,
                                dss.getNightDurationMinutes() - light.getBeforeSunriseMinutes());
                    }
                    break;
                default:
                    lightCondition = getAquariumCondition(Status.OFF,
                            dss.getNightDurationMinutes());
                    break;
            }
        }
    }

    public Condition getAerateCondition() {
        return aerateCondition;
    }

    private Condition getAquariumCondition(Status status, long duration) {
        return Condition.getDefaultInstance().toBuilder()
                .setStatus(status)
                .setDuration((int) duration)
                .build();
    }
}
