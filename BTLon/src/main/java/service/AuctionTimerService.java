package service;

import model.auction.Auction;
import model.auction.AuctionStatus;
import java.time.LocalDateTime;

public class AuctionTimerService {

    // Tách logic đếm ngược từ lớp Auction sang đây
    public void startTimer(Auction auction) {
        Thread timerThread = new Thread(() -> {
            while (LocalDateTime.now().isBefore(auction.getEndTime()) && auction.getStatus() == AuctionStatus.RUNNING) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (auction.getStatus() == AuctionStatus.RUNNING) {
                auction.closeAuction(); // Cập nhật trạng thái và gọi notifyObservers()
                System.out.println("Phiên đấu giá " + auction.getId() + " đã tự động đóng bởi TimerService.");
            }
        });
        timerThread.start();
    }
}