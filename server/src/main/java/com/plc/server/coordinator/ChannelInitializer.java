package com.plc.server.coordinator;


import com.plc.core.code.NettyMessageDecoder;
import com.plc.core.code.NettyMessageEncoder;
import com.plc.server.ServerApp;
import io.netty.channel.Channel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class ChannelInitializer extends io.netty.channel.ChannelInitializer<Channel> {

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ch.pipeline().addLast(new ObjectEncoder());
		ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null))); // 最大长度
		
		ch.pipeline().addLast(new NettyMessageDecoder());//设置服务器端的编码和解码
		ch.pipeline().addLast(new NettyMessageEncoder());

		ch.pipeline().addLast(new ResultServerHandler());
		ch.pipeline().addLast(new SecureServerHandler());
		ch.pipeline().addLast(new TextServerHandler());
		ch.pipeline().addLast(new SendInfoServerHandler());
		ch.pipeline().addLast(new ReissueDataServerHandler());
	}

}
