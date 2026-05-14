package com.auction.shared;

/**
 * MessageType - tất cả loại message giữa Client ↔ Server.
 *
 * Đặt ở package SHARED vì cả 2 team (Client/Server) đều cần import.
 * Đây là HỢP ĐỒNG giao tiếp giữa 2 đầu - thay đổi 1 enum value
 * → BẮT BUỘC thông báo cả nhóm.
 */
public enum MessageType {

    /* ====== AUTHENTICATION ====== */
    LOGIN_REQUEST,
    LOGIN_RESPONSE,
    REGISTER_REQUEST,
    REGISTER_RESPONSE,
    LOGOUT_REQUEST,

    /* ====== AUCTION CRUD ====== */
    CREATE_AUCTION_REQUEST,
    CREATE_AUCTION_RESPONSE,
    GET_AUCTIONS_REQUEST,
    GET_AUCTIONS_RESPONSE,
    GET_AUCTION_DETAIL_REQUEST,
    GET_AUCTION_DETAIL_RESPONSE,

    /* ====== BIDDING (CORE) ====== */
    BID_REQUEST,
    BID_RESPONSE,
    UPDATE_AUCTION,        // broadcast khi có bid mới — quan trọng nhất

    /* ====== NOTIFICATIONS ====== */
    NOTIFICATION,
    GET_NOTIFICATIONS_REQUEST,
    GET_NOTIFICATIONS_RESPONSE,
    MARK_NOTI_READ,

    /* ====== WATCHLIST ====== */
    ADD_TO_WATCHLIST_REQUEST,
    REMOVE_FROM_WATCHLIST_REQUEST,
    GET_WATCHLIST_REQUEST,
    GET_WATCHLIST_RESPONSE,

    /* ====== PROFILE & ACCOUNT ====== */
    GET_PROFILE_REQUEST,
    GET_PROFILE_RESPONSE,
    UPDATE_PROFILE_REQUEST,
    UPDATE_PROFILE_RESPONSE,
    CHANGE_PASSWORD,

    /* ====== SEARCH & FILTER ====== */
    SEARCH_AUCTIONS_REQUEST,
    SEARCH_AUCTIONS_RESPONSE,

    /* ====== SYSTEM ====== */
    ERROR,
    OK,
    PING,
    PONG,
    FORCE_LOGOUT
}
