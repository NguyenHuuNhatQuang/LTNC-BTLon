public class Bidder extends Users {
    private double balance; // so du tk de dau gia

    public Bidder(String id, String name, String username, String password, double balance) {
        super(id, name, username, password);
        this.balance = balance;
    }

    public void placeBid(Item item, double amount) {
        System.out.println(this.name + " nâng giá đấu " + amount + " cho vật phẩm " + item.getId());
    }
