package model.user;
import model.Entity;

public abstract class User extends Entity {
    private String username;

    public User(String id, String username) {
        super(id);
        this.username = username;
    }

    public String getUsername() { return username; }
}