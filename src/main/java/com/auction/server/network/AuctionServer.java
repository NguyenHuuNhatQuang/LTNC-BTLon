package com.auction.server.network;

import com.auction.client.network.MessageType;
import com.auction.client.network.NetworkMessage;
import com.auction.client.network.Payloads;
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

            // Attach update observer to any auction present at startup.
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
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
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
        broadcast(new NetworkMessage(MessageType.UPDATE_AUCTION, summary));
    }

    public void broadcast(NetworkMessage message) {
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
