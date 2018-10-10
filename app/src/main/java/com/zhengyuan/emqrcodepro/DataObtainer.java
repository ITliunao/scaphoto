package com.zhengyuan.emqrcodepro;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.ChatUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by zy on 2018/9/5.
 */

public enum DataObtainer {
    INSTANCE;
    private final String LOG_TAG = "DataObtainer";
    private final String Project_TAG = "EMQRCodePro";

    //获取选项的值
    public void submitQRInfo(String QRInfo, String loginID, final NetworkCallbacks.SimpleDataCallback callback) {
        Element element = new Element("mybody");
        element.addProperty("type", "requestSubmitQRInfo" + Project_TAG);
        element.addProperty("data", QRInfo);
        element.addProperty("loginID", loginID);
        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(), "returnSubmitQRInfo" + Project_TAG,
                new NetworkCallbacks.MessageListenerThinner() {
                    @Override
                    public void processMessage(Element element, Message message, Chat chat) {
                        boolean isSuccess = element.getBody() != null &&
                                !element.getBody().equals("");
                        callback.onFinish(isSuccess, "", element.getProperty("result"));
                    }
                });
    }
}
