package Controller;

import java.sql.*;

/**
 * Temizleme: Hatalı 'kullanıcı' (Türkçe ı) rolünü 'kullanici' (ASCII) ile birleştir.
 */
public class MigrationRunner {
    public static void main(String[] args) {
        System.out.println("=== Migration Başlatılıyor ===");
        try {
            Connection conn = DatabaseBaglanti.baglantiGetir();
            if (conn == null) { System.out.println("❌ DB bağlantısı kurulamadı!"); return; }

            // ADIM 0: Tüm rolleri listele
            Statement listStmt = conn.createStatement();
            ResultSet listRs = listStmt.executeQuery("SELECT role_id, rol_adi FROM roller ORDER BY role_id");
            System.out.println("Mevcut roller:");
            int dupRolId = -1;
            int kullaniciRolId = -1;
            while (listRs.next()) {
                int rid = listRs.getInt("role_id");
                String radi = listRs.getString("rol_adi");
                System.out.println("  [" + rid + "] = '" + radi + "' (len=" + radi.length() + ")");
                if (radi.equals("kullanici")) kullaniciRolId = rid;
                // Farklı yazımlı olanları bul (kullanıcı, kullanıcı vb.)
                if (!radi.equals("kullanici") && radi.toLowerCase().startsWith("kullan")) dupRolId = rid;
            }
            listRs.close(); listStmt.close();

            // ADIM 1: 'kullanici' rolü yoksa ekle
            if (kullaniciRolId < 0) {
                PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO roller(rol_adi) VALUES(N'kullanici')",
                    Statement.RETURN_GENERATED_KEYS);
                ins.executeUpdate();
                ResultSet gk = ins.getGeneratedKeys();
                if (gk.next()) kullaniciRolId = gk.getInt(1);
                gk.close(); ins.close();
                System.out.println("✅ 'kullanici' rolü eklendi. role_id=" + kullaniciRolId);
            }

            // ADIM 2: Varsa hatalı yazımlı rol ID'sine sahip kullanıcıları doğru role taşı
            if (dupRolId > 0 && kullaniciRolId > 0) {
                PreparedStatement migrate = conn.prepareStatement(
                    "UPDATE kullanicilar SET rol_id = ? WHERE rol_id = ?");
                migrate.setInt(1, kullaniciRolId);
                migrate.setInt(2, dupRolId);
                int cnt = migrate.executeUpdate();
                migrate.close();
                System.out.println("✅ " + cnt + " kullanıcı hatalı rol " + dupRolId + " → " + kullaniciRolId + " taşındı.");

                // Hatalı rolü sil
                PreparedStatement delRol = conn.prepareStatement("DELETE FROM roller WHERE role_id = ?");
                delRol.setInt(1, dupRolId);
                delRol.executeUpdate(); delRol.close();
                System.out.println("✅ Hatalı rol (id=" + dupRolId + ") silindi.");
            }

            // ADIM 3: 'uye' rolünde ama uyeler tablosunda KAYDI OLMAYAN kullanıcıları 'kullanici' yap
            PreparedStatement uyeRolStmt = conn.prepareStatement(
                "SELECT role_id FROM roller WHERE rol_adi = N'uye'");
            ResultSet uyeRs = uyeRolStmt.executeQuery();
            int uyeRolId = -1;
            if (uyeRs.next()) uyeRolId = uyeRs.getInt("role_id");
            uyeRs.close(); uyeRolStmt.close();

            if (uyeRolId > 0 && kullaniciRolId > 0) {
                PreparedStatement fix = conn.prepareStatement(
                    "UPDATE kullanicilar SET rol_id = ? " +
                    "WHERE rol_id = ? AND kullanici_id NOT IN (SELECT kullanici_id FROM uyeler)");
                fix.setInt(1, kullaniciRolId);
                fix.setInt(2, uyeRolId);
                int updated = fix.executeUpdate();
                fix.close();
                System.out.println("✅ " + updated + " kayıt 'uye' → 'kullanici' rolüne düzeltildi.");
            }

            // ADIM 4: Özet
            Statement sumStmt = conn.createStatement();
            ResultSet sumRs = sumStmt.executeQuery(
                "SELECT r.rol_adi, COUNT(*) AS adet FROM kullanicilar k " +
                "JOIN roller r ON k.rol_id = r.role_id GROUP BY r.rol_adi ORDER BY r.rol_adi");
            System.out.println("\n--- Kullanıcı Rol Dağılımı ---");
            while (sumRs.next()) {
                System.out.println("  " + sumRs.getString("rol_adi") + ": " + sumRs.getInt("adet") + " kişi");
            }
            sumRs.close(); sumStmt.close();

            System.out.println("\n=== Migration Tamamlandı ===");
        } catch (Exception e) {
            System.out.println("❌ Migration hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
