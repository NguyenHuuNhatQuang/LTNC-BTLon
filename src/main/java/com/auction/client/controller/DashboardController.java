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
import javafx.scene.paint.Color;

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
    @FXML private VBox trendingBox;

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
            "Mới nhất", "Sắp kết thúc", "Giá cao nhất", "Giá thấp nhất"));
        sortBox.getSelectionModel().selectFirst();

        ObservableList<AuctionView> data = mockData();

        configureTable(data);
        configureCards(data);
        configureTrending(data);

        // Tuần 8 - Task 3.5: click đúp vào dòng -> sang AuctionDetail kèm data
        auctionTable.setRowFactory(tv -> {
            TableRow<AuctionView> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openAuctionDetail(row.getItem());
                }
            });
            return row;
        });

        // Search filter realtime
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String key = newV == null ? "" : newV.toLowerCase().trim();
            ObservableList<AuctionView> filtered = data.filtered(a ->
                a.getItemName().toLowerCase().contains(key) ||
                a.getCategory().toLowerCase().contains(key) ||
                a.getSellerName().toLowerCase().contains(key));
            auctionTable.setItems(FXCollections.observableArrayList(filtered));
            auctionGrid.getChildren().setAll(filtered.stream().map(this::buildCard).toList());
        });
    }

    private void configureTable(ObservableList<AuctionView> data) {
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        colSeller.setCellValueFactory(new PropertyValueFactory<>("sellerName"));
        colBid.setCellValueFactory(c ->
            new SimpleStringProperty(money.format(c.getValue().getCurrentBid()) + " ₫"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("timeLeft"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Cell tô màu badge cho status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(s);
                switch (s) {
                    case "LIVE"     -> badge.getStyleClass().add("badge-live");
                    case "OPEN"     -> badge.getStyleClass().add("badge-open");
                    case "FINISHED" -> badge.getStyleClass().add("badge-finished");
                    default -> {}
                }
                setText(null);
                setGraphic(badge);
            }
        });

        auctionTable.setItems(data);
    }

    private void configureCards(ObservableList<AuctionView> data) {
        auctionGrid.getChildren().setAll(data.stream().map(this::buildCard).toList());
    }

    private void configureTrending(ObservableList<AuctionView> data) {
        data.stream().limit(4).forEach(a -> {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8));
            row.setStyle("-fx-cursor: hand; -fx-background-radius: 8;");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: -fx-bg; -fx-background-radius: 8; -fx-cursor: hand;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand; -fx-background-radius: 8;"));
            row.setOnMouseClicked(e -> openAuctionDetail(a));

            Region thumb = new Region();
            thumb.setPrefSize(48, 48);
            thumb.getStyleClass().add("image-placeholder");

            VBox info = new VBox(2);
            Label name = new Label(a.getItemName());
            name.setStyle("-fx-font-weight: bold;");
            Label price = new Label(money.format(a.getCurrentBid()) + " ₫");
            price.getStyleClass().add("muted");
            info.getChildren().addAll(name, price);

            row.getChildren().addAll(thumb, info);
            trendingBox.getChildren().add(row);
        });
    }

    /**
     * Card 1 phiên đấu giá - kiểu Instagram post.
     */
    private VBox buildCard(AuctionView a) {
        VBox card = new VBox(0);
        card.getStyleClass().add("auction-card");
        card.setPrefWidth(260);
        card.setOnMouseClicked(e -> openAuctionDetail(a));

        // Image header (gradient placeholder)
        Region img = new Region();
        img.setPrefHeight(180);
        img.setStyle("-fx-background-color: linear-gradient(to bottom right, " + colorFor(a.getCategory()) + ");"
                  + "-fx-background-radius: 12 12 0 0;");

        // Status badge overlay
        Label badge = new Label(a.getStatus());
        badge.getStyleClass().add(switch (a.getStatus()) {
            case "LIVE" -> "badge-live";
            case "OPEN" -> "badge-open";
            default     -> "badge-finished";
        });
        StackPane imgWrap = new StackPane(img, badge);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(12));

        // Body
        VBox body = new VBox(6);
        body.setPadding(new Insets(14));

        Label name = new Label(a.getItemName());
        name.getStyleClass().add("h3");
        name.setWrapText(true);

        Label seller = new Label("@" + a.getSellerName() + " • " + a.getCategory());
        seller.getStyleClass().add("muted");

        HBox priceRow = new HBox();
        priceRow.setAlignment(Pos.CENTER_LEFT);
        VBox priceBox = new VBox(2);
        Label priceLbl = new Label("Giá hiện tại");
        priceLbl.getStyleClass().add("caption");
        Label price = new Label(money.format(a.getCurrentBid()) + " ₫");
        price.getStyleClass().add("price-tag");
        priceBox.getChildren().addAll(priceLbl, price);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label time = new Label("⏱  " + a.getTimeLeft());
        time.getStyleClass().add("muted");

        priceRow.getChildren().addAll(priceBox, spacer, time);

        Button bidBtn = new Button("Vào đấu giá →");
        bidBtn.getStyleClass().add("btn-secondary");
        bidBtn.setMaxWidth(Double.MAX_VALUE);
        bidBtn.setOnAction(e -> openAuctionDetail(a));

        body.getChildren().addAll(name, seller, new Region(){{ setPrefHeight(4); }}, priceRow, bidBtn);

        card.getChildren().addAll(imgWrap, body);
        return card;
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

    private void openAuctionDetail(AuctionView auction) {
        SceneRouter.<AuctionDetailController>goWithData(
            "auction-detail", c -> c.initData(auction));
    }

    /* ===== Navbar handlers ===== */
    @FXML private void handleCreateItem() { SceneRouter.go("create-item"); }
    @FXML private void handleNotifications() {
        AlertHelper.info("Thông báo", "Bạn không có thông báo mới.");
    }
    @FXML private void handleLogout() {
        if (AlertHelper.confirm("Đăng xuất", "Bạn chắc chắn muốn thoát?"))
            SceneRouter.go("login");
    }

    /* ===== Sidebar handlers (placeholder) ===== */
    @FXML private void handleNavHome()     { /* hiện đang ở home */ }
    @FXML private void handleNavLive()     { AlertHelper.info("Live", "Lọc các phiên đang LIVE - sẽ làm Tuần 9."); }
    @FXML private void handleNavWatching() { AlertHelper.info("Theo dõi", "Danh sách phiên bạn theo dõi - sẽ làm Tuần 9."); }
    @FXML private void handleNavMyItems()  { AlertHelper.info("Sản phẩm của tôi", "Đang phát triển."); }
    @FXML private void handleNavHistory()  { AlertHelper.info("Lịch sử bid", "Đang phát triển."); }

    /**
     * Mock data Tuần 7. Tuần 9+ sẽ thay bằng list từ Server.
     */
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
