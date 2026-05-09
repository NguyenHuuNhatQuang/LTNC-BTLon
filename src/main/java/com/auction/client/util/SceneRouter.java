package com.auction.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý chuyển scene tập trung. Tránh việc Controller nào cũng load FXML
 * tự do dẫn đến code lặp.
 *
 * Cách dùng:
 *   SceneRouter.go("dashboard");
 *   SceneRouter.goWithData("auction-detail", controller -> controller.initData(auction));
 *
 * Đây là giải pháp cho Task 3.5 (Tuần 8): truyền data giữa các controller.
 */
public final class SceneRouter {

    private static Stage stage;
    private static final Map<String, String> ROUTES = new HashMap<>();

    static {
        ROUTES.put("login",          "/view/login.fxml");
        ROUTES.put("register",       "/view/register.fxml");
        ROUTES.put("dashboard",      "/view/dashboard.fxml");
        ROUTES.put("auction-detail", "/view/auction-detail.fxml");
        ROUTES.put("create-item",    "/view/create-item.fxml");
        // Tuần 9 - bổ sung 5 màn hình mới
        ROUTES.put("watchlist",      "/view/watchlist.fxml");
        ROUTES.put("live-auction",   "/view/live-auction.fxml");
        ROUTES.put("notifications",  "/view/notifications.fxml");
        ROUTES.put("profile",        "/view/profile.fxml");
        ROUTES.put("my-bids",        "/view/my-bids.fxml");
    }

    private SceneRouter() {}

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    /** Chuyển sang scene mới, không truyền data. */
    public static void go(String routeName) {
        try {
            FXMLLoader loader = loaderFor(routeName);
            Parent root = loader.load();
            applyScene(root);
        } catch (IOException e) {
            throw new RuntimeException("Không load được FXML: " + routeName, e);
        }
    }

    /** Chuyển sang scene mới + truyền data qua controller. */
    public static <T> void goWithData(String routeName, java.util.function.Consumer<T> initializer) {
        try {
            FXMLLoader loader = loaderFor(routeName);
            Parent root = loader.load();
            T controller = loader.getController();
            initializer.accept(controller);
            applyScene(root);
        } catch (IOException e) {
            throw new RuntimeException("Không load được FXML: " + routeName, e);
        }
    }

    private static FXMLLoader loaderFor(String routeName) {
        String path = ROUTES.get(routeName);
        if (path == null) throw new IllegalArgumentException("Route không tồn tại: " + routeName);
        return new FXMLLoader(SceneRouter.class.getResource(path));
    }

    private static void applyScene(Parent root) {
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            scene.getStylesheets().add(
                SceneRouter.class.getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
    }
}
