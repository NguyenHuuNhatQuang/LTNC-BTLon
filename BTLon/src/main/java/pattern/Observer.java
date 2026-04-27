package pattern;

public interface Observer {
    // Hàm được gọi khi Subject có sự thay đổi
    void update(Object data);
}