import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MockAuctionServer — server đơn giản chạy 1 file để demo Socket realtime.
 *
 * Không phụ thuộc Maven, không phụ thuộc Message class S-Team.
 * Protocol: text dòng (đơn giản nhất).
 *
 * CHẠY:
 *   javac MockAuctionServer.java
 *   java MockAuctionServer
 *
 * Protocol Client → Server:
 *   LOGIN|username|password
 *   BID|auctionId|bidderName|amount
 *   QUIT
 *
 * Protocol Server → Client:
 *   LOGIN_OK|sessionId
 *   LOGIN_FAIL|reason
 *   BID_OK|auctionId|newPrice
 *   BID_FAIL|reason
 *   UPDATE_AUCTION|auctionId|newPrice|leaderName    (broadcast cho TẤT CẢ client)
 */
public class MockAuctionServer {

    private static final int PORT = 7070;
    private static final List<ClientConn> CLIENTS = new CopyOnWriteArrayList<>();
    private static final Map<String, Double> AUCTION_PRICE = new ConcurrentHashMap<>();
    private static final AtomicLong SESSION_SEQ = new AtomicLong(1000);

    public static void main(String[] args) throws IOException {
        // Seed 3 phiên đấu giá mock
        AUCTION_PRICE.put("A001", 42_500_000.0);
        AUCTION_PRICE.put("A002", 28_900_000.0);
        AUCTION_PRICE.put("A005", 120_000_000.0);

        ServerSocket ss = new ServerSocket(PORT);
        log("Server lắng nghe port " + PORT + "  (Ctrl+C để dừng)");
        log("Đã seed 3 phiên: A001, A002, A005");

        ExecutorService pool = Executors.newCachedThreadPool();
        while (true) {
            Socket s = ss.accept();
            ClientConn conn = new ClientConn(s);
            CLIENTS.add(conn);
            log("Client mới kết nối: " + s.getInetAddress() + "  (tổng: " + CLIENTS.size() + ")");
            pool.submit(conn);
        }
    }

    /** Broadcast 1 dòng text tới TẤT CẢ client đang kết nối. */
    static void broadcast(String line) {
        log("BROADCAST → " + CLIENTS.size() + " client: " + line);
        for (ClientConn c : CLIENTS) c.send(line);
    }

    static void log(String msg) {
        System.out.println("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + msg);
    }

    // ===================================================================
    static class ClientConn implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter   out;
        private String        username = "anon";

        ClientConn(Socket s) { this.socket = s; }

        public void send(String line) {
            if (out != null) { out.println(line); out.flush(); }
        }

        @Override
        public void run() {
            try {
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                String line;
                while ((line = in.readLine()) != null) {
                    log("RECV (" + username + "): " + line);
                    handle(line);
                }
            } catch (IOException e) {
                log("Client " + username + " disconnect: " + e.getMessage());
            } finally {
                CLIENTS.remove(this);
                try { socket.close(); } catch (IOException ignored) {}
                log("Đã remove " + username + " (còn " + CLIENTS.size() + ")");
            }
        }

        private void handle(String line) {
            String[] p = line.split("\\|");
            switch (p[0]) {
                case "LOGIN" -> {
                    if (p.length < 3 || p[2].length() < 4) {
                        send("LOGIN_FAIL|Mật khẩu phải >= 4 ký tự");
                        return;
                    }
                    username = p[1];
                    long sid = SESSION_SEQ.incrementAndGet();
                    send("LOGIN_OK|sess_" + sid);
                }
                case "BID" -> {
                    if (p.length < 4) { send("BID_FAIL|Sai format"); return; }
                    String auctionId = p[1];
                    String bidder    = p[2];
                    double amount;
                    try { amount = Double.parseDouble(p[3]); }
                    catch (NumberFormatException e) { send("BID_FAIL|Số tiền không hợp lệ"); return; }

                    Double current = AUCTION_PRICE.get(auctionId);
                    if (current == null) { send("BID_FAIL|Không tìm thấy phiên " + auctionId); return; }
                    if (amount < current + 500_000) {
                        send("BID_FAIL|Giá phải >= " + String.format("%.0f", current + 500_000));
                        return;
                    }

                    // Update + broadcast
                    AUCTION_PRICE.put(auctionId, amount);
                    send("BID_OK|" + auctionId + "|" + String.format("%.0f", amount));
                    broadcast("UPDATE_AUCTION|" + auctionId + "|" + String.format("%.0f", amount) + "|" + bidder);
                }
                case "QUIT" -> {
                    try { socket.close(); } catch (IOException ignored) {}
                }
                default -> send("ERROR|Lệnh không hỗ trợ: " + p[0]);
            }
        }
    }
}
