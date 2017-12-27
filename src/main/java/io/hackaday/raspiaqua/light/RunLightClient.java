package io.hackaday.raspiaqua.light;

import io.hackaday.raspiaqua.service.ClientInitializer;
import io.hackaday.raspiaqua.service.ClientHandler;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import io.hackaday.raspiaqua.proto.Aquarium;
import io.hackaday.raspiaqua.proto.Aquarium.AquaDevice.Condition;
import io.hackaday.raspiaqua.proto.Aquarium.AquaDevice.Equipment;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
        String sHost = prop.getProperty("server.ip", "127.0.0.1");
        int iPort = Integer.parseInt(prop.getProperty("server.port", "8997"));

        GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalOutput led1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer());

            for (;;) {
                try {
                    // Create connection 
                    Channel c = bootstrap.connect(sHost, iPort).sync().channel();
                    ClientHandler handle = c.pipeline().get(ClientHandler.class);
                    // Get handle to handler so we can send message
                    logger.debug("AquaRequest send to the IoT server");
                    Aquarium.MessagePacket resp;
                    try {
                        resp = handle.sendRequest(sHost, InetAddress.getLocalHost().getHostName());
                    } catch (UnknownHostException ex) {
                        resp = handle.sendRequest(sHost, "unknown-host");
                    }
                    logger.debug("AquaResponse receive from the IoT server");
                    Aquarium.AquaDevice.Condition condition = Condition.getDefaultInstance();
                    for (int i = 0; i < resp.getDevicesCount(); i++) {
                        condition = resp.getDevicesList().get(i).getCondition();
                        if (resp.getDevicesList().get(i).getEquipmentType() == Equipment.LIGHT) {

                            if (condition.getStatus() == Condition.Status.ON) {
                                logger.info("LightingLamp: ON, Duration: {}", condition.getDuration());
//                                led1.high();
                            } else {
                                logger.info("LightingLamp: OFF, Duration: {}", condition.getDuration());
                                led1.low();
                            }
                            break;
                        }
                    }
                    c.close();
                    try {
                        if (condition.getDuration() > 0) {
                            Thread.sleep(condition.getDuration() * WAIT_MINUTE + WAIT_MINUTE);
                        } else {
                            Thread.sleep(10 * WAIT_MINUTE);
                        }
                    } catch (InterruptedException ex) {
                        logger.error(ex.toString());
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
