package com.auction.client.controller;

import com.auction.client.util.SceneRouter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MyBidsController implements Initializable {

    @FXML private VBox bidList;
    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi", "VN"));

    record MyBid(String name, String seller, double myBid, double currentBid, String status, String time) {}

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<MyBid> data = List.of(
            new MyBid("MacBook Pro M3 14\"",  "minh.tran",   42_500_000, 42_500_000, "ĐANG DẪN ĐẦU", "Còn 02:34"),
            new MyBid("iPhone 15 Pro Max",    "hoa.nguyen",  28_400_000, 28_900_000, "BỊ VƯỢT GIÁ",  "Còn 00:48"),
            new MyBid("Sony A7 IV body",      "camera.pro",  44_500_000, 45_000_000, "BỊ VƯỢT GIÁ",  "Còn 12h"),
            new MyBid("Đồng hồ Rolex cổ",     "luxury.vn",  119_000_000, 120_000_000,"BỊ VƯỢT GIÁ",  "Còn 05:12"));
        for (MyBid b : data) bidList.getChildren().add(buildRow(b));
    }

    private HBox buildRow(MyBid b) {
        HBox row = new HBox(14);
        row.getStyleClass().add("watch-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Region img = new Region();
        img.setPrefSize(80, 80);
        img.setStyle("-fx-background-color: linear-gradient(to bottom right, #1877F2, #0095F6); -fx-background-radius: 8;");

        VBox info = new VBox(4);
        Label name = new Label(b.name());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        Label seller = new Label("@" + b.seller());
        seller.getStyleClass().add("muted");

        HBox prices = new HBox(20);
        VBox myBidBox = new VBox(2);
        Label l1 = new Label("Giá bạn đặt:"); l1.getStyleClass().add("caption");
        Label v1 = new Label(money.format(b.myBid()) + " ₫");
        v1.setStyle("-fx-font-weight: bold;");
        myBidBox.getChildren().addAll(l1, v1);

        VBox curBidBox = new VBox(2);
        Label l2 = new Label("Giá hiện tại:"); l2.getStyleClass().add("caption");
        Label v2 = new Label(money.format(b.currentBid()) + " ₫");
        v2.getStyleClass().add("price-tag");
        curBidBox.getChildren().addAll(l2, v2);

        prices.getChildren().addAll(myBidBox, curBidBox);
        info.getChildren().addAll(name, seller, prices);
        HBox.setHgrow(info, Priority.ALWAYS);

        VBox statusBox = new VBox(6);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        Label badge = new Label(b.status());
        badge.getStyleClass().add(b.status().contains("DẪN") ? "badge-open" : "badge-live");
        Label time = new Label("⏱ " + b.time());
        time.getStyleClass().add("muted");
        statusBox.getChildren().addAll(badge, time);

        row.getChildren().addAll(img, info, statusBox);
        return row;
    }

    @FXML private void handleBack() { SceneRouter.go("dashboard"); }
}
