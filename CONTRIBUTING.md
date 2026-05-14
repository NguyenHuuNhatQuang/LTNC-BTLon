# Contributing to BidNow

Hướng dẫn cho thành viên nhóm phát triển BidNow (BTL LTNC 2026).

## 🌿 Branch convention

| Branch | Mục đích |
|---|---|
| `main` | Code stable, demo cuối kỳ |
| `ui` | C-Team UI/UX (FXML, Controller, CSS) |
| `c-network` | C-Team Socket Client (NetworkManager) |
| `sunlight_dev` | S-Team backend (OOP, ServerSocket) |
| `feature/<name>` | Feature mới |
| `fix/<name>` | Bug fix |

**Quy tắc:**
- KHÔNG commit trực tiếp lên `main` — phải qua Pull Request
- Mỗi feature mới mở 1 branch riêng, merge bằng PR
- PR phải có ít nhất 1 reviewer approve

## 📝 Commit message convention

```
<type>(<scope>): <subject>
```

| Type | Khi nào dùng |
|---|---|
| `feat` | Tính năng mới |
| `fix` | Sửa bug |
| `refactor` | Cải thiện code, không đổi behavior |
| `style` | Đổi CSS, format, không đổi logic |
| `docs` | Cập nhật tài liệu |
| `test` | Thêm/sửa test |
| `chore` | Config, dependency, build script |

**Ví dụ:**
```
feat(controller): add LiveAuctionController with realtime bot feed
fix(network): out.reset() before writeObject to prevent stream cache
docs(readme): update run instructions for Maven Wrapper
```

## 🛠 Setup local

```bash
# 1. Clone
git clone https://github.com/NguyenHuuNhatQuang/LTNC-BTLon.git
cd LTNC-BTLon

# 2. Checkout branch
git checkout ui

# 3. Build với Maven Wrapper (không cần cài Maven)
./mvnw clean compile     # Linux/Mac
mvnw.cmd clean compile   # Windows

# 4. Chạy app
./mvnw javafx:run
```

## ✅ Trước khi mở Pull Request

- [ ] Code compile không lỗi: `mvnw clean compile`
- [ ] Test pass: `mvnw test`
- [ ] Đã pull main mới nhất, rebase
- [ ] Commit message theo convention
- [ ] Không có file rác (target/, .idea/, *.class)
- [ ] Cập nhật README nếu cần

## 🔍 Code review checklist

Reviewer kiểm tra:
- [ ] Logic đúng yêu cầu PDF
- [ ] Có exception handling
- [ ] Đa luồng có lock/synchronized
- [ ] Update UI bọc `Platform.runLater`
- [ ] Code style chuẩn camelCase
- [ ] Không hardcode magic number / string

## 📞 Liên hệ

- **C-Team Lead:** @nam_dev
- **S-Team Lead:** _(điền tên)_
- **Họp đồng bộ:** Thứ 4 21:00 Discord
