import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class HashYazdir {
    public static String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) hex.append('0');
            hex.append(h);
        }
        return hex.toString();
    }

    public static void main(String[] args) throws Exception {
        // Tüm kullanıcılar için aynı şifre: admin123
        String[] emails = {
            "admin@fitzone.com", "ahmet@mail.com", "fatma@mail.com",
            "can@mail.com", "selin@mail.com", "emre@mail.com",
            "zeynep@mail.com", "murat@mail.com", "ayse@mail.com",
            "kemal@fitzone.com", "deniz@fitzone.com", "elif@fitzone.com"
        };
        String hash = sha256("admin123");
        System.out.println("SHA-256('admin123') = " + hash);
        System.out.println();
        System.out.println("-- SQL UPDATE komutu:");
        System.out.println("UPDATE kullanicilar SET sifre_hash = '" + hash + "';");
    }
}
