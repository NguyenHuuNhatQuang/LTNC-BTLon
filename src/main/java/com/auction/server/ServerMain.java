package com.auction.server;

import com.auction.server.network.AuctionServer;
import model.auction.Auction;
import model.auction.AuctionManager;
import model.item.Art;
import model.item.Electronics;
import model.item.Item;

import java.time.LocalDateTime;

/**
 * ServerMain - entry point của Auction Server.
 *
 * Chạy:
 *   mvn compile exec:java -Dexec.mainClass="com.auction.server.ServerMain"
 * Hoặc trong IntelliJ: right-click → Run 'ServerMain.main()'
 *
 * Server lắng nghe port 7070, seed 5 phiên đấu giá khớp ID với UI client.
 */
public class ServerMain {

    public static void main(String[] args) {
        AuctionServer server = new AuctionServer(7070);

        seedAuctions(server);
        server.start();

        System.out.println("================================================");
        System.out.println("  BidNow Server running on port 7070");
        System.out.println("  Press ENTER to stop");
        System.out.println("================================================");
        try {
            System.in.read();
        } catch (Exception ignored) {}

        server.stop();
        System.out.println("[ServerMain] Stopped.");
    }

    /**
     * Seed dữ liệu mock - ID khớp với UI client (DashboardController.mockData()).
     * Tuần 12 PDF: tích hợp E2E giữa UI và Server với data thống nhất.
     */
    private static void seedAuctions(AuctionServer server) {
        AuctionManager manager = AuctionManager.getInstance();
        LocalDateTime now = LocalDateTime.now();

        Auction[] auctions = new Auction[] {
            createAuction("A001", "MacBook Pro M3 14\"", "Electronics", "12",
                          now.minusMinutes(2), now.plusMinutes(30), 42_000_000.0, 500_000.0),
            createAuction("A002", "iPhone 15 Pro Max", "Electronics", "12",
                          now.minusMinutes(2), now.plusMinutes(20), 28_000_000.0, 500_000.0),
            createAuction("A003", "Tranh sơn dầu phố cổ", "Art", "Bùi Xuân Phái",
                          now.minusMinutes(2), now.plusHours(26), 8_000_000.0, 200_000.0),
            createAuction("A005", "Đồng hồ Rolex cổ", "Art", "Rolex Vintage",
                          now.minusMinutes(2), now.plusHours(5), 119_500_000.0, 1_000_000.0),
            createAuction("A006", "Sony A7 IV body", "Electronics", "24",
                          now.minusMinutes(2), now.plusHours(12), 44_500_000.0, 500_000.0)
        };

        for (Auction a : auctions) {
            manager.addAuction(a);
            a.startAuction();
            server.registerAuction(a);
            System.out.println("[Seed] " + a.getId() + " - " + a.getItem().getName()
                + " @ " + a.getCurrentHighestBid() + " ₫");
        }
    }

    private static Auction createAuction(String id, String itemName, String category,
                                         String extra, LocalDateTime start, LocalDateTime end,
                                         double startPrice, double stepPrice) {
        Item item;
        if ("Electronics".equals(category)) {
            item = new Electronics("I" + id, itemName, Integer.parseInt(extra));
        } else {
            item = new Art("I" + id, itemName, extra);
        }
        return new Auction(id, item, start, end, startPrice, stepPrice);
    }
}
