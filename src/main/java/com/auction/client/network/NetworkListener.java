package com.auction.client.network;

import com.auction.shared.Message;
import com.auction.shared.MessageType;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NetworkListener - Background thread đọc message từ server.
 * Task 4.5 PDF.
 *
 * FIX so với bản trước:
 *  - Bỏ logic switch-case trùng lặp (giờ luôn publish qua MessageBus).
 *    MessageBus đã auto chạy callback trên FX thread, không cần
 *    Platform.runLater ở đây nữa.
 *  - Tách rõ EOFException và SocketException (server disconnect normal vs lỗi)
 *  - Không tham chiếu ObjectOutputStream (PING handle về NetworkManager)
 */
public class NetworkListener implements Runnable {
    private final ObjectInputStream in;
    private final AtomicBoolean running  = new AtomicBoolean(true);
    private final AtomicBoolean stopped  = new AtomicBoolean(false);

    public NetworkListener(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        System.out.println("[NetworkListener] Started.");
        try {
            while (running.get()) {
                Object obj;
                try {
                    obj = in.readObject();        // CHẶN cho đến khi có dữ liệu
                } catch (EOFException | SocketException e) {
                    if (running.get()) {
                        System.out.println("[NetworkListener] Server disconnected: " + e.getClass().getSimpleName());
                    }
                    break;
                }

                if (!(obj instanceof Message msg)) {
                    System.err.println("[NetworkListener] Unknown object: " + obj);
                    continue;
                }

                // Tự động trả lời PING ngay tại đây (low-latency)
                if (msg.getType() == MessageType.PING) {
                    NetworkManager.getInstance().send(MessageType.PONG, null);
                    continue;
                }

                // Mọi message khác publish qua MessageBus
                // (MessageBus tự bọc Platform.runLater)
                MessageBus.getInstance().publish(msg);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (running.get()) {
                System.err.println("[NetworkListener] Error: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            stopped.set(true);
            // Báo Controller biết để hiện Alert "Mất kết nối"
            MessageBus.getInstance().publish(
                new Message(MessageType.ERROR, "Connection lost"));
            System.out.println("[NetworkListener] Stopped.");
        }
    }

    public void stop()         { running.set(false); }
    public boolean isStopped() { return stopped.get(); }
}
