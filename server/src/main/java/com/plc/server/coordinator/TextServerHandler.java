package com.plc.server.coordinator;

import com.plc.core.model.TextModel;
import com.plc.core.session.Session;
import com.plc.core.util.SessionUtil;
import com.plc.server.DataFactory;
import com.plc.server.ReceiveData;
import com.plc.server.ReceiveDataThread;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对客户端的上传数据 有效性验证
 *
 * @author plc
 */
public class TextServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(TextServerHandler.class);


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TextModel) {
            TextModel textModel = (TextModel) msg;
            if (textModel.getData() == null){
                System.out.println("线程" + textModel.getThreadIndex() + "，进行线程数据校验");
                ReceiveDataThread receiveDataThreads = ReceiveData.getInstance().getReceiveDataThreads().get(textModel.getThreadIndex());
                receiveDataThreads.checkDataAndWrite(0);
            }
        } else {
            ctx.fireChannelRead(msg);   //继续执行
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
