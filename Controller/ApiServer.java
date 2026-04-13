package Controller;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Locale;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * FitZone Pro — Backend API Sunucusu
 * Frontend (app.js) ile SQL Server arasında köprü görevi görür.
 * 
 * Çalıştırmak için:
 *   javac -cp ".;mssql-jdbc-12.4.2.jre11.jar" Controller/ApiServer.java Controller/DatabaseBaglanti.java
 *   java -cp ".;mssql-jdbc-12.4.2.jre11.jar" Controller.ApiServer
 * 
 * Endpoints:
 *   POST /api/kayit   → Yeni kullanıcı kaydı
 *   POST /api/giris   → Kullanıcı girişi
 *   GET  /api/uyeler   → Tüm üyeleri getir
 *   GET  /api/test     → Bağlantı testi
 */
public class ApiServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        // JSON'da ondalık sayıların nokta kullanması için
        Locale.setDefault(Locale.US);
        // Bağlantı testi
        System.out.println("═══════════════════════════════════════════");
        System.out.println("  FitZone Pro — Backend API Sunucusu");
        System.out.println("═══════════════════════════════════════════");

        if (!DatabaseBaglanti.baglantiTest()) {
            System.out.println("⚠️ Veritabanı bağlantısı kurulamadı! Lütfen ayarları kontrol edin.");
            System.out.println("   → DatabaseBaglanti.java dosyasını düzenleyin.");
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Endpoint'leri tanımla
        server.createContext("/api/kayit",            new KayitHandler());
        server.createContext("/api/giris",            new GirisHandler());
        server.createContext("/api/uyeler",           new UyelerHandler());
        server.createContext("/api/uye-guncelle",     new UyeGuncelleHandler());
        server.createContext("/api/uye-sil",          new UyeSilHandler());
        server.createContext("/api/istatistikler",    new IstatistiklerHandler());
        server.createContext("/api/odemeler",         new OdemelerHandler());
        server.createContext("/api/abonelikler",      new AboneliklerHandler());
        server.createContext("/api/planlar",          new PlanlarHandler());
        server.createContext("/api/dersler",          new DerslerHandler());
        server.createContext("/api/antrenorler-detay", new AntrenorlerDetayHandler());
        server.createContext("/api/giris-cikis",      new GirisCikisHandler());
        server.createContext("/api/ekipman",          new EkipmanHandler());
        server.createContext("/api/test",             new TestHandler());
        server.createContext("/",                     new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("✅ API Sunucusu başlatıldı: http://localhost:" + PORT);
        System.out.println("   → 🌐 http://localhost:" + PORT + "  — Frontend (index.html)");
        System.out.println("   → POST   /api/kayit            — Yeni kullanıcı kaydı");
        System.out.println("   → POST   /api/giris            — Kullanıcı girişi");
        System.out.println("   → GET    /api/uyeler           — Tüm üyeleri getir");
        System.out.println("   → POST   /api/uye-guncelle     — Üye güncelle");
        System.out.println("   → POST   /api/uye-sil          — Üye sil");
        System.out.println("   → GET    /api/istatistikler    — Dashboard istatistikleri");
        System.out.println("   → GET    /api/odemeler         — Ödeme listesi");
        System.out.println("   → GET    /api/abonelikler      — Abonelik listesi");
        System.out.println("   → GET    /api/planlar          — Üyelik planları");
        System.out.println("   → GET    /api/dersler          — Dersler ve program");
        System.out.println("   → GET    /api/antrenorler-detay — Antrenör detayları");
        System.out.println("   → GET    /api/giris-cikis      — Giriş/çıkış kayıtları");
        System.out.println("   → GET    /api/ekipman          — Ekipman listesi");
        System.out.println("   → GET    /api/test             — Bağlantı testi");
        System.out.println("═══════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════
    // CORS Header ekle (frontend ile iletişim için)
    // ═══════════════════════════════════════════
    private static void corsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    }

    private static void sendResponse(HttpExchange exchange, int code, String json) throws IOException {
        corsHeaders(exchange);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    // Basit JSON parser (harici kütüphane olmadan)
    private static String jsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx);
        int start = json.indexOf("\"", colon + 1) + 1;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    // Şifreleri SHA-256 ile hashlemek için yardımcı metot
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algoritması bulunamadı!", e);
        }
    }

    // ═══════════════════════════════════════════
    // KAYIT ENDPOINT — POST /api/kayit
    // ═══════════════════════════════════════════
    static class KayitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // OPTIONS isteği (CORS preflight)
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"basarili\":false,\"mesaj\":\"Sadece POST kabul edilir\"}");
                return;
            }

            String body = readBody(exchange);
            String ad     = jsonValue(body, "ad");
            String soyad  = jsonValue(body, "soyad");
            String email  = jsonValue(body, "email");
            String sifre  = jsonValue(body, "sifre");
            String rol    = jsonValue(body, "rol");
            
            String telefon = jsonValue(body, "telefon");
            String cinsiyet = jsonValue(body, "cinsiyet");
            String dogumTarihi = jsonValue(body, "dogum_tarihi");

            if (ad == null || soyad == null || email == null || sifre == null) {
                sendResponse(exchange, 400, "{\"basarili\":false,\"mesaj\":\"Eksik alanlar!\"}");
                return;
            }
            if (rol == null || rol.isEmpty()) rol = "uye";
            
            String sifreHash = hashPassword(sifre);

            // Email tekrar kontrolü
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();

                // 1) Email kontrolü
                PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM kullanicilar WHERE email = ?");
                check.setString(1, email.toLowerCase());
                ResultSet rs = check.executeQuery();
                boolean emailVar = rs.next() && rs.getInt(1) > 0;
                rs.close();
                check.close();

                if (emailVar) {
                    sendResponse(exchange, 409, "{\"basarili\":false,\"mesaj\":\"Bu email zaten kayıtlı!\"}");
                    return;
                }

                // 2) Rol adından rol_id bul
                int rolId = 2; // varsayılan: uye
                PreparedStatement rolStmt = conn.prepareStatement("SELECT role_id FROM roller WHERE rol_adi = ?");
                rolStmt.setString(1, rol);
                ResultSet rolRs = rolStmt.executeQuery();
                if (rolRs.next()) {
                    rolId = rolRs.getInt("role_id");
                }
                rolRs.close();
                rolStmt.close();

                // 3) Kaydet
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO kullanicilar (ad, soyad, email, sifre_hash, telefon, cinsiyet, dogum_tarihi, rol_id, durum) VALUES (?, ?, ?, ?, ?, ?, ?, ?, N'aktif')");
                stmt.setString(1, ad);
                stmt.setString(2, soyad);
                stmt.setString(3, email.toLowerCase());
                stmt.setString(4, sifreHash);
                stmt.setString(5, telefon != null && !telefon.isEmpty() ? telefon : null);
                stmt.setString(6, cinsiyet != null && !cinsiyet.isEmpty() ? cinsiyet : null);
                
                if (dogumTarihi != null && !dogumTarihi.isEmpty()) {
                    stmt.setDate(7, java.sql.Date.valueOf(dogumTarihi));
                } else {
                    stmt.setNull(7, java.sql.Types.DATE);
                }
                
                stmt.setInt(8, rolId);
                stmt.executeUpdate();
                stmt.close();

                System.out.println("✅ Yeni kullanıcı kaydedildi: " + ad + " " + soyad + " (" + email + ")");
                sendResponse(exchange, 200, "{\"basarili\":true,\"mesaj\":\"Kayıt başarılı!\"}");

            } catch (SQLException e) {
                System.out.println("❌ Kayıt hatası: " + e.getMessage());
                sendResponse(exchange, 500, "{\"basarili\":false,\"mesaj\":\"Veritabanı hatası: " + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // GİRİŞ ENDPOINT — POST /api/giris
    // ═══════════════════════════════════════════
    static class GirisHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"basarili\":false,\"mesaj\":\"Sadece POST kabul edilir\"}");
                return;
            }

            String body = readBody(exchange);
            String email = jsonValue(body, "email");
            String sifre = jsonValue(body, "sifre");

            if (email == null || sifre == null) {
                sendResponse(exchange, 400, "{\"basarili\":false,\"mesaj\":\"Email ve şifre gerekli!\"}");
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT k.kullanici_id, k.ad, k.soyad, k.email, k.sifre_hash, r.rol_adi " +
                        "FROM kullanicilar k JOIN roller r ON k.rol_id = r.role_id WHERE k.email = ?");
                stmt.setString(1, email.toLowerCase());
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    sendResponse(exchange, 401, "{\"basarili\":false,\"mesaj\":\"Kullanıcı bulunamadı!\"}");
                    return;
                }

                // Tüm değerleri önce değişkenlere al
                int id = rs.getInt("kullanici_id");
                String ad = rs.getString("ad");
                String soyad = rs.getString("soyad");
                String dbEmail = rs.getString("email");
                String kayitliSifre = rs.getString("sifre_hash");
                String rolAdi = rs.getString("rol_adi");

                rs.close();
                stmt.close();

                if (!hashPassword(sifre).equals(kayitliSifre)) {
                    sendResponse(exchange, 401, "{\"basarili\":false,\"mesaj\":\"Şifre yanlış!\"}");
                    return;
                }

                // Son giriş tarihini güncelle
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE kullanicilar SET son_giris = GETDATE() WHERE kullanici_id = ?");
                updateStmt.setInt(1, id);
                updateStmt.executeUpdate();
                updateStmt.close();

                // Giriş başarılı — kullanıcı bilgilerini gönder
                String json = String.format(
                        "{\"basarili\":true,\"mesaj\":\"Giriş başarılı!\",\"kullanici\":{\"id\":%d,\"ad\":\"%s\",\"soyad\":\"%s\",\"email\":\"%s\",\"rol\":\"%s\"}}",
                        id, ad, soyad, dbEmail, rolAdi
                );
                System.out.println("✅ Giriş yapıldı: " + ad + " " + soyad);
                sendResponse(exchange, 200, json);

            } catch (SQLException e) {
                System.out.println("❌ Giriş hatası: " + e.getMessage());
                sendResponse(exchange, 500, "{\"basarili\":false,\"mesaj\":\"Veritabanı hatası\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // ÜYELER ENDPOINT — GET /api/uyeler
    // ═══════════════════════════════════════════
    static class UyelerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT k.kullanici_id, k.ad, k.soyad, k.email, k.telefon, k.cinsiyet, " +
                        "r.rol_adi, k.durum, k.kayit_tarihi " +
                        "FROM kullanicilar k JOIN roller r ON k.rol_id = r.role_id " +
                        "WHERE r.rol_adi = N'uye' " +
                        "ORDER BY k.kullanici_id");

                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    String telefon = rs.getString("telefon");
                    String cinsiyet = rs.getString("cinsiyet");
                    Timestamp kayitTs = rs.getTimestamp("kayit_tarihi");
                    String kayitTarihi = kayitTs != null ? kayitTs.toString().substring(0, 10) : "";
                    json.append(String.format(
                            "{\"id\":%d,\"ad\":\"%s\",\"soyad\":\"%s\",\"email\":\"%s\"," +
                            "\"telefon\":\"%s\",\"cinsiyet\":\"%s\"," +
                            "\"rol\":\"%s\",\"durum\":\"%s\",\"kayitTarihi\":\"%s\"}",
                            rs.getInt("kullanici_id"),
                            rs.getString("ad"),
                            rs.getString("soyad"),
                            rs.getString("email"),
                            telefon != null ? telefon : "",
                            cinsiyet != null ? cinsiyet : "",
                            rs.getString("rol_adi"),
                            rs.getString("durum"),
                            kayitTarihi
                    ));
                    first = false;
                }
                json.append("]");
                rs.close();
                stmt.close();

                sendResponse(exchange, 200, json.toString());

            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // ÜYE GÜNCELLE ENDPOINT — POST /api/uye-guncelle
    // ═══════════════════════════════════════════
    static class UyeGuncelleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"basarili\":false,\"mesaj\":\"Sadece POST kabul edilir\"}");
                return;
            }

            String body = readBody(exchange);
            String idStr   = jsonValue(body, "id");
            String ad      = jsonValue(body, "ad");
            String soyad   = jsonValue(body, "soyad");
            String email   = jsonValue(body, "email");
            String telefon = jsonValue(body, "telefon");
            String durum   = jsonValue(body, "durum");

            if (idStr == null || ad == null || soyad == null || email == null) {
                sendResponse(exchange, 400, "{\"basarili\":false,\"mesaj\":\"Eksik alanlar!\"}");
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE kullanicilar SET ad=?, soyad=?, email=?, telefon=?, durum=? WHERE kullanici_id=?");
                stmt.setString(1, ad);
                stmt.setString(2, soyad);
                stmt.setString(3, email.toLowerCase());
                stmt.setString(4, telefon != null ? telefon : "");
                stmt.setString(5, durum != null ? durum : "aktif");
                stmt.setInt(6, Integer.parseInt(idStr));
                int updated = stmt.executeUpdate();
                stmt.close();

                if (updated > 0) {
                    System.out.println("✅ Üye güncellendi: " + ad + " " + soyad);
                    sendResponse(exchange, 200, "{\"basarili\":true,\"mesaj\":\"Üye güncellendi!\"}");
                } else {
                    sendResponse(exchange, 404, "{\"basarili\":false,\"mesaj\":\"Üye bulunamadı!\"}");
                }
            } catch (SQLException e) {
                System.out.println("❌ Güncelleme hatası: " + e.getMessage());
                sendResponse(exchange, 500, "{\"basarili\":false,\"mesaj\":\"Veritabanı hatası\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // ÜYE SİL ENDPOINT — POST /api/uye-sil
    // ═══════════════════════════════════════════
    static class UyeSilHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"basarili\":false,\"mesaj\":\"Sadece POST kabul edilir\"}");
                return;
            }

            String body = readBody(exchange);
            String idStr = jsonValue(body, "id");

            if (idStr == null) {
                sendResponse(exchange, 400, "{\"basarili\":false,\"mesaj\":\"ID gerekli!\"}");
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                int uid = Integer.parseInt(idStr);

                // Önce bağımlı tablolardan sil (uyeler → abonelikler vs.)
                PreparedStatement delUye = conn.prepareStatement("DELETE FROM uyeler WHERE kullanici_id = ?");
                delUye.setInt(1, uid);
                delUye.executeUpdate();
                delUye.close();

                // Kullanıcıyı sil
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM kullanicilar WHERE kullanici_id = ?");
                stmt.setInt(1, uid);
                int deleted = stmt.executeUpdate();
                stmt.close();

                if (deleted > 0) {
                    System.out.println("✅ Üye silindi: ID=" + uid);
                    sendResponse(exchange, 200, "{\"basarili\":true,\"mesaj\":\"Üye silindi!\"}");
                } else {
                    sendResponse(exchange, 404, "{\"basarili\":false,\"mesaj\":\"Üye bulunamadı!\"}");
                }
            } catch (Exception e) {
                System.out.println("❌ Silme hatası: " + e.getMessage());
                sendResponse(exchange, 500, "{\"basarili\":false,\"mesaj\":\"Veritabanı hatası: " + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // İSTATİSTİKLER ENDPOINT — GET /api/istatistikler
    // ═══════════════════════════════════════════
    static class IstatistiklerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange); exchange.sendResponseHeaders(204, -1); return;
            }
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();

                // Toplam üye sayısı (sadece 'uye' rolü)
                ResultSet rs1 = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM kullanicilar k " +
                    "JOIN roller r ON k.rol_id = r.role_id WHERE r.rol_adi = N'uye'");
                int toplamUye = rs1.next() ? rs1.getInt("cnt") : 0;
                rs1.close();

                // Aktif üye sayısı
                ResultSet rs2 = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM kullanicilar k " +
                    "JOIN roller r ON k.rol_id = r.role_id " +
                    "WHERE r.rol_adi = N'uye' AND k.durum = N'aktif'");
                int aktifUye = rs2.next() ? rs2.getInt("cnt") : 0;
                rs2.close();

                // Pasif üye sayısı
                ResultSet rs3 = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM kullanicilar k " +
                    "JOIN roller r ON k.rol_id = r.role_id " +
                    "WHERE r.rol_adi = N'uye' AND k.durum = N'pasif'");
                int pasifUye = rs3.next() ? rs3.getInt("cnt") : 0;
                rs3.close();

                // Süresi dolan abonelik sayısı
                ResultSet rs4 = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM uye_abonelikleri WHERE durum = N'suresi_doldu'");
                int suresiDolan = rs4.next() ? rs4.getInt("cnt") : 0;
                rs4.close();

                // Bu ay gelir (tamamlanan ödemeler)
                ResultSet rs5 = stmt.executeQuery(
                    "SELECT ISNULL(SUM(miktar), 0) AS toplam FROM odemeler " +
                    "WHERE durum = N'tamamlandi' AND MONTH(odeme_tarihi) = MONTH(GETDATE()) AND YEAR(odeme_tarihi) = YEAR(GETDATE())");
                double buAyGelir = rs5.next() ? rs5.getDouble("toplam") : 0;
                rs5.close();

                // Toplam gelir
                ResultSet rs6 = stmt.executeQuery(
                    "SELECT ISNULL(SUM(miktar), 0) AS toplam FROM odemeler WHERE durum = N'tamamlandi'");
                double toplamGelir = rs6.next() ? rs6.getDouble("toplam") : 0;
                rs6.close();

                // Bugün içeride olan üye sayısı
                ResultSet rs7 = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM giris_cikis_kayitlari " +
                    "WHERE durum = N'giris' AND CAST(giris_saat AS DATE) = CAST(GETDATE() AS DATE)");
                int bugunIceride = rs7.next() ? rs7.getInt("cnt") : 0;
                rs7.close();

                // Bu ay yeni kaydolan üye sayısı
                ResultSet rs8 = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM kullanicilar k " +
                    "JOIN roller r ON k.rol_id = r.role_id " +
                    "WHERE r.rol_adi = N'uye' " +
                    "AND MONTH(k.kayit_tarihi) = MONTH(GETDATE()) " +
                    "AND YEAR(k.kayit_tarihi) = YEAR(GETDATE())");
                int buAyYeniKayit = rs8.next() ? rs8.getInt("cnt") : 0;
                rs8.close();

                // Aktif abonelik sayısı (geriye dönük uyumluluk)
                ResultSet rs9 = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM uye_abonelikleri WHERE durum = N'aktif'");
                int aktifAbonelik = rs9.next() ? rs9.getInt("cnt") : 0;
                rs9.close();

                stmt.close();

                String json = String.format(
                    "{\"toplamUye\":%d,\"aktifUye\":%d,\"pasifUye\":%d," +
                    "\"suresiDolan\":%d,\"aktifAbonelik\":%d," +
                    "\"buAyGelir\":%.2f,\"toplamGelir\":%.2f," +
                    "\"bugunIceride\":%d,\"buAyYeniKayit\":%d}",
                    toplamUye, aktifUye, pasifUye,
                    suresiDolan, aktifAbonelik,
                    buAyGelir, toplamGelir,
                    bugunIceride, buAyYeniKayit);
                sendResponse(exchange, 200, json);

            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // ÖDEMELER ENDPOINT — GET /api/odemeler
    // ═══════════════════════════════════════════
    static class OdemelerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange); exchange.sendResponseHeaders(204, -1); return;
            }
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT o.odeme_id, k.ad, k.soyad, p.plan_adi, o.miktar, " +
                    "o.odeme_yontemi, o.odeme_tarihi, o.durum " +
                    "FROM odemeler o " +
                    "JOIN uye_abonelikleri a ON o.abonelik_id = a.abonelik_id " +
                    "JOIN uyeler u ON a.uye_id = u.uye_id " +
                    "JOIN kullanicilar k ON u.kullanici_id = k.kullanici_id " +
                    "JOIN uye_planlari p ON a.plan_id = p.plan_id " +
                    "ORDER BY o.odeme_tarihi DESC");

                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    String yontem = rs.getString("odeme_yontemi");
                    String yontemLabel = "Bilinmiyor";
                    if ("kredi_karti".equals(yontem)) yontemLabel = "Kredi Kartı";
                    else if ("nakit".equals(yontem)) yontemLabel = "Nakit";
                    else if ("havale".equals(yontem)) yontemLabel = "Havale";
                    else if ("online".equals(yontem)) yontemLabel = "Online";

                    Timestamp ts = rs.getTimestamp("odeme_tarihi");
                    String tarih = ts != null ? ts.toString().substring(0, 10) : "";

                    json.append(String.format(
                        "{\"id\":%d,\"uye\":\"%s %s\",\"plan\":\"%s\",\"miktar\":%.2f," +
                        "\"yontem\":\"%s\",\"tarih\":\"%s\",\"durum\":\"%s\"}",
                        rs.getInt("odeme_id"),
                        rs.getString("ad"), rs.getString("soyad"),
                        rs.getString("plan_adi"), rs.getDouble("miktar"),
                        yontemLabel, tarih, rs.getString("durum")));
                    first = false;
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(exchange, 200, json.toString());

            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // ABONELİKLER ENDPOINT — GET /api/abonelikler
    // ═══════════════════════════════════════════
    static class AboneliklerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange); exchange.sendResponseHeaders(204, -1); return;
            }
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT a.abonelik_id, k.ad, k.soyad, p.plan_adi, " +
                    "a.baslangic_tarihi, a.bitis_tarihi, a.otomatik_yenile, a.durum " +
                    "FROM uye_abonelikleri a " +
                    "JOIN uyeler u ON a.uye_id = u.uye_id " +
                    "JOIN kullanicilar k ON u.kullanici_id = k.kullanici_id " +
                    "JOIN uye_planlari p ON a.plan_id = p.plan_id " +
                    "ORDER BY a.baslangic_tarihi DESC");

                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append(String.format(
                        "{\"id\":%d,\"uye\":\"%s %s\",\"plan\":\"%s\"," +
                        "\"baslangic\":\"%s\",\"bitis\":\"%s\",\"otomatik\":%s,\"durum\":\"%s\"}",
                        rs.getInt("abonelik_id"),
                        rs.getString("ad"), rs.getString("soyad"),
                        rs.getString("plan_adi"),
                        rs.getString("baslangic_tarihi"), rs.getString("bitis_tarihi"),
                        rs.getBoolean("otomatik_yenile") ? "true" : "false",
                        rs.getString("durum")));
                    first = false;
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(exchange, 200, json.toString());

            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // PLANLAR ENDPOINT — GET /api/planlar
    // ═══════════════════════════════════════════
    static class PlanlarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange); exchange.sendResponseHeaders(204, -1); return;
            }
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT p.plan_id, p.plan_adi, p.fiyat, p.sure_ay, p.aciklama, p.ozellikler, p.durum, " +
                    "(SELECT COUNT(*) FROM uye_abonelikleri a WHERE a.plan_id = p.plan_id AND a.durum = N'aktif') AS aktifUye " +
                    "FROM uye_planlari p ORDER BY p.fiyat DESC");

                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    String ozellikler = rs.getString("ozellikler");
                    if (ozellikler == null) ozellikler = "[]";
                    json.append(String.format(
                        "{\"id\":%d,\"ad\":\"%s\",\"fiyat\":%.2f,\"sureAy\":%d," +
                        "\"aciklama\":\"%s\",\"ozellikler\":%s,\"durum\":\"%s\",\"aktifUye\":%d}",
                        rs.getInt("plan_id"),
                        rs.getString("plan_adi"), rs.getDouble("fiyat"), rs.getInt("sure_ay"),
                        rs.getString("aciklama") != null ? rs.getString("aciklama").replace("\"", "'") : "",
                        ozellikler,
                        rs.getString("durum"), rs.getInt("aktifUye")));
                    first = false;
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(exchange, 200, json.toString());

            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // DERSLER ENDPOINT — GET /api/dersler
    // ═══════════════════════════════════════════
    static class DerslerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange); exchange.sendResponseHeaders(204, -1); return;
            }
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();

                // Dersler
                ResultSet rs = stmt.executeQuery(
                    "SELECT s.ders_id, s.ders_adi, k.ad + ' ' + k.soyad AS antrenor, " +
                    "s.kontenjan, s.sure_dakika, s.kategori, s.durum " +
                    "FROM siniflar s " +
                    "LEFT JOIN antrenorler a ON s.antrenor_id = a.antrenor_id " +
                    "LEFT JOIN kullanicilar k ON a.kullanici_id = k.kullanici_id");

                StringBuilder derslerJson = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) derslerJson.append(",");
                    derslerJson.append(String.format(
                        "{\"id\":%d,\"ders\":\"%s\",\"antrenor\":\"%s\"," +
                        "\"kontenjan\":%d,\"sure\":%d,\"kategori\":\"%s\",\"durum\":\"%s\"}",
                        rs.getInt("ders_id"), rs.getString("ders_adi"),
                        rs.getString("antrenor") != null ? rs.getString("antrenor") : "",
                        rs.getInt("kontenjan"), rs.getInt("sure_dakika"),
                        rs.getString("kategori") != null ? rs.getString("kategori") : "",
                        rs.getString("durum")));
                    first = false;
                }
                derslerJson.append("]");
                rs.close();

                // Program
                ResultSet rs2 = stmt.executeQuery(
                    "SELECT sp.program_id, s.ders_adi, sp.gun, " +
                    "CONVERT(VARCHAR(5), sp.baslangic_saati, 108) AS bSaat, " +
                    "CONVERT(VARCHAR(5), sp.bitis_saati, 108) AS bsSaat, " +
                    "sp.salon, sp.durum " +
                    "FROM sinif_programlari sp " +
                    "JOIN siniflar s ON sp.ders_id = s.ders_id " +
                    "ORDER BY CASE sp.gun " +
                    "WHEN N'Pazartesi' THEN 1 WHEN N'Salı' THEN 2 WHEN N'Çarşamba' THEN 3 " +
                    "WHEN N'Perşembe' THEN 4 WHEN N'Cuma' THEN 5 WHEN N'Cumartesi' THEN 6 WHEN N'Pazar' THEN 7 END");

                StringBuilder programJson = new StringBuilder("[");
                first = true;
                while (rs2.next()) {
                    if (!first) programJson.append(",");
                    programJson.append(String.format(
                        "{\"id\":%d,\"ders\":\"%s\",\"gun\":\"%s\",\"saat\":\"%s–%s\",\"salon\":\"%s\",\"durum\":\"%s\"}",
                        rs2.getInt("program_id"), rs2.getString("ders_adi"),
                        rs2.getString("gun"),
                        rs2.getString("bSaat"), rs2.getString("bsSaat"),
                        rs2.getString("salon") != null ? rs2.getString("salon") : "",
                        rs2.getString("durum")));
                    first = false;
                }
                programJson.append("]");
                rs2.close();

                // Rezervasyonlar
                ResultSet rs3 = stmt.executeQuery(
                    "SELECT r.rezervasyon_id, k.ad + ' ' + k.soyad AS uye, s.ders_adi, " +
                    "CONVERT(VARCHAR(10), r.r_tarih, 120) AS tarih, " +
                    "CONVERT(VARCHAR(5), sp.baslangic_saati, 108) + '–' + CONVERT(VARCHAR(5), sp.bitis_saati, 108) AS saat, " +
                    "r.durum " +
                    "FROM sinif_rezervasyonlari r " +
                    "JOIN uyeler u ON r.uye_id = u.uye_id " +
                    "JOIN kullanicilar k ON u.kullanici_id = k.kullanici_id " +
                    "JOIN sinif_programlari sp ON r.program_id = sp.program_id " +
                    "JOIN siniflar s ON sp.ders_id = s.ders_id " +
                    "ORDER BY r.r_tarih DESC");

                StringBuilder rezJson = new StringBuilder("[");
                first = true;
                while (rs3.next()) {
                    if (!first) rezJson.append(",");
                    rezJson.append(String.format(
                        "{\"id\":%d,\"uye\":\"%s\",\"ders\":\"%s\",\"tarih\":\"%s\",\"saat\":\"%s\",\"durum\":\"%s\"}",
                        rs3.getInt("rezervasyon_id"), rs3.getString("uye"),
                        rs3.getString("ders_adi"), rs3.getString("tarih"),
                        rs3.getString("saat"), rs3.getString("durum")));
                    first = false;
                }
                rezJson.append("]");
                rs3.close();
                stmt.close();

                String json = "{\"dersler\":" + derslerJson + ",\"program\":" + programJson +
                              ",\"rezervasyonlar\":" + rezJson + "}";
                sendResponse(exchange, 200, json);

            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // ANTRENÖRLER DETAY ENDPOINT — GET /api/antrenorler-detay
    // ═══════════════════════════════════════════
    static class AntrenorlerDetayHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange); exchange.sendResponseHeaders(204, -1); return;
            }
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT a.antrenor_id, k.ad + ' ' + k.soyad AS isim, k.email, " +
                    "a.uzmanlik, a.deneyim_yili, a.sertifikalar, a.biyografi, a.durum, " +
                    "(SELECT COUNT(*) FROM siniflar s WHERE s.antrenor_id = a.antrenor_id) AS dersCount " +
                    "FROM antrenorler a " +
                    "JOIN kullanicilar k ON a.kullanici_id = k.kullanici_id");

                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append(String.format(
                        "{\"id\":%d,\"isim\":\"%s\",\"email\":\"%s\"," +
                        "\"uzmanlik\":\"%s\",\"deneyim\":%d,\"sertifikalar\":\"%s\"," +
                        "\"biyografi\":\"%s\",\"durum\":\"%s\",\"dersCount\":%d}",
                        rs.getInt("antrenor_id"),
                        rs.getString("isim"),
                        rs.getString("email") != null ? rs.getString("email") : "",
                        rs.getString("uzmanlik") != null ? rs.getString("uzmanlik").replace("\"", "'") : "",
                        rs.getInt("deneyim_yili"),
                        rs.getString("sertifikalar") != null ? rs.getString("sertifikalar").replace("\"", "'") : "",
                        rs.getString("biyografi") != null ? rs.getString("biyografi").replace("\"", "'") : "",
                        rs.getString("durum"),
                        rs.getInt("dersCount")));
                    first = false;
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(exchange, 200, json.toString());

            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // GİRİŞ/ÇIKIŞ ENDPOINT — GET /api/giris-cikis
    // ═══════════════════════════════════════════
    static class GirisCikisHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange); exchange.sendResponseHeaders(204, -1); return;
            }
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT g.g_c_id, k.ad + ' ' + k.soyad AS isim, " +
                    "CONVERT(VARCHAR(5), g.giris_saat, 108) AS girisSaat, " +
                    "CASE WHEN g.cikis_saat IS NOT NULL THEN CONVERT(VARCHAR(5), g.cikis_saat, 108) ELSE NULL END AS cikisSaat, " +
                    "g.giris_turu, g.durum " +
                    "FROM giris_cikis_kayitlari g " +
                    "JOIN kullanicilar k ON g.kullanici_id = k.kullanici_id " +
                    "WHERE CAST(g.giris_saat AS DATE) = CAST(GETDATE() AS DATE) " +
                    "ORDER BY g.giris_saat DESC");

                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    String cikis = rs.getString("cikisSaat");
                    json.append(String.format(
                        "{\"id\":%d,\"uye\":\"%s\",\"giris\":\"%s\",\"cikis\":%s," +
                        "\"turu\":\"%s\",\"durum\":\"%s\"}",
                        rs.getInt("g_c_id"), rs.getString("isim"),
                        rs.getString("girisSaat"),
                        cikis != null ? "\"" + cikis + "\"" : "null",
                        rs.getString("giris_turu"), rs.getString("durum")));
                    first = false;
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(exchange, 200, json.toString());

            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // EKİPMAN ENDPOINT — GET /api/ekipman
    // ═══════════════════════════════════════════
    static class EkipmanHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                corsHeaders(exchange); exchange.sendResponseHeaders(204, -1); return;
            }
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();

                // Ekipman listesi
                ResultSet rs = stmt.executeQuery(
                    "SELECT e.ekipman_id, e.ekipman_adi, e.kategori, e.miktar, " +
                    "CONVERT(VARCHAR(10), e.satin_alma_tarihi, 120) AS satinAlma, " +
                    "e.satin_alma_fiyati, e.durum " +
                    "FROM ekipman e ORDER BY e.ekipman_id");

                StringBuilder ekipmanJson = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) ekipmanJson.append(",");
                    ekipmanJson.append(String.format(
                        "{\"id\":%d,\"ad\":\"%s\",\"kategori\":\"%s\",\"adet\":%d," +
                        "\"satinAlma\":\"%s\",\"fiyat\":%.2f,\"durum\":\"%s\"}",
                        rs.getInt("ekipman_id"),
                        rs.getString("ekipman_adi"),
                        rs.getString("kategori") != null ? rs.getString("kategori") : "",
                        rs.getInt("miktar"),
                        rs.getString("satinAlma") != null ? rs.getString("satinAlma") : "",
                        rs.getDouble("satin_alma_fiyati"),
                        rs.getString("durum")));
                    first = false;
                }
                ekipmanJson.append("]");
                rs.close();

                // Bakım geçmişi
                ResultSet rs2 = stmt.executeQuery(
                    "SELECT b.bakim_id, e.ekipman_adi, " +
                    "CONVERT(VARCHAR(10), b.bakim_tarihi, 120) AS tarih, " +
                    "b.maliyet, b.yapan_kisi, b.aciklama, " +
                    "CONVERT(VARCHAR(10), b.sonraki_bakim, 120) AS sonraki, b.durum " +
                    "FROM ekipman_bakimi b " +
                    "JOIN ekipman e ON b.ekipman_id = e.ekipman_id " +
                    "ORDER BY b.bakim_tarihi DESC");

                StringBuilder bakimJson = new StringBuilder("[");
                first = true;
                while (rs2.next()) {
                    if (!first) bakimJson.append(",");
                    bakimJson.append(String.format(
                        "{\"id\":%d,\"ekipman\":\"%s\",\"tarih\":\"%s\",\"maliyet\":%.2f," +
                        "\"yapan\":\"%s\",\"aciklama\":\"%s\",\"sonraki\":\"%s\",\"durum\":\"%s\"}",
                        rs2.getInt("bakim_id"), rs2.getString("ekipman_adi"),
                        rs2.getString("tarih"),
                        rs2.getDouble("maliyet"),
                        rs2.getString("yapan_kisi") != null ? rs2.getString("yapan_kisi") : "",
                        rs2.getString("aciklama") != null ? rs2.getString("aciklama").replace("\"", "'") : "",
                        rs2.getString("sonraki") != null ? rs2.getString("sonraki") : "",
                        rs2.getString("durum")));
                    first = false;
                }
                bakimJson.append("]");
                rs2.close(); stmt.close();

                String json = "{\"ekipman\":" + ekipmanJson + ",\"bakim\":" + bakimJson + "}";
                sendResponse(exchange, 200, json);

            } catch (SQLException e) {
                sendResponse(exchange, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════
    // TEST ENDPOINT — GET /api/test
    // ═══════════════════════════════════════════
    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            corsHeaders(exchange);
            boolean ok = DatabaseBaglanti.baglantiTest();
            String json = ok
                    ? "{\"basarili\":true,\"mesaj\":\"SQL Server bağlantısı aktif!\"}"
                    : "{\"basarili\":false,\"mesaj\":\"SQL Server bağlantısı başarısız!\"}";
            sendResponse(exchange, ok ? 200 : 500, json);
        }
    }

    // ═══════════════════════════════════════════
    // STATIC FILE HANDLER — Frontend dosyalarını sun
    // ═══════════════════════════════════════════
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) path = "/index.html";

            // View/ klasöründen dosyayı oku
            File file = new File("View" + path);
            if (!file.exists() || !file.isFile()) {
                String notFound = "404 — Dosya bulunamadı: " + path;
                exchange.sendResponseHeaders(404, notFound.getBytes().length);
                exchange.getResponseBody().write(notFound.getBytes());
                exchange.getResponseBody().close();
                return;
            }

            // MIME type belirle
            String mime = "application/octet-stream";
            if (path.endsWith(".html")) mime = "text/html; charset=UTF-8";
            else if (path.endsWith(".css"))  mime = "text/css; charset=UTF-8";
            else if (path.endsWith(".js"))   mime = "application/javascript; charset=UTF-8";
            else if (path.endsWith(".png"))  mime = "image/png";
            else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) mime = "image/jpeg";
            else if (path.endsWith(".svg"))  mime = "image/svg+xml";
            else if (path.endsWith(".ico"))  mime = "image/x-icon";

            exchange.getResponseHeaders().set("Content-Type", mime);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}

