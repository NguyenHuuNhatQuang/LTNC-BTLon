package com.auction.server.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import com.auction.client.network.MessageType;
import com.auction.client.network.NetworkMessage;
import com.auction.client.network.Payloads;

import model.auction.Auction;
import model.auction.AuctionManager;
import model.user.Bidder;

public class ClientHandler implements Runnable {
    private final AuctionServer server;
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean connected = true;
    private String sessionToken;
    private String username;

    public ClientHandler(Socket socket, AuctionServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (connected) {
                Object obj = in.readObject();
                if (!(obj instanceof NetworkMessage message)) {
                    send(new NetworkMessage(MessageType.ERROR, new Payloads.ErrorPayload("INVALID_MESSAGE", "Unknown object received")));
                    continue;
                }
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (connected) {
                System.err.println("[ClientHandler] Connection error: " + e.getMessage());
            }
        } finally {
            closeConnection();
        }
    }

    private void handleMessage(NetworkMessage message) {
        switch (message.getType()) {
            case PING -> send(new NetworkMessage(MessageType.PONG));
            case LOGIN_REQUEST -> handleLogin((Payloads.LoginPayload) message.getPayload());
            case GET_AUCTIONS_REQUEST -> handleGetAuctions();
            case BID_REQUEST -> handleBidRequest((Payloads.BidPayload) message.getPayload());
            case LOGOUT_REQUEST -> handleLogout();
            default -> send(new NetworkMessage(MessageType.ERROR,
                    new Payloads.ErrorPayload("UNSUPPORTED", "Message type not supported: " + message.getType())));
        }
    }

    private void handleLogin(Payloads.LoginPayload payload) {
        if (payload == null || payload.username() == null || payload.password() == null) {
            sendLoginError("Missing username or password.");
            return;
        }
        this.username = payload.username();
        this.sessionToken = UUID.randomUUID().toString();

        Payloads.LoginResponsePayload response = new Payloads.LoginResponsePayload(
                username,
                username,
                username,
                "bidder",
                sessionToken,
                null
        );
        send(new NetworkMessage(MessageType.LOGIN_RESPONSE, response, sessionToken));
    }

    private void sendLoginError(String error) {
        send(new NetworkMessage(MessageType.LOGIN_RESPONSE,
                new Payloads.LoginResponsePayload(null, null, null, null, null, error)));
    }

    private void handleGetAuctions() {
        var items = AuctionManager.getInstance().getActiveAuctions().stream()
                .map(Payloads.AuctionSummaryPayload::fromAuction)
                .toList();
        Payloads.AuctionListPayload payload = new Payloads.AuctionListPayload(items, items.size(), 0);
        send(new NetworkMessage(MessageType.GET_AUCTIONS_RESPONSE, payload));
    }

    private void handleBidRequest(Payloads.BidPayload payload) {
        if (payload == null) {
            sendBidResponse(false, 0, null, "Bid request is empty.");
            return;
        }
        Auction auction = findAuction(payload.auctionId());
        if (auction == null) {
            sendBidResponse(false, 0, null, "Auction not found: " + payload.auctionId());
            return;
        }
        try {
            auction.placeBid(new Bidder(payload.bidderId(), payload.bidderId()), payload.amount());
            sendBidResponse(true, auction.getCurrentHighestBid(), payload.bidderId(), null);
            server.broadcastUpdate(auction);
        } catch (Exception e) {
            sendBidResponse(false, auction.getCurrentHighestBid(), null, e.getMessage());
        }
    }

    private void sendBidResponse(boolean success, double currentBid, String currentLeader, String errorMessage) {
        Payloads.BidResponsePayload response = new Payloads.BidResponsePayload(success, currentBid, currentLeader, errorMessage);
        send(new NetworkMessage(MessageType.BID_RESPONSE, response, sessionToken));
    }

    private void handleLogout() {
        send(new NetworkMessage(MessageType.LOGOUT_REQUEST, null, sessionToken));
        closeConnection();
    }

    private Auction findAuction(String auctionId) {
        return AuctionManager.getInstance().getActiveAuctions().stream()
                .filter(a -> a.getId().equals(auctionId))
                .findFirst()
                .orElse(null);
    }

    public void send(NetworkMessage message) {
        if (!connected) return;
        try {
            synchronized (out) {
                out.reset();
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("[ClientHandler] Send failed: " + e.getMessage());
            closeConnection();
        }
    }

    public void closeConnection() {
        if (!connected) return;
        connected = false;
        server.removeClient(this);
        try {
            if (in != null) in.close();
        } catch (IOException ignored) {
        }
        try {
            if (out != null) out.close();
        } catch (IOException ignored) {
        }
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {
        }
    }
}
