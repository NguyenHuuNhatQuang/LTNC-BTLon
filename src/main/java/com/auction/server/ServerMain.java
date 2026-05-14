package com.auction.server;

import com.auction.server.network.AuctionServer;
import model.auction.Auction;
import model.auction.AuctionManager;
import model.item.Art;
import model.item.Electronics;

import java.time.LocalDateTime;

public class ServerMain {
    public static void main(String[] args) {
        AuctionServer server = new AuctionServer(7070);

        seedAuctions(server);
        server.start();

        System.out.println("Server is running on port 7070.");
        System.out.println("Press ENTER to exit.");
        try {
            System.in.read();
        } catch (Exception ignored) {
        }

        server.stop();
        System.out.println("Server stopped.");
    }

    private static void seedAuctions(AuctionServer server) {
        AuctionManager manager = AuctionManager.getInstance();

        Auction a1 = new Auction(
            "A1",
            new Art("I1", "Mona Lisa", "Leonardo da Vinci"),
            LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now().plusMinutes(15),
            1000.0,
            50.0
        );
        Auction a2 = new Auction(
            "A2",
            new Electronics("I2", "MacBook Pro", 12),
            LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now().plusMinutes(30),
            2500.0,
            100.0
        );

        manager.addAuction(a1);
        manager.addAuction(a2);

        a1.startAuction();
        a2.startAuction();

        server.registerAuction(a1);
        server.registerAuction(a2);
    }
}
