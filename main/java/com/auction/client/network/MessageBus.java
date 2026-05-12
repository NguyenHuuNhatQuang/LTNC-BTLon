package com.auction.client.network;

import javafx.application.Platform;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * MessageBus - Event bus pub/sub Observer pattern.
 * Task 4.5 + 6.3: cầu nối Listener thread <-> Controller (UI thread).
 *
 * FIX so với bản trước:
 *  - Đổi HashMap + synchronized -> ConcurrentHashMap + CopyOnWriteArrayList
 *    (tránh deadlock khi callback gọi lại subscribe/unsubscribe)
 *  - Callback gọi NGOÀI lock, KHÔNG iterate khi đang giữ lock
 *  - publish() auto bọc Platform.runLater nếu không đang ở FX thread
 *    (double-protect, Controller dùng yên tâm)
 */
public class MessageBus {
    private static final MessageBus instance = new MessageBus();

    private final Map<MessageType, List<Consumer<NetworkMessage>>> subscribers
        = new ConcurrentHashMap<>();

    private MessageBus() {}
    public static MessageBus getInstance() { return instance; }

    public void subscribe(MessageType type, Consumer<NetworkMessage> callback) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(callback);
    }

    public void unsubscribe(MessageType type, Consumer<NetworkMessage> callback) {
        List<Consumer<NetworkMessage>> list = subscribers.get(type);
        if (list != null) list.remove(callback);
    }

    /**
     * Publish 1 message. Tự đảm bảo callback chạy trên JavaFX thread.
     */
    public void publish(NetworkMessage message) {
        List<Consumer<NetworkMessage>> list = subscribers.get(message.getType());
        if (list == null || list.isEmpty()) return;

        Runnable dispatch = () -> {
            for (Consumer<NetworkMessage> cb : list) {  // CopyOnWriteArrayList -> safe iterate
                try { cb.accept(message); }
                catch (Exception e) {
                    System.err.println("[MessageBus] Subscriber error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        if (Platform.isFxApplicationThread()) dispatch.run();
        else Platform.runLater(dispatch);
    }

    public void publish(MessageType type, Object payload) {
        publish(new NetworkMessage(type, payload));
    }

    public void clear() { subscribers.clear(); }

    public void printSubscribers() {
        subscribers.forEach((t, l) -> System.out.println(t + " -> " + l.size() + " subs"));
    }
}
