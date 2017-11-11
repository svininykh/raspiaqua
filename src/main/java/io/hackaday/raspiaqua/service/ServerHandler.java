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
        logger.info("AquaRequest read");
        if (msg.getEquipmentType() == Aquarium.Equipment.LIGHTING) {
            logger.info("EquipmentType: LIGHTING");
            if (dss.getDayDurationMinutes() > 0) {
                builder.setLightingLamp(Aquarium.Lighting.newBuilder()
                        .setBasic(Aquarium.Condition.newBuilder()
                                .setStatus(prop.getProperty("light.day", "off").equalsIgnoreCase("on") ? Aquarium.Condition.Status.ON : Aquarium.Condition.Status.OFF)
                                .setDuration((int) dss.getDayDurationMinutes())
                                .build()
                        )
                );
            }
            if (dss.getNightDurationMinutes() > 0) {
                builder.setLightingLamp(Aquarium.Lighting.newBuilder()
                        .setBasic(Aquarium.Condition.newBuilder()
                                .setStatus(prop.getProperty("light.night", "off").equalsIgnoreCase("on") ? Aquarium.Condition.Status.ON : Aquarium.Condition.Status.OFF)
                                .setDuration((int) dss.getNightDurationMinutes())
                                .build()
                        )
                );
            }
        }
        ctx.write(builder.build());
        logger.info("AquaResponse write");
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
