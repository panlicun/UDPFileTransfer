package com.plc.server.coordinator;

import com.plc.core.model.ResultModel;
import com.plc.core.model.SendInfoModel;
import com.plc.core.model.TextModel;
import com.plc.core.model.enums.ResultStatusEnum;
import com.plc.server.DataFactory;
import com.plc.server.ReceiveData;
import com.plc.server.ServerApp;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author plc
 */
public class SendInfoServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SendInfoServerHandler.class);
    DataFactory dataFactory = DataFactory.getInstance();


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof SendInfoModel) {
            SendInfoModel sendInfoModel = (SendInfoModel) msg;
            System.out.println("协调器发来信息："+sendInfoModel.toString());
            //实例化接收数据的数组
            //获取线程数
            int threadNum = sendInfoModel.getThreadNum();
            String fileName = sendInfoModel.getFileName();

            ReceiveData.getInstance().setFileName(fileName);
            //接收数据
            ServerApp.THREAD_NUM = threadNum;
            ServerApp.START_RECEIVE = true;

//            return;

//            //实例化三位数组存放接收的数据
//            byte[][][] totalData = totalData = new byte[threadNum][][];
//            for (int i = 0; i < threadNum; i++) {
//                startPos[i] = i * threadBlock; // 设置每个线程开始发送文件的起始位置
//                //发送文件基本信息
//                int sendCount = 0;   //发送的次数
//                if(i == threadNum -1){
//                    sendCount = (int)(fileSize - startPos[i])/byteLength + 1;
//                }else{
//                    sendCount = (int)Math.ceil(threadBlock/byteLength);
//                }
//                totalData[i] = new byte[sendCount][];
//            }
//            dataFactory.setTotalData(totalData);
//            ctx.writeAndFlush(new ResultModel(ResultStatusEnum.SEND_INFO_SUCCESS));
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
