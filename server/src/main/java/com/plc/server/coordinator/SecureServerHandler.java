package com.plc.server.coordinator;

import com.plc.core.model.SecureModel;
import com.plc.core.session.Session;
import com.plc.core.util.SessionUtil;
import com.plc.server.ClientChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对客户端的上传数据 有效性验证
 * @author 洋白菜
 *
 */
public class SecureServerHandler extends ChannelInboundHandlerAdapter {
	private static final Logger log = LoggerFactory.getLogger(SecureServerHandler.class);

    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if( msg  instanceof SecureModel){
        	SecureModel secureModel = (SecureModel)msg;
			ClientChannel.getInstance().setChannel(ctx.channel());
        	ctx.writeAndFlush(secureModel);
        }else{
			ctx.fireChannelRead(msg);   //继续执行
        }
    }
    
    
    @Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		SessionUtil.unBindServerSession(ctx.channel());
		ctx.close();
	}


}
