package com.auction.client.util;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Tuần 10 - Task 5.3 prep:
 * Banner trượt từ trên xuống báo trạng thái kết nối Socket.
 * Gắn vào StackPane root của bất cứ scene nào để hiển thị overlay.
 *
 * Sử dụng:
 *   ConnectionStatusBar.attach(rootStackPane);
 *   ConnectionStatusBar.showConnected();
 *   ConnectionStatusBar.showDisconnected();
 *   ConnectionStatusBar.showReconnecting(2);
 */
public final class ConnectionStatusBar {

    private static HBox bar;
    private static Label icon;
    private static Label text;

    private ConnectionStatusBar() {}

    public static void attach(StackPane root) {
        if (bar != null) return;
        bar = new HBox(10);
        bar.setStyle("-fx-background-color: #42B72A; -fx-padding: 8 16 8 16;"
                   + "-fx-background-radius: 0 0 12 12;");
        bar.setMaxHeight(Region.USE_PREF_SIZE);
        bar.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(bar, javafx.geometry.Pos.TOP_CENTER);
        StackPane.setMargin(bar, new Insets(0));

        icon = new Label("●");
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 12;");
        text = new Label("");
        text.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13;");

        bar.getChildren().addAll(icon, text);
        bar.setOpacity(0);
        root.getChildren().add(bar);
    }

    public static void showConnected() {
        runOnFx(() -> {
            if (bar == null) return;
            bar.setStyle("-fx-background-color: #42B72A; -fx-padding: 8 16 8 16;"
                       + "-fx-background-radius: 0 0 12 12;");
            text.setText("Đã kết nối server");
            fadeInOut();
        });
    }

    public static void showDisconnected() {
        runOnFx(() -> {
            if (bar == null) return;
            bar.setStyle("-fx-background-color: #FA383E; -fx-padding: 8 16 8 16;"
                       + "-fx-background-radius: 0 0 12 12;");
            text.setText("Mất kết nối server. Đang thử lại...");
            FadeTransition ft = new FadeTransition(Duration.millis(300), bar);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
        });
    }

    public static void showReconnecting(int attempt) {
        runOnFx(() -> {
            if (bar == null) return;
            bar.setStyle("-fx-background-color: #F7B928; -fx-padding: 8 16 8 16;"
                       + "-fx-background-radius: 0 0 12 12;");
            text.setText("Đang kết nối lại... (lần " + attempt + ")");
        });
    }

    private static void fadeInOut() {
        FadeTransition in = new FadeTransition(Duration.millis(250), bar);
        in.setFromValue(0); in.setToValue(1);
        FadeTransition out = new FadeTransition(Duration.millis(500), bar);
        out.setFromValue(1); out.setToValue(0);
        out.setDelay(Duration.seconds(2));
        in.setOnFinished(e -> out.play());
        in.play();
    }

    private static void runOnFx(Runnable r) {
        if (Platform.isFxApplicationThread()) r.run();
        else Platform.runLater(r);
    }
}
