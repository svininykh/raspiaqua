package io.hackaday.raspiaqua.service;

import io.hackaday.raspiaqua.aeration.Aerate;
import io.hackaday.raspiaqua.light.Light;
import io.hackaday.raspiaqua.proto.Aquarium;
import io.hackaday.raspiaqua.proto.Aquarium.AquaDevice.Equipment;
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
public class ServerHandler extends SimpleChannelInboundHandler<Aquarium.MessagePacket> {

    Properties prop = new Properties();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Aquarium.MessagePacket msg)
            throws Exception {
        Logger logger = LoggerFactory.getLogger(ServerHandler.class);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        prop.load(loader.getResourceAsStream("raspiaquaconfig.properties"));
        DetermineSunriseSunset dss = new DetermineSunriseSunset(new Date(), prop);
        Aquarium.MessagePacket.Builder builder = Aquarium.MessagePacket.newBuilder();
        Timer timer = new Timer(dss);
        logger.debug("AquaRequest read");
        logger.debug("Server: %s Client: %s", msg.getServerName(), msg.getClientName());
        Light light = new Light();
        light.setDayMode(prop.getProperty("light.day", "off"));
        light.setNightMode(prop.getProperty("light.night", "on"));
        light.setBeforeSunriseMinutes(Long.parseLong(prop.getProperty("light.beforesunrise", "0")));
        light.setAfterSunriseMinutes(Long.parseLong(prop.getProperty("light.aftersunrise", "0")));
        light.setBeforeSunsetMinutes(Long.parseLong(prop.getProperty("light.beforesunset", "0")));
        light.setBeforeSunsetMinutes(Long.parseLong(prop.getProperty("light.aftersunset", "0")));
        timer.setLightCondition(light);
        Aquarium.AquaDevice device = Aquarium.AquaDevice.newBuilder()
                .setCondition(timer.getLightCondition()).build();
        builder.addDevices(device);
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
