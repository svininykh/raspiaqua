package io.hackaday.raspiaqua.service;

import io.hackaday.raspiaqua.light.Light;
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
        Timer timer = new Timer(dss);
        logger.debug("AquaRequest read");
        switch (msg.getEquipmentType()) {
            case LIGHTING:
                logger.info("EquipmentType: LIGHTING");
                timer.setMode(prop.getProperty("light.day", "off"));
                Light light = new Light();
                light.setBeforeSunriseMinutes(Long.parseLong(prop.getProperty("light.beforesunrise", "0")));
                light.setAfterSunriseMinutes(Long.parseLong(prop.getProperty("light.aftersunrise", "0")));
                light.setBeforeSunsetMinutes(Long.parseLong(prop.getProperty("light.beforesunset", "0")));
                light.setBeforeSunsetMinutes(Long.parseLong(prop.getProperty("light.aftersunset", "0")));
                timer.setLightCondition(light);
                builder.setLightingLamp(Aquarium.Lighting.newBuilder()
                        .setBasic(timer.getLightCondition())
                );
                break;
            case AERATION:
                logger.info("EquipmentType: AERATION");
                /*
                 TODO: Aeration algorithm.
                 */
                break;
            default:
                break;
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
