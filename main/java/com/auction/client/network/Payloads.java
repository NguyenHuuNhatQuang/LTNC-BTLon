package com.auction.client.network;

import java.io.Serializable;
import java.util.List;

/**
 * Payloads - container chứa toàn bộ DTO payload dùng cho NetworkMessage.
 * Mỗi inner record/class implements Serializable + serialVersionUID.
 * Cấu trúc đã chốt với S-Team trong HANDOFF-S-TEAM-internal §8.
 *
 * Sử dụng:
 *   NetworkManager.getInstance().send(MessageType.LOGIN_REQUEST,
 *       new Payloads.LoginPayload("nam_dev", "1234"));
 */
public final class Payloads {

    private Payloads() {}

    /* ===== AUTH ===== */
    public record LoginPayload(String username, String password) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    public record LoginResponsePayload(String userId, String username,
                                       String fullName, String role,
                                       String sessionToken, String errorMessage)
            implements Serializable {
        private static final long serialVersionUID = 1L;
        public boolean isSuccess() { return errorMessage == null; }
    }

    public record RegisterPayload(String firstName, String lastName,
                                  String username, String email,
                                  String password, String role)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ===== AUCTION LIST / DETAIL ===== */
    public record FetchFilter(String category, String status, String keyword,
                              String sortBy, int page, int pageSize)
            implements Serializable {
        private static final long serialVersionUID = 1L;
        public static FetchFilter all() { return new FetchFilter(null, null, null, "newest", 0, 20); }
    }

    public record AuctionListPayload(List<?> items, int totalCount, int currentPage)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    public record AuctionDetailPayload(Object auction, List<?> history, List<?> topBidders)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ===== BIDDING ===== */
    public record BidPayload(String auctionId, String bidderId, double amount)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    public record BidResponsePayload(boolean success, double newCurrentBid,
                                     String currentLeader, String errorMessage)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ===== CREATE ITEM ===== */
    public record CreateItemPayload(String name, String category, String condition,
                                    String description,
                                    double startPrice, double stepPrice,
                                    Double buyNowPrice,
                                    int durationHours,
                                    byte[] imageBytes)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ===== WATCHLIST ===== */
    public record WatchlistPayload(String userId, String auctionId)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ===== NOTIFICATION ===== */
    public record NotificationPayload(String id, String type, String title,
                                      String body, long timestamp, boolean unread)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ===== PROFILE ===== */
    public record ProfilePayload(String userId, String username, String fullName,
                                 String role, double walletBalance, double escrowAmount,
                                 int totalWonAuctions, int totalBids, double rating)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    /* ===== GENERIC ERROR ===== */
    public record ErrorPayload(String code, String message) implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
