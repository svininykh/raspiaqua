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

    public static void main(String[] args) throws InterruptedException, IOException {
        Logger logger = LoggerFactory.getLogger(RunLightClient.class);
        Properties prop = new Properties();
        EventLoopGroup group = new NioEventLoopGroup();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        prop.load(loader.getResourceAsStream("raspiaquaconfig.properties"));
        String sHost = prop.getProperty("host", "127.0.0.1");
        int iPort = Integer.parseInt(prop.getProperty("port", "8997"));

        GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalOutput led1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new LightClientInitializer());

            // Create connection 
            Channel c = bootstrap.connect(sHost, iPort).sync().channel();

            // Get handle to handler so we can send message
            LightClientHandler handle = c.pipeline().get(LightClientHandler.class);
            logger.info("AquaRequest send to the IoT server");
            Aquarium.AquaResponse resp = handle.sendRequest();
            logger.info("AquaResponse receive from the IoT server");
            if (resp.hasLightingLamp()) {
                if (resp.getLightingLamp().getBasic().getStatus() == Aquarium.Condition.Status.ON) {
                    logger.info("LightingLamp: ON, Duration: {}", resp.getLightingLamp().getBasic().getDuration());
                    led1.high();
                } else {
                    logger.info("LightingLamp: OFF, Duration: {}", resp.getLightingLamp().getBasic().getDuration());
                    led1.low();
                }
                Thread.sleep(resp.getLightingLamp().getBasic().getDuration() * 60000);
            }
            c.close();

        } finally {
            group.shutdownGracefully();
        }

    }
}
