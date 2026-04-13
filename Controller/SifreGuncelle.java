package Controller;

import java.sql.*;

/**
 * Veritabanındaki bcrypt şifre hash'lerini SHA-256 ile günceller.
 * Tüm kullanıcı şifrelerini "admin123" yapar (SHA-256 hash olarak).
 * 
 * Çalıştır:
 *   javac -cp ".;mssql-jdbc-12.4.2.jre11.jar" Controller/SifreGuncelle.java Controller/DatabaseBaglanti.java
 *   java  -cp ".;mssql-jdbc-12.4.2.jre11.jar" Controller.SifreGuncelle
 */
public class SifreGuncelle {

    // SHA-256("admin123") = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
    private static final String YENI_HASH = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";

    public static void main(String[] args) throws Exception {
        System.out.println("Veritabanı bağlantısı kuruluyor...");
        Connection conn = DatabaseBaglanti.baglantiGetir();
        if (conn == null) {
            System.out.println("HATA: Veritabanına bağlanılamadı!");
            return;
        }

        // Güncellemeden önce kaç kullanıcı var göster
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT kullanici_id, ad, soyad, email FROM kullanicilar ORDER BY kullanici_id");
        System.out.println("\nMevcut kullanıcılar:");
        while (rs.next()) {
            System.out.printf("  [%d] %s %s (%s)%n",
                rs.getInt("kullanici_id"), rs.getString("ad"), rs.getString("soyad"), rs.getString("email"));
        }
        rs.close();
        st.close();

        // Tüm kullanıcıların şifresini güncelle
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE kullanicilar SET sifre_hash = ?"
        );
        ps.setString(1, YENI_HASH);
        int updated = ps.executeUpdate();
        ps.close();

        System.out.println("\n✅ " + updated + " kullanıcının şifresi güncellendi.");
        System.out.println("   Yeni şifre: admin123");
        System.out.println("   SHA-256 hash: " + YENI_HASH);
        System.out.println("\nArtık tüm kullanıcılar 'admin123' şifresiyle giriş yapabilir.");

        DatabaseBaglanti.baglantiKapat();
    }
}
