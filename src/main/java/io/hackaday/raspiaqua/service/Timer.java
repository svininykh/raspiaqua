package io.hackaday.raspiaqua.service;

import io.hackaday.raspiaqua.light.Light;
import io.hackaday.raspiaqua.proto.Aquarium;

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
    Aquarium.Condition lightCondition = Aquarium.Condition.getDefaultInstance();

    public Timer(DetermineSunriseSunset dss) {
        this.dss = dss;
    }
    
    public Aquarium.Condition getLightCondition() {
        return lightCondition;
    }

    public void setLightCondition(Light light) {
        if (dss.isDayNow()) {
            switch (light.getDayMode()) {
                case ON:
                    lightCondition = Aquarium.Condition.getDefaultInstance().toBuilder()
                            .setStatus(Aquarium.Condition.Status.ON)
                            .setDuration((int) dss.getDayDurationMinutes())
                            .build();
                    break;
                case AUTO:
                    if (light.getAfterSunriseMinutes() > 0 && light.getAfterSunriseMinutes() > dss.getAfterSunriseMinutes()) {
                        lightCondition = Aquarium.Condition.getDefaultInstance().toBuilder()
                                .setStatus(Aquarium.Condition.Status.ON)
                                .setDuration((int) (light.getAfterSunriseMinutes() - dss.getAfterSunriseMinutes()))
                                .build();
                    } else if (light.getBeforeSunsetMinutes() > 0 && light.getBeforeSunsetMinutes() > dss.getDayDurationMinutes()) {
                        lightCondition = Aquarium.Condition.getDefaultInstance().toBuilder()
                                .setStatus(Aquarium.Condition.Status.ON)
                                .setDuration((int) dss.getDayDurationMinutes())
                                .build();
                    } else {
                        lightCondition = Aquarium.Condition.getDefaultInstance().toBuilder()
                                .setStatus(Aquarium.Condition.Status.OFF)
                                .setDuration((int) (dss.getDayDurationMinutes() - light.getBeforeSunsetMinutes()))
                                .build();
                    }
                    break;
                default:
                    lightCondition = Aquarium.Condition.getDefaultInstance().toBuilder()
                            .setStatus(Aquarium.Condition.Status.OFF)
                            .setDuration((int) dss.getDayDurationMinutes())
                            .build();
                    break;
            }
        } else {
            switch (light.getNightMode()) {
                case ON:
                    lightCondition = Aquarium.Condition.getDefaultInstance().toBuilder()
                            .setStatus(Aquarium.Condition.Status.ON)
                            .setDuration((int) dss.getNightDurationMinutes())
                            .build();
                    break;
                case AUTO:
                    if (light.getAfterSunsetMinutes() > 0 && light.getAfterSunsetMinutes() > dss.getAfterSunsetMinutes()) {
                        lightCondition = Aquarium.Condition.getDefaultInstance().toBuilder()
                                .setStatus(Aquarium.Condition.Status.ON)
                                .setDuration((int) (light.getAfterSunsetMinutes() - dss.getAfterSunsetMinutes()))
                                .build();
                    } else if (light.getBeforeSunriseMinutes() > 0 && light.getBeforeSunriseMinutes() > dss.getNightDurationMinutes()) {
                        lightCondition = Aquarium.Condition.getDefaultInstance().toBuilder()
                                .setStatus(Aquarium.Condition.Status.ON)
                                .setDuration((int) dss.getNightDurationMinutes())
                                .build();
                    } else {
                        lightCondition = Aquarium.Condition.getDefaultInstance().toBuilder()
                                .setStatus(Aquarium.Condition.Status.OFF)
                                .setDuration((int) (dss.getNightDurationMinutes() - light.getBeforeSunriseMinutes()))
                                .build();
                    }
                    break;
                default:
                    lightCondition = Aquarium.Condition.getDefaultInstance().toBuilder()
                            .setStatus(Aquarium.Condition.Status.OFF)
                            .setDuration((int) dss.getNightDurationMinutes())
                            .build();
                    break;
            }
        }
    }
}
