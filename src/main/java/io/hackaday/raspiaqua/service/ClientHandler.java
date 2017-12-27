package io.hackaday.raspiaqua.service;

import io.hackaday.raspiaqua.proto.Aquarium;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author svininykh-av
 */
public class ClientHandler extends SimpleChannelInboundHandler<Aquarium.MessagePacket> {

    private Channel channel;
    private Aquarium.MessagePacket resp;
    BlockingQueue<Aquarium.MessagePacket> resps = new LinkedBlockingQueue<>();

    public Aquarium.MessagePacket sendRequest(String server, String client) {
        Aquarium.MessagePacket req = Aquarium.MessagePacket.newBuilder()
                .setServerName(server)
                .setClientName(client)                
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
    protected void channelRead0(ChannelHandlerContext ctx, Aquarium.MessagePacket msg)
            throws Exception {
        resps.add(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
