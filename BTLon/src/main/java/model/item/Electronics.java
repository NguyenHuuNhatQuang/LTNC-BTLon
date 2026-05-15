package model.item;

public class Electronics extends Item {
    private final int warrantyMonths;

    public Electronics(String id, String name, int warrantyMonths) {
        super(id, name);
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String getDetails() {
        return "Electronics - Tên: " + getName() + " | Bảo hành: " + warrantyMonths + " tháng"; // Thêm thông tin bảo hành
    }
}