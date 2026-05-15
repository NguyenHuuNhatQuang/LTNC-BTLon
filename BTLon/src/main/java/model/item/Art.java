package model.item;

public class Art extends Item {
    private final String author;

    public Art(String id, String name, String author) {
        super(id, name);
        this.author = author;
    }

    @Override
    public String getDetails() {
        return "Art - Tên: " + getName() + " | Tác giả: " + author; // Thêm tên tác giả
    }
}