package io.hackaday.raspiaqua.light;

import io.hackaday.raspiaqua.proto.Aquarium;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.applet.Applet;
import java.applet.AudioClip;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author svininykh-av
 */
public class RunLightClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8997"));

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        AudioClip clip = Applet.newAudioClip(RunLightClient.class.getResource("/sound/Steel-Bell-C6.wav"));

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new LightClientInitializer());

            // Create connection 
            Channel c = bootstrap.connect(HOST, PORT).sync().channel();

            // Get handle to handler so we can send message
            LightClientHandler handle = c.pipeline().get(LightClientHandler.class);
            Aquarium.Lighting resp = handle.sendRequest();
            if (resp.getBasicLight().getStatus() == Aquarium.Lighting.Status.ON) {
                for (int i = 0; i < resp.getBasicLight().getDuration(); i++) {
                    clip.play();
                    TimeUnit.SECONDS.sleep(1);
                }
            }
            c.close();

        } finally {
            group.shutdownGracefully();
        }

    }
}
