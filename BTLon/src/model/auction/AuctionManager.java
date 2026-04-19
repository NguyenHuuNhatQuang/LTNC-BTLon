// File: model/auction/AuctionManager.java
package model.auction;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    private static AuctionManager instance;
    private List<Auction> activeAuctions;

    private AuctionManager() {
        activeAuctions = new ArrayList<>();
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addAuction(Auction auction) {
        activeAuctions.add(auction);
    }

    public List<Auction> getActiveAuctions() {
        return activeAuctions;
    }
}