package com.auction.server.network;

import com.auction.shared.Message;
import com.auction.shared.MessageType;
import com.auction.shared.Payloads;
import model.auction.Auction;
import model.auction.AuctionManager;
import pattern.Observer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AuctionServer - Quản lý ServerSocket và danh sách ClientHandler.
 *
 * Task 4.2 PDF: ServerSocket(port) + ExecutorService cho mỗi client.
 * Task 5.1 PDF: Broadcast UPDATE_AUCTION khi Auction (Subject) notify.
 *
 * FIXED so với phiên bản gốc trên s-network:
 *  - Import từ com.auction.shared.* (KHÔNG còn từ client.network)
 *  - Đổi NetworkMessage -> Message theo refactor của C-Team
 */
public class AuctionServer {
    private final int port;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    private Thread acceptThread;

    public AuctionServer(int port) {
        this.port = port;
    }

    public void start() {
        if (running.get()) return;
        running.set(true);
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[AuctionServer] Listening on port " + port);

            // Attach update observer cho mọi auction hiện có
            for (Auction auction : AuctionManager.getInstance().getActiveAuctions()) {
                registerAuction(auction);
            }

            acceptThread = new Thread(this::acceptLoop, "AuctionServer-Accept");
            acceptThread.setDaemon(true);
            acceptThread.start();
        } catch (IOException e) {
            throw new RuntimeException("Unable to start server", e);
        }
    }

    public void stop() {
        running.set(false);
        if (serverSocket != null) {
            try { serverSocket.close(); } catch (IOException ignored) {}
        }
        for (ClientHandler client : clients) {
            client.closeConnection();
        }
        executor.shutdownNow();
        if (acceptThread != null) {
            try {
                acceptThread.join(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("[AuctionServer] New connection from " + socket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(socket, this);
                addClient(handler);
                executor.submit(handler);
            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("[AuctionServer] Accept error: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Gắn Observer vào Auction - khi có bid mới sẽ tự broadcast UPDATE_AUCTION
     * cho TẤT CẢ client đang kết nối (Task 5.1 PDF).
     */
    public void registerAuction(Auction auction) {
        auction.attach(new Observer() {
            @Override
            public void update(Object data) {
                if (data instanceof Auction updated) {
                    broadcastUpdate(updated);
                }
            }
        });
    }

    public void broadcastUpdate(Auction auction) {
        Payloads.AuctionSummaryPayload summary = Payloads.AuctionSummaryPayload.fromAuction(auction);
        broadcast(new Message(MessageType.UPDATE_AUCTION, summary));
    }

    public void broadcast(Message message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    public void addClient(ClientHandler client) {
        clients.add(client);
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}
