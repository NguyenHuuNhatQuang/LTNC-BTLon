package com.auction.client.controller;

import com.auction.client.util.AlertHelper;
import com.auction.client.util.SceneRouter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateItemController implements Initializable {

    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private ComboBox<String> conditionBox;
    @FXML private TextArea descArea;
    @FXML private TextField startPriceField;
    @FXML private TextField stepPriceField;
    @FXML private ComboBox<String> durationBox;
    @FXML private TextField buyNowField;
    @FXML private Label errorLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        categoryBox.setItems(FXCollections.observableArrayList(
            "Điện tử", "Nghệ thuật", "Phương tiện", "Trang sức"));
        categoryBox.getSelectionModel().selectFirst();

        conditionBox.setItems(FXCollections.observableArrayList(
            "Mới", "Như mới", "Tốt", "Đã qua sử dụng"));
        conditionBox.getSelectionModel().selectFirst();

        durationBox.setItems(FXCollections.observableArrayList(
            "1 giờ", "6 giờ", "12 giờ", "24 giờ", "3 ngày", "7 ngày"));
        durationBox.getSelectionModel().select(3);
    }

    @FXML
    private void handlePickImage() {
        AlertHelper.info("Chọn ảnh", "FileChooser sẽ được tích hợp ở Tuần 9 khi có Server upload.");
    }

    @FXML
    private void handleSubmit() {
        try {
            validate();
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            AlertHelper.error("Form không hợp lệ", ex.getMessage());
            return;
        }

        AlertHelper.info("Đăng thành công",
            "Phiên đấu giá đã được gửi (mock). Tuần 9+ sẽ thực sự gửi qua Socket dạng CREATE_ITEM_REQUEST.");
        SceneRouter.go("dashboard");
    }

    @FXML
    private void handleDraft() {
        AlertHelper.info("Đã lưu nháp", "Bạn có thể tiếp tục sau ở mục Sản phẩm của tôi.");
    }

    @FXML
    private void handleCancel() {
        if (AlertHelper.confirm("Hủy?", "Dữ liệu đã nhập sẽ bị mất."))
            SceneRouter.go("dashboard");
    }

    private void validate() {
        if (nameField.getText().isBlank())
            throw new IllegalArgumentException("Vui lòng nhập tên sản phẩm.");
        if (descArea.getText().length() < 10)
            throw new IllegalArgumentException("Mô tả phải có ít nhất 10 ký tự.");

        double start = parsePositive(startPriceField.getText(), "Giá khởi điểm");
        double step  = parsePositive(stepPriceField.getText(),  "Bước giá");

        if (step > start)
            throw new IllegalArgumentException("Bước giá không được lớn hơn giá khởi điểm.");

        String buyNowRaw = buyNowField.getText();
        if (buyNowRaw != null && !buyNowRaw.isBlank()) {
            double buyNow = parsePositive(buyNowRaw, "Giá mua ngay");
            if (buyNow <= start)
                throw new IllegalArgumentException("Giá mua ngay phải lớn hơn giá khởi điểm.");
        }
    }

    private double parsePositive(String raw, String fieldName) {
        try {
            double v = Double.parseDouble(raw.trim().replaceAll("[,\\s]", ""));
            if (v <= 0) throw new IllegalArgumentException(fieldName + " phải > 0.");
            return v;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ.");
        }
    }
}
