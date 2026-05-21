package com.auction.client.controller;

import com.auction.client.model.AuctionView;
import com.auction.client.util.AlertHelper;
import com.auction.client.util.NetworkBridge;
import com.auction.client.util.SceneRouter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class WatchlistController implements Initializable {

    @FXML private Label totalCountLbl;
    @FXML private Label totalValueLbl;
    @FXML private VBox watchList;

    @FXML private Button tabWatching, tabEnding, tabWon, tabLost;

    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<AuctionView> data = List.of(
            new AuctionView("A001", "MacBook Pro M3 14\"", "Điện tử", "minh.tran", 42500000, "LIVE", "02:34", ""),
            new AuctionView("A005", "Đồng hồ Rolex cổ",   "Trang sức","luxury.vn",120000000, "LIVE", "05:12", ""),
            new AuctionView("A002", "iPhone 15 Pro Max",  "Điện tử", "hoa.nguyen",28900000, "LIVE", "00:48", ""),
            new AuctionView("A006", "Sony A7 IV body",    "Điện tử", "camera.pro",45000000, "OPEN", "12h",   ""),
            new AuctionView("A003", "Tranh sơn dầu",      "Nghệ thuật","art.studio",8500000, "OPEN", "1d 2h",""));

        totalCountLbl.setText(data.size() + " phiên");
        double total = data.stream().mapToDouble(AuctionView::getCurrentBid).sum();
        totalValueLbl.setText(money.format(total) + " ₫");

        for (AuctionView a : data) watchList.getChildren().add(buildRow(a));
    }

    private HBox buildRow(AuctionView a) {
        HBox row = new HBox(14);
        row.getStyleClass().add("watch-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setOnMouseClicked(e ->
            SceneRouter.<AuctionDetailController>goWithData("auction-detail", c -> c.initData(a)));

        CheckBox cb = new CheckBox();

        Region img = new Region();
        img.setPrefSize(96, 96);
        img.setStyle("-fx-background-color: linear-gradient(to bottom right, #1877F2, #0095F6);"
                  + "-fx-background-radius: 8;");

        VBox info = new VBox(4);
        Label name = new Label(a.getItemName());
        name.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");
        Label meta = new Label("@" + a.getSellerName() + " • " + a.getCategory());
        meta.getStyleClass().add("muted");
        HBox status = new HBox(8);
        Label badge = new Label(a.getStatus());
        badge.getStyleClass().add(switch (a.getStatus()) {
            case "LIVE" -> "badge-live"; case "OPEN" -> "badge-open"; default -> "badge-finished";
        });
        Label time = new Label("⏱ " + a.getTimeLeft());
        time.setStyle("-fx-text-fill: -fx-error; -fx-font-weight: bold;");
        status.getChildren().addAll(badge, time);
        info.getChildren().addAll(name, meta, status);
        HBox.setHgrow(info, Priority.ALWAYS);

        VBox priceBox = new VBox(2);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        Label priceLbl = new Label("Giá hiện tại");
        priceLbl.getStyleClass().add("caption");
        Label price = new Label(money.format(a.getCurrentBid()) + " ₫");
        price.getStyleClass().add("price-tag");
        priceBox.getChildren().addAll(priceLbl, price);

        Button bidBtn = new Button("⚡ Đặt giá nhanh");
        bidBtn.getStyleClass().add("btn-primary");
        bidBtn.setOnAction(e ->
            SceneRouter.<AuctionDetailController>goWithData("auction-detail", c -> c.initData(a)));

        Button removeBtn = new Button("🗑");
        removeBtn.getStyleClass().add("btn-ghost");
        removeBtn.setOnAction(e -> {
            if (AlertHelper.confirm("Xoá khỏi danh sách theo dõi?", a.getItemName())) {
                watchList.getChildren().remove(row);
                // Tuần 12: hook gọi server xoá watchlist khi network sẵn sàng
                // NetworkManager.send(WATCHLIST_REMOVE, new WatchlistPayload(userId, a.getId()))
                updateTotal();
            }
        });

        row.getChildren().addAll(cb, img, info, priceBox, bidBtn, removeBtn);
        return row;
    }

    @FXML private void handleBack()        { SceneRouter.go("dashboard"); }
    @FXML private void handleTabWatching() { activateTab(tabWatching); }
    @FXML private void handleTabEnding()   { activateTab(tabEnding); }
    @FXML private void handleTabWon()      { activateTab(tabWon); }
    @FXML private void handleTabLost()     { activateTab(tabLost); }

    /** Tuần 12: chuyển style active tab. */
    private void activateTab(Button selected) {
        for (Button b : new Button[]{tabWatching, tabEnding, tabWon, tabLost}) {
            b.getStyleClass().removeAll("tab-pill-active");
        }
        selected.getStyleClass().add("tab-pill-active");
    }

    /** Tuần 12: tính lại tổng giá trị sau khi xoá item. */
    private void updateTotal() {
        totalCountLbl.setText(watchList.getChildren().size() + " phiên");
    }
}
