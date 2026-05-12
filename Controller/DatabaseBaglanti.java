package Controller; // Controller paketine ait sınıf

import java.sql.Connection;       // Veritabanı bağlantı nesnesi için gerekli import
import java.sql.DriverManager;    // JDBC bağlantı yöneticisi — veritabanına bağlanmak için kullanılır
import java.sql.SQLException;     // SQL hatalarını yakalamak için kullanılan istisna sınıfı

/**
 * DatabaseBaglanti — SQL Server veritabanı bağlantı yönetim sınıfı.
 * 
 * Singleton benzeri yapıda çalışır: tek bir Connection nesnesi tutulur,
 * bağlantı kapalıysa veya null ise yeniden oluşturulur.
 * 
 * AWS RDS üzerindeki SQL Server'a bağlanır.
 * Eğer hedef veritabanı yoksa, master DB üzerinden otomatik oluşturur.
 */
public class DatabaseBaglanti {

    // ─── SQL Server Bağlantı Bilgileri ───
    // AWS RDS SQL Server örneğinin adresi (endpoint)
    private static final String SUNUCU = "fitzone.cpicuywuucpp.eu-north-1.rds.amazonaws.com";
    private static final String PORT = "1433";           // SQL Server varsayılan portu
    private static final String VERITABANI = "fitzone_db"; // Kullanılacak veritabanı adı
    private static final String KULLANICI_ADI = "admin";   // SQL Server giriş kullanıcı adı
    private static final String SIFRE = "admin1234";       // SQL Server giriş şifresi

    // SQL Authentication ile bağlantı URL'si:
    // - databaseName: Bağlanılacak veritabanı
    // - encrypt=false: SSL şifreleme kapalı (geliştirme ortamı için)
    // - loginTimeout=30: Bağlantı zaman aşımı 30 saniye
    private static final String URL = "jdbc:sqlserver://" + SUNUCU + ":" + PORT
            + ";databaseName=" + VERITABANI
            + ";encrypt=false;loginTimeout=30";

    // Master veritabanına bağlantı URL'si — hedef DB yoksa oluşturmak için kullanılır
    // databaseName belirtilmediğinde varsayılan olarak master'a bağlanır
    private static final String MASTER_URL = "jdbc:sqlserver://" + SUNUCU + ":" + PORT
            + ";encrypt=false;loginTimeout=30";

    // Singleton bağlantı nesnesi — tüm uygulama boyunca tek bağlantı kullanılır
    private static Connection baglanti = null;

    // ═══════════════════════════════════════════
    // BAĞLANTI AÇ — Mevcut bağlantıyı döndürür veya yeni bağlantı oluşturur
    // ═══════════════════════════════════════════
    /**
     * Veritabanı bağlantısını döndürür.
     * - Bağlantı yoksa veya kapalıysa yeni bağlantı oluşturur.
     * - Hedef veritabanı bulunamazsa master üzerinden DB oluşturup tekrar bağlanır.
     * 
     * @return Connection nesnesi (bağlantı başarısızsa null döner)
     */
    public static Connection baglantiGetir() {
        try {
            // Bağlantı null veya kapalıysa yeniden oluştur
            if (baglanti == null || baglanti.isClosed()) {
                // JDBC sürücüsünü yükle — SQL Server için Microsoft JDBC Driver
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                try {
                    // İlk deneme: Doğrudan hedef veritabanına bağlan (fitzone_db)
                    baglanti = DriverManager.getConnection(URL, KULLANICI_ADI, SIFRE);
                    System.out.println("✅ SQL Server bağlantısı başarılı! (" + VERITABANI + ")");
                } catch (SQLException e1) {
                    // Hedef veritabanı bulunamadı — master üzerinden oluşturmaya çalış
                    System.out.println("⚠️ '" + VERITABANI + "' veritabanına bağlanılamadı, master üzerinden deneniyor...");
                    System.out.println("   → Hata: " + e1.getMessage());

                    // Master veritabanına bağlan (varsayılan sistem DB'si)
                    Connection masterConn = DriverManager.getConnection(MASTER_URL, KULLANICI_ADI, SIFRE);
                    System.out.println("✅ Master bağlantısı başarılı! Veritabanı oluşturuluyor...");

                    // sys.databases tablosunda kontrol et, yoksa oluştur
                    try (java.sql.Statement st = masterConn.createStatement()) {
                        // IF NOT EXISTS ile güvenli oluşturma — zaten varsa hata vermez
                        st.execute("IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'" + VERITABANI + "') CREATE DATABASE [" + VERITABANI + "]");
                        System.out.println("✅ Veritabanı '" + VERITABANI + "' oluşturuldu (veya zaten vardı).");
                    }
                    masterConn.close(); // Master bağlantısını kapat

                    // Artık oluşturulmuş olan hedef veritabanına bağlan
                    baglanti = DriverManager.getConnection(URL, KULLANICI_ADI, SIFRE);
                    System.out.println("✅ SQL Server bağlantısı başarılı! (" + VERITABANI + ")");
                }
            }
        } catch (ClassNotFoundException e) {
            // JDBC sürücüsü classpath'te bulunamadı — .jar dosyası eksik olabilir
            System.out.println("❌ SQL Server JDBC Driver bulunamadı!");
            e.printStackTrace();
        } catch (SQLException e) {
            // Genel SQL bağlantı hatası — sunucu erişilemez, yetki hatası vb.
            System.out.println("❌ SQL Server bağlantı hatası!");
            System.out.println("   → Sunucu: " + SUNUCU);
            System.out.println("   → Kullanıcı: " + KULLANICI_ADI);
            System.out.println("   → Hata kodu: " + e.getErrorCode());
            System.out.println("   → Hata mesajı: " + e.getMessage());
            e.printStackTrace();
        }
        return baglanti; // Bağlantı nesnesini döndür (başarısızsa null)
    }

    // ═══════════════════════════════════════════
    // BAĞLANTI KAPAT — Açık bağlantıyı güvenli şekilde kapatır
    // ═══════════════════════════════════════════
    /**
     * Mevcut veritabanı bağlantısını kapatır ve null'a eşitler.
     * Uygulama kapatılırken çağrılmalıdır.
     */
    public static void baglantiKapat() {
        try {
            // Bağlantı varsa ve açıksa kapat
            if (baglanti != null && !baglanti.isClosed()) {
                baglanti.close();  // JDBC bağlantısını kapat
                baglanti = null;   // Referansı null yap — sonraki çağrıda yeniden oluşturulur
                System.out.println("✅ SQL Server bağlantısı kapatıldı.");
            }
        } catch (SQLException e) {
            // Kapatma sırasında hata oluşursa logla
            System.out.println("❌ Bağlantı kapatma hatası!");
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════
    // BAĞLANTI TEST — Veritabanı erişilebilirliğini kontrol eder
    // ═══════════════════════════════════════════
    /**
     * Veritabanı bağlantısının çalışıp çalışmadığını test eder.
     * Sunucu başlatılırken çağrılır — bağlantı kurulamazsa sunucu başlamaz.
     * 
     * @return true = bağlantı başarılı, false = bağlantı kurulamadı
     */
    public static boolean baglantiTest() {
        Connection conn = baglantiGetir(); // Bağlantıyı al (yoksa oluşturmayı dener)
        if (conn != null) {
            System.out.println("✅ Veritabanı bağlantı testi başarılı!");
            return true;
        }
        System.out.println("❌ Veritabanı bağlantı testi başarısız!");
        return false;
    }
}
