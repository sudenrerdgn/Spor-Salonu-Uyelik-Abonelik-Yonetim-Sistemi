import Controller.DatabaseBaglanti;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class HashGen {
    public static void main(String[] args) throws Exception {
        String[] passwords = {"admin1234", "Fitzone2026!"};
        for (String p : passwords) {
            System.out.println(p + " => " + sha256(p));
        }
    }
    static String sha256(String s) throws NoSuchAlgorithmException {
        MessageDigest d = MessageDigest.getInstance("SHA-256");
        byte[] h = d.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : h) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }
}
