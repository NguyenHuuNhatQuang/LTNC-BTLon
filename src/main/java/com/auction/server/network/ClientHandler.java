package com.auction.server.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import com.auction.shared.Message;
import com.auction.shared.MessageType;
import com.auction.shared.Payloads;

import model.auction.Auction;
import model.auction.AuctionManager;
import model.user.Bidder;

/**
 * ClientHandler - mỗi Client kết nối có 1 instance chạy trên thread riêng.
 *
 * Task 4.2 PDF: 1 thread per client.
 * Task 4.3 PDF: Đọc Message từ Socket, xử lý theo Type.
 *
 * FIXED so với phiên bản gốc trên s-network:
 *  - Import từ com.auction.shared.* (KHÔNG từ client.network)
 *  - Đổi NetworkMessage -> Message
 *  - Constructor 3-arg đổi thành builder (new Message + setSessionToken)
 *  - Constructor 1-arg đổi thành 2-arg với payload=null
 */
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
        // QUAN TRỌNG: out tạo TRƯỚC in để tránh deadlock (PDF cảnh báo)
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (connected) {
                Object obj = in.readObject();
                if (!(obj instanceof Message message)) {
                    send(new Message(MessageType.ERROR,
                        new Payloads.ErrorPayload("INVALID_MESSAGE", "Unknown object received")));
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

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case PING -> send(new Message(MessageType.PONG, null));
            case LOGIN_REQUEST -> handleLogin((Payloads.LoginPayload) message.getPayload());
            case GET_AUCTIONS_REQUEST -> handleGetAuctions();
            case BID_REQUEST -> handleBidRequest((Payloads.BidPayload) message.getPayload());
            case LOGOUT_REQUEST -> handleLogout();
            default -> send(new Message(MessageType.ERROR,
                    new Payloads.ErrorPayload("UNSUPPORTED", "Message type not supported: " + message.getType())));
        }
    }

    private void handleLogin(Payloads.LoginPayload payload) {
        if (payload == null || payload.username() == null || payload.password() == null) {
            sendLoginError("Missing username or password.");
            return;
        }
        // Demo: chưa check password thật, accept mọi user (cho test E2E nhanh)
        this.username = payload.username();
        this.sessionToken = UUID.randomUUID().toString();

        Payloads.LoginResponsePayload response = new Payloads.LoginResponsePayload(
                username, username, username, "bidder", sessionToken, null);
        Message resp = new Message(MessageType.LOGIN_RESPONSE, response);
        resp.setSessionToken(sessionToken);
        send(resp);
    }

    private void sendLoginError(String error) {
        send(new Message(MessageType.LOGIN_RESPONSE,
                new Payloads.LoginResponsePayload(null, null, null, null, null, error)));
    }

    private void handleGetAuctions() {
        var items = AuctionManager.getInstance().getActiveAuctions().stream()
                .map(Payloads.AuctionSummaryPayload::fromAuction)
                .toList();
        Payloads.AuctionListPayload payload = new Payloads.AuctionListPayload(items, items.size(), 0);
        send(new Message(MessageType.GET_AUCTIONS_RESPONSE, payload));
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
        Payloads.BidResponsePayload response =
            new Payloads.BidResponsePayload(success, currentBid, currentLeader, errorMessage);
        Message msg = new Message(MessageType.BID_RESPONSE, response);
        if (sessionToken != null) msg.setSessionToken(sessionToken);
        send(msg);
    }

    private void handleLogout() {
        Message ok = new Message(MessageType.OK, null);
        if (sessionToken != null) ok.setSessionToken(sessionToken);
        send(ok);
        closeConnection();
    }

    private Auction findAuction(String auctionId) {
        return AuctionManager.getInstance().getActiveAuctions().stream()
                .filter(a -> a.getId().equals(auctionId))
                .findFirst()
                .orElse(null);
    }

    public void send(Message message) {
        if (!connected) return;
        try {
            synchronized (out) {
                out.reset();   // CHỐNG ObjectStream CACHE (PDF Tuần 10 §5.1)
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
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
    }
}
