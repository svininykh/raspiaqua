package io.hackaday.raspiaqua.light;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import io.hackaday.raspiaqua.proto.Aquarium;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author svininykh-av
 */
public class RunLightClient {

    private static final int WAIT_MINUTE = 60 * 1000;

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(RunLightClient.class);
        Properties prop = new Properties();
        EventLoopGroup group = new NioEventLoopGroup();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            prop.load(loader.getResourceAsStream("raspiaquaconfig.properties"));
        } catch (IOException ex) {
            logger.error(ex.toString());
        }
        String sHost = prop.getProperty("host", "127.0.0.1");
        int iPort = Integer.parseInt(prop.getProperty("port", "8997"));

        GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalOutput led1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new LightClientInitializer());

            for (;;) {
                try {
                    // Create connection 
                    Channel c = bootstrap.connect(sHost, iPort).sync().channel();
                    LightClientHandler handle = c.pipeline().get(LightClientHandler.class);
                    // Get handle to handler so we can send message
                    logger.debug("AquaRequest send to the IoT server");
                    Aquarium.AquaResponse resp = handle.sendRequest();
                    logger.debug("AquaResponse receive from the IoT server");
                    if (resp.hasLightingLamp()) {
                        if (resp.getLightingLamp().getBasic().getStatus() == Aquarium.Condition.Status.ON) {
                            logger.info("LightingLamp: ON, Duration: {}", resp.getLightingLamp().getBasic().getDuration());
                            led1.high();
                        } else {
                            logger.info("LightingLamp: OFF, Duration: {}", resp.getLightingLamp().getBasic().getDuration());
                            led1.low();
                        }
                        c.close();
                        try {
                            Thread.sleep(WAIT_MINUTE);
                            Thread.sleep(resp.getLightingLamp().getBasic().getDuration() * WAIT_MINUTE);
                        } catch (InterruptedException ex) {
                            logger.error(ex.toString());
                        }
                    }
                } catch (InterruptedException ex) {
                    logger.error(ex.toString());
                }
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
