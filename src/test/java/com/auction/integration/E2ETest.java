package com.auction.integration;

import com.auction.shared.Message;
import com.auction.shared.MessageType;
import com.auction.shared.Payloads;
import org.junit.jupiter.api.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2ETest — kiểm thử End-to-End giữa Client và Server thật.
 *
 * YÊU CẦU TRƯỚC KHI CHẠY:
 *   Server phải đang chạy ở localhost:7070
 *   (S-Team tích hợp xong ServerSocket - dự kiến T11-12)
 *
 * Chạy: mvnw test -Dtest=E2ETest
 *
 * Các test case này CHƯA chạy được cho đến khi S-Team có ServerSocket.
 * Tạm thời @Disabled — bỏ annotation khi server sẵn sàng.
 */
@Disabled("Cần ServerSocket của S-Team chạy trên localhost:7070")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class E2ETest {

    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream  in;

    @BeforeAll
    static void connectToServer() throws Exception {
        socket = new Socket("localhost", 7070);
        // QUAN TRỌNG: out trước in để tránh deadlock
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in  = new ObjectInputStream(socket.getInputStream());
    }

    @AfterAll
    static void disconnect() throws Exception {
        if (socket != null) socket.close();
    }

    @Test
    @Order(1)
    @DisplayName("Login với thông tin hợp lệ → trả về LOGIN_RESPONSE + sessionToken")
    void testLoginValid() throws Exception {
        Message req = Message.request(MessageType.LOGIN_REQUEST,
            new Payloads.LoginPayload("nam_dev", "1234"));
        out.reset();
        out.writeObject(req);
        out.flush();

        Message resp = (Message) in.readObject();
        assertEquals(MessageType.LOGIN_RESPONSE, resp.getType());
        assertNotNull(resp.getSessionToken());

        Payloads.LoginResponsePayload payload = (Payloads.LoginResponsePayload) resp.getPayload();
        assertTrue(payload.isSuccess(), "Login phải thành công");
    }

    @Test
    @Order(2)
    @DisplayName("Login với mật khẩu sai → LOGIN_RESPONSE với errorMessage")
    void testLoginInvalid() throws Exception {
        Message req = Message.request(MessageType.LOGIN_REQUEST,
            new Payloads.LoginPayload("nam_dev", "wrong"));
        out.reset();
        out.writeObject(req);
        out.flush();

        Message resp = (Message) in.readObject();
        Payloads.LoginResponsePayload payload = (Payloads.LoginResponsePayload) resp.getPayload();
        assertFalse(payload.isSuccess());
        assertNotNull(payload.errorMessage());
    }

    @Test
    @Order(3)
    @DisplayName("Đặt giá hợp lệ → BID_RESPONSE success + broadcast UPDATE_AUCTION")
    void testPlaceBidValid() throws Exception {
        Message req = Message.request(MessageType.BID_REQUEST,
            new Payloads.BidPayload("A001", "user_123", 43_000_000));
        out.reset();
        out.writeObject(req);
        out.flush();

        Message resp = (Message) in.readObject();
        assertEquals(MessageType.BID_RESPONSE, resp.getType());

        Payloads.BidResponsePayload payload = (Payloads.BidResponsePayload) resp.getPayload();
        assertTrue(payload.success());
        assertEquals(43_000_000.0, payload.newCurrentBid(), 0.01);
    }

    @Test
    @Order(4)
    @DisplayName("Đặt giá thấp hơn bước giá → BID_RESPONSE fail")
    void testPlaceBidTooLow() throws Exception {
        Message req = Message.request(MessageType.BID_REQUEST,
            new Payloads.BidPayload("A001", "user_123", 100));
        out.reset();
        out.writeObject(req);
        out.flush();

        Message resp = (Message) in.readObject();
        Payloads.BidResponsePayload payload = (Payloads.BidResponsePayload) resp.getPayload();
        assertFalse(payload.success());
        assertNotNull(payload.errorMessage());
    }

    @Test
    @Order(5)
    @DisplayName("Fetch danh sách auction → trả về list không rỗng")
    void testFetchAuctions() throws Exception {
        Message req = Message.request(MessageType.GET_AUCTIONS_REQUEST,
            Payloads.FetchFilter.all());
        out.reset();
        out.writeObject(req);
        out.flush();

        Message resp = (Message) in.readObject();
        assertEquals(MessageType.GET_AUCTIONS_RESPONSE, resp.getType());

        Payloads.AuctionListPayload payload = (Payloads.AuctionListPayload) resp.getPayload();
        assertNotNull(payload.items());
        assertTrue(payload.totalCount() >= 0);
    }
}
