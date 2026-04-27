package model.auction;
import model.item.Item;
import model.user.Bidder;
import pattern.Subject;
import pattern.Observer;
import service.AuctionTimerService;
import exception.InvalidBidException;
import exception.AuctionClosedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Auction implements Subject {
    private String id;
    private Item item;
    private List<Bidder> bidders;
    private List<BidTransaction> bidTransactions;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double currentHighestBid;
    private double bidStep; // Bước giá
    private AuctionStatus status;

    // Yêu cầu Tuần 7: Quản lý Observer và Xử lý Đa luồng
    private List<Observer> observers;
    private final ReentrantLock lock;

    public Auction(String id, Item item, LocalDateTime startTime, LocalDateTime endTime, double startingPrice, double bidStep) {
        this.id = id;
        this.item = item;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentHighestBid = startingPrice;
        this.bidStep = bidStep;
        this.status = AuctionStatus.OPEN;

        this.bidders = new ArrayList<>();
        this.bidTransactions = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.lock = new ReentrantLock(); // Khởi tạo Lock
    }

    public void placeBid(Bidder bidder, double amount) throws InvalidBidException, AuctionClosedException {
        lock.lock();
        try {
            if (this.status != AuctionStatus.RUNNING) {
                throw new AuctionClosedException("Lỗi: Phiên đấu giá đã đóng hoặc chưa bắt đầu!"); // Ném ngoại lệ [cite: 79, 80]
            }

            double minimumRequired = this.bidTransactions.isEmpty() ? this.currentHighestBid : this.currentHighestBid + this.bidStep;

            if (amount < minimumRequired) {
                throw new InvalidBidException("Lỗi: Giá đặt phải lớn hơn hoặc bằng " + minimumRequired); // Ném ngoại lệ [cite: 78, 80]
            }

            // Nếu hợp lệ thì tiến hành cập nhật
            this.currentHighestBid = amount;
            this.bidTransactions.add(new BidTransaction(bidder, amount));
            if (!this.bidders.contains(bidder)) {
                this.bidders.add(bidder);
            }
            notifyObservers();

        } finally {
            lock.unlock();
        }
    }

    // Task 2.2: Luồng (Thread) đếm ngược thời gian
    public void startAuction() {
        this.status = AuctionStatus.RUNNING;
        System.out.println("Phiên đấu giá " + this.id + " đã BẮT ĐẦU.");

        new AuctionTimerService().startTimer(this);
    }

    public String getId() {
        return id;
    }

    // Cho phép TimerService biết khi nào thì dừng đếm ngược
    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Kiểm tra trạng thái để đảm bảo Timer chỉ chạy khi phiên đang RUNNING
    public AuctionStatus getStatus() {
        return status;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void closeAuction() {
        this.lock.lock(); // Đảm bảo an toàn đa luồng khi đổi trạng thái [cite: 58]
        try {
            if (this.status == AuctionStatus.RUNNING) {
                this.status = AuctionStatus.FINISHED; // Chuyển sang trạng thái kết thúc
                System.out.println("Hệ thống: Phiên " + this.id + " đã đóng.");
                notifyObservers(); // Phát sóng (broadcast) trạng thái mới tới các Client [cite: 49, 134]
            }
        } finally {
            this.lock.unlock();
        }
    }

    // --- Triển khai các phương thức của Subject ---
    @Override
    public void attach(Observer o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void detach(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        // Gửi chính đối tượng Auction này (chứa thông tin mới nhất) cho các Client
        for (Observer o : observers) {
            o.update(this);
        }
    }
}