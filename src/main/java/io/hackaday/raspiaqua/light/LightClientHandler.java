package io.hackaday.raspiaqua.light;

import io.hackaday.raspiaqua.proto.Aquarium;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author svininykh-av
 */
public class LightClientHandler extends SimpleChannelInboundHandler<Aquarium.Lighting> {

    private Channel channel;
    private Aquarium.Lighting resp;
    BlockingQueue<Aquarium.Lighting> resps = new LinkedBlockingQueue<Aquarium.Lighting>();

    public Aquarium.Lighting sendRequest() {
        Aquarium.Lighting req = Aquarium.Lighting.newBuilder()
                .setBasicLight(
                        Aquarium.Lighting.Condition.newBuilder()
                                .setStatus(Aquarium.Lighting.Status.OFF)
                                .setDuration(0)
                                .build()
                ).build();

        // Send request
        channel.writeAndFlush(req);

        // Now wait for response from server
        boolean interrupted = false;
        for (;;) {
            try {
                resp = resps.take();
                break;
            } catch (InterruptedException ignore) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return resp;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Aquarium.Lighting msg)
            throws Exception {
        resps.add(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
