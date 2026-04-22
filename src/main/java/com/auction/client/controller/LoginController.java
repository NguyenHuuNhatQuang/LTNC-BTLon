package com.auction.client.controller;

import com.auction.client.util.AlertHelper;
import com.auction.client.util.SceneRouter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }
        if (pass.length() < 4) {
            showError("Mật khẩu phải có ít nhất 4 ký tự.");
            return;
        }

        clearError();
        SceneRouter.go("dashboard");
    }

    @FXML
    private void handleGoRegister() {
        SceneRouter.go("register");
    }

    @FXML
    private void handleForgot() {
        AlertHelper.info("Quên mật khẩu",
            "Tính năng đang phát triển. Vui lòng liên hệ admin để đặt lại mật khẩu.");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
