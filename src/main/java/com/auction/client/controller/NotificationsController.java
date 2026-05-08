package com.auction.client.controller;

import com.auction.client.util.SceneRouter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class NotificationsController implements Initializable {

    @FXML private VBox notiList;

    record Noti(String icon, String title, String body, String time, boolean unread) {}

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<Noti> data = List.of(
            new Noti("🏆", "Bạn đã thắng phiên đấu giá!",     "Macbook Pro M3 - 42.500.000 ₫. Vui lòng thanh toán trong 24h.", "5 phút trước",  true),
            new Noti("⚡", "Phiên bạn theo dõi sắp kết thúc", "iPhone 15 Pro Max còn 48 giây. Đặt giá ngay để không bỏ lỡ.", "12 phút trước", true),
            new Noti("📈", "Có người vượt giá của bạn",        "@hoa.nguyen vừa đặt 28.900.000 ₫ cho iPhone 15 Pro Max.",       "30 phút trước", true),
            new Noti("✅", "Người bán đã xác nhận giao hàng", "Sony A7 IV body đang trên đường giao tới bạn.",                "2 giờ trước",   false),
            new Noti("💰", "Hoàn tiền đã được xử lý",         "Số dư ví +5.000.000 ₫ từ phiên đấu giá bị hủy A-0034.",       "1 ngày trước",  false),
            new Noti("⭐", "Phiên đấu giá mới phù hợp với bạn","Đồng hồ Rolex cổ vừa được đăng - giá khởi điểm 80.000.000 ₫.",  "2 ngày trước",  false));
        for (Noti n : data) notiList.getChildren().add(buildRow(n));
    }

    private HBox buildRow(Noti n) {
        HBox row = new HBox(14);
        row.getStyleClass().addAll("noti-row", n.unread() ? "noti-row-unread" : "noti-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(n.icon());
        icon.setStyle("-fx-font-size: 24;");

        VBox info = new VBox(2);
        Label title = new Label(n.title());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        Label body = new Label(n.body());
        body.getStyleClass().add("muted");
        body.setWrapText(true);
        Label time = new Label(n.time());
        time.getStyleClass().add("caption");
        info.getChildren().addAll(title, body, time);
        HBox.setHgrow(info, Priority.ALWAYS);

        row.getChildren().addAll(icon, info);
        if (n.unread()) {
            Region dot = new Region();
            dot.getStyleClass().add("noti-dot");
            row.getChildren().add(dot);
        }
        return row;
    }

    @FXML private void handleBack() { SceneRouter.go("dashboard"); }
}
