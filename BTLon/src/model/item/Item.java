package model.item;
import model.Entity;

public abstract class Item extends Entity {
    private String name;

    public Item(String id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() { return name; }

    public abstract String getDetails();
}