package io.hackaday.raspiaqua.service;

import io.hackaday.raspiaqua.proto.Aquarium;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 *
 * @author svininykh-av
 */
public class ServerHandler extends SimpleChannelInboundHandler<Aquarium.AquaRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Aquarium.AquaRequest msg)
            throws Exception {
        Aquarium.AquaResponse.Builder builder = Aquarium.AquaResponse.newBuilder();
        if (msg.getEquipmentType() == Aquarium.Equipment.LIGHTING) {
            builder.setLightingLamp(Aquarium.Lighting.newBuilder()
                    .setBasic(Aquarium.Condition.newBuilder()
                            .setStatus(Aquarium.Condition.Status.ON)
                            .setDuration(3)
                            .build()
                    )
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
