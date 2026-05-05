package Controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseBaglanti {

    // ─── SQL Server Bağlantı Bilgileri ───
    // AWS RDS SQL Server
    private static final String SUNUCU = "fitzone.cpicuywuucpp.eu-north-1.rds.amazonaws.com";
    private static final String PORT = "1433";
    private static final String VERITABANI = "fitzone_db";
    private static final String KULLANICI_ADI = "admin"; // SQL Server kullanıcısı
    private static final String SIFRE = "admin1234"; // SQL Server şifresi

    // SQL Authentication (kullanıcı adı + şifre):
    // Not: databaseName belirtilmezse master'a bağlanır; fitzone DB yoksa hata verir
    private static final String URL = "jdbc:sqlserver://" + SUNUCU + ":" + PORT
            + ";databaseName=" + VERITABANI
            + ";encrypt=false;loginTimeout=30";

    // Test için master bağlantı URL
    private static final String MASTER_URL = "jdbc:sqlserver://" + SUNUCU + ":" + PORT
            + ";encrypt=false;loginTimeout=30";

    private static Connection baglanti = null;

    // ═══════════════════════════════════════════
    // BAĞLANTI AÇ
    // ═══════════════════════════════════════════
    public static Connection baglantiGetir() {
        try {
            if (baglanti == null || baglanti.isClosed()) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                try {
                    // Önce hedef veritabanına bağlan
                    baglanti = DriverManager.getConnection(URL, KULLANICI_ADI, SIFRE);
                    System.out.println("✅ SQL Server bağlantısı başarılı! (" + VERITABANI + ")");
                } catch (SQLException e1) {
                    System.out.println("⚠️ '" + VERITABANI + "' veritabanına bağlanılamadı, master üzerinden deneniyor...");
                    System.out.println("   → Hata: " + e1.getMessage());
                    // master üzerinden bağlan ve fitzone DB'yi oluşturmayı dene
                    Connection masterConn = DriverManager.getConnection(MASTER_URL, KULLANICI_ADI, SIFRE);
                    System.out.println("✅ Master bağlantısı başarılı! Veritabanı oluşturuluyor...");
                    try (java.sql.Statement st = masterConn.createStatement()) {
                        // Veritabanı yoksa oluştur
                        st.execute("IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'" + VERITABANI + "') CREATE DATABASE [" + VERITABANI + "]");
                        System.out.println("✅ Veritabanı '" + VERITABANI + "' oluşturuldu (veya zaten vardı).");
                    }
                    masterConn.close();
                    // Şimdi asıl veritabanına bağlan
                    baglanti = DriverManager.getConnection(URL, KULLANICI_ADI, SIFRE);
                    System.out.println("✅ SQL Server bağlantısı başarılı! (" + VERITABANI + ")");
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ SQL Server JDBC Driver bulunamadı!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ SQL Server bağlantı hatası!");
            System.out.println("   → Sunucu: " + SUNUCU);
            System.out.println("   → Kullanıcı: " + KULLANICI_ADI);
            System.out.println("   → Hata kodu: " + e.getErrorCode());
            System.out.println("   → Hata mesajı: " + e.getMessage());
            e.printStackTrace();
        }
        return baglanti;
    }

    // ═══════════════════════════════════════════
    // BAĞLANTI KAPAT
    // ═══════════════════════════════════════════
    public static void baglantiKapat() {
        try {
            if (baglanti != null && !baglanti.isClosed()) {
                baglanti.close();
                baglanti = null;
                System.out.println("✅ SQL Server bağlantısı kapatıldı.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Bağlantı kapatma hatası!");
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════
    // BAĞLANTI TEST
    // ═══════════════════════════════════════════
    public static boolean baglantiTest() {
        Connection conn = baglantiGetir();
        if (conn != null) {
            System.out.println("✅ Veritabanı bağlantı testi başarılı!");
            return true;
        }
        System.out.println("❌ Veritabanı bağlantı testi başarısız!");
        return false;
    }
}
