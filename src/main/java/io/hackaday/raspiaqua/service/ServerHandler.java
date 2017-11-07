package io.hackaday.raspiaqua.service;

import io.hackaday.raspiaqua.proto.Aquarium;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.Date;

/**
 *
 * @author svininykh-av
 */
public class ServerHandler extends SimpleChannelInboundHandler<Aquarium.AquaRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Aquarium.AquaRequest msg)
            throws Exception {
        DetermineSunriseSunset dss = new DetermineSunriseSunset(new Date());
        Aquarium.AquaResponse.Builder builder = Aquarium.AquaResponse.newBuilder();
        if (msg.getEquipmentType() == Aquarium.Equipment.LIGHTING) {
            builder.setLightingLamp(Aquarium.Lighting.newBuilder()
                    .setBasic(Aquarium.Condition.newBuilder()
                            .setStatus(dss.getNightDurationMinutes() > 0 ? Aquarium.Condition.Status.ON : Aquarium.Condition.Status.OFF)
                            .setDuration(dss.getNightDurationMinutes() > 0 ? (int) dss.getNightDurationMinutes() : (int) dss.getDayDurationMinutes())
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
