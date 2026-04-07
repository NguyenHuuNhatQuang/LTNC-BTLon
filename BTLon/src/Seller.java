import java.util.ArrayList;
import java.util.List;

public class Seller extends Users {
    private List<String> itemsForSale = new ArrayList<>();
    public Seller() {
        super();
    }
    public void addAuctionItem(String itemName) {
        itemsForSale.add(itemName);
        System.out.println("Người bán " + this.name + " đã thêm sản phẩm: " + itemName);
    }
}
