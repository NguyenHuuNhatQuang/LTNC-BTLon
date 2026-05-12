package com.auction.client.network;

/**
 * MessageType - Enum định nghĩa tất cả loại message giữa Client-Server.
 * Task 4.4: Xây dựng Client Socket
 */
public enum MessageType {
    // Authentication
    LOGIN_REQUEST,
    LOGIN_RESPONSE,
    REGISTER_REQUEST,
    REGISTER_RESPONSE,
    LOGOUT_REQUEST,

    // Auction Operations
    CREATE_AUCTION_REQUEST,
    CREATE_AUCTION_RESPONSE,
    GET_AUCTIONS_REQUEST,
    GET_AUCTIONS_RESPONSE,
    GET_AUCTION_DETAIL_REQUEST,
    GET_AUCTION_DETAIL_RESPONSE,

    // Bidding
    BID_REQUEST,
    BID_RESPONSE,
    UPDATE_AUCTION,  // Broadcast khi có bid mới từ bất kỳ client nào

    // Notifications
    NOTIFICATION,
    GET_NOTIFICATIONS_REQUEST,
    GET_NOTIFICATIONS_RESPONSE,

    // Watchlist
    ADD_TO_WATCHLIST_REQUEST,
    REMOVE_FROM_WATCHLIST_REQUEST,
    GET_WATCHLIST_REQUEST,
    GET_WATCHLIST_RESPONSE,

    // Profile & Account
    UPDATE_PROFILE_REQUEST,
    UPDATE_PROFILE_RESPONSE,
    GET_PROFILE_REQUEST,
    GET_PROFILE_RESPONSE,

    // Search & Filter
    SEARCH_AUCTIONS_REQUEST,
    SEARCH_AUCTIONS_RESPONSE,

    // Error
    ERROR,

    // Connection
    PING,
    PONG
}
