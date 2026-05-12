package com.auction.client.network;

import java.io.Serializable;

/**
 * NetworkMessage - Container cho tất cả message giữa Client-Server.
 * Implements Serializable để có thể send qua ObjectOutputStream/ObjectInputStream.
 * Task 4.4: Xây dựng Client Socket
 */
public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private Object payload;
    private String senderId;
    private long timestamp;

    /**
     * Constructor đầy đủ
     */
    public NetworkMessage(MessageType type, Object payload, String senderId) {
        this.type = type;
        this.payload = payload;
        this.senderId = senderId;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Constructor không có senderId (sử dụng cho request từ client)
     */
    public NetworkMessage(MessageType type, Object payload) {
        this(type, payload, null);
    }

    /**
     * Constructor chỉ có type (cho các message đơn giản như PING, PONG)
     */
    public NetworkMessage(MessageType type) {
        this(type, null, null);
    }

    // Getters
    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    public String getSenderId() {
        return senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setType(MessageType type) {
        this.type = type;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    @Override
    public String toString() {
        return "NetworkMessage{" +
                "type=" + type +
                ", senderId='" + senderId + '\'' +
                ", timestamp=" + timestamp +
                ", payload=" + (payload != null ? payload.getClass().getSimpleName() : "null") +
                '}';
    }
}
