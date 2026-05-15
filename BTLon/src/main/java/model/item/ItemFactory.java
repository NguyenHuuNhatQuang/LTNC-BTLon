package model.item;

public class ItemFactory {
    public static Item createItem(String type, String id, String name, String extraDetail) {
        if (type.equalsIgnoreCase("Electronics")) {
            return new Electronics(id, name, Integer.parseInt(extraDetail));
        } else if (type.equalsIgnoreCase("Art")) {
            return new Art(id, name, extraDetail);
        }
        throw new IllegalArgumentException("Loại sản phẩm không hợp lệ: " + type);
    }
}