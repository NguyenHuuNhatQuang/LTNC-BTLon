package com.auction.client.util;

import com.auction.client.model.AuctionView;
import javafx.application.Platform;

import java.util.function.Consumer;

/**
 * Tuần 10 - Task 5.3:
 * Cầu nối giữa tầng Network (do C-Team network engineer làm) và Controller.
 *
 * Controller subscribe vào sự kiện bằng cách gọi:
 *   NetworkBridge.onAuctionUpdate(auction -> applyUpdate(auction));
 *   NetworkBridge.onConnectionLost(() -> showOfflineMode());
 *
 * Tầng Network khi nhận UPDATE_AUCTION từ Server gọi:
 *   NetworkBridge.fireAuctionUpdate(auction);
 *
 * Tách interface để UI không phụ thuộc trực tiếp vào NetworkManager.
 * Khi network chưa sẵn sàng, UI vẫn chạy được với mock data.
 */
public final class NetworkBridge {

    private static Consumer<AuctionView> auctionUpdateListener;
    private static Consumer<String>      bidAcceptedListener;
    private static Consumer<String>      bidRejectedListener;
    private static Runnable              connectionLostListener;
    private static Runnable              connectionRestoredListener;

    private NetworkBridge() {}

    /* ========== SUBSCRIBE (Controller gọi) ========== */

    public static void onAuctionUpdate(Consumer<AuctionView> listener) {
        auctionUpdateListener = listener;
    }

    public static void onBidAccepted(Consumer<String> listener) {
        bidAcceptedListener = listener;
    }

    public static void onBidRejected(Consumer<String> listener) {
        bidRejectedListener = listener;
    }

    public static void onConnectionLost(Runnable listener) {
        connectionLostListener = listener;
    }

    public static void onConnectionRestored(Runnable listener) {
        connectionRestoredListener = listener;
    }

    /* ========== FIRE (Network layer gọi) ========== */

    /** Tầng Network gọi khi nhận UPDATE_AUCTION broadcast. */
    public static void fireAuctionUpdate(AuctionView auction) {
        invoke(() -> {
            if (auctionUpdateListener != null) auctionUpdateListener.accept(auction);
        });
    }

    public static void fireBidAccepted(String confirmationMsg) {
        invoke(() -> {
            if (bidAcceptedListener != null) bidAcceptedListener.accept(confirmationMsg);
        });
    }

    public static void fireBidRejected(String reason) {
        invoke(() -> {
            if (bidRejectedListener != null) bidRejectedListener.accept(reason);
        });
    }

    public static void fireConnectionLost() {
        invoke(() -> {
            ConnectionStatusBar.showDisconnected();
            if (connectionLostListener != null) connectionLostListener.run();
        });
    }

    public static void fireConnectionRestored() {
        invoke(() -> {
            ConnectionStatusBar.showConnected();
            if (connectionRestoredListener != null) connectionRestoredListener.run();
        });
    }

    /** Reset khi đổi scene để tránh leak listener cũ. */
    public static void clear() {
        auctionUpdateListener      = null;
        bidAcceptedListener        = null;
        bidRejectedListener        = null;
        connectionLostListener     = null;
        connectionRestoredListener = null;
    }

    private static void invoke(Runnable r) {
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }
}
