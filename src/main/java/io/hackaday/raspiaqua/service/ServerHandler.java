package io.hackaday.raspiaqua.service;

import io.hackaday.raspiaqua.proto.Aquarium;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 *
 * @author svininykh-av
 */
public class ServerHandler extends SimpleChannelInboundHandler<Aquarium.Lighting> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Aquarium.Lighting msg)
            throws Exception {
        Aquarium.Lighting.Builder builder = Aquarium.Lighting.newBuilder();
        if (msg.getBasicLight().getStatus() == Aquarium.Lighting.Status.OFF) {
            builder.setBasicLight(
                    Aquarium.Lighting.Condition.newBuilder()
                            .setStatus(Aquarium.Lighting.Status.ON)
                            .setDuration(3)
                            .build()
            );
        }
        ctx.write(builder.build());
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
