import model.auction.*;
import model.item.*;
import model.user.*;
import java.time.LocalDateTime;

public class TestWeek6 {
    public static void main(String[] args) {
        System.out.println("--- BẮT ĐẦU TEST TUẦN 6 ---");

        // 1. Kiểm tra Factory Pattern & Tính Đa hình (Polymorphism)
        System.out.println("\n[1] Test Khởi tạo Sản phẩm:");
        Item laptop = ItemFactory.createItem("Electronics", "ITM001", "Laptop ThinkPad", "24");
        Item painting = ItemFactory.createItem("Art", "ITM002", "Bức tranh Mùa Thu", "Van Gogh");

        // Gọi cùng 1 hàm getDetails() nhưng in ra kết quả khác nhau
        System.out.println(laptop.getDetails());
        System.out.println(painting.getDetails());

        // 2. Kiểm tra Khởi tạo User
        System.out.println("\n[2] Test Khởi tạo User:");
        Bidder bidder1 = new Bidder("USR001", "Alice");
        Bidder bidder2 = new Bidder("USR002", "Bob");
        System.out.println("Đã tạo Bidder: " + bidder1.getUsername() + " và " + bidder2.getUsername());

        // 3. Kiểm tra Singleton Pattern (AuctionManager)
        System.out.println("\n[3] Test Singleton AuctionManager:");
        AuctionManager manager1 = AuctionManager.getInstance();
        AuctionManager manager2 = AuctionManager.getInstance();
        System.out.println("manager1 và manager2 có cùng bộ nhớ không? " + (manager1 == manager2));

        // 4. Kiểm tra Logic Đấu giá (Encapsulation & Business Logic)
        System.out.println("\n[4] Test Logic Đấu Giá:");
        Auction auction1 = new Auction("AUC001", laptop, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1000.0);
        manager1.addAuction(auction1); // Thêm vào hệ thống quản lý

        System.out.println("Trạng thái ban đầu (OPEN), Bob thử đặt 1500$:");
        boolean failBid = auction1.placeBid(bidder2, 1500.0);
        System.out.println("Kết quả: " + (failBid ? "Thành công" : "Thất bại (Đúng thiết kế)"));

        System.out.println("\nChuyển trạng thái phiên đấu giá sang RUNNING...");
        auction1.startAuction();

        System.out.println("Alice đặt 1200$ (Hợp lệ):");
        boolean bid1 = auction1.placeBid(bidder1, 1200.0);
        System.out.println("Kết quả: " + (bid1 ? "Thành công" : "Thất bại"));

        System.out.println("Bob đặt 1100$ (Thất bại do thấp hơn giá Alice):");
        boolean bid2 = auction1.placeBid(bidder2, 1100.0);
        System.out.println("Kết quả: " + (bid2 ? "Thành công" : "Thất bại (Đúng thiết kế)"));

        System.out.println("Bob đặt 1500$ (Hợp lệ):");
        boolean bid3 = auction1.placeBid(bidder2, 1500.0);
        System.out.println("Kết quả: " + (bid3 ? "Thành công" : "Thất bại"));
    }
}