package com.auction.client;

import com.auction.client.util.SceneRouter;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneRouter.init(stage);
        stage.setTitle("BidNow - Auction");
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        SceneRouter.go("login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
