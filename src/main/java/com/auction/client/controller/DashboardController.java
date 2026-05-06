package com.auction.client.controller;

import com.auction.client.model.AuctionView;
import com.auction.client.util.AlertHelper;
import com.auction.client.util.SceneRouter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Label avatarLabel;
    @FXML private Label userNameLabel;
    @FXML private ComboBox<String> sortBox;
    @FXML private FlowPane auctionGrid;
    @FXML private HBox flashRow;

    @FXML private TableView<AuctionView> auctionTable;
    @FXML private TableColumn<AuctionView, String> colName;
    @FXML private TableColumn<AuctionView, String> colCat;
    @FXML private TableColumn<AuctionView, String> colSeller;
    @FXML private TableColumn<AuctionView, String> colBid;
    @FXML private TableColumn<AuctionView, String> colTime;
    @FXML private TableColumn<AuctionView, String> colStatus;

    private final NumberFormat money = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sortBox.setItems(FXCollections.observableArrayList(
            "Mới nhất", "Sắp kết thúc", "Giá cao nhất", "Giá thấp nhất", "Lượt bid nhiều"));
        sortBox.getSelectionModel().selectFirst();

        ObservableList<AuctionView> data = mockData();

        configureTable(data);
        auctionGrid.getChildren().setAll(data.stream().map(this::buildProductCard).toList());
        flashRow.getChildren().setAll(data.stream().limit(4).map(this::buildFlashCard).toList());

        // Tuần 8 - Task 3.5: click đúp -> AuctionDetail truyền data
        auctionTable.setRowFactory(tv -> {
            TableRow<AuctionView> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) openDetail(row.getItem());
            });
            return row;
        });

        searchField.textProperty().addListener((obs, o, n) -> {
            String key = n == null ? "" : n.toLowerCase().trim();
            ObservableList<AuctionView> filtered = data.filtered(a ->
                a.getItemName().toLowerCase().contains(key) ||
                a.getCategory().toLowerCase().contains(key));
            auctionTable.setItems(FXCollections.observableArrayList(filtered));
            auctionGrid.getChildren().setAll(filtered.stream().map(this::buildProductCard).toList());
        });
    }

    private void configureTable(ObservableList<AuctionView> data) {
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        colSeller.setCellValueFactory(new PropertyValueFactory<>("sellerName"));
        colBid.setCellValueFactory(c -> new SimpleStringProperty(money.format(c.getValue().getCurrentBid()) + " ₫"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("timeLeft"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().add(switch (s) {
                    case "LIVE" -> "badge-live";
                    case "OPEN" -> "badge-open";
                    default     -> "badge-finished";
                });
                setText(null); setGraphic(badge);
            }
        });
        auctionTable.setItems(data);
    }

    /** Card kiểu Shopee product: ảnh - tên - giá lớn - sold count - badge giảm. */
    private VBox buildProductCard(AuctionView a) {
        VBox card = new VBox(0);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(220);
        card.setOnMouseClicked(e -> openDetail(a));

        StackPane imgWrap = new StackPane();
        Region img = new Region();
        img.setPrefHeight(180);
        img.setStyle("-fx-background-color: linear-gradient(to bottom right, " + colorFor(a.getCategory()) + ");"
                  + "-fx-background-radius: 8 8 0 0;");

        // Discount badge (giả lập)
        Label discount = new Label("-" + (10 + (a.getId().hashCode() & 30)) + "%");
        discount.getStyleClass().add("discount-badge");
        StackPane.setAlignment(discount, Pos.TOP_LEFT);
        StackPane.setMargin(discount, new Insets(8));

        Label statusBadge = new Label(a.getStatus());
        statusBadge.getStyleClass().add(switch (a.getStatus()) {
            case "LIVE" -> "badge-live";
            case "OPEN" -> "badge-open";
            default     -> "badge-finished";
        });
        StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(statusBadge, new Insets(8));

        imgWrap.getChildren().addAll(img, discount, statusBadge);

        VBox body = new VBox(4);
        body.setPadding(new Insets(10));

        Label name = new Label(a.getItemName());
        name.setWrapText(true);
        name.setStyle("-fx-font-size: 13;");
        name.setMaxHeight(36);

        Label price = new Label(money.format(a.getCurrentBid()) + " ₫");
        price.getStyleClass().add("price-tag");

        HBox meta = new HBox(8);
        Label rating = new Label("⭐ 4.8");
        rating.setStyle("-fx-font-size: 11; -fx-text-fill: -fx-warning;");
        Label sold = new Label("Đã bid: " + (10 + (a.getId().hashCode() & 50)));
        sold.getStyleClass().add("sold-count");
        meta.getChildren().addAll(rating, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, sold);

        Label time = new Label("⏱ " + a.getTimeLeft());
        time.setStyle("-fx-font-size: 11; -fx-text-fill: -fx-error; -fx-font-weight: bold;");

        body.getChildren().addAll(name, price, meta, time);
        card.getChildren().addAll(imgWrap, body);
        return card;
    }

    /** Card cho Flash band (smaller, có giá gạch ngang). */
    private VBox buildFlashCard(AuctionView a) {
        VBox card = new VBox(6);
        card.setPrefWidth(160);
        card.setStyle("-fx-cursor: hand;");
        card.setOnMouseClicked(e -> openDetail(a));

        Region img = new Region();
        img.setPrefHeight(140);
        img.setStyle("-fx-background-color: linear-gradient(to bottom right, " + colorFor(a.getCategory()) + ");"
                  + "-fx-background-radius: 8;");

        Label price = new Label(money.format(a.getCurrentBid()) + " ₫");
        price.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: -fx-error;");

        Label oldPrice = new Label(money.format(a.getCurrentBid() * 1.4) + " ₫");
        oldPrice.setStyle("-fx-font-size: 11; -fx-text-fill: -fx-text-secondary; -fx-strikethrough: true;");

        // Progress bar
        Region progress = new Region();
        progress.setPrefHeight(6);
        progress.setStyle("-fx-background-color: linear-gradient(to right, #FA383E 70%, #FFCFD2 70%);"
                        + "-fx-background-radius: 50;");

        Label progressLbl = new Label("ĐANG BID NHANH");
        progressLbl.setStyle("-fx-font-size: 10; -fx-text-fill: -fx-error; -fx-font-weight: bold;");

        card.getChildren().addAll(img, price, oldPrice, progress, progressLbl);
        return card;
    }

    private void openDetail(AuctionView a) {
        SceneRouter.<AuctionDetailController>goWithData("auction-detail", c -> c.initData(a));
    }

    /* ===== Header / nav handlers ===== */
    @FXML private void handleCreateItem()    { SceneRouter.go("create-item"); }
    @FXML private void handleNotifications() { SceneRouter.go("notifications"); }
    @FXML private void handleWatchlist()     { SceneRouter.go("watchlist"); }
    @FXML private void handleLogout() {
        if (AlertHelper.confirm("Đăng xuất", "Bạn chắc chắn muốn thoát?"))
            SceneRouter.go("login");
    }

    /* ===== Bottom nav ===== */
    @FXML private void handleNavHome()    { /* đang ở home */ }
    @FXML private void handleNavLive()    { SceneRouter.go("live-auction"); }
    @FXML private void handleNavMyBids()  { SceneRouter.go("my-bids"); }
    @FXML private void handleNavProfile() { SceneRouter.go("profile"); }

    private String colorFor(String cat) {
        return switch (cat) {
            case "Điện tử"     -> "#1877F2, #0095F6";
            case "Nghệ thuật"  -> "#9B51E0, #1877F2";
            case "Phương tiện" -> "#0095F6, #00C2FF";
            case "Trang sức"   -> "#1877F2, #9B51E0";
            default             -> "#1877F2, #0095F6";
        };
    }

    private ObservableList<AuctionView> mockData() {
        return FXCollections.observableArrayList(List.of(
            new AuctionView("A001", "MacBook Pro M3 14\"", "Điện tử",     "minh.tran",   42500000, "LIVE",     "02:34", ""),
            new AuctionView("A002", "iPhone 15 Pro Max",   "Điện tử",     "hoa.nguyen",  28900000, "LIVE",     "00:48", ""),
            new AuctionView("A003", "Tranh sơn dầu phố cổ", "Nghệ thuật", "art.studio",   8500000, "OPEN",     "1d 2h", ""),
            new AuctionView("A004", "Honda Civic 2020",    "Phương tiện", "auto.house", 580000000, "OPEN",     "3d",    ""),
            new AuctionView("A005", "Đồng hồ Rolex cổ",    "Trang sức",   "luxury.vn",  120000000, "LIVE",     "05:12", ""),
            new AuctionView("A006", "Sony A7 IV body",      "Điện tử",    "camera.pro",  45000000, "OPEN",     "12h",   ""),
            new AuctionView("A007", "Lego Millennium Falcon","Nghệ thuật","brick.fan",   12500000, "FINISHED", "Hết",    ""),
            new AuctionView("A008", "Nhẫn kim cương 1ct",   "Trang sức",  "diamond.co", 350000000, "OPEN",     "2d 6h", "")
        ));
    }
}
