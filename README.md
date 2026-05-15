# BidNow — Sàn đấu giá trực tuyến realtime

Bài tập lớn môn **Lập trình Nâng cao** (LTNC). Hệ thống đấu giá đa người dùng,
giao tiếp Socket TCP, giao diện JavaFX hiện đại học theo Shopee/Lazada.

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![JavaFX](https://img.shields.io/badge/JavaFX-21-orange)
![Maven](https://img.shields.io/badge/Maven-3.9-red)

## ✨ Tính năng

- 9 màn hình: Login, Register, Trang chủ, Chi tiết, Live Auction, Watchlist,
  Notifications, Profile, My Bids, Create Item
- Realtime broadcast khi có bid mới (Observer Pattern + Socket)
- Multi-client: 3-4 user cùng đấu giá 1 phiên không bị Lost Update
- Biểu đồ giá realtime LineChart
- Design system CSS đồng bộ tone xanh, học theo FB/IG/Shopee

## 🏗 Kiến trúc

```
Client (JavaFX)                           Server (Socket)
┌──────────────────────┐                  ┌──────────────────────┐
│  View (FXML)         │                  │  ServerSocket :7070  │
│        ↓             │                  │        ↓             │
│  Controller          │ <── ObjectStream │  ClientHandler thread│
│        ↓             │     (Message)    │        ↓             │
│  NetworkManager      │ ───────────────> │  AuctionManager      │
│  (Singleton)         │                  │  (Singleton)         │
│        ↓             │                  │        ↓             │
│  NetworkListener     │ <─── broadcast   │  Auction (Subject)   │
│  (Task<Void>)        │     UPDATE_AUCTION│  + ReentrantLock     │
│        ↓             │                  │                      │
│  MessageBus (Pub/Sub)│                  │                      │
└──────────────────────┘                  └──────────────────────┘
```

## 🚀 Cách chạy

### Yêu cầu
- JDK 17+
- Maven 3.9+
- (Tự động tải) JavaFX 21 qua Maven dependency

### 🚀 Cách 1 — IntelliJ Clone-and-Run (30 GIÂY)

1. **IntelliJ → File → New → Project from Version Control**
2. URL: `https://github.com/NguyenHuuNhatQuang/LTNC-BTLon.git`
3. Mở **Git tab** (góc dưới) → Branches → Remote → checkout **`ui`**
4. Đợi popup **"Load Maven Project"** xuất hiện → click → đợi 1-3 phút tải JavaFX

**Sau khi Maven load xong, có 3 run config sẵn ở dropdown góc trên phải:**

| Config | Lệnh tương đương | Khi nào dùng |
|---|---|---|
| **1 - Run BidNow Client** | `mvn javafx:run` | Demo bình thường |
| **2 - Build JAR** | `mvn clean package` | Tạo file `.jar` đóng gói |
| **3 - Run All Tests** | `mvn test` | Chạy JUnit |

→ Chỉ cần **click ▶️** cạnh dropdown là app khởi động.

### Cách 2 — Command line

```bash
git clone https://github.com/NguyenHuuNhatQuang/LTNC-BTLon.git
cd LTNC-BTLon
git checkout ui
./mvnw javafx:run        # Linux/Mac
mvnw.cmd javafx:run      # Windows
```

**Lưu ý:** Dùng `./mvnw` (Maven Wrapper) — không cần cài Maven hệ thống.

### Cách 3 — JAR đóng gói (không cần Maven)

Tải release ở: https://github.com/NguyenHuuNhatQuang/LTNC-BTLon/releases
→ Giải nén → double-click `1-CHAY-APP.bat`. Chỉ cần JDK 17+.

### Chạy Server (S-Team branch)
```bash
git checkout sunlight_dev
cd BTLon
mvn compile exec:java -Dexec.mainClass="server.AuctionServerMain"
```

### Build .jar đóng gói
```bash
mvn clean package
java -jar target/auction-client-1.0-SNAPSHOT.jar
```

## ⚙ Cấu hình

File `src/main/resources/config/network.properties`:
```properties
server.host=localhost
server.port=7070
connection.timeout=8000
```

## 📐 Design Patterns áp dụng

| Pattern   | Class                                              |
|-----------|----------------------------------------------------|
| Singleton | AuctionManager, SceneRouter, NetworkManager, MessageBus |
| Factory   | ItemFactory.createItem(type)                       |
| Observer  | Auction → ClientHandler, MessageBus → Controller   |
| MVC       | FXML View · Controller · Model entities            |

## 🧪 Test

```bash
mvn test
```

Test plan:
- `AuctionTest.placeBid_validAmount_updates()` — happy path
- `AuctionTest.placeBid_lowAmount_throwsInvalidBidException()` — exception
- `AuctionTest.placeBid_closedAuction_throws()` — state check
- `AuctionTest.placeBid_concurrent10Threads_noLostUpdate()` — stress test

## 📂 Cấu trúc thư mục

```
btl/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/auction/
    │   ├── client/
    │   │   ├── Main.java
    │   │   ├── controller/   (10 controller, mỗi màn hình 1 file)
    │   │   ├── model/        (AuctionView - ViewModel)
    │   │   ├── network/      (NetworkManager, MessageBus, Payloads)
    │   │   └── util/         (SceneRouter, AlertHelper, NetworkBridge,
    │   │                       ConnectionStatusBar, LoadingOverlay)
    │   └── shared/           (Message - giao thức chung)
    └── resources/
        ├── view/   (10 file FXML)
        ├── css/style.css      (~570 dòng, design system)
        ├── config/network.properties
        └── images/
```

## 👥 Nhóm phát triển

- **C-Team UI/UX** — @nam_dev (lead)
- **C-Team Network** — Socket Client, NetworkManager
- **S-Team Backend** — Server Socket, AuctionManager, Persistence

## 📅 Tiến độ

| Tuần | Nội dung | Trạng thái |
|------|----------|-----------|
| 6-8  | Setup, OOP, MVC, FXML 9 màn hình | ✅ Done |
| 9    | Maven, Serialization, Client Socket | ✅ Done |
| 10   | Network Bridge, Connection Status | ✅ Done |
| 11   | Loading overlay, Integration | ✅ Done |
| 12   | Watchlist/Notif/MyBids polish | ✅ Done |
| 13   | LineChart, CSS polish, README | ✅ Done |

