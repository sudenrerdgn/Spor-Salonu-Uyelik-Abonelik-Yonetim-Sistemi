package Controller; // Controller paketine ait sınıf

import java.sql.*; // JDBC sınıfları — Connection, Statement, ResultSet, PreparedStatement

/**
 * MigrationRunner — Veritabanı rol düzeltme ve temizleme aracı.
 * 
 * Bu sınıf, veritabanındaki tutarsızlıkları düzeltmek için tek seferlik çalıştırılır.
 * Özellikle Türkçe karakter sorunu nedeniyle oluşan hatalı rol isimlerini
 * ('kullanıcı' → 'kullanici') birleştirir ve düzeltir.
 * 
 * Çalıştırma: java -cp ".;mssql-jdbc-12.4.2.jre11.jar" Controller.MigrationRunner
 */
public class MigrationRunner {
    public static void main(String[] args) {
        System.out.println("=== Migration Başlatılıyor ===");
        try {
            // Veritabanı bağlantısını al
            Connection conn = DatabaseBaglanti.baglantiGetir();
            if (conn == null) { System.out.println("❌ DB bağlantısı kurulamadı!"); return; }

            // ──────────────────────────────────────────────────
            // ADIM 0: Mevcut rolleri listele — sorunlu rolleri tespit et
            // ──────────────────────────────────────────────────
            Statement listStmt = conn.createStatement();
            // roller tablosundaki tüm kayıtları ID sırasına göre çek
            ResultSet listRs = listStmt.executeQuery("SELECT role_id, rol_adi FROM roller ORDER BY role_id");
            System.out.println("Mevcut roller:");

            int dupRolId = -1;        // Hatalı yazılmış rolün ID'si (örn: 'kullanıcı')
            int kullaniciRolId = -1;  // Doğru yazılmış 'kullanici' rolünün ID'si

            while (listRs.next()) {
                int rid = listRs.getInt("role_id");          // Rol ID
                String radi = listRs.getString("rol_adi");   // Rol adı
                // Her rolü ekrana yazdır — karakter uzunluğuyla birlikte (Türkçe karakter farkını görmek için)
                System.out.println("  [" + rid + "] = '" + radi + "' (len=" + radi.length() + ")");

                // Doğru yazılmış 'kullanici' rolünü bul
                if (radi.equals("kullanici")) kullaniciRolId = rid;

                // Hatalı yazılmış versiyonları bul:
                // 'kullanıcı' (Türkçe ı ile) gibi farklı yazımlar — "kullan" ile başlayıp "kullanici" olmayan
                if (!radi.equals("kullanici") && radi.toLowerCase().startsWith("kullan")) dupRolId = rid;
            }
            listRs.close(); listStmt.close();

            // ──────────────────────────────────────────────────
            // ADIM 1: 'kullanici' rolü tabloda yoksa oluştur
            // ──────────────────────────────────────────────────
            if (kullaniciRolId < 0) {
                // roller tablosuna 'kullanici' ekle ve oluşturulan ID'yi al
                PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO roller(rol_adi) VALUES(N'kullanici')",
                    Statement.RETURN_GENERATED_KEYS); // Auto-generated ID'yi almak için flag
                ins.executeUpdate();
                ResultSet gk = ins.getGeneratedKeys();
                if (gk.next()) kullaniciRolId = gk.getInt(1); // Oluşturulan role_id
                gk.close(); ins.close();
                System.out.println("✅ 'kullanici' rolü eklendi. role_id=" + kullaniciRolId);
            }

            // ──────────────────────────────────────────────────
            // ADIM 2: Hatalı rolle kaydedilmiş kullanıcıları doğru role taşı
            // ──────────────────────────────────────────────────
            // Örnek: rol_id=5 ('kullanıcı') olan kullanıcılar → rol_id=4 ('kullanici') yapılır
            if (dupRolId > 0 && kullaniciRolId > 0) {
                PreparedStatement migrate = conn.prepareStatement(
                    "UPDATE kullanicilar SET rol_id = ? WHERE rol_id = ?");
                migrate.setInt(1, kullaniciRolId); // Hedef: doğru rol ID
                migrate.setInt(2, dupRolId);       // Kaynak: hatalı rol ID
                int cnt = migrate.executeUpdate();  // Etkilenen satır sayısı
                migrate.close();
                System.out.println("✅ " + cnt + " kullanıcı hatalı rol " + dupRolId + " → " + kullaniciRolId + " taşındı.");

                // Artık hiçbir kullanıcı referans etmediği için hatalı rolü sil
                PreparedStatement delRol = conn.prepareStatement("DELETE FROM roller WHERE role_id = ?");
                delRol.setInt(1, dupRolId);
                delRol.executeUpdate(); delRol.close();
                System.out.println("✅ Hatalı rol (id=" + dupRolId + ") silindi.");
            }

            // ──────────────────────────────────────────────────
            // ADIM 3: 'uye' rolündeki ama uyeler tablosunda kaydı olmayan kullanıcıları düzelt
            // ──────────────────────────────────────────────────
            // Mantık: Bir kullanıcı 'uye' rolünde ama uyeler tablosunda kaydı yoksa,
            // bu kullanıcı henüz abonelik almamıştır ve rolü 'kullanici' olmalıdır.
            PreparedStatement uyeRolStmt = conn.prepareStatement(
                "SELECT role_id FROM roller WHERE rol_adi = N'uye'");
            ResultSet uyeRs = uyeRolStmt.executeQuery();
            int uyeRolId = -1;
            if (uyeRs.next()) uyeRolId = uyeRs.getInt("role_id"); // 'uye' rolünün ID'si
            uyeRs.close(); uyeRolStmt.close();

            if (uyeRolId > 0 && kullaniciRolId > 0) {
                // uyeler tablosunda kullanici_id'si olmayan 'uye' rolündeki kayıtları 'kullanici' yap
                PreparedStatement fix = conn.prepareStatement(
                    "UPDATE kullanicilar SET rol_id = ? " +
                    "WHERE rol_id = ? AND kullanici_id NOT IN (SELECT kullanici_id FROM uyeler)");
                fix.setInt(1, kullaniciRolId); // Hedef: kullanici rolü
                fix.setInt(2, uyeRolId);       // Kaynak: uye rolü (hatalı atanmış)
                int updated = fix.executeUpdate();
                fix.close();
                System.out.println("✅ " + updated + " kayıt 'uye' → 'kullanici' rolüne düzeltildi.");
            }

            // ──────────────────────────────────────────────────
            // ADIM 4: Migration sonrası durum özeti — rol bazlı kullanıcı dağılımı
            // ──────────────────────────────────────────────────
            Statement sumStmt = conn.createStatement();
            // Her rol için kaç kullanıcı olduğunu hesapla
            ResultSet sumRs = sumStmt.executeQuery(
                "SELECT r.rol_adi, COUNT(*) AS adet FROM kullanicilar k " +
                "JOIN roller r ON k.rol_id = r.role_id GROUP BY r.rol_adi ORDER BY r.rol_adi");
            System.out.println("\n--- Kullanıcı Rol Dağılımı ---");
            while (sumRs.next()) {
                // Örnek çıktı: "admin: 1 kişi", "uye: 5 kişi", "kullanici: 3 kişi"
                System.out.println("  " + sumRs.getString("rol_adi") + ": " + sumRs.getInt("adet") + " kişi");
            }
            sumRs.close(); sumStmt.close();

            System.out.println("\n=== Migration Tamamlandı ===");
        } catch (Exception e) {
            // Herhangi bir adımda hata olursa yakala ve logla
            System.out.println("❌ Migration hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
