package com.auction.client.controller;

import com.auction.client.util.AlertHelper;
import com.auction.client.util.SceneRouter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button tabLoginBtn;
    @FXML private Button tabRegisterBtn;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass == null || pass.isEmpty()) {
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

    @FXML private void handleSwitchToLogin() { /* đã ở tab login */ }
    @FXML private void handleGoRegister()    { SceneRouter.go("register"); }
    @FXML private void handleForgot() {
        AlertHelper.info("Quên mật khẩu",
            "Vui lòng liên hệ admin@bidnow.vn để đặt lại mật khẩu.");
    }
    @FXML private void handleSocialLogin() {
        AlertHelper.info("Đăng nhập mạng xã hội",
            "Tính năng OAuth sẽ tích hợp ở giai đoạn nâng cao (Tuần 13-14).");
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
