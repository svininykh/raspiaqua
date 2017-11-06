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

/**
 *
 * @author svininykh-av
 */
public class RunLightClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8997"));

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalOutput led1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new LightClientInitializer());

            // Create connection 
            Channel c = bootstrap.connect(HOST, PORT).sync().channel();

            // Get handle to handler so we can send message
            LightClientHandler handle = c.pipeline().get(LightClientHandler.class);
            Aquarium.AquaResponse resp = handle.sendRequest();
            if (resp.hasLightingLamp() &&
                    resp.getLightingLamp().getBasic().getStatus() == Aquarium.Condition.Status.ON) {
                led1.high();
                Thread.sleep(resp.getLightingLamp().getBasic().getDuration() * 60000);
            }
            led1.low();            
            c.close();

        } finally {
            group.shutdownGracefully();
        }

    }
}
