package com.auction.shared;

import java.io.Serializable;
import java.util.UUID;

/**
 * Message - gói tin chuẩn giao tiếp Client ↔ Server.
 *
 * Đặt ở package SHARED để cả 2 team import cùng class.
 * Mọi object qua Socket phải bọc trong Message.
 *
 * Sử dụng:
 *   Message req = Message.request(MessageType.LOGIN_REQUEST,
 *                                 new Payloads.LoginPayload("nam", "123"));
 *   networkManager.send(req);
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private Object      payload;
    private String      requestId;     // UUID để correlate request ↔ response
    private String      sessionToken;  // auth token, null khi LOGIN
    private long        timestamp;

    public Message() {}

    public Message(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    /** Tạo request mới có sẵn UUID để client/server correlate. */
    public static Message request(MessageType type, Object payload) {
        Message m = new Message(type, payload);
        m.requestId = UUID.randomUUID().toString();
        return m;
    }

    /** Tạo response, gán cùng requestId của request gốc. */
    public static Message response(Message original, MessageType type, Object payload) {
        Message m = new Message(type, payload);
        m.requestId = original.getRequestId();
        m.sessionToken = original.getSessionToken();
        return m;
    }

    public MessageType getType()           { return type; }
    public Object      getPayload()        { return payload; }
    public String      getRequestId()      { return requestId; }
    public String      getSessionToken()   { return sessionToken; }
    public long        getTimestamp()      { return timestamp; }

    public void setType(MessageType type)              { this.type = type; }
    public void setPayload(Object payload)             { this.payload = payload; }
    public void setRequestId(String requestId)         { this.requestId = requestId; }
    public void setSessionToken(String sessionToken)   { this.sessionToken = sessionToken; }

    @Override
    public String toString() {
        return "Message{type=" + type
            + ", payload=" + (payload != null ? payload.getClass().getSimpleName() : "null")
            + ", requestId=" + requestId + "}";
    }
}
