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
public class LightClientHandler extends SimpleChannelInboundHandler<Aquarium.AquaResponse> {

    private Channel channel;
    private Aquarium.AquaResponse resp;
    BlockingQueue<Aquarium.AquaResponse> resps = new LinkedBlockingQueue<>();

    public Aquarium.AquaResponse sendRequest() {
        Aquarium.AquaRequest req = Aquarium.AquaRequest.newBuilder()
                .setEquipmentType(Aquarium.Equipment.LIGHTING)
                .build();

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
    protected void channelRead0(ChannelHandlerContext ctx, Aquarium.AquaResponse msg)
            throws Exception {
        resps.add(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
