package com.auction.client.network;

import com.auction.shared.Message;
import com.auction.shared.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NetworkManager - Singleton quản lý Socket connection với Server.
 * Task 4.4: Xây dựng Client Socket
 *
 * Lưu ý quan trọng:
 *  - ObjectOutputStream PHẢI tạo TRƯỚC ObjectInputStream ở cả 2 đầu (tránh deadlock)
 *  - PHẢI gọi out.reset() trước mỗi writeObject() (tránh ObjectStream cache - PDF T10 §5.1)
 *  - Port mặc định 7070 (đã chốt với S-Team trong HANDOFF-S-TEAM-internal §7)
 */
public class NetworkManager {
    private static final NetworkManager instance = new NetworkManager();

    private static final String DEFAULT_SERVER_HOST = "localhost";
    private static final int    DEFAULT_SERVER_PORT = 7070;          // FIX: 8888 -> 7070

    private String serverHost;
    private int    serverPort;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;

    private NetworkListener listener;
    private Thread          listenerThread;

    private final AtomicBoolean isConnected   = new AtomicBoolean(false);
    private final Object        connectionLock = new Object();

    private String sessionToken;  // Thêm: lưu token sau LOGIN_OK

    private NetworkManager() {
        this.serverHost = DEFAULT_SERVER_HOST;
        this.serverPort = DEFAULT_SERVER_PORT;
    }

    public static NetworkManager getInstance() { return instance; }

    public synchronized boolean connect(String host, int port) {
        if (isConnected.get()) {
            System.out.println("[NetworkManager] Already connected to " + serverHost + ":" + serverPort);
            return true;
        }

        try {
            synchronized (connectionLock) {
                System.out.println("[NetworkManager] Connecting to " + host + ":" + port + "...");
                this.serverHost = host;
                this.serverPort = port;

                socket = new Socket(host, port);

                // QUAN TRỌNG: Khởi tạo Output TRƯỚC Input để tránh deadlock
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in  = new ObjectInputStream(socket.getInputStream());

                isConnected.set(true);

                listener = new NetworkListener(in);
                listenerThread = new Thread(listener, "NetworkListener");
                listenerThread.setDaemon(true);
                listenerThread.start();
                System.out.println("[NetworkManager] Connected. Listener started.");
                return true;
            }
        } catch (IOException e) {
            System.err.println("[NetworkManager] Connection failed: " + e.getMessage());
            isConnected.set(false);
            cleanupResources();
            return false;
        }
    }

    public synchronized boolean connect() { return connect(serverHost, serverPort); }

    public boolean isConnected() {
        return isConnected.get() && socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Gửi message đến server.
     * CRITICAL: gọi out.reset() trước writeObject() để tránh client nhận object cache cũ.
     */
    public boolean send(Message message) {
        if (!isConnected()) {
            System.err.println("[NetworkManager] Not connected. Cannot send: " + message.getType());
            return false;
        }
        // Auto inject session token nếu đã login
        if (sessionToken != null && message.getSessionToken() == null) {
            message.setSessionToken(sessionToken);
        }
        try {
            synchronized (out) {
                out.reset();              // ⭐ FIX: chống ObjectStream cache (PDF T10)
                out.writeObject(message);
                out.flush();
            }
            return true;
        } catch (IOException e) {
            System.err.println("[NetworkManager] Send failed: " + e.getMessage());
            handleDisconnection();
            return false;
        }
    }

    public boolean send(MessageType type, Object payload) {
        return send(new Message(type, payload));
    }

    public synchronized void disconnect() {
        if (!isConnected.get()) return;
        System.out.println("[NetworkManager] Disconnecting...");

        if (listener != null) listener.stop();

        // FIX: dùng socket.close() để force ngắt readObject() đang block
        // (chứ Thread.sleep(500) không reliable)
        cleanupResources();
        isConnected.set(false);

        // Đợi listener thread thoát hẳn
        if (listenerThread != null) {
            try {
                listenerThread.join(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("[NetworkManager] Disconnected.");
    }

    private void cleanupResources() {
        synchronized (connectionLock) {
            close(in);   in  = null;
            close(out);  out = null;
            close(socket); socket = null;
        }
    }

    private void close(AutoCloseable c) {
        if (c == null) return;
        try { c.close(); } catch (Exception ignored) {}
    }

    private void handleDisconnection() {
        System.err.println("[NetworkManager] Connection lost!");
        isConnected.set(false);
        cleanupResources();
        MessageBus.getInstance().publish(
            new Message(MessageType.ERROR, "Connection lost with server"));
    }

    public void setSessionToken(String token) { this.sessionToken = token; }
    public String getSessionToken() { return sessionToken; }

    public String getServerHost() { return serverHost; }
    public int    getServerPort() { return serverPort; }

    public void printStatus() {
        System.out.println("[NetworkManager] connected=" + isConnected()
            + ", server=" + serverHost + ":" + serverPort
            + ", listener=" + (listener != null && !listener.isStopped()));
    }
}
