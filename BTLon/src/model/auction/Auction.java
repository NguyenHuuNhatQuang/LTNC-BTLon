package model.auction;
import model.item.Item;
import model.user.Bidder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Auction {
    private String id;
    private Item item;
    private List<Bidder> bidders;
    private List<BidTransaction> bidTransactions;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double currentHighestBid;
    private AuctionStatus status;

    public Auction(String id, Item item, LocalDateTime startTime, LocalDateTime endTime, double startingPrice) {
        this.id = id;
        this.item = item;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentHighestBid = startingPrice;
        this.status = AuctionStatus.OPEN;
        this.bidders = new ArrayList<>();
        this.bidTransactions = new ArrayList<>();
    }

    public boolean placeBid(Bidder bidder, double amount) {
        if (this.status == AuctionStatus.RUNNING && amount > currentHighestBid) {
            this.currentHighestBid = amount;
            this.bidTransactions.add(new BidTransaction(bidder, amount));
            if (!this.bidders.contains(bidder)) {
                this.bidders.add(bidder);
            }
            return true;
        }
        return false;
    }

    public void startAuction() {
        this.status = AuctionStatus.RUNNING;
    }

    // Getters cho các thuộc tính private...
}