package io.hackaday.raspiaqua.service;

import io.hackaday.raspiaqua.proto.Aquarium;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.Date;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author svininykh-av
 */
public class ServerHandler extends SimpleChannelInboundHandler<Aquarium.AquaRequest> {

    Properties prop = new Properties();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Aquarium.AquaRequest msg)
            throws Exception {
        Logger logger = LoggerFactory.getLogger(ServerHandler.class);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        prop.load(loader.getResourceAsStream("raspiaquaconfig.properties"));
        DetermineSunriseSunset dss = new DetermineSunriseSunset(new Date(), prop);
        Aquarium.AquaResponse.Builder builder = Aquarium.AquaResponse.newBuilder();
        logger.debug("AquaRequest read");
        if (msg.getEquipmentType() == Aquarium.Equipment.LIGHTING) {
            logger.info("EquipmentType: LIGHTING");
            Aquarium.Condition.Status lightStatus = Aquarium.Condition.Status.OFF;
            long lightDurationMinutes = 0;
            long lightBeforeSunrise = Long.parseLong(prop.getProperty("light.beforesunrise", "0"));
            long lightAfterSunrise = Long.parseLong(prop.getProperty("light.aftersunrise", "0"));
            long lightBeforeSunset = Long.parseLong(prop.getProperty("light.beforesunset", "0"));
            long lightAfterSuset = Long.parseLong(prop.getProperty("light.aftersunset", "0"));
            if (dss.isDayNow()) {
                lightStatus = prop.getProperty("light.day", "off").equalsIgnoreCase("on") ? Aquarium.Condition.Status.ON : Aquarium.Condition.Status.OFF;
                if (lightStatus == Aquarium.Condition.Status.ON) {
                    lightDurationMinutes = dss.getDayDurationMinutes();
                } else {
                    if(lightAfterSunrise > 0 && lightAfterSunrise > dss.getAfterSunriseMinutes()) {
                        lightStatus = Aquarium.Condition.Status.ON;
                        lightDurationMinutes = dss.getAfterSunriseMinutes();
                    } else if(lightBeforeSunset > 0 && lightBeforeSunset > dss.getDayDurationMinutes()) {
                        lightStatus = Aquarium.Condition.Status.ON;
                        lightDurationMinutes = dss.getDayDurationMinutes();
                    } else {
                        lightDurationMinutes = dss.getDayDurationMinutes() - lightBeforeSunset;
                    }
                }
            } else {
                lightStatus = prop.getProperty("light.night", "on").equalsIgnoreCase("on") ? Aquarium.Condition.Status.ON : Aquarium.Condition.Status.OFF;
                if (lightStatus == Aquarium.Condition.Status.ON) {
                    lightDurationMinutes = dss.getNightDurationMinutes();
                } else {
                    if(lightAfterSuset > 0 && lightAfterSuset > dss.getAfterSunsetMinutes()) {
                        lightStatus = Aquarium.Condition.Status.ON;
                        lightDurationMinutes = dss.getAfterSunsetMinutes();
                    } else if (lightBeforeSunrise > 0 && lightBeforeSunrise > dss.getNightDurationMinutes()) {
                        lightStatus = Aquarium.Condition.Status.ON;
                        lightDurationMinutes = dss.getNightDurationMinutes();
                    } else {
                        lightDurationMinutes = dss.getNightDurationMinutes() - lightBeforeSunrise;
                    }
                }
            }
            builder.setLightingLamp(Aquarium.Lighting.newBuilder()
                    .setBasic(Aquarium.Condition.newBuilder()
                            .setStatus(lightStatus)
                            .setDuration((int) lightDurationMinutes)
                            .build()
                    )
            );
        }
        ctx.write(builder.build());
        logger.debug("AquaResponse write");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
