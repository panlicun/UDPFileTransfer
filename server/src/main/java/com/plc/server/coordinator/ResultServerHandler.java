package com.plc.server.coordinator;

import com.plc.core.model.ResultModel;
import com.plc.core.model.TextModel;
import com.plc.core.model.enums.ResultStatusEnum;
import com.plc.server.DataFactory;
import com.plc.server.ReceiveData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;

/**
 * 对客户端的上传数据 有效性验证
 *
 * @author plc
 */
public class ResultServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ResultModel) {
            ResultModel resultModel = (ResultModel) msg;
            System.out.println(resultModel.toString());
            if(resultModel.getCode() == ResultStatusEnum.THREAD_SEND_FINISH.getCode()){
                ReceiveData.getInstance().getReceiveDataThreads().get(resultModel.getThreadIndex()).checkDataAndWrite(resultModel.getSendTimes());
                ReceiveData.getInstance().getReceiveDataThreads().get(resultModel.getThreadIndex()).closeThread();
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
