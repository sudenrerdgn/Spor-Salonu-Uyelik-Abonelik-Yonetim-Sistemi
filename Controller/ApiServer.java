package Controller;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * FitZone Pro — Backend API Sunucusu v2.0
 * JWT Auth + IP Rate Limiting + Rol Tabanlı Erişim Kontrolü
 *
 * Çalıştırmak için (proje kök dizininden):
 *   javac -cp ".;mssql-jdbc-12.4.2.jre11.jar;javax.mail-1.6.2.jar;activation-1.1.1.jar" Controller/ApiServer.java Controller/DatabaseBaglanti.java
 *   java  -cp ".;mssql-jdbc-12.4.2.jre11.jar;javax.mail-1.6.2.jar;activation-1.1.1.jar" Controller.ApiServer
 *
 * Public (token gerekmez):
 *   POST /api/kayit        → Yeni üye kaydı
 *   POST /api/giris        → Giriş (JWT token döner)
 *   GET  /api/planlar      → Üyelik planları (landing sayfası)
 *   GET  /api/test         → Bağlantı testi
 *
 * Auth gerektiren:
 *   GET  /api/dogrula             → Token doğrula / oturum refresh
 *   GET  /api/uyeler              → [Admin] Tüm üyeler
 *   POST /api/uye-guncelle        → [Admin] Üye güncelle
 *   POST /api/uye-sil             → [Admin] Üye sil
 *   GET  /api/istatistikler       → [Admin] Dashboard istatistikleri
 *   GET  /api/odemeler            → [Admin] Tümü | [Üye] Kendi ödemeleri
 *   GET  /api/abonelikler         → [Admin] Tümü | [Üye] Kendi abonelikleri
 *   GET  /api/dersler             → [Admin|Üye|Antrenör]
 *   GET  /api/antrenorler-detay   → [Admin|Antrenör]
 *   GET  /api/giris-cikis         → [Admin|Antrenör]
 *   GET  /api/ekipman             → [Admin|Antrenör]
 */
public class ApiServer {

    private static final int    PORT       = 8080;
    // Production'da bunu environment variable'dan okuyun:
    //   JWT_SECRET = System.getenv("FITZONE_JWT_SECRET");
    private static final String JWT_SECRET = "FitZonePro_2026_GizliAnahtar_#$@!_DeğiştirBunu";

    // ─────────────────────────────────────────────
    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  FitZone Pro — Backend API Sunucusu v2.0");
        System.out.println("  🔐 JWT Auth + Rate Limiting Aktif");
        System.out.println("═══════════════════════════════════════════════════");

        if (!DatabaseBaglanti.baglantiTest()) {
            System.out.println("⚠️  Veritabanı bağlantısı kurulamadı! Lütfen ayarları kontrol edin.");
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // ── Public endpointler (token gerekmez) ──────────────
        server.createContext("/api/kayit",             new KayitHandler());
        server.createContext("/api/giris",             new GirisHandler());
        server.createContext("/api/sifremi-unuttum",   new SifremiUnuttumHandler());
        server.createContext("/api/sifre-sifirla",     new SifreSifirlaHandler());
        server.createContext("/api/planlar",           new PlanlarHandler());   // landing sayfası için public
        server.createContext("/api/test",              new TestHandler());

        // ── Kimlik doğrulama ─────────────────────────────────
        server.createContext("/api/dogrula",           new DogrulaHandler());

        // ── Admin ────────────────────────────────────────────
        server.createContext("/api/uyeler",            new UyelerHandler());
        server.createContext("/api/kullanicilar",      new KullanicilarHandler());
        server.createContext("/api/uye-guncelle",      new UyeGuncelleHandler());
        server.createContext("/api/uye-sil",           new UyeSilHandler());
        server.createContext("/api/istatistikler",     new IstatistiklerHandler());

        // ── Admin | Üye (kendi verisi) ────────────────────────
        server.createContext("/api/odemeler",          new OdemelerHandler());
        server.createContext("/api/abonelikler",       new AboneliklerHandler());
        server.createContext("/api/profil-guncelle",   new ProfilGuncelleHandler());
        server.createContext("/api/abonelik-satin-al", new AbonelikSatinAlHandler());
        server.createContext("/api/odeme-yap",         new OdemeYapHandler());

        // ── Tüm giriş yapmış kullanıcılar ────────────────────
        server.createContext("/api/dersler",           new DerslerHandler());

        // ── Admin | Antrenör ──────────────────────────────────
        server.createContext("/api/antrenorler-detay", new AntrenorlerDetayHandler());
        server.createContext("/api/giris-cikis",       new GirisCikisHandler());
        server.createContext("/api/ekipman",           new EkipmanHandler());

        // ── Static dosyalar ───────────────────────────────────
        server.createContext("/",                      new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("✅ API aktif  : http://localhost:" + PORT);
        System.out.println("─────────────────────────────────────────────────");
        System.out.println("  Public   → POST /api/giris   POST /api/kayit");
        System.out.println("  Admin    → /api/uyeler  /api/istatistikler  ...");
        System.out.println("  Üye      → /api/odemeler (kendi)  /api/abonelikler (kendi)");
        System.out.println("  Antrenör → /api/dersler  /api/giris-cikis  ...");
        System.out.println("═══════════════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════
    // JWT UTILITY — HMAC-SHA256 token üret/doğrula
    // ═══════════════════════════════════════════════════
    static class JwtUtil {
        private static final long EXPIRY_SEC = 8L * 60 * 60; // 8 saat

        /** Token üret: {kullanici_id, email, rol} → JWT string */
        static String generate(int id, String email, String rol) {
            try {
                long now = System.currentTimeMillis() / 1000;
                String header  = b64u("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
                String payload = b64u(String.format(
                    "{\"sub\":%d,\"email\":\"%s\",\"rol\":\"%s\",\"iat\":%d,\"exp\":%d}",
                    id, email.replace("\"", ""), rol.replace("\"", ""), now, now + EXPIRY_SEC));
                String sig = sign(header + "." + payload);
                return header + "." + payload + "." + sig;
            } catch (Exception e) { throw new RuntimeException("Token üretilemedi", e); }
        }

        /**
         * Token doğrula.
         * @return String[3] = {kullanici_id(string), email, rol} — geçersizse null
         */
        static String[] verify(String token) {
            if (token == null) return null;
            String[] p = token.split("\\.", -1);
            if (p.length != 3) return null;
            try {
                if (!sign(p[0] + "." + p[1]).equals(p[2])) return null;
                String json = new String(Base64.getUrlDecoder().decode(pad(p[1])), StandardCharsets.UTF_8);
                long exp = Long.parseLong(jNum(json, "exp"));
                if (System.currentTimeMillis() / 1000 > exp) return null;
                return new String[]{ jNum(json, "sub"), jStr(json, "email"), jStr(json, "rol") };
            } catch (Exception e) { return null; }
        }

        private static String b64u(String s) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
        }
        private static String pad(String s) {
            int n = s.length() % 4;
            return n == 0 ? s : s + "====".substring(n);
        }
        private static String sign(String data) throws Exception {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        }
        private static String jStr(String json, String key) {
            String s = "\"" + key + "\":\""; int i = json.indexOf(s);
            if (i < 0) return "";
            int start = i + s.length();
            return json.substring(start, json.indexOf("\"", start));
        }
        private static String jNum(String json, String key) {
            String s = "\"" + key + "\":"; int i = json.indexOf(s);
            if (i < 0) return "0";
            int st = i + s.length(), en = st;
            while (en < json.length() && (Character.isDigit(json.charAt(en)) || json.charAt(en) == '-')) en++;
            return json.substring(st, en);
        }
    }

    // ═══════════════════════════════════════════════════
    // RATE LIMITER — Kaba kuvvet saldırı koruması
    // ═══════════════════════════════════════════════════
    static class RateLimiter {
        private static final int   MAX_FAIL = 5;
        private static final long  WIN_MS   = 15L * 60 * 1000; // 15 dakika
        // IP → [hataSayisi, ilkHataZamani]
        private static final ConcurrentHashMap<String, long[]> map = new ConcurrentHashMap<>();

        static boolean blocked(String ip) {
            long[] v = map.get(ip);
            if (v == null) return false;
            if (System.currentTimeMillis() - v[1] > WIN_MS) { map.remove(ip); return false; }
            return v[0] >= MAX_FAIL;
        }
        static long waitSec(String ip) {
            long[] v = map.get(ip); if (v == null) return 0;
            return Math.max(0, (WIN_MS - (System.currentTimeMillis() - v[1])) / 1000);
        }
        static void fail(String ip) {
            long now = System.currentTimeMillis();
            map.compute(ip, (k, v) -> {
                if (v == null || now - v[1] > WIN_MS) return new long[]{1, now};
                v[0]++; return v;
            });
        }
        static void ok(String ip) { map.remove(ip); }
    }

    // ═══════════════════════════════════════════════════
    // CORS + YARDIMCI METOTLAR
    // ═══════════════════════════════════════════════════
    private static void corsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.getResponseHeaders().set("Access-Control-Expose-Headers","Authorization");
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
    }

    private static void sendResponse(HttpExchange ex, int code, String json) throws IOException {
        corsHeaders(ex);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes); os.close();
    }

    private static String readBody(HttpExchange ex) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line; while ((line = r.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    private static String jsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search); if (idx == -1) return null;
        int colon = json.indexOf(":", idx);
        int start = json.indexOf("\"", colon + 1) + 1;
        int end   = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            byte[] hash = d.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) { String h = Integer.toHexString(0xff & b); if (h.length()==1) sb.append('0'); sb.append(h); }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }

    // ─── Auth yardımcıları ───────────────────────────
    // Döndürür: [kullanici_id, email, rol] — geçersizse null
    private static String[] authUser(HttpExchange ex) {
        String h = ex.getRequestHeaders().getFirst("Authorization");
        if (h == null || !h.startsWith("Bearer ")) return null;
        return JwtUtil.verify(h.substring(7));
    }

    private static boolean requireAdmin(HttpExchange ex) throws IOException {
        String[] u = authUser(ex);
        if (u == null)          { sendResponse(ex, 401, errJson("Giriş yapmanız gerekiyor!", "UNAUTHORIZED")); return false; }
        if (!"admin".equals(u[2])) { sendResponse(ex, 403, errJson("Bu işlem için yetkiniz yok!", "FORBIDDEN")); return false; }
        return true;
    }

    private static boolean requireAuth(HttpExchange ex) throws IOException {
        if (authUser(ex) == null) { sendResponse(ex, 401, errJson("Giriş yapmanız gerekiyor!", "UNAUTHORIZED")); return false; }
        return true;
    }

    private static boolean requireRole(HttpExchange ex, String... roles) throws IOException {
        String[] u = authUser(ex);
        if (u == null) { sendResponse(ex, 401, errJson("Giriş yapmanız gerekiyor!", "UNAUTHORIZED")); return false; }
        for (String r : roles) if (r.equals(u[2])) return true;
        sendResponse(ex, 403, errJson("Bu işlem için yetkiniz yok!", "FORBIDDEN")); return false;
    }

    private static String errJson(String msg, String kod) {
        return "{\"basarili\":false,\"mesaj\":\"" + msg + "\",\"kod\":\"" + kod + "\"}";
    }

    // ═══════════════════════════════════════════════════
    // KAYIT — POST /api/kayit  (Public)
    // ═══════════════════════════════════════════════════
    static class KayitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!"POST".equals(ex.getRequestMethod())) { sendResponse(ex,405,errJson("Sadece POST", "METHOD_NOT_ALLOWED")); return; }

            String body       = readBody(ex);
            String ad         = jsonValue(body, "ad");
            String soyad      = jsonValue(body, "soyad");
            String email      = jsonValue(body, "email");
            String sifre      = jsonValue(body, "sifre");
            String rol        = jsonValue(body, "rol");
            String telefon    = jsonValue(body, "telefon");
            String cinsiyet   = jsonValue(body, "cinsiyet");
            String dogumTarihi= jsonValue(body, "dogum_tarihi");

            if (ad==null||soyad==null||email==null||sifre==null) {
                sendResponse(ex,400,errJson("Eksik alanlar!","BAD_REQUEST")); return;
            }
            if (sifre.length() < 6) {
                sendResponse(ex,400,errJson("Şifre en az 6 karakter olmalıdır!","BAD_REQUEST")); return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                sendResponse(ex,400,errJson("Geçersiz email formatı!","BAD_REQUEST")); return;
            }
            if (rol==null||rol.isEmpty()) rol="uye";
            String sifreHash = hashPassword(sifre);

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM kullanicilar WHERE email=?");
                check.setString(1,email.toLowerCase());
                ResultSet rs = check.executeQuery();
                boolean emailVar = rs.next() && rs.getInt(1)>0;
                rs.close(); check.close();
                if (emailVar) { sendResponse(ex,409,errJson("Bu email zaten kayıtlı!","CONFLICT")); return; }

                int rolId=2;
                PreparedStatement rolStmt = conn.prepareStatement("SELECT role_id FROM roller WHERE rol_adi=?");
                rolStmt.setString(1,rol);
                ResultSet rolRs = rolStmt.executeQuery();
                if (rolRs.next()) rolId=rolRs.getInt("role_id");
                rolRs.close(); rolStmt.close();

                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO kullanicilar (ad,soyad,email,sifre_hash,telefon,cinsiyet,dogum_tarihi,rol_id,durum) VALUES(?,?,?,?,?,?,?,?,N'aktif')",
                    Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1,ad); stmt.setString(2,soyad);
                stmt.setString(3,email.toLowerCase()); stmt.setString(4,sifreHash);
                stmt.setString(5,telefon!=null&&!telefon.isEmpty()?telefon:null);
                stmt.setString(6,cinsiyet!=null&&!cinsiyet.isEmpty()?cinsiyet:null);
                if (dogumTarihi!=null&&!dogumTarihi.isEmpty()) stmt.setDate(7,java.sql.Date.valueOf(dogumTarihi));
                else stmt.setNull(7,java.sql.Types.DATE);
                stmt.setInt(8,rolId);
                stmt.executeUpdate();

                ResultSet gk = stmt.getGeneratedKeys();
                int yeniId=-1;
                if (gk.next()) yeniId=gk.getInt(1);
                gk.close(); stmt.close();

                if ("uye".equals(rol) && yeniId>0) {
                    Statement cs = conn.createStatement();
                    ResultSet cr = cs.executeQuery("SELECT COUNT(*) AS cnt FROM uyeler");
                    int cnt = cr.next()?cr.getInt("cnt"):0; cr.close(); cs.close();
                    String uNo = String.format("FZ-%d-%03d", java.time.Year.now().getValue(), cnt+1);
                    PreparedStatement us = conn.prepareStatement("INSERT INTO uyeler(kullanici_id,uyelik_no)VALUES(?,?)");
                    us.setInt(1,yeniId); us.setString(2,uNo); us.executeUpdate(); us.close();
                }

                System.out.println("✅ Kayıt: "+ad+" "+soyad+" ("+email+")");
                sendResponse(ex,200,"{\"basarili\":true,\"mesaj\":\"Kayıt başarılı!\"}");
            } catch (SQLException e) {
                System.out.println("❌ Kayıt hatası: "+e.getMessage());
                sendResponse(ex,500,errJson("Veritabanı hatası: "+e.getMessage().replace("\"","'"),"DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // GİRİŞ — POST /api/giris  (Public + Rate Limiting)
    // ═══════════════════════════════════════════════════
    static class GirisHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!"POST".equals(ex.getRequestMethod())) { sendResponse(ex,405,errJson("Sadece POST","METHOD_NOT_ALLOWED")); return; }

            String clientIp = ex.getRemoteAddress().getAddress().getHostAddress();

            // Rate limit kontrolü
            if (RateLimiter.blocked(clientIp)) {
                long wait = RateLimiter.waitSec(clientIp);
                System.out.println("🛑 Rate limit: "+clientIp);
                sendResponse(ex,429,String.format(
                    "{\"basarili\":false,\"mesaj\":\"Çok fazla başarısız deneme. %d saniye bekleyin!\",\"kod\":\"RATE_LIMITED\"}",wait));
                return;
            }

            String body  = readBody(ex);
            String email = jsonValue(body,"email");
            String sifre = jsonValue(body,"sifre");

            if (email==null||sifre==null) { sendResponse(ex,400,errJson("Email ve şifre gerekli!","BAD_REQUEST")); return; }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT k.kullanici_id,k.ad,k.soyad,k.email,k.telefon,k.sifre_hash,k.durum,r.rol_adi " +
                    "FROM kullanicilar k JOIN roller r ON k.rol_id=r.role_id WHERE k.email=?");
                stmt.setString(1,email.toLowerCase());
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    rs.close(); stmt.close();
                    RateLimiter.fail(clientIp);
                    sendResponse(ex,401,errJson("Email veya şifre yanlış!","INVALID_CREDENTIALS")); return;
                }

                int    id          = rs.getInt("kullanici_id");
                String ad          = rs.getString("ad");
                String soyad       = rs.getString("soyad");
                String dbEmail     = rs.getString("email");
                String telefon     = rs.getString("telefon");
                String kayitliHash = rs.getString("sifre_hash");
                String durum       = rs.getString("durum");
                String rolAdi      = rs.getString("rol_adi");
                rs.close(); stmt.close();

                if (!"aktif".equals(durum)) {
                    RateLimiter.fail(clientIp);
                    sendResponse(ex,401,errJson("Bu hesap devre dışı bırakılmış!","ACCOUNT_DISABLED")); return;
                }

                if (!hashPassword(sifre).equals(kayitliHash)) {
                    RateLimiter.fail(clientIp);
                    System.out.println("🔐 Başarısız giriş: "+email+" | IP: "+clientIp);
                    sendResponse(ex,401,errJson("Email veya şifre yanlış!","INVALID_CREDENTIALS")); return;
                }

                // Başarılı giriş
                RateLimiter.ok(clientIp);

                PreparedStatement upd = conn.prepareStatement(
                    "UPDATE kullanicilar SET son_giris=GETDATE() WHERE kullanici_id=?");
                upd.setInt(1,id); upd.executeUpdate(); upd.close();

                String token = JwtUtil.generate(id, dbEmail, rolAdi);

                String json = String.format(
                    "{\"basarili\":true,\"mesaj\":\"Giriş başarılı!\",\"token\":\"%s\"," +
                    "\"kullanici\":{\"id\":%d,\"ad\":\"%s\",\"soyad\":\"%s\",\"email\":\"%s\",\"rol\":\"%s\",\"telefon\":\"%s\"}}",
                    token, id, ad, soyad, dbEmail, rolAdi, telefon!=null?telefon:"");

                System.out.println("✅ Giriş: "+ad+" "+soyad+" ("+rolAdi+")");
                sendResponse(ex,200,json);

            } catch (SQLException e) {
                System.out.println("❌ Giriş hatası: "+e.getMessage());
                sendResponse(ex,500,errJson("Sunucu hatası!","SERVER_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // DOĞRULA — GET /api/dogrula  (Oturum yenileme)
    // ═══════════════════════════════════════════════════
    static class DogrulaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }

            String[] u = authUser(ex);
            if (u == null) {
                sendResponse(ex,401,"{\"basarili\":false,\"mesaj\":\"Token geçersiz veya süresi dolmuş!\",\"kod\":\"TOKEN_INVALID\"}");
                return;
            }

            // u[0]=kullanici_id, u[1]=email, u[2]=rol
            try {
                int kullaniciId = Integer.parseInt(u[0]);
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT ad,soyad,email,telefon FROM kullanicilar WHERE kullanici_id=? AND durum=N'aktif'");
                stmt.setInt(1,kullaniciId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    rs.close(); stmt.close();
                    sendResponse(ex,401,"{\"basarili\":false,\"mesaj\":\"Hesap aktif değil!\",\"kod\":\"ACCOUNT_INACTIVE\"}");
                    return;
                }

                String ad    = rs.getString("ad");
                String soyad = rs.getString("soyad");
                String email = rs.getString("email");
                String telefon= rs.getString("telefon");
                rs.close(); stmt.close();

                String json = String.format(
                    "{\"basarili\":true,\"kullanici\":{\"id\":%d,\"ad\":\"%s\",\"soyad\":\"%s\",\"email\":\"%s\",\"rol\":\"%s\",\"telefon\":\"%s\"}}",
                    kullaniciId, ad, soyad, email, u[2], telefon!=null?telefon:"");
                sendResponse(ex,200,json);

            } catch (Exception e) {
                sendResponse(ex,500,errJson("Sunucu hatası!","SERVER_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ÜYELER — GET /api/uyeler  [Admin]
    // ═══════════════════════════════════════════════════
    static class UyelerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!requireAdmin(ex)) return;

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt  = conn.createStatement();
                ResultSet rs    = stmt.executeQuery(
                    "SELECT k.kullanici_id,k.ad,k.soyad,k.email,k.telefon,k.cinsiyet," +
                    "k.dogum_tarihi,r.rol_adi,k.durum,k.kayit_tarihi," +
                    "u.uyelik_no," +
                    "p.plan_adi AS abonelik_plan," +
                    "a.bitis_tarihi AS abonelik_bitis," +
                    "a.durum AS abonelik_durum " +
                    "FROM kullanicilar k " +
                    "JOIN roller r ON k.rol_id=r.role_id " +
                    "LEFT JOIN uyeler u ON k.kullanici_id=u.kullanici_id " +
                    "LEFT JOIN (SELECT uye_id,plan_id,bitis_tarihi,durum," +
                    "ROW_NUMBER()OVER(PARTITION BY uye_id ORDER BY baslangic_tarihi DESC) AS rn " +
                    "FROM uye_abonelikleri) a ON u.uye_id=a.uye_id AND a.rn=1 " +
                    "LEFT JOIN uye_planlari p ON a.plan_id=p.plan_id " +
                    "WHERE r.rol_adi=N'uye' ORDER BY k.kullanici_id");

                StringBuilder json = new StringBuilder("[");
                boolean first=true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    String telefon     = rs.getString("telefon");
                    String cinsiyet    = rs.getString("cinsiyet");
                    String uyelikNo    = rs.getString("uyelik_no");
                    String abPlan      = rs.getString("abonelik_plan");
                    String abBitis     = rs.getString("abonelik_bitis");
                    String abDurum     = rs.getString("abonelik_durum");
                    Timestamp kayitTs  = rs.getTimestamp("kayit_tarihi");
                    String kayitTarihi = kayitTs!=null?kayitTs.toString().substring(0,10):"";
                    java.sql.Date dogum = rs.getDate("dogum_tarihi");
                    String dogumStr    = dogum!=null?dogum.toString():"";
                    json.append(String.format(
                        "{\"id\":%d,\"ad\":\"%s\",\"soyad\":\"%s\",\"email\":\"%s\"," +
                        "\"telefon\":\"%s\",\"cinsiyet\":\"%s\",\"dogumTarihi\":\"%s\"," +
                        "\"uyelikNo\":\"%s\"," +
                        "\"abonelikPlan\":\"%s\",\"abonelikBitis\":\"%s\",\"abonelikDurum\":\"%s\"," +
                        "\"rol\":\"%s\",\"durum\":\"%s\",\"kayitTarihi\":\"%s\"}",
                        rs.getInt("kullanici_id"),rs.getString("ad"),rs.getString("soyad"),rs.getString("email"),
                        telefon!=null?telefon:"",cinsiyet!=null?cinsiyet:"",dogumStr,
                        uyelikNo!=null?uyelikNo:"",
                        abPlan!=null?abPlan:"",abBitis!=null?abBitis:"",abDurum!=null?abDurum:"",
                        rs.getString("rol_adi"),rs.getString("durum"),kayitTarihi));
                    first=false;
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(ex,200,json.toString());
            } catch (SQLException e) {
                sendResponse(ex,500,"{\"hata\":\""+e.getMessage().replace("\"","'")+"\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // KULLANICILAR — GET /api/kullanicilar  [Admin]
    // ═══════════════════════════════════════════════════
    static class KullanicilarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!requireAdmin(ex)) return;

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt  = conn.createStatement();
                ResultSet rs    = stmt.executeQuery(
                    "SELECT k.kullanici_id, k.ad, k.soyad, k.email, r.rol_adi, k.durum " +
                    "FROM kullanicilar k JOIN roller r ON k.rol_id=r.role_id " +
                    "ORDER BY k.kullanici_id");

                StringBuilder json = new StringBuilder("[");
                boolean first=true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    json.append(String.format(
                        "{\"id\":%d,\"ad\":\"%s\",\"soyad\":\"%s\",\"email\":\"%s\",\"rol\":\"%s\",\"durum\":\"%s\"}",
                        rs.getInt("kullanici_id"), rs.getString("ad").replace("\"", "\\\""), rs.getString("soyad").replace("\"", "\\\""),
                        rs.getString("email").replace("\"", "\\\""), rs.getString("rol_adi"), rs.getString("durum")));
                    first=false;
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(ex,200,json.toString());
            } catch (SQLException e) {
                sendResponse(ex,500,"{\"hata\":\""+e.getMessage().replace("\"","'")+"\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ÜYE GÜNCELLE — POST /api/uye-guncelle  [Admin]
    // ═══════════════════════════════════════════════════
    static class UyeGuncelleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!requireAdmin(ex)) return;
            if (!"POST".equals(ex.getRequestMethod())) { sendResponse(ex,405,errJson("Sadece POST","METHOD_NOT_ALLOWED")); return; }

            String body   = readBody(ex);
            String idStr  = jsonValue(body,"id");
            String ad     = jsonValue(body,"ad");
            String soyad  = jsonValue(body,"soyad");
            String email  = jsonValue(body,"email");
            String telefon= jsonValue(body,"telefon");
            String durum  = jsonValue(body,"durum");

            if (idStr==null||ad==null||soyad==null||email==null) {
                sendResponse(ex,400,errJson("Eksik alanlar!","BAD_REQUEST")); return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE kullanicilar SET ad=?,soyad=?,email=?,telefon=?,durum=? WHERE kullanici_id=?");
                stmt.setString(1,ad); stmt.setString(2,soyad);
                stmt.setString(3,email.toLowerCase());
                stmt.setString(4,telefon!=null?telefon:"");
                stmt.setString(5,durum!=null?durum:"aktif");
                stmt.setInt(6,Integer.parseInt(idStr));
                int n=stmt.executeUpdate(); stmt.close();

                if (n>0) { System.out.println("✅ Üye güncellendi: "+ad+" "+soyad); sendResponse(ex,200,"{\"basarili\":true,\"mesaj\":\"Üye güncellendi!\"}"); }
                else     { sendResponse(ex,404,errJson("Üye bulunamadı!","NOT_FOUND")); }
            } catch (SQLException e) {
                System.out.println("❌ Güncelleme hatası: "+e.getMessage());
                sendResponse(ex,500,errJson("Veritabanı hatası","DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // PROFİL GÜNCELLE — POST /api/profil-guncelle  [Giriş Yapmış Herkes]
    // ═══════════════════════════════════════════════════
    static class ProfilGuncelleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!requireAuth(ex)) return;
            if (!"POST".equals(ex.getRequestMethod())) { sendResponse(ex,405,errJson("Sadece POST","METHOD_NOT_ALLOWED")); return; }
            
            String[] u = authUser(ex);
            int kId = Integer.parseInt(u[0]);
            
            String body   = readBody(ex);
            String ad     = jsonValue(body,"ad");
            String soyad  = jsonValue(body,"soyad");
            String telefon= jsonValue(body,"telefon");

            if (ad==null||soyad==null) {
                sendResponse(ex,400,errJson("Eksik alanlar!","BAD_REQUEST")); return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE kullanicilar SET ad=?,soyad=?,telefon=? WHERE kullanici_id=?");
                stmt.setString(1,ad); stmt.setString(2,soyad);
                stmt.setString(3,telefon!=null?telefon:"");
                stmt.setInt(4,kId);
                int n=stmt.executeUpdate(); stmt.close();

                if (n>0) { System.out.println("✅ Profil güncellendi: ID="+kId); sendResponse(ex,200,"{\"basarili\":true,\"mesaj\":\"Profil güncellendi!\"}"); }
                else     { sendResponse(ex,404,errJson("Kullanıcı bulunamadı!","NOT_FOUND")); }
            } catch (SQLException e) {
                sendResponse(ex,500,errJson("Veritabanı hatası","DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ÜYE SİL — POST /api/uye-sil  [Admin]
    // ═══════════════════════════════════════════════════
    static class UyeSilHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!requireAdmin(ex)) return;
            if (!"POST".equals(ex.getRequestMethod())) { sendResponse(ex,405,errJson("Sadece POST","METHOD_NOT_ALLOWED")); return; }

            String idStr = jsonValue(readBody(ex),"id");
            if (idStr==null) { sendResponse(ex,400,errJson("ID gerekli!","BAD_REQUEST")); return; }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                int uid = Integer.parseInt(idStr);

                PreparedStatement fu = conn.prepareStatement("SELECT uye_id FROM uyeler WHERE kullanici_id=?");
                fu.setInt(1,uid); ResultSet ur=fu.executeQuery();
                int uyeId=-1; if(ur.next()) uyeId=ur.getInt("uye_id");
                ur.close(); fu.close();

                if (uyeId>0) {
                    PreparedStatement d1=conn.prepareStatement("DELETE FROM odemeler WHERE abonelik_id IN(SELECT abonelik_id FROM uye_abonelikleri WHERE uye_id=?)");
                    d1.setInt(1,uyeId); d1.executeUpdate(); d1.close();
                    PreparedStatement d2=conn.prepareStatement("DELETE FROM sinif_rezervasyonlari WHERE uye_id=?");
                    d2.setInt(1,uyeId); d2.executeUpdate(); d2.close();
                    PreparedStatement d3=conn.prepareStatement("DELETE FROM uye_abonelikleri WHERE uye_id=?");
                    d3.setInt(1,uyeId); d3.executeUpdate(); d3.close();
                    PreparedStatement d4=conn.prepareStatement("DELETE FROM uyeler WHERE uye_id=?");
                    d4.setInt(1,uyeId); d4.executeUpdate(); d4.close();
                }

                PreparedStatement dg=conn.prepareStatement("DELETE FROM giris_cikis_kayitlari WHERE kullanici_id=?");
                dg.setInt(1,uid); dg.executeUpdate(); dg.close();
                PreparedStatement dk=conn.prepareStatement("DELETE FROM kullanicilar WHERE kullanici_id=?");
                dk.setInt(1,uid); int n=dk.executeUpdate(); dk.close();

                if (n>0) { System.out.println("✅ Üye silindi: ID="+uid); sendResponse(ex,200,"{\"basarili\":true,\"mesaj\":\"Üye başarıyla silindi!\"}"); }
                else     { sendResponse(ex,404,errJson("Üye bulunamadı!","NOT_FOUND")); }
            } catch (Exception e) {
                System.out.println("❌ Silme hatası: "+e.getMessage());
                sendResponse(ex,500,errJson("Veritabanı hatası: "+e.getMessage().replace("\"","'"),"DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // İSTATİSTİKLER — GET /api/istatistikler  [Admin]
    // ═══════════════════════════════════════════════════
    static class IstatistiklerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!requireAdmin(ex)) return;

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt  = conn.createStatement();

                ResultSet r1=stmt.executeQuery("SELECT COUNT(*)AS cnt FROM kullanicilar k JOIN roller r ON k.rol_id=r.role_id WHERE r.rol_adi=N'uye'");
                int toplamUye=r1.next()?r1.getInt("cnt"):0; r1.close();

                ResultSet r2=stmt.executeQuery("SELECT COUNT(*)AS cnt FROM kullanicilar k JOIN roller r ON k.rol_id=r.role_id WHERE r.rol_adi=N'uye' AND k.durum=N'aktif'");
                int aktifUye=r2.next()?r2.getInt("cnt"):0; r2.close();

                ResultSet r3=stmt.executeQuery("SELECT COUNT(*)AS cnt FROM kullanicilar k JOIN roller r ON k.rol_id=r.role_id WHERE r.rol_adi=N'uye' AND k.durum=N'pasif'");
                int pasifUye=r3.next()?r3.getInt("cnt"):0; r3.close();

                ResultSet r4=stmt.executeQuery("SELECT COUNT(*)AS cnt FROM uye_abonelikleri WHERE durum=N'suresi_doldu'");
                int suresiDolan=r4.next()?r4.getInt("cnt"):0; r4.close();

                ResultSet r5=stmt.executeQuery("SELECT ISNULL(SUM(miktar),0)AS toplam FROM odemeler WHERE durum=N'tamamlandi' AND MONTH(odeme_tarihi)=MONTH(GETDATE()) AND YEAR(odeme_tarihi)=YEAR(GETDATE())");
                double buAyGelir=r5.next()?r5.getDouble("toplam"):0; r5.close();

                ResultSet r6=stmt.executeQuery("SELECT ISNULL(SUM(miktar),0)AS toplam FROM odemeler WHERE durum=N'tamamlandi'");
                double toplamGelir=r6.next()?r6.getDouble("toplam"):0; r6.close();

                ResultSet r7=stmt.executeQuery("SELECT COUNT(*)AS cnt FROM giris_cikis_kayitlari WHERE durum=N'giris' AND CAST(giris_saat AS DATE)=CAST(GETDATE() AS DATE)");
                int bugunIceride=r7.next()?r7.getInt("cnt"):0; r7.close();

                ResultSet r8=stmt.executeQuery("SELECT COUNT(*)AS cnt FROM kullanicilar k JOIN roller r ON k.rol_id=r.role_id WHERE r.rol_adi=N'uye' AND MONTH(k.kayit_tarihi)=MONTH(GETDATE()) AND YEAR(k.kayit_tarihi)=YEAR(GETDATE())");
                int buAyYeni=r8.next()?r8.getInt("cnt"):0; r8.close();

                ResultSet r9=stmt.executeQuery("SELECT COUNT(*)AS cnt FROM uye_abonelikleri WHERE durum=N'aktif'");
                int aktifAbonelik=r9.next()?r9.getInt("cnt"):0; r9.close();

                stmt.close();
                sendResponse(ex,200,String.format(
                    "{\"toplamUye\":%d,\"aktifUye\":%d,\"pasifUye\":%d," +
                    "\"suresiDolan\":%d,\"aktifAbonelik\":%d," +
                    "\"buAyGelir\":%.2f,\"toplamGelir\":%.2f," +
                    "\"bugunIceride\":%d,\"buAyYeniKayit\":%d}",
                    toplamUye,aktifUye,pasifUye,suresiDolan,aktifAbonelik,
                    buAyGelir,toplamGelir,bugunIceride,buAyYeni));

            } catch (SQLException e) {
                sendResponse(ex,500,"{\"hata\":\""+e.getMessage().replace("\"","'")+"\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ÖDEMELER — GET /api/odemeler  [Admin=tümü | Üye=kendi]
    // ═══════════════════════════════════════════════════
    static class OdemelerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            String[] u = authUser(ex);
            if (u==null) { sendResponse(ex,401,errJson("Giriş yapmanız gerekiyor!","UNAUTHORIZED")); return; }

            String rol=u[2];
            if (!"admin".equals(rol) && !"uye".equals(rol)) {
                sendResponse(ex,403,errJson("Bu işlem için yetkiniz yok!","FORBIDDEN")); return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement stmt;
                String baseQ =
                    "SELECT o.odeme_id,k.ad,k.soyad,p.plan_adi,o.miktar," +
                    "o.odeme_yontemi,o.odeme_tarihi,o.durum " +
                    "FROM odemeler o " +
                    "JOIN uye_abonelikleri a ON o.abonelik_id=a.abonelik_id " +
                    "JOIN uyeler u ON a.uye_id=u.uye_id " +
                    "JOIN kullanicilar k ON u.kullanici_id=k.kullanici_id " +
                    "JOIN uye_planlari p ON a.plan_id=p.plan_id ";

                if ("admin".equals(rol)) {
                    stmt=conn.prepareStatement(baseQ+"ORDER BY o.odeme_tarihi DESC");
                } else {
                    stmt=conn.prepareStatement(baseQ+"WHERE u.kullanici_id=? ORDER BY o.odeme_tarihi DESC");
                    stmt.setInt(1,Integer.parseInt(u[0]));
                }

                ResultSet rs=stmt.executeQuery();
                StringBuilder json=new StringBuilder("[");
                boolean first=true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    String yon=rs.getString("odeme_yontemi");
                    String yL="Bilinmiyor";
                    if ("kredi_karti".equals(yon)) yL="Kredi Kartı";
                    else if ("nakit".equals(yon))  yL="Nakit";
                    else if ("havale".equals(yon))  yL="Havale";
                    else if ("online".equals(yon))  yL="Online";
                    Timestamp ts=rs.getTimestamp("odeme_tarihi");
                    String tarih=ts!=null?ts.toString().substring(0,10):"";
                    json.append(String.format(
                        "{\"id\":%d,\"uye\":\"%s %s\",\"plan\":\"%s\",\"miktar\":%.2f," +
                        "\"yontem\":\"%s\",\"tarih\":\"%s\",\"durum\":\"%s\"}",
                        rs.getInt("odeme_id"),rs.getString("ad"),rs.getString("soyad"),
                        rs.getString("plan_adi"),rs.getDouble("miktar"),yL,tarih,rs.getString("durum")));
                    first=false;
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(ex,200,json.toString());
            } catch (SQLException e) {
                sendResponse(ex,500,"{\"hata\":\""+e.getMessage().replace("\"","'")+"\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ABONELİKLER — GET /api/abonelikler  [Admin=tümü | Üye=kendi]
    // ═══════════════════════════════════════════════════
    static class AboneliklerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            String[] u=authUser(ex);
            if (u==null) { sendResponse(ex,401,errJson("Giriş yapmanız gerekiyor!","UNAUTHORIZED")); return; }

            String rol=u[2];
            if (!"admin".equals(rol) && !"uye".equals(rol)) {
                sendResponse(ex,403,errJson("Bu işlem için yetkiniz yok!","FORBIDDEN")); return;
            }

            try {
                Connection conn=DatabaseBaglanti.baglantiGetir();

                // ADIM 1: Her üye için en yüksek abonelik_id'li kaydı bul, diğerlerini 'iptal' yap.
                // CTE kullanarak daha güvenilir bir temizlik yapıyoruz.
                conn.createStatement().executeUpdate(
                    "WITH LatestPerMember AS (" +
                    "  SELECT abonelik_id, uye_id, " +
                    "         ROW_NUMBER() OVER (PARTITION BY uye_id ORDER BY abonelik_id DESC) AS rn " +
                    "  FROM uye_abonelikleri" +
                    ") " +
                    "UPDATE uye_abonelikleri SET durum = N'iptal' " +
                    "WHERE abonelik_id IN (" +
                    "  SELECT abonelik_id FROM LatestPerMember WHERE rn > 1" +
                    ") AND durum <> N'iptal'");

                // ADIM 2: En son kaydı olan ve süresi dolmuş aktif abonelikleri işaretle
                conn.createStatement().executeUpdate(
                    "UPDATE uye_abonelikleri SET durum = N'suresi_doldu' " +
                    "WHERE durum = N'aktif' AND bitis_tarihi < CAST(GETDATE() AS DATE)");

                System.out.println("[Cleanup] Uye-basi tek abonelik kurali uygulandı.");

                PreparedStatement stmt;
                String baseQ=
                    "SELECT a.abonelik_id,k.ad,k.soyad,p.plan_adi," +
                    "a.baslangic_tarihi,a.bitis_tarihi,a.otomatik_yenile,a.durum " +
                    "FROM uye_abonelikleri a " +
                    "JOIN uyeler u ON a.uye_id=u.uye_id " +
                    "JOIN kullanicilar k ON u.kullanici_id=k.kullanici_id " +
                    "JOIN uye_planlari p ON a.plan_id=p.plan_id ";

                if ("admin".equals(rol)) {
                    stmt=conn.prepareStatement(baseQ+"ORDER BY a.baslangic_tarihi DESC");
                } else {
                    stmt=conn.prepareStatement(baseQ+"WHERE u.kullanici_id=? ORDER BY a.baslangic_tarihi DESC");
                    stmt.setInt(1,Integer.parseInt(u[0]));
                }

                ResultSet rs=stmt.executeQuery();
                StringBuilder json=new StringBuilder("[");
                boolean first=true;
                while(rs.next()) {
                    if (!first) json.append(",");
                    json.append(String.format(
                        "{\"id\":%d,\"uye\":\"%s %s\",\"plan\":\"%s\"," +
                        "\"baslangic\":\"%s\",\"bitis\":\"%s\",\"otomatik\":%s,\"durum\":\"%s\"}",
                        rs.getInt("abonelik_id"),rs.getString("ad"),rs.getString("soyad"),
                        rs.getString("plan_adi"),rs.getString("baslangic_tarihi"),rs.getString("bitis_tarihi"),
                        rs.getBoolean("otomatik_yenile")?"true":"false",rs.getString("durum")));
                    first=false;
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(ex,200,json.toString());
            } catch (SQLException e) {
                sendResponse(ex,500,"{\"hata\":\""+e.getMessage().replace("\"","'")+"\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // PLANLAR — GET /api/planlar  (Public — landing için)
    // ═══════════════════════════════════════════════════
    static class PlanlarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            try {
                Connection conn=DatabaseBaglanti.baglantiGetir();
                
                // Lazy Cleanup — her üye için yalnızca en son aboneliği tut
                conn.createStatement().executeUpdate(
                    "WITH CTE AS (SELECT abonelik_id, ROW_NUMBER() OVER(PARTITION BY uye_id ORDER BY abonelik_id DESC) as rn " +
                    "FROM uye_abonelikleri) " +
                    "UPDATE uye_abonelikleri SET durum=N'iptal' WHERE abonelik_id IN (SELECT abonelik_id FROM CTE WHERE rn > 1) AND durum <> N'iptal'");

                Statement stmt=conn.createStatement();
                ResultSet rs=stmt.executeQuery(
                    "SELECT p.plan_id,p.plan_adi,p.fiyat,p.sure_ay,p.aciklama,p.ozellikler,p.durum," +
                    "(SELECT COUNT(*) FROM uye_abonelikleri a WHERE a.plan_id=p.plan_id AND a.durum=N'aktif')AS aktifUye " +
                    "FROM uye_planlari p ORDER BY p.fiyat DESC");
                StringBuilder json=new StringBuilder("[");
                boolean first=true;
                while(rs.next()) {
                    if (!first) json.append(",");
                    String oz=rs.getString("ozellikler"); if(oz==null) oz="[]";
                    json.append(String.format(
                        "{\"id\":%d,\"ad\":\"%s\",\"fiyat\":%.2f,\"sureAy\":%d," +
                        "\"aciklama\":\"%s\",\"ozellikler\":%s,\"durum\":\"%s\",\"aktifUye\":%d}",
                        rs.getInt("plan_id"),rs.getString("plan_adi"),rs.getDouble("fiyat"),rs.getInt("sure_ay"),
                        rs.getString("aciklama")!=null?rs.getString("aciklama").replace("\"","'"):"",
                        oz,rs.getString("durum"),rs.getInt("aktifUye")));
                    first=false;
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(ex,200,json.toString());
            } catch (SQLException e) {
                sendResponse(ex,500,"{\"hata\":\""+e.getMessage().replace("\"","'")+"\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // DERSLER — GET /api/dersler  [Hepsi (giriş yapmış)]
    // ═══════════════════════════════════════════════════
    static class DerslerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!requireAuth(ex)) return;

            try {
                Connection conn=DatabaseBaglanti.baglantiGetir();
                Statement stmt=conn.createStatement();

                ResultSet rs=stmt.executeQuery(
                    "SELECT s.ders_id,s.ders_adi,k.ad+' '+k.soyad AS antrenor," +
                    "s.kontenjan,s.sure_dakika,s.kategori,s.durum " +
                    "FROM siniflar s " +
                    "LEFT JOIN antrenorler a ON s.antrenor_id=a.antrenor_id " +
                    "LEFT JOIN kullanicilar k ON a.kullanici_id=k.kullanici_id");
                StringBuilder dJ=new StringBuilder("["); boolean f=true;
                while(rs.next()) {
                    if(!f) dJ.append(","); f=false;
                    dJ.append(String.format("{\"id\":%d,\"ders\":\"%s\",\"antrenor\":\"%s\",\"kontenjan\":%d,\"sure\":%d,\"kategori\":\"%s\",\"durum\":\"%s\"}",
                        rs.getInt("ders_id"),rs.getString("ders_adi"),
                        rs.getString("antrenor")!=null?rs.getString("antrenor"):"",
                        rs.getInt("kontenjan"),rs.getInt("sure_dakika"),
                        rs.getString("kategori")!=null?rs.getString("kategori"):"",rs.getString("durum")));
                }
                dJ.append("]"); rs.close();

                ResultSet r2=stmt.executeQuery(
                    "SELECT sp.program_id,s.ders_adi,sp.gun," +
                    "CONVERT(VARCHAR(5),sp.baslangic_saati,108)AS bSaat," +
                    "CONVERT(VARCHAR(5),sp.bitis_saati,108)AS bsSaat," +
                    "sp.salon,sp.durum FROM sinif_programlari sp " +
                    "JOIN siniflar s ON sp.ders_id=s.ders_id " +
                    "ORDER BY CASE sp.gun WHEN N'Pazartesi' THEN 1 WHEN N'Salı' THEN 2 WHEN N'Çarşamba' THEN 3 " +
                    "WHEN N'Perşembe' THEN 4 WHEN N'Cuma' THEN 5 WHEN N'Cumartesi' THEN 6 WHEN N'Pazar' THEN 7 END");
                StringBuilder pJ=new StringBuilder("["); f=true;
                while(r2.next()) {
                    if(!f) pJ.append(","); f=false;
                    pJ.append(String.format("{\"id\":%d,\"ders\":\"%s\",\"gun\":\"%s\",\"saat\":\"%s\u2013%s\",\"salon\":\"%s\",\"durum\":\"%s\"}",
                        r2.getInt("program_id"),r2.getString("ders_adi"),r2.getString("gun"),
                        r2.getString("bSaat"),r2.getString("bsSaat"),
                        r2.getString("salon")!=null?r2.getString("salon"):"",r2.getString("durum")));
                }
                pJ.append("]"); r2.close();

                ResultSet r3=stmt.executeQuery(
                    "SELECT r.rezervasyon_id,k.ad+' '+k.soyad AS uye,s.ders_adi," +
                    "CONVERT(VARCHAR(10),r.r_tarih,120)AS tarih," +
                    "CONVERT(VARCHAR(5),sp.baslangic_saati,108)+'-'+CONVERT(VARCHAR(5),sp.bitis_saati,108)AS saat," +
                    "r.durum FROM sinif_rezervasyonlari r " +
                    "JOIN uyeler u ON r.uye_id=u.uye_id " +
                    "JOIN kullanicilar k ON u.kullanici_id=k.kullanici_id " +
                    "JOIN sinif_programlari sp ON r.program_id=sp.program_id " +
                    "JOIN siniflar s ON sp.ders_id=s.ders_id ORDER BY r.r_tarih DESC");
                StringBuilder rJ=new StringBuilder("["); f=true;
                while(r3.next()) {
                    if(!f) rJ.append(","); f=false;
                    rJ.append(String.format("{\"id\":%d,\"uye\":\"%s\",\"ders\":\"%s\",\"tarih\":\"%s\",\"saat\":\"%s\",\"durum\":\"%s\"}",
                        r3.getInt("rezervasyon_id"),r3.getString("uye"),r3.getString("ders_adi"),
                        r3.getString("tarih"),r3.getString("saat"),r3.getString("durum")));
                }
                rJ.append("]"); r3.close(); stmt.close();

                sendResponse(ex,200,"{\"dersler\":"+dJ+",\"program\":"+pJ+",\"rezervasyonlar\":"+rJ+"}");
            } catch (SQLException e) {
                sendResponse(ex,500,"{\"hata\":\""+e.getMessage().replace("\"","'")+"\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ANTRENÖRLER — GET /api/antrenorler-detay  [Admin | Antrenör]
    // ═══════════════════════════════════════════════════
    static class AntrenorlerDetayHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!requireRole(ex,"admin","antrenor")) return;

            try {
                Connection conn=DatabaseBaglanti.baglantiGetir();
                Statement stmt=conn.createStatement();
                ResultSet rs=stmt.executeQuery(
                    "SELECT a.antrenor_id,k.ad+' '+k.soyad AS isim,k.email," +
                    "a.uzmanlik,a.deneyim_yili,a.sertifikalar,a.biyografi,a.durum," +
                    "(SELECT COUNT(*) FROM siniflar s WHERE s.antrenor_id=a.antrenor_id)AS dersCount " +
                    "FROM antrenorler a JOIN kullanicilar k ON a.kullanici_id=k.kullanici_id");
                StringBuilder json=new StringBuilder("["); boolean f=true;
                while(rs.next()) {
                    if(!f) json.append(","); f=false;
                    json.append(String.format(
                        "{\"id\":%d,\"isim\":\"%s\",\"email\":\"%s\"," +
                        "\"uzmanlik\":\"%s\",\"deneyim\":%d,\"sertifikalar\":\"%s\"," +
                        "\"biyografi\":\"%s\",\"durum\":\"%s\",\"dersCount\":%d}",
                        rs.getInt("antrenor_id"),rs.getString("isim"),
                        rs.getString("email")!=null?rs.getString("email"):"",
                        rs.getString("uzmanlik")!=null?rs.getString("uzmanlik").replace("\"","'"):"",
                        rs.getInt("deneyim_yili"),
                        rs.getString("sertifikalar")!=null?rs.getString("sertifikalar").replace("\"","'"):"",
                        rs.getString("biyografi")!=null?rs.getString("biyografi").replace("\"","'"):"",
                        rs.getString("durum"),rs.getInt("dersCount")));
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(ex,200,json.toString());
            } catch (SQLException e) {
                sendResponse(ex,500,"{\"hata\":\""+e.getMessage().replace("\"","'")+"\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // GİRİŞ/ÇIKIŞ — GET /api/giris-cikis  [Admin | Antrenör]
    // ═══════════════════════════════════════════════════
    static class GirisCikisHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!requireRole(ex,"admin","antrenor")) return;

            try {
                Connection conn=DatabaseBaglanti.baglantiGetir();
                Statement stmt=conn.createStatement();
                ResultSet rs=stmt.executeQuery(
                    "SELECT g.g_c_id,k.ad+' '+k.soyad AS isim," +
                    "CONVERT(VARCHAR(5),g.giris_saat,108)AS girisSaat," +
                    "CASE WHEN g.cikis_saat IS NOT NULL THEN CONVERT(VARCHAR(5),g.cikis_saat,108) ELSE NULL END AS cikisSaat," +
                    "g.giris_turu,g.durum " +
                    "FROM giris_cikis_kayitlari g " +
                    "JOIN kullanicilar k ON g.kullanici_id=k.kullanici_id " +
                    "WHERE CAST(g.giris_saat AS DATE)=CAST(GETDATE() AS DATE) " +
                    "ORDER BY g.giris_saat DESC");
                StringBuilder json=new StringBuilder("["); boolean f=true;
                while(rs.next()) {
                    if(!f) json.append(","); f=false;
                    String cikis=rs.getString("cikisSaat");
                    json.append(String.format(
                        "{\"id\":%d,\"uye\":\"%s\",\"giris\":\"%s\",\"cikis\":%s,\"turu\":\"%s\",\"durum\":\"%s\"}",
                        rs.getInt("g_c_id"),rs.getString("isim"),rs.getString("girisSaat"),
                        cikis!=null?"\""+cikis+"\"":"null",
                        rs.getString("giris_turu"),rs.getString("durum")));
                }
                json.append("]");
                rs.close(); stmt.close();
                sendResponse(ex,200,json.toString());
            } catch (SQLException e) {
                sendResponse(ex,500,"{\"hata\":\""+e.getMessage().replace("\"","'")+"\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // EKİPMAN — GET /api/ekipman  [Admin | Antrenör]
    // ═══════════════════════════════════════════════════
    static class EkipmanHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!requireRole(ex,"admin","antrenor")) return;

            try {
                Connection conn=DatabaseBaglanti.baglantiGetir();
                Statement stmt=conn.createStatement();

                ResultSet rs=stmt.executeQuery(
                    "SELECT e.ekipman_id,e.ekipman_adi,e.kategori,e.miktar," +
                    "CONVERT(VARCHAR(10),e.satin_alma_tarihi,120)AS satinAlma," +
                    "e.satin_alma_fiyati,e.durum FROM ekipman e ORDER BY e.ekipman_id");
                StringBuilder eJ=new StringBuilder("["); boolean f=true;
                while(rs.next()) {
                    if(!f) eJ.append(","); f=false;
                    eJ.append(String.format(
                        "{\"id\":%d,\"ad\":\"%s\",\"kategori\":\"%s\",\"adet\":%d,\"satinAlma\":\"%s\",\"fiyat\":%.2f,\"durum\":\"%s\"}",
                        rs.getInt("ekipman_id"),rs.getString("ekipman_adi"),
                        rs.getString("kategori")!=null?rs.getString("kategori"):"",
                        rs.getInt("miktar"),rs.getString("satinAlma")!=null?rs.getString("satinAlma"):"",
                        rs.getDouble("satin_alma_fiyati"),rs.getString("durum")));
                }
                eJ.append("]"); rs.close();

                ResultSet r2=stmt.executeQuery(
                    "SELECT b.bakim_id,e.ekipman_adi," +
                    "CONVERT(VARCHAR(10),b.bakim_tarihi,120)AS tarih," +
                    "b.maliyet,b.yapan_kisi,b.aciklama," +
                    "CONVERT(VARCHAR(10),b.sonraki_bakim,120)AS sonraki,b.durum " +
                    "FROM ekipman_bakimi b JOIN ekipman e ON b.ekipman_id=e.ekipman_id " +
                    "ORDER BY b.bakim_tarihi DESC");
                StringBuilder bJ=new StringBuilder("["); f=true;
                while(r2.next()) {
                    if(!f) bJ.append(","); f=false;
                    bJ.append(String.format(
                        "{\"id\":%d,\"ekipman\":\"%s\",\"tarih\":\"%s\",\"maliyet\":%.2f," +
                        "\"yapan\":\"%s\",\"aciklama\":\"%s\",\"sonraki\":\"%s\",\"durum\":\"%s\"}",
                        r2.getInt("bakim_id"),r2.getString("ekipman_adi"),r2.getString("tarih"),
                        r2.getDouble("maliyet"),
                        r2.getString("yapan_kisi")!=null?r2.getString("yapan_kisi"):"",
                        r2.getString("aciklama")!=null?r2.getString("aciklama").replace("\"","'"):"",
                        r2.getString("sonraki")!=null?r2.getString("sonraki"):"",
                        r2.getString("durum")));
                }
                bJ.append("]"); r2.close(); stmt.close();

                sendResponse(ex,200,"{\"ekipman\":"+eJ+",\"bakim\":"+bJ+"}");
            } catch (SQLException e) {
                sendResponse(ex,500,"{\"hata\":\""+e.getMessage().replace("\"","'")+"\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // TEST — GET /api/test  (Public)
    // ═══════════════════════════════════════════════════
    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            corsHeaders(ex);
            boolean ok=DatabaseBaglanti.baglantiTest();
            sendResponse(ex, ok?200:500,
                ok ? "{\"basarili\":true,\"mesaj\":\"SQL Server bağlantısı aktif!\"}"
                   : "{\"basarili\":false,\"mesaj\":\"SQL Server bağlantısı başarısız!\"}");
        }
    }

    // ═══════════════════════════════════════════════════
    // STATIC FILE HANDLER — Frontend dosyalarını sun
    // ═══════════════════════════════════════════════════
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) path = "/index.html";

            File file = new File("view" + path);
            if (!file.exists() || !file.isFile()) {
                // Fallback: View/ (büyük harf)
                file = new File("View" + path);
            }
            if (!file.exists() || !file.isFile()) {
                String msg = "404 — Dosya bulunamadı: " + path;
                ex.sendResponseHeaders(404, msg.getBytes().length);
                ex.getResponseBody().write(msg.getBytes()); ex.getResponseBody().close(); return;
            }

            String mime = "application/octet-stream";
            if      (path.endsWith(".html")) mime="text/html; charset=UTF-8";
            else if (path.endsWith(".css"))  mime="text/css; charset=UTF-8";
            else if (path.endsWith(".js"))   mime="application/javascript; charset=UTF-8";
            else if (path.endsWith(".png"))  mime="image/png";
            else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) mime="image/jpeg";
            else if (path.endsWith(".svg"))  mime="image/svg+xml";
            else if (path.endsWith(".ico"))  mime="image/x-icon";
            else if (path.endsWith(".woff2"))mime="font/woff2";

            ex.getResponseHeaders().set("Content-Type", mime);
            ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            // Statik dosyalar için cache
            ex.getResponseHeaders().set("Cache-Control", "public, max-age=3600");

            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            ex.sendResponseHeaders(200, bytes.length);
            OutputStream os = ex.getResponseBody();
            os.write(bytes); os.close();
        }
    }

    // ═══════════════════════════════════════════════════
    // ŞİFREMİ UNUTTUM — POST /api/sifremi-unuttum
    // ═══════════════════════════════════════════════════
    static class SifremiUnuttumHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!"POST".equals(ex.getRequestMethod())) { sendResponse(ex,405,errJson("Sadece POST","METHOD_NOT_ALLOWED")); return; }
            
            String body  = readBody(ex);
            String email = jsonValue(body,"email");
            if (email==null) { sendResponse(ex,400,errJson("E-posta gerekli!","BAD_REQUEST")); return; }
            
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement stmt = conn.prepareStatement("SELECT kullanici_id FROM kullanicilar WHERE email=?");
                stmt.setString(1, email.toLowerCase());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int kId = rs.getInt("kullanici_id");
                    String resetToken = UUID.randomUUID().toString();
                    PreparedStatement upd = conn.prepareStatement("UPDATE kullanicilar SET sifre_token=?, token_gecerli=DATEADD(hour, 1, GETDATE()) WHERE kullanici_id=?");
                    upd.setString(1, resetToken);
                    upd.setInt(2, kId);
                    upd.executeUpdate();
                    upd.close();
                    
                    System.out.println("📧 E-Posta gönderimi başlatılıyor: " + email);
                    // Gerçekten Mail Gönder (Thread içinde atıyoruz ki API'yi bekletmesin)
                    new Thread(() -> {
                        MailSender.sendResetMail(email, resetToken);
                    }).start();
                    
                    sendResponse(ex,200,"{\"basarili\":true,\"mesaj\":\"Şifre sıfırlama linki e-postanıza gönderildi.\",\"token\":\"\"}");
                } else {
                    sendResponse(ex,200,"{\"basarili\":true,\"mesaj\":\"Eğer bu e-posta sistemimize kayıtlıysa sıfırlama linki gönderilmiştir.\"}");
                }
                rs.close(); stmt.close();
            } catch (SQLException e) {
                sendResponse(ex,500,errJson("Veritabanı hatası!","DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ŞİFRE SIFIRLA — POST /api/sifre-sifirla
    // ═══════════════════════════════════════════════════
    static class SifreSifirlaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!"POST".equals(ex.getRequestMethod())) { sendResponse(ex,405,errJson("Sadece POST","METHOD_NOT_ALLOWED")); return; }
            
            String body  = readBody(ex);
            String token = jsonValue(body,"token");
            String yeniSifre = jsonValue(body,"yeni_sifre");
            
            if (token==null||yeniSifre==null) { sendResponse(ex,400,errJson("Token ve yeni şifre gerekli!","BAD_REQUEST")); return; }
            if (yeniSifre.length() < 6) { sendResponse(ex,400,errJson("Şifre en az 6 karakter olmalıdır!","BAD_REQUEST")); return; }
            
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement stmt = conn.prepareStatement("SELECT kullanici_id FROM kullanicilar WHERE sifre_token=? AND token_gecerli > GETDATE()");
                stmt.setString(1, token);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int kId = rs.getInt("kullanici_id");
                    String sifreHash = hashPassword(yeniSifre);
                    PreparedStatement upd = conn.prepareStatement("UPDATE kullanicilar SET sifre_hash=?, sifre_token=NULL, token_gecerli=NULL WHERE kullanici_id=?");
                    upd.setString(1, sifreHash);
                    upd.setInt(2, kId);
                    upd.executeUpdate();
                    upd.close();
                    
                    System.out.println("✅ Şifre sıfırlandı: ID=" + kId);
                    sendResponse(ex,200,"{\"basarili\":true,\"mesaj\":\"Şifreniz başarıyla sıfırlandı!\"}");
                } else {
                    sendResponse(ex,400,errJson("Geçersiz veya süresi dolmuş token!","INVALID_TOKEN"));
                }
                rs.close(); stmt.close();
            } catch (SQLException e) {
                sendResponse(ex,500,errJson("Veritabanı hatası!","DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // MAİL GÖNDERİM YARDIMCISI (Javax.Mail / SMTP)
    // ═══════════════════════════════════════════════════
    // ═══════════════════════════════════════════════════
    // ABONELIK SATIN AL — POST /api/abonelik-satin-al  [Üye]
    // Bekleyen ödeme kaydı oluşturur (odemeler tablosuna beklemede olarak ekler)
    // ═══════════════════════════════════════════════════
    static class AbonelikSatinAlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!"POST".equals(ex.getRequestMethod())) { sendResponse(ex,405,errJson("Sadece POST","METHOD_NOT_ALLOWED")); return; }
            String[] u = authUser(ex);
            System.out.println("?? /api/abonelik-satin-al istegi - auth: " + (u==null?"NULL":u[2]));
            if (u==null) { sendResponse(ex,401,errJson("Giriş yapmanız gerekiyor!","UNAUTHORIZED")); return; }

            String body    = readBody(ex);
            String planId  = jsonValue(body, "plan_id");
            System.out.println("?? body: " + body + " | planId: " + planId);
            if (planId==null) { sendResponse(ex,400,errJson("Plan ID gerekli!","BAD_REQUEST")); return; }

            int kullaniciId = Integer.parseInt(u[0]);
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();

                // Plan bilgilerini al
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT plan_id,plan_adi,fiyat,sure_ay FROM uye_planlari WHERE plan_id=? AND durum=N'aktif'");
                ps.setInt(1, Integer.parseInt(planId));
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    rs.close(); ps.close();
                    sendResponse(ex,404,errJson("Plan bulunamadı!","NOT_FOUND")); return;
                }
                int    pId     = rs.getInt("plan_id");
                String pAdi    = rs.getString("plan_adi");
                double fiyat   = rs.getDouble("fiyat");
                int    sureAy  = rs.getInt("sure_ay");
                rs.close(); ps.close();

                // Üye uyeler tablosunda var mı? (kayit anında ekleniyor ama kontrol edelim)
                PreparedStatement uq = conn.prepareStatement(
                    "SELECT uye_id FROM uyeler WHERE kullanici_id=?");
                uq.setInt(1, kullaniciId);
                ResultSet ur = uq.executeQuery();
                int uyeId = -1;
                if (ur.next()) uyeId = ur.getInt("uye_id");
                ur.close(); uq.close();

                // uyeler'de yoksa ekle (eski kullanıcılar için güvenlik)
                if (uyeId < 0) {
                    Statement cs = conn.createStatement();
                    ResultSet cr = cs.executeQuery("SELECT COUNT(*) AS cnt FROM uyeler");
                    int cnt = cr.next() ? cr.getInt("cnt") : 0; cr.close(); cs.close();
                    String uNo = String.format("FZ-%d-%03d", java.time.Year.now().getValue(), cnt+1);
                    PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO uyeler(kullanici_id,uyelik_no) VALUES(?,?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS);
                    ins.setInt(1, kullaniciId);
                    ins.setString(2, uNo);
                    ins.executeUpdate();
                    ResultSet gk = ins.getGeneratedKeys();
                    if (gk.next()) uyeId = gk.getInt(1);
                    gk.close(); ins.close();
                }

                // Mevcut tÜM aktif veya pasif abonelikleri iptal et (bir üye = bir plan kuralı)
                PreparedStatement cancelOld = conn.prepareStatement(
                    "UPDATE uye_abonelikleri SET durum=N'iptal' " +
                    "WHERE uye_id=? AND durum IN (N'aktif', N'pasif', N'suresi_doldu')");
                cancelOld.setInt(1, uyeId);
                int cancelledCount = cancelOld.executeUpdate();
                cancelOld.close();
                System.out.println("?? SAtın Alma Öncesi Temizlik: uye_id=" + uyeId + " - " + cancelledCount + " kayıt 'iptal' yapıldı.");

                // Yeni pasif abonelik kaydı oluştur (ödeme yapılınca aktif olacak)
                // TALEP: Basic 1ay, Silver 3ay, Gold 6ay, Platinum 12ay.
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDate bitis = today.plusMonths(sureAy);

                PreparedStatement ab = conn.prepareStatement(
                    "INSERT INTO uye_abonelikleri(uye_id,plan_id,baslangic_tarihi,bitis_tarihi,durum) VALUES(?,?,?,?,N'pasif')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
                ab.setInt(1, uyeId);
                ab.setInt(2, pId);
                ab.setDate(3, java.sql.Date.valueOf(today));
                ab.setDate(4, java.sql.Date.valueOf(bitis));
                ab.executeUpdate();
                ResultSet abk = ab.getGeneratedKeys();
                int abonelikId = -1;
                if (abk.next()) abonelikId = abk.getInt(1);
                abk.close(); ab.close();

                // Bekleyen ödeme kaydı oluştur
                PreparedStatement od = conn.prepareStatement(
                    "INSERT INTO odemeler(abonelik_id,miktar,odeme_yontemi,durum,aciklama) VALUES(?,?,N'online',N'beklemede',?)");
                od.setInt(1, abonelikId);
                od.setDouble(2, fiyat);
                od.setString(3, pAdi + " planı - bekleyen ödeme");
                od.executeUpdate(); od.close();

                System.out.println("✅ Abonelik satın alma isteği: kullanici_id="+kullaniciId+" plan="+pAdi);
                sendResponse(ex,200,String.format(
                    "{\"basarili\":true,\"mesaj\":\"Plan seçildi! Ödeme bekleniyor.\"," +
                    "\"abonelik_id\":%d,\"plan\":\"%s\",\"fiyat\":%.2f}",
                    abonelikId, pAdi, fiyat));

            } catch (Exception e) {
                System.out.println("❌ Abonelik satın alma hatası: "+e.getMessage());
                sendResponse(ex,500,errJson("Veritabanı hatası: "+e.getMessage().replace("\"","'"),"DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ODEME YAP — POST /api/odeme-yap  [Üye]
    // Ödemeyi tamamlar: abonelik aktif edilir, ödeme tamamlandi yapılır
    // ═══════════════════════════════════════════════════
    static class OdemeYapHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) { corsHeaders(ex); ex.sendResponseHeaders(204,-1); return; }
            if (!"POST".equals(ex.getRequestMethod())) { sendResponse(ex,405,errJson("Sadece POST","METHOD_NOT_ALLOWED")); return; }
            String[] u = authUser(ex);
            System.out.println(">> /api/odeme-yap istegi - auth: " + (u==null?"NULL":u[2]));
            if (u==null) { sendResponse(ex,401,errJson("Giriş yapmanız gerekiyor!","UNAUTHORIZED")); return; }
            // Admin ve üye ödeme yapabilir
            if (!"uye".equals(u[2]) && !"admin".equals(u[2])) {
                sendResponse(ex,403,errJson("Bu işlem için yetkiniz yok!","FORBIDDEN")); return;
            }

            String body       = readBody(ex);
            String abonelikId = jsonValue(body, "abonelik_id");
            String yontem     = jsonValue(body, "odeme_yontemi");
            if (abonelikId==null) { sendResponse(ex,400,errJson("Abonelik ID gerekli!","BAD_REQUEST")); return; }
            if (yontem==null || yontem.isEmpty()) yontem = "online";

            int kullaniciId = Integer.parseInt(u[0]);
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();

                // Aboneliğin bu kullanıcıya ait olduğunu doğrula
                PreparedStatement chk = conn.prepareStatement(
                    "SELECT a.abonelik_id,a.durum FROM uye_abonelikleri a " +
                    "JOIN uyeler uy ON a.uye_id=uy.uye_id " +
                    "WHERE a.abonelik_id=? AND uy.kullanici_id=?");
                chk.setInt(1, Integer.parseInt(abonelikId));
                chk.setInt(2, kullaniciId);
                ResultSet cr = chk.executeQuery();
                if (!cr.next()) {
                    cr.close(); chk.close();
                    sendResponse(ex,403,errJson("Bu aboneliğe erişim yetkiniz yok!","FORBIDDEN")); return;
                }
                String abonelikDurum = cr.getString("durum");
                cr.close(); chk.close();
                if (!"pasif".equals(abonelikDurum)) {
                    sendResponse(ex,400,errJson("Bu abonelik zaten aktif veya iptal edilmiş!","INVALID_STATE")); return;
                }

                // Aboneliği aktif yap
                PreparedStatement upd = conn.prepareStatement(
                    "UPDATE uye_abonelikleri SET durum=N'aktif' WHERE abonelik_id=?");
                upd.setInt(1, Integer.parseInt(abonelikId));
                upd.executeUpdate(); upd.close();

                // Ödemeyi tamamlandi yap + yöntemi güncelle
                PreparedStatement pod = conn.prepareStatement(
                    "UPDATE odemeler SET durum=N'tamamlandi', odeme_yontemi=?, odeme_tarihi=GETDATE() " +
                    "WHERE abonelik_id=? AND durum=N'beklemede'");
                pod.setString(1, yontem);
                pod.setInt(2, Integer.parseInt(abonelikId));
                pod.executeUpdate(); pod.close();

                System.out.println("✅ Ödeme tamamlandı: abonelik_id="+abonelikId+" kullanici="+kullaniciId);
                sendResponse(ex,200,
                    "{\"basarili\":true,\"mesaj\":\"\u00d6deme başarıyla tamamlandı! Aboneliğiniz aktif edildi.\"}");

            } catch (Exception e) {
                System.out.println("❌ Ödeme hatası: "+e.getMessage());
                sendResponse(ex,500,errJson("Veritabanı hatası: "+e.getMessage().replace("\"","'"),"DB_ERROR"));
            }
        }
    }

    static class MailSender {
        // LÜTFEN KENDİ GMAIL ADRESİNİZİ VE "UYGULAMA ŞİFRENİZİ" (App Password) BURAYA YAZIN
        static final String username = "fitzonedestek@gmail.com"; 
        static final String password = "srtrfiadbvtqldlk";

        public static void sendResetMail(String toEmail, String token) {
            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "587");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true"); // TLS

            Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username, "FitZone Pro"));
                message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
                );
                message.setSubject("FitZone Pro - Şifre Sıfırlama İsteği");

                String resetLink = "http://localhost:8080/?resetToken=" + token;
                
                String htmlContent = "<div style='font-family:sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #ddd;border-radius:10px;'>"
                                   + "<h2 style='color:#0ea5e9;'>FitZone Pro</h2>"
                                   + "<p>Merhaba,</p>"
                                   + "<p>Hesabınız için bir şifre sıfırlama isteği aldık. Şifrenizi sıfırlamak için aşağıdaki butona tıklayın:</p>"
                                   + "<div style='text-align:center;margin:30px 0;'>"
                                   + "<a href='" + resetLink + "' style='background:#6366f1;color:white;padding:12px 24px;text-decoration:none;border-radius:8px;font-weight:bold;'>Şifremi Sıfırla</a>"
                                   + "</div>"
                                   + "<p style='font-size:12px;color:#777;'>Bu isteği siz yapmadıysanız, bu e-postayı dikkate almayınız.</p>"
                                   + "</div>";

                message.setContent(htmlContent, "text/html; charset=utf-8");

                Transport.send(message);
                System.out.println("✅ E-posta başarıyla gönderildi: " + toEmail);
            } catch (Exception e) {
                System.out.println("❌ E-posta gönderim hatası (Gmail bağlanamadı veya yetki yok): " + e.getMessage());
            }
        }
    }
}
