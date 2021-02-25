package com.plc.server.coordinator;

import com.plc.core.model.ReissueDataModel;
import com.plc.core.model.ResultModel;
import com.plc.core.model.enums.ResultStatusEnum;
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
public class ReissueDataServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ReissueDataServerHandler.class);


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ReissueDataModel) {
            ReissueDataModel reissueDataModel = (ReissueDataModel) msg;
            ReceiveDataThread receiveDataThreads = ReceiveData.getInstance().getReceiveDataThreads().get(reissueDataModel.getThreadIndex());
            receiveDataThreads.packagData(reissueDataModel);
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
