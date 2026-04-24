package com.auction.client.controller;

import com.auction.client.util.AlertHelper;
import com.auction.client.util.SceneRouter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class RegisterController implements Initializable {

    private static final Pattern EMAIL_RE =
        Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleBox;
    @FXML private Label errorLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roleBox.setItems(FXCollections.observableArrayList("Bidder (Người mua)", "Seller (Người bán)"));
        roleBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleRegister() {
        if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank()) {
            showError("Họ tên không được để trống.");
            return;
        }
        if (usernameField.getText().length() < 3) {
            showError("Tên đăng nhập phải có ít nhất 3 ký tự.");
            return;
        }
        if (!EMAIL_RE.matcher(emailField.getText()).matches()) {
            showError("Email không hợp lệ.");
            return;
        }
        if (passwordField.getText().length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }

        AlertHelper.info("Đăng ký thành công",
            "Tài khoản đã được tạo (mock). Vui lòng đăng nhập lại.");
        SceneRouter.go("login");
    }

    @FXML
    private void handleGoLogin() {
        SceneRouter.go("login");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
