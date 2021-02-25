package com.plc.server;

import io.netty.channel.Channel;

public class ClientChannel {

    private Channel channel;

    private static class ClientChannelHoler {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static ClientChannel instance = new ClientChannel();
    }

    private ClientChannel() {
    }

    public static ClientChannel getInstance() {
        return ClientChannel.ClientChannelHoler.instance;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
