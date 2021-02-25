package com.plc.client.coordinator;


import com.plc.client.Constant;
import com.plc.client.sendDataAllImpl.SendDataAll;
import com.plc.client.sendDatas10M.SendData10M;
import com.plc.core.model.*;
import com.plc.core.model.enums.ResultStatusEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {


    public void channelActive(ChannelHandlerContext ctx) {
        try {
            SecureModel secure = new SecureModel();
            secure.setClientAddr(ctx.channel().localAddress().toString().substring(1));
            secure.setToken("");  //服务端定义的密钥
            ctx.writeAndFlush(secure);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof SecureModel) {
            SecureModel secureModel = (SecureModel) msg;
            return;
        }

        if (msg instanceof TextModel) {
            TextModel textModel = (TextModel) msg;
            System.err.println("需要补发的数据包" + textModel.toString());
            //返回丢失的数据包信息，并重新发送
            SendDataAll.getInstance().replenish(textModel);
            return;
        }
        if(msg instanceof ResultModel){
            ResultModel resultModel = (ResultModel) msg;
            System.out.println(resultModel.toString());
            if(resultModel.getCode() == ResultStatusEnum.SEND_INFO_SUCCESS.getCode()){

            }else if(resultModel.getCode() == ResultStatusEnum.SEND_FILE_SUCCESS.getCode()){
                System.out.println("文件发送成功");
            } else if(resultModel.getCode() == ResultStatusEnum.SERVER_START_SUCCESS.getCode()){
                Constant.IS_SEND_BASE_INFO_SUCCESS = true;
            }else if(resultModel.getCode() == ResultStatusEnum.DATA_CHECK_SUCCESS.getCode()){
                SendData10M.getInstance().getSendData10MThreads().get(resultModel.getThreadIndex()).cSend();
            }
            return;
        }

        if(msg instanceof SendInfoModel){
            SendInfoModel sendInfoModel = (SendInfoModel) msg;
            return;
        }

        //补发数据
        if(msg instanceof ReissueDataModel){
            ReissueDataModel reissueDataModel = (ReissueDataModel)msg;
            SendData10M.getInstance().replenish(reissueDataModel);
            return;
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
