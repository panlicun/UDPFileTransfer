package com.plc.core.util;

import com.plc.core.attribute.Attributes;
import com.plc.core.session.Session;
import io.netty.channel.Channel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SessionUtil {

	/**
	 * 存放的是上一节点链接到当前服务端的channel，key的规则，起始IP：端口
	 */
	private static final Map<String, Channel> SERVER_CHANNEL_MAP = new HashMap<>();



	public static void bindServerSession(Session session, Channel channel) {
		SERVER_CHANNEL_MAP.put(session.getNodeId(), channel);
		channel.attr(Attributes.SESSION).set(session);
	}

	public static void unBindServerSession(Channel channel) {
		if (hasLogin(channel)) {
			Session session = getSession(channel);
			SERVER_CHANNEL_MAP.remove(session.getNodeId());
			channel.attr(Attributes.SESSION).set(null);
			System.out.println(new Date() + " " + session + "退出集群");
		}
	}


	private static boolean hasLogin(Channel channel) {
		return channel.hasAttr(Attributes.SESSION);
	}

	public static Session getSession(Channel channel) {
		return channel.attr(Attributes.SESSION).get();
	}

	public static Map getServerChannelMap() {
		return SERVER_CHANNEL_MAP;
	}


}
