package com.auction.client.controller;

import com.auction.client.util.SceneRouter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Tuần 9 prep: màn hình live giả lập bot bid mỗi 2-4s.
 * Khi có NetworkManager (Tuần 9 thật), thay Timeline bằng callback từ Socket.
 */
public class LiveAuctionController implements Initializable {

    @FXML private VBox liveFeed;
    @FXML private VBox leaderBoard;
    @FXML private Label currentBidLbl;
    @FXML private Label statusLbl;
    @FXML private TextField bidInput;

    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final Random rng = new Random();
    private double currentBid = 42_500_000;
    private final List<String> botUsers = List.of("hoa", "long", "minh", "linh", "tuan", "bot.fast", "kim.le");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Seed một vài bid trong feed cho có nội dung
        for (int i = 0; i < 6; i++) appendFakeBid(false);
        rebuildLeaderboard();

        // Simulate realtime: 1 bid mới mỗi 2.5s
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(2.5), e -> {
            Platform.runLater(() -> {
                currentBid += 500_000L * (1 + rng.nextInt(4));
                appendFakeBid(true);
                currentBidLbl.setText(money.format(currentBid) + " ₫");
                rebuildLeaderboard();
            });
        }));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    private void appendFakeBid(boolean highlight) {
        String user = botUsers.get(rng.nextInt(botUsers.size()));
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label("[" + time + "]");
        t.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");
        Label u = new Label("@" + user);
        u.getStyleClass().add("live-bid-user");
        Label msg = new Label("đặt giá");
        msg.setStyle("-fx-text-fill: white;");
        Label amt = new Label(money.format(currentBid) + " ₫");
        amt.getStyleClass().add("live-bid-amount");
        row.getChildren().addAll(t, u, msg, amt);
        liveFeed.getChildren().add(row);
        if (liveFeed.getChildren().size() > 16) liveFeed.getChildren().remove(0);
    }

    private void rebuildLeaderboard() {
        leaderBoard.getChildren().clear();
        String[] crown = {"🥇", "🥈", "🥉"};
        for (int i = 0; i < 3; i++) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            Label c = new Label(crown[i]);
            c.setStyle("-fx-font-size: 18;");
            Label name = new Label("@" + botUsers.get(i));
            name.setStyle("-fx-font-weight: bold;");
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label amt = new Label(money.format(currentBid - i * 500_000L) + " ₫");
            amt.setStyle("-fx-text-fill: -fx-primary; -fx-font-weight: bold;");
            row.getChildren().addAll(c, name, sp, amt);
            leaderBoard.getChildren().add(row);
        }
    }

    @FXML private void handlePlaceBid() {
        statusLbl.setText("⏳ Đang gửi yêu cầu...");
        statusLbl.setStyle("-fx-text-fill: -fx-warning;");
    }
    @FXML private void handleBack() { SceneRouter.go("dashboard"); }
}
