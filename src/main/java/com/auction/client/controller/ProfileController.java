package com.auction.client.controller;

import com.auction.client.util.AlertHelper;
import com.auction.client.util.SceneRouter;
import javafx.fxml.FXML;

public class ProfileController {
    @FXML private void handleBack()   { SceneRouter.go("dashboard"); }
    @FXML private void handleLogout() {
        if (AlertHelper.confirm("Đăng xuất", "Bạn chắc chắn muốn thoát?"))
            SceneRouter.go("login");
    }
}
