package com.auction.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho Message - đảm bảo factory + getter/setter hoạt động đúng.
 * Chạy: mvnw test
 */
class MessageTest {

    @Test
    void testCreateMessage_setsTypeAndPayload() {
        Payloads.LoginPayload payload = new Payloads.LoginPayload("nam_dev", "1234");
        Message m = new Message(MessageType.LOGIN_REQUEST, payload);

        assertEquals(MessageType.LOGIN_REQUEST, m.getType());
        assertSame(payload, m.getPayload());
        assertTrue(m.getTimestamp() > 0);
    }

    @Test
    void testRequest_generatesRequestId() {
        Message req = Message.request(MessageType.BID_REQUEST,
            new Payloads.BidPayload("A001", "user", 1000));
        assertNotNull(req.getRequestId());
        assertEquals(36, req.getRequestId().length()); // UUID length
    }

    @Test
    void testResponse_inheritsRequestIdAndToken() {
        Message req = Message.request(MessageType.LOGIN_REQUEST,
            new Payloads.LoginPayload("a", "b"));
        req.setSessionToken("token_xyz");

        Message resp = Message.response(req, MessageType.LOGIN_RESPONSE, "ok");
        assertEquals(req.getRequestId(), resp.getRequestId());
        assertEquals("token_xyz", resp.getSessionToken());
    }

    @Test
    void testLoginResponsePayload_isSuccess() {
        Payloads.LoginResponsePayload ok = new Payloads.LoginResponsePayload(
            "u1", "nam", "Nam Dev", "BIDDER", "tok", null);
        assertTrue(ok.isSuccess());

        Payloads.LoginResponsePayload fail = new Payloads.LoginResponsePayload(
            null, null, null, null, null, "Sai mật khẩu");
        assertFalse(fail.isSuccess());
    }

    @Test
    void testFetchFilter_allDefault() {
        Payloads.FetchFilter f = Payloads.FetchFilter.all();
        assertNull(f.category());
        assertEquals(0, f.page());
        assertEquals(20, f.pageSize());
        assertEquals("newest", f.sortBy());
    }

    @Test
    void testToString_doesNotLeakSensitiveData() {
        Message m = new Message(MessageType.LOGIN_REQUEST,
            new Payloads.LoginPayload("nam", "SECRET_PASSWORD"));
        String s = m.toString();
        assertFalse(s.contains("SECRET_PASSWORD"),
            "toString() KHÔNG được lộ password");
    }
}
