package com.auction.client.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Wrapper cho Alert (Task 3.4 Tuần 8).
 * Giúp tầng Controller chỉ cần gọi 1 dòng để hiện popup lỗi/cảnh báo,
 * style đồng bộ qua style.css (xem .dialog-pane).
 */
public final class AlertHelper {

    private AlertHelper() {}

    public static void error(String header, String content) {
        show(Alert.AlertType.ERROR, "Lỗi", header, content);
    }

    public static void warn(String header, String content) {
        show(Alert.AlertType.WARNING, "Cảnh báo", header, content);
    }

    public static void info(String header, String content) {
        show(Alert.AlertType.INFORMATION, "Thông báo", header, content);
    }

    public static boolean confirm(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(header);
        alert.setContentText(content);
        styled(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void show(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styled(alert);
        alert.showAndWait();
    }

    private static void styled(Alert alert) {
        alert.getDialogPane().getStylesheets().add(
            AlertHelper.class.getResource("/css/style.css").toExternalForm());
    }
}
