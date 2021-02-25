package com.plc.core.attribute;

import com.plc.core.session.Session;
import io.netty.util.AttributeKey;

public interface Attributes {

	AttributeKey<Session> SESSION = AttributeKey.newInstance("session");

}
