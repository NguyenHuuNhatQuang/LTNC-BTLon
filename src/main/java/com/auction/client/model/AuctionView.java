package com.auction.client.model;

import javafx.beans.property.*;

/**
 * View model dùng cho TableView/Card ở Dashboard.
 * Lưu ý: dùng JavaFX Property để TableView tự cập nhật khi data đổi.
 * Sau này (Tuần 9+) khi có Socket, ta sẽ map từ entity Auction (S-Team) sang lớp này.
 */
public class AuctionView {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty itemName = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final StringProperty sellerName = new SimpleStringProperty();
    private final DoubleProperty currentBid = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty timeLeft = new SimpleStringProperty();
    private final StringProperty imageUrl = new SimpleStringProperty();

    public AuctionView(String id, String itemName, String category, String sellerName,
                       double currentBid, String status, String timeLeft, String imageUrl) {
        this.id.set(id);
        this.itemName.set(itemName);
        this.category.set(category);
        this.sellerName.set(sellerName);
        this.currentBid.set(currentBid);
        this.status.set(status);
        this.timeLeft.set(timeLeft);
        this.imageUrl.set(imageUrl);
    }

    public StringProperty idProperty()         { return id; }
    public StringProperty itemNameProperty()   { return itemName; }
    public StringProperty categoryProperty()   { return category; }
    public StringProperty sellerNameProperty() { return sellerName; }
    public DoubleProperty currentBidProperty() { return currentBid; }
    public StringProperty statusProperty()     { return status; }
    public StringProperty timeLeftProperty()   { return timeLeft; }
    public StringProperty imageUrlProperty()   { return imageUrl; }

    public String getId()         { return id.get(); }
    public String getItemName()   { return itemName.get(); }
    public String getCategory()   { return category.get(); }
    public String getSellerName() { return sellerName.get(); }
    public double getCurrentBid() { return currentBid.get(); }
    public String getStatus()     { return status.get(); }
    public String getTimeLeft()   { return timeLeft.get(); }
    public String getImageUrl()   { return imageUrl.get(); }
}
