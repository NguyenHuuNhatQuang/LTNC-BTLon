package com.auction.client.controller;

import com.auction.client.model.AuctionView;
import com.auction.client.util.AlertHelper;
import com.auction.client.util.NetworkBridge;
import com.auction.client.util.SceneRouter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AuctionDetailController {

    @FXML private Region heroImage;
    @FXML private Label categoryLbl;
    @FXML private Label statusLbl;
    @FXML private Label auctionIdLbl;
    @FXML private Label itemTitleLbl;
    @FXML private Label sellerAvatar;
    @FXML private Label sellerLbl;
    @FXML private Label descLbl;
    @FXML private Label startPriceLbl;
    @FXML private Label stepPriceLbl;
    @FXML private Label bidCountLbl;
    @FXML private Label bidCountSmall;

    @FXML private Label currentBidLbl;
    @FXML private Label leaderLbl;
    @FXML private Label timeLeftLbl;

    @FXML private TextField bidInput;
    @FXML private Button placeBidBtn;
    @FXML private Label bidErrorLbl;

    @FXML private ListView<String> bidHistoryList;

    @FXML private LineChart<String, Number> priceChart;
    @FXML private CategoryAxis timeAxis;
    @FXML private NumberAxis priceAxis;
    @FXML private Label chartRangeLabel;
    private final XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
    private static final int CHART_MAX_POINTS = 20;

    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final ObservableList<String> bidHistory = FXCollections.observableArrayList();
    private static final double STEP = 500_000;

    private AuctionView auction;
    private double currentBid;
    private String currentLeader = "—";

    /**
     * Hàm "cửa" mà DashboardController gọi qua SceneRouter.goWithData().
     * Đây là cách chính tắc Task 3.5 - Tuần 8.
     */
    public void initData(AuctionView a) {
        this.auction = a;
        this.currentBid = a.getCurrentBid();

        itemTitleLbl.setText(a.getItemName());
        categoryLbl.setText(a.getCategory());
        auctionIdLbl.setText("#" + a.getId());
        sellerLbl.setText("@" + a.getSellerName());
        sellerAvatar.setText(a.getSellerName().substring(0, 1).toUpperCase());
        currentBidLbl.setText(money.format(currentBid) + " ₫");
        startPriceLbl.setText(money.format(currentBid * 0.7) + " ₫");
        timeLeftLbl.setText(a.getTimeLeft());

        // Status badge
        statusLbl.setText(a.getStatus());
        statusLbl.getStyleClass().removeAll("badge-live", "badge-open", "badge-finished");
        statusLbl.getStyleClass().add(switch (a.getStatus()) {
            case "LIVE" -> "badge-live";
            case "OPEN" -> "badge-open";
            default     -> "badge-finished";
        });

        // Hero gradient theo category
        heroImage.setStyle("-fx-background-color: linear-gradient(to bottom right, " + colorFor(a.getCategory()) + ");"
                        + "-fx-background-radius: 12 12 0 0;");

        // Disable bid nếu đã kết thúc
        if ("FINISHED".equals(a.getStatus())) {
            placeBidBtn.setDisable(true);
            placeBidBtn.setText("Phiên đã kết thúc");
        }

        // Cell hiển thị bid history kiểu IG comment
        bidHistoryList.setItems(bidHistory);
        bidHistoryList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) { setGraphic(null); setText(null); return; }
                setGraphic(buildBidRow(entry));
                setText(null);
            }
        });

        // Auto-suggest giá tiếp theo
        bidInput.setText(String.valueOf((long)(currentBid + STEP)));

        // Tuần 13 - Task 7.3: khởi tạo LineChart giá realtime
        priceChart.getData().clear();
        priceSeries.getData().clear();
        priceChart.getData().add(priceSeries);
        addPriceDataPoint(currentBid);

        // Tuần 10 - Task 5.3: hook nhận UPDATE_AUCTION realtime từ NetworkBridge
        NetworkBridge.onAuctionUpdate(this::applyRemoteUpdate);
        NetworkBridge.onBidRejected(reason -> {
            showError(reason);
            AlertHelper.error("Đặt giá bị từ chối", reason);
        });
    }

    /**
     * Tuần 10 - Task 5.3:
     * Gọi từ NetworkBridge khi nhận UPDATE_AUCTION từ Server.
     * Đảm bảo chạy trên FX thread (NetworkBridge đã bọc Platform.runLater).
     */
    public void applyRemoteUpdate(AuctionView updated) {
        if (auction == null || !auction.getId().equals(updated.getId())) return;
        currentBid = updated.getCurrentBid();
        currentBidLbl.setText(money.format(currentBid) + " ₫");
        timeLeftLbl.setText(updated.getTimeLeft());

        // Đẩy bid mới lên đầu list nếu có người khác bid
        String now = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        bidHistory.add(0, "remote|" + money.format(currentBid) + "|" + now);

        // Update suggest giá tiếp theo
        bidInput.setText(String.valueOf((long)(currentBid + STEP)));

        // Tuần 13 - Task 7.3: thêm điểm dữ liệu mới vào biểu đồ giá
        addPriceDataPoint(currentBid);

        flashSuccess();
    }

    /**
     * Tuần 13 - Task 7.3: thêm 1 điểm vào LineChart.
     * Giữ tối đa CHART_MAX_POINTS điểm gần nhất (sliding window).
     */
    private void addPriceDataPoint(double price) {
        String time = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        priceSeries.getData().add(new XYChart.Data<>(time, price));
        if (priceSeries.getData().size() > CHART_MAX_POINTS) {
            priceSeries.getData().remove(0);
        }
        chartRangeLabel.setText(priceSeries.getData().size() + " / " + CHART_MAX_POINTS + " điểm");
    }

    private HBox buildBidRow(String entry) {
        // entry format: "user|amount|time"
        String[] parts = entry.split("\\|");
        String user = parts[0], amount = parts[1], time = parts[2];

        Label av = new Label(user.substring(0,1).toUpperCase());
        av.getStyleClass().add("avatar-circle");

        VBox info = new VBox(2);
        Label u = new Label("@" + user);
        u.setStyle("-fx-font-weight: bold;");
        Label a = new Label(amount + " ₫");
        a.setStyle("-fx-text-fill: -fx-primary; -fx-font-weight: bold;");
        info.getChildren().addAll(u, a);

        Label t = new Label(time);
        t.getStyleClass().add("caption");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox row = new HBox(10, av, info, sp, t);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    @FXML
    private void handleBack() {
        SceneRouter.go("dashboard");
    }

    @FXML private void handleQuickBid500() { addToBidInput(500_000); }
    @FXML private void handleQuickBid1M()  { addToBidInput(1_000_000); }
    @FXML private void handleQuickBid5M()  { addToBidInput(5_000_000); }

    private void addToBidInput(double delta) {
        double base = currentBid + STEP;
        try { base = Double.parseDouble(bidInput.getText().replaceAll("\\D", "")); }
        catch (NumberFormatException ignored) {}
        bidInput.setText(String.valueOf((long)(base + delta)));
    }

    @FXML
    private void handlePlaceBid() {
        clearError();

        // Disable nút để tránh spam (chuẩn bị Tuần 11 - Task 6.3 UX loading)
        placeBidBtn.setDisable(true);

        try {
            double amount = parseAmount(bidInput.getText());

            if (amount < currentBid + STEP) {
                throw new IllegalArgumentException(
                    "Giá đặt phải ≥ " + money.format(currentBid + STEP) + " ₫ (giá hiện tại + bước giá).");
            }

            // Cập nhật state (sau này sẽ là response từ Server)
            currentBid = amount;
            currentLeader = "nam_dev"; // user hiện tại - placeholder

            currentBidLbl.setText(money.format(currentBid) + " ₫");
            leaderLbl.setText("@" + currentLeader);

            String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            bidHistory.add(0, currentLeader + "|" + money.format(amount) + "|" + now);

            int count = Integer.parseInt(bidCountLbl.getText().replaceAll("\\D", "0")) + 1;
            bidCountLbl.setText(String.valueOf(count));
            bidCountSmall.setText(count + " lượt");

            // Suggest giá tiếp theo
            bidInput.setText(String.valueOf((long)(currentBid + STEP)));

            // Tuần 13 - Task 7.3: thêm điểm dữ liệu vào biểu đồ
            addPriceDataPoint(currentBid);

            // Hiệu ứng nhấp xanh (chuẩn bị cho Task 5.3 Tuần 10)
            flashSuccess();

        } catch (IllegalArgumentException ex) {
            // Theo Task 3.4 Tuần 8: hiển thị Exception qua Alert + label
            showError(ex.getMessage());
            AlertHelper.error("Đặt giá không hợp lệ", ex.getMessage());
        } finally {
            placeBidBtn.setDisable("FINISHED".equals(auction.getStatus()));
        }
    }

    private double parseAmount(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.isEmpty())
            throw new IllegalArgumentException("Vui lòng nhập số tiền hợp lệ.");
        return Double.parseDouble(digits);
    }

    private void flashSuccess() {
        currentBidLbl.setStyle(currentBidLbl.getStyle()
            + "; -fx-background-color: #DAF6E3; -fx-background-radius: 6;");
        // Reset sau 600ms (đơn giản dùng Timeline)
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(600), e ->
                currentBidLbl.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: -fx-primary;"))
        );
        tl.play();
    }

    private void showError(String msg) {
        bidErrorLbl.setText(msg);
        bidErrorLbl.setVisible(true);
        bidErrorLbl.setManaged(true);
    }

    private void clearError() {
        bidErrorLbl.setVisible(false);
        bidErrorLbl.setManaged(false);
    }

    private String colorFor(String cat) {
        return switch (cat) {
            case "Điện tử"     -> "#1877F2, #0095F6";
            case "Nghệ thuật"  -> "#9B51E0, #1877F2";
            case "Phương tiện" -> "#0095F6, #00C2FF";
            case "Trang sức"   -> "#1877F2, #9B51E0";
            default             -> "#1877F2, #0095F6";
        };
    }
}
