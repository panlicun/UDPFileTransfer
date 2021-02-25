package com.plc.core.util;

import com.alibaba.fastjson.JSON;
import com.plc.core.model.*;

/**
 * 传输数据转化包
 *
 * @author Administrator
 */
public class ObjectConvertUtil {

    public static String convertModle(SecureModel secureModel) {
        RecvieMessage recevie = new RecvieMessage();
        recevie.setData(JSON.toJSONString(secureModel));
        recevie.setMsgType(Event.MESSAGE_TYPE_SECURE_MODEL);
        return JSON.toJSONString(recevie);
    }

    public static String convertModle(TextModel textModel) {
        RecvieMessage recevie = new RecvieMessage();
        recevie.setData(JSON.toJSONString(textModel));
        recevie.setMsgType(Event.MESSAGE_TYPE_TEXT_MODEL);
        return JSON.toJSONString(recevie);
    }

    public static String convertModle(SendInfoModel sendInfo) {
        RecvieMessage recevie = new RecvieMessage();
        recevie.setData(JSON.toJSONString(sendInfo));
        recevie.setMsgType(Event.MESSAGE_TYPE_SENDINFO_MODEL);
        return JSON.toJSONString(recevie);
    }

    public static String convertModle(ResultModel resultModel) {
        RecvieMessage recevie = new RecvieMessage();
        recevie.setData(JSON.toJSONString(resultModel));
        recevie.setMsgType(Event.MESSAGE_TYPE_RESULT_MODEL);
        return JSON.toJSONString(recevie);
    }

    public static String convertModle(ReissueDataModel reissueDataModel) {
        RecvieMessage recevie = new RecvieMessage();
        recevie.setData(JSON.toJSONString(reissueDataModel));
        recevie.setMsgType(Event.MESSAGE_TYPE_REISSUEDATA_MODEL);
        return JSON.toJSONString(recevie);
    }


    public static Object convertModle(String recviejson) {
        RecvieMessage recvie = (RecvieMessage) JSON.parseObject(recviejson, RecvieMessage.class);
        Object obj = null;
        switch (recvie.getMsgType()) {
            case Event.MESSAGE_TYPE_SECURE_MODEL:
                obj = (SecureModel) JSON.parseObject(recvie.getData().toString(), SecureModel.class);
                break;
            case Event.MESSAGE_TYPE_TEXT_MODEL:
                obj = (TextModel) JSON.parseObject(recvie.getData().toString(), TextModel.class);
                break;
            case Event.MESSAGE_TYPE_SENDINFO_MODEL:
                obj = (SendInfoModel) JSON.parseObject(recvie.getData().toString(), SendInfoModel.class);
                break;
            case Event.MESSAGE_TYPE_RESULT_MODEL:
                obj = (ResultModel) JSON.parseObject(recvie.getData().toString(), ResultModel.class);
                break;
            case Event.MESSAGE_TYPE_REISSUEDATA_MODEL:
                obj = (ReissueDataModel) JSON.parseObject(recvie.getData().toString(), ReissueDataModel.class);
                break;
        }
        return obj;
    }

    public static String request(Object obj) {
        if (obj instanceof SecureModel) {
            SecureModel secureModel = (SecureModel) obj;
            return convertModle(secureModel);
        }  else if (obj instanceof TextModel) {
            TextModel textModel = (TextModel) obj;
            return convertModle(textModel);
        }else if (obj instanceof SendInfoModel) {
            SendInfoModel sendInfoModel = (SendInfoModel) obj;
            return convertModle(sendInfoModel);
        } else if (obj instanceof ResultModel) {
            ResultModel resultModel = (ResultModel) obj;
            return convertModle(resultModel);
        } else if (obj instanceof ReissueDataModel) {
            ReissueDataModel reissueDataModel = (ReissueDataModel) obj;
            return convertModle(reissueDataModel);
        }else {
            return null;
        }

    }


}
