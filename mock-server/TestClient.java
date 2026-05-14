import java.io.*;
import java.net.Socket;

/**
 * TestClient — kiểm tra MockAuctionServer chạy được.
 *
 * BƯỚC:
 *   1. Mở terminal A:   javac MockAuctionServer.java && java MockAuctionServer
 *   2. Mở terminal B:   javac TestClient.java && java TestClient nam_dev
 *   3. Mở terminal C:   javac TestClient.java && java TestClient minh_tran
 *   4. Cả 2 client gửi BID, sẽ thấy server BROADCAST cho cả 2
 *
 * Khi gõ tin nhắn:
 *   LOGIN|nam_dev|1234
 *   BID|A001|nam_dev|43000000
 *   QUIT
 */
public class TestClient {

    public static void main(String[] args) throws Exception {
        String name = args.length > 0 ? args[0] : "client_" + System.currentTimeMillis() % 100;

        Socket s = new Socket("localhost", 7070);
        BufferedReader in  = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
        PrintWriter   out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"), true);

        // Thread đọc message từ server ngầm
        Thread listener = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(">>> " + line);
                }
            } catch (IOException ignored) {}
        });
        listener.setDaemon(true);
        listener.start();

        // Auto login
        out.println("LOGIN|" + name + "|1234");
        Thread.sleep(300);

        // Đọc input từ console gửi đi
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Đã login như " + name + ". Gõ message rồi Enter (hoặc QUIT để thoát):");
        System.out.println("Vd: BID|A001|" + name + "|43000000");

        String cmd;
        while ((cmd = console.readLine()) != null) {
            if (cmd.isBlank()) continue;
            out.println(cmd);
            if (cmd.equalsIgnoreCase("QUIT")) break;
        }
        s.close();
    }
}
