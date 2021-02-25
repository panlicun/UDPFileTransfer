package com.plc.client.coordinator;

import com.plc.core.code.NettyMessageDecoder;
import com.plc.core.code.NettyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class ClientCoordinator implements Runnable {

    public static Channel channel = null;

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK,new WriteBufferWaterMark(1,1024*1024*1024)).handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder());
                    ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null))); // 最大长度

                    ch.pipeline().addLast(new NettyMessageDecoder());//设置服务器端的编码和解码
                    ch.pipeline().addLast(new NettyMessageEncoder());
                    ch.pipeline().addLast(new ClientHandler());
                }
            });
            ChannelFuture f = b.localAddress(7776).connect("192.168.3.14",7777).sync();
            if (f.isSuccess()) {
                System.out.println("连接服务端协调器成功");
                channel = f.channel();
            } else {
                System.out.println("连接服务端协调器成功");
            }
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
