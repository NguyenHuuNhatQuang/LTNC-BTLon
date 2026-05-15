import exception.AuctionClosedException;
import exception.InvalidBidException;
import model.auction.Auction;
import model.item.Item;
import model.item.ItemFactory;
import model.user.Bidder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

// Import các hàm assert của JUnit 5
import static org.junit.jupiter.api.Assertions.*;

public class AuctionTest {

    private Auction auction;
    private Bidder bidder1;
    private Bidder bidder2;

    // Annotation @BeforeEach giúp chạy hàm này TRƯỚC MỖI test case
    // Đảm bảo mỗi test case đều có một môi trường sạch, không bị ảnh hưởng lẫn nhau
    @BeforeEach
    public void setUp() {
        Item laptop = ItemFactory.createItem("Electronics", "ITM001", "Macbook Pro", "12");
        LocalDateTime now = LocalDateTime.now();

        // Khởi tạo phiên với giá khởi điểm 1000.0, bước giá 50.0
        auction = new Auction("AUC001", laptop, now, now.plusMinutes(30), 1000.0, 50.0);
        auction.startAuction(); // Chuyển trạng thái sang RUNNING

        bidder1 = new Bidder("USR001", "Alice");
        bidder2 = new Bidder("USR002", "Bob");
    }

    @Test
    public void testPlaceValidBid() {
        // Kịch bản: Trả giá hợp lệ
        auction.placeBid(bidder1, 1050.0);

        // Kiểm tra xem giá hiện tại có đúng bằng giá vừa đặt không 
        assertEquals(1050.0, auction.getCurrentHighestBid(), "Giá cao nhất phải được cập nhật thành 1050.0");
    }

    @Test
    public void testPlaceBidTooLowThrowsException() {
        // Kịch bản: Trả giá thấp hơn quy định
        auction.placeBid(bidder1, 1050.0); // Alice đặt 1050.0 trước

        // Bob đặt 1080.0 (Nhỏ hơn giá hiện tại 1050 + bước giá 50 = 1100)
        // Mong đợi ném ra ngoại lệ InvalidBidException 
        Exception exception = assertThrows(InvalidBidException.class, () -> {
            auction.placeBid(bidder2, 1080.0);
        });

        // Tùy chọn: Kiểm tra thông báo lỗi có đúng như thiết kế không
        assertTrue(exception.getMessage().contains("Giá đặt phải lớn hơn hoặc bằng"));
    }

    @Test
    public void testPlaceBidOnClosedAuctionThrowsException() {
        // Kịch bản: Phiên đã đóng nhưng vẫn cố đặt giá
        auction.closeAuction(); // Ép đóng phiên đấu giá

        // Mong đợi ném ra ngoại lệ AuctionClosedException
        assertThrows(AuctionClosedException.class, () -> {
            auction.placeBid(bidder1, 5000.0);
        });
    }
}