package com.auction.client;

/**
 * Launcher — workaround chính tắc cho JavaFX fat-jar.
 *
 * VẤN ĐỀ: Khi chạy `java -jar app.jar` với Main.class extends Application,
 *          JavaFX throw "Error: JavaFX runtime components are missing".
 *
 * NGUYÊN NHÂN: JVM phát hiện main class extends Application và try lookup
 *              JavaFX modules trên module path, không tìm thấy → crash.
 *
 * GIẢI PHÁP: Launcher KHÔNG extends Application. Nó chỉ gọi Main.main().
 *            JVM thấy entry point bình thường, không trigger module check,
 *            sau đó Application.launch() vẫn chạy ngon từ classpath jar.
 *
 * Đây là cách được khuyến nghị bởi cộng đồng JavaFX cho fat-jar packaging.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
