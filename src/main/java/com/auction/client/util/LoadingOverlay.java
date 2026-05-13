package com.auction.client.util;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Tuần 11 - Task 6.3 (UX loading state):
 * Overlay loading hiển thị khi đang chờ network response.
 * Tránh user spam click.
 *
 * Sử dụng:
 *   LoadingOverlay.show(rootStackPane, "Đang đặt giá...");
 *   LoadingOverlay.hide();
 */
public final class LoadingOverlay {

    private static StackPane currentRoot;
    private static VBox overlay;
    private static Label messageLabel;
    private static RotateTransition spinner;

    private LoadingOverlay() {}

    public static void show(StackPane root, String message) {
        runOnFx(() -> {
            if (overlay == null) build();
            messageLabel.setText(message);
            if (currentRoot != root) {
                if (currentRoot != null) currentRoot.getChildren().remove(overlay);
                currentRoot = root;
                root.getChildren().add(overlay);
            }
            overlay.setVisible(true);
            overlay.setManaged(true);
            spinner.play();
        });
    }

    public static void hide() {
        runOnFx(() -> {
            if (overlay == null) return;
            spinner.stop();
            overlay.setVisible(false);
            overlay.setManaged(false);
        });
    }

    private static void build() {
        overlay = new VBox(14);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");

        Region ring = new Region();
        ring.setPrefSize(50, 50);
        ring.setStyle("-fx-background-color: transparent;"
                   + "-fx-border-color: white transparent transparent transparent;"
                   + "-fx-border-width: 4;"
                   + "-fx-border-radius: 50;");

        messageLabel = new Label("Đang xử lý...");
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        overlay.getChildren().addAll(ring, messageLabel);

        spinner = new RotateTransition(Duration.millis(900), ring);
        spinner.setByAngle(360);
        spinner.setCycleCount(RotateTransition.INDEFINITE);
        spinner.setInterpolator(javafx.animation.Interpolator.LINEAR);
    }

    private static void runOnFx(Runnable r) {
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }
}
