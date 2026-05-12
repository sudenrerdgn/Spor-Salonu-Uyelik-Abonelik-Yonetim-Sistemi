package Controller; // Controller paketine ait sınıf

// ─── Java HTTP Sunucu Kütüphaneleri ───
import com.sun.net.httpserver.HttpServer; // HTTP sunucu oluşturmak için
import com.sun.net.httpserver.HttpHandler; // İstek işleyici arayüzü
import com.sun.net.httpserver.HttpExchange; // İstek/yanıt nesnesi — her HTTP isteği için oluşturulur

// ─── Temel Java Kütüphaneleri ───
import java.io.*; // Giriş/Çıkış akışları (InputStream, OutputStream, vb.)
import java.net.InetSocketAddress; // IP adresi + port çifti — sunucu dinleme adresi
import java.nio.charset.StandardCharsets; // UTF-8 karakter kodlaması
import java.sql.*; // JDBC — veritabanı bağlantısı ve sorguları
import java.util.Base64; // Base64 kodlama/çözme — JWT token'lar için
import java.util.Locale; // Yerel ayar — ondalık sayı formatı için (nokta vs virgül)
import java.util.UUID; // Benzersiz ID üretimi — CSRF token için
import java.util.Properties; // Anahtar-değer yapılandırması — SMTP ayarları için
import java.util.concurrent.ConcurrentHashMap; // Thread-safe HashMap — Rate Limiter için
import java.util.concurrent.Executors; // Thread havuzu — eşzamanlı istekleri işlemek için

// ─── E-posta Gönderim Kütüphaneleri (javax.mail) ───
import javax.mail.Message; // E-posta mesaj nesnesi
import javax.mail.MessagingException; // E-posta hata yönetimi
import javax.mail.PasswordAuthentication; // SMTP kimlik doğrulama
import javax.mail.Session; // SMTP oturum yönetimi
import javax.mail.Transport; // E-posta gönderim aktarımı
import javax.mail.internet.InternetAddress; // E-posta adresi formatı
import javax.mail.internet.MimeMessage; // MIME formatında e-posta oluşturma

// ─── Güvenlik / Kriptografi Kütüphaneleri ───
import java.security.MessageDigest; // SHA-256 şifre hash'leme için
import java.security.NoSuchAlgorithmException; // Hash algoritması bulunamadığında fırlatılır
import javax.crypto.Mac; // HMAC — JWT imzalama için
import javax.crypto.spec.SecretKeySpec; // HMAC gizli anahtar tanımı

/**
 * FitZone Pro — Backend API Sunucusu v2.0
 * JWT Auth + IP Rate Limiting + Rol Tabanlı Erişim Kontrolü
 *
 * Çalıştırmak için (proje kök dizininden):
 * javac -cp
 * ".;mssql-jdbc-12.4.2.jre11.jar;javax.mail-1.6.2.jar;activation-1.1.1.jar"
 * Controller/ApiServer.java Controller/DatabaseBaglanti.java
 * java -cp
 * ".;mssql-jdbc-12.4.2.jre11.jar;javax.mail-1.6.2.jar;activation-1.1.1.jar"
 * Controller.ApiServer
 *
 * Public (token gerekmez):
 * POST /api/kayit → Yeni üye kaydı
 * POST /api/giris → Giriş (JWT token döner)
 * GET /api/planlar → Üyelik planları (landing sayfası)
 * GET /api/test → Bağlantı testi
 *
 * Auth gerektiren:
 * GET /api/dogrula → Token doğrula / oturum refresh
 * GET /api/uyeler → [Admin] Tüm üyeler
 * POST /api/uye-guncelle → [Admin] Üye güncelle
 * POST /api/uye-sil → [Admin] Üye sil
 * GET /api/istatistikler → [Admin] Dashboard istatistikleri
 * GET /api/odemeler → [Admin] Tümü | [Üye] Kendi ödemeleri
 * GET /api/abonelikler → [Admin] Tümü | [Üye] Kendi abonelikleri
 * GET /api/dersler → [Admin|Üye|Antrenör]
 * GET /api/antrenorler-detay → [Admin|Antrenör]
 * GET /api/giris-cikis → [Admin|Antrenör]
 * GET /api/ekipman → [Admin|Antrenör]
 */
public class ApiServer {

    private static final int PORT = 8080; // Sunucunun dinleyeceği port numarası
    // JWT token imzalamak için kullanılan gizli anahtar
    // ÖNEMLİ: Production'da bunu environment variable'dan okuyun:
    // JWT_SECRET = System.getenv("FITZONE_JWT_SECRET");
    private static final String JWT_SECRET = "FitZonePro_2026_GizliAnahtar_#$@!_DeğiştirBunu";
    // Canlı sunucu URL'si — şifre sıfırlama e-postasındaki link için kullanılır
    private static final String BASE_URL = "http://ec2-13-53-225-142.eu-north-1.compute.amazonaws.com:" + PORT;

    // ─────────────────────────────────────────────
    // ─── Uygulama giriş noktası — Sunucu burada başlatılır ───
    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US); // Ondalık sayılarda nokta kullanımı için (ör: 850.00 — 850,00 değil)
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  FitZone Pro — Backend API Sunucusu v2.0");
        System.out.println("  🔐 JWT Auth + Rate Limiting Aktif");
        System.out.println("═══════════════════════════════════════════════════");

        // Sunucu başlamadan önce veritabanı bağlantısını test et — bağlanamazsa çık
        if (!DatabaseBaglanti.baglantiTest()) {
            System.out.println("⚠️  Veritabanı bağlantısı kurulamadı! Lütfen ayarları kontrol edin.");
            return; // DB yoksa sunucu başlatılmaz
        }

        // HTTP sunucu oluştur — belirtilen portta tüm arayüzlerden dinler (0.0.0.0)
        // İkinci parametre (0) = backlog: bekleyen bağlantı kuyruğu boyutu (sistem
        // varsayılanı)
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // ── Public endpointler (token gerekmez) ──────────────
        server.createContext("/api/kayit", new KayitHandler());
        server.createContext("/api/giris", new GirisHandler());
        server.createContext("/api/sifremi-unuttum", new SifremiUnuttumHandler());
        server.createContext("/api/sifre-sifirla", new SifreSifirlaHandler());
        server.createContext("/api/planlar", new PlanlarHandler()); // landing sayfası için public
        server.createContext("/api/test", new TestHandler());

        // ── Kimlik doğrulama ─────────────────────────────────
        server.createContext("/api/dogrula", new DogrulaHandler());

        // ── Admin ────────────────────────────────────────────
        server.createContext("/api/uyeler", new UyelerHandler());
        server.createContext("/api/kullanicilar", new KullanicilarHandler());
        server.createContext("/api/uye-guncelle", new UyeGuncelleHandler());
        server.createContext("/api/uye-sil", new UyeSilHandler());
        server.createContext("/api/istatistikler", new IstatistiklerHandler());
        server.createContext("/api/aylik-gelir", new AylikGelirHandler());
        server.createContext("/api/aylik-uye", new AylikUyeHandler());
        server.createContext("/api/aylik-devamsiz", new AylikDevamsizHandler());
        server.createContext("/api/rapor-odeme", new RaporOdemeHandler());
        server.createContext("/api/rapor-ders", new RaporDersHandler());

        // ── Admin | Üye (kendi verisi) ────────────────────────
        server.createContext("/api/odemeler", new OdemelerHandler());
        server.createContext("/api/abonelikler", new AboneliklerHandler());
        server.createContext("/api/profil-guncelle", new ProfilGuncelleHandler());
        server.createContext("/api/abonelik-satin-al", new AbonelikSatinAlHandler());
        server.createContext("/api/odeme-yap", new OdemeYapHandler());

        // ── Tüm giriş yapmış kullanıcılar ────────────────────

        server.createContext("/api/rezervasyon-yap", new RezervasyonYapHandler());
        server.createContext("/api/uye-aktivite", new UyeAktiviteHandler());
        server.createContext("/api/abonelik-guncelle", new AbonelikGuncelleHandler());
        server.createContext("/api/public-istatistikler", new PublicIstatistiklerHandler());
        server.createContext("/api/dersler", new DerslerHandler());

        // ── Admin | Antrenör ──────────────────────────────────
        server.createContext("/api/antrenorler-detay", new AntrenorlerDetayHandler());
        server.createContext("/api/giris-cikis", new GirisCikisHandler());
        server.createContext("/api/ekipman", new EkipmanHandler());

        // ── Yeni Ekleme İşlemleri (Admin) ──────────────────────
        server.createContext("/api/antrenor-ekle", new AntrenorEkleHandler());
        server.createContext("/api/ekipman-ekle", new EkipmanEkleHandler());
        server.createContext("/api/ders-ekle", new DersEkleHandler());

        // ─── Düzenle ve Sil İşlemleri (Admin) ───
        server.createContext("/api/ders-guncelle", new DersGuncelleHandler());
        server.createContext("/api/ders-sil", new DersSilHandler());
        server.createContext("/api/program-ekle", new ProgramEkleHandler());
        server.createContext("/api/program-sil", new ProgramSilHandler());
        server.createContext("/api/rezervasyon-iptal", new RezervasyonIptalHandler());
        server.createContext("/api/antrenor-guncelle", new AntrenorGuncelleHandler());
        server.createContext("/api/antrenor-sil", new AntrenorSilHandler());
        server.createContext("/api/ekipman-guncelle", new EkipmanGuncelleHandler());
        server.createContext("/api/ekipman-sil", new EkipmanSilHandler());
        // ── Static dosyalar ───────────────────────────────────
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors.newFixedThreadPool(10)); // 10 thread — eşzamanlı istekleri işler (tek thread'de
                                                              // takılma sorununu çözer)
        server.start(); // Sunucuyu başlat — artık istekleri kabul eder

        // Başlatma bilgisi — hangi endpointlerin aktif olduğunu gösterir
        System.out.println("✅ API aktif  : http://0.0.0.0:" + PORT + " (tüm arayüzlerden erişilebilir)");
        System.out.println("─────────────────────────────────────────────────");
        System.out.println("  Public   → POST /api/giris   POST /api/kayit");
        System.out.println("  Admin    → /api/uyeler  /api/istatistikler  ...");
        System.out.println("  Üye      → /api/odemeler (kendi)  /api/abonelikler (kendi)");
        System.out.println("  Antrenör → /api/dersler  /api/giris-cikis  ...");
        System.out.println("═══════════════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════
    // JWT UTILITY — JSON Web Token üretme ve doğrulama sınıfı
    // HMAC-SHA256 algoritması ile token imzalanır
    // Token yapısı: header.payload.signature (Base64URL kodlanmış)
    // ═══════════════════════════════════════════════════
    static class JwtUtil {
        private static final long EXPIRY_SEC = 8L * 60 * 60; // Token geçerlilik süresi: 8 saat (saniye cinsinden)

        /**
         * JWT token üret.
         * Payload içeriği: kullanici_id (sub), email, rol, csrf token, üretim zamanı
         * (iat), son kullanma (exp)
         * 
         * @param id    Kullanıcı ID
         * @param email Kullanıcı e-posta adresi
         * @param rol   Kullanıcı rolü (admin, uye, antrenor, kullanici)
         * @param csrf  CSRF koruma tokenı (UUID)
         * @return İmzalanmış JWT string (header.payload.signature)
         */
        static String generate(int id, String email, String rol, String csrf) {
            try {
                long now = System.currentTimeMillis() / 1000; // Şu anki zaman (Unix timestamp, saniye)
                // JWT header: Algoritma (HS256) ve tip (JWT) bilgisi
                String header = b64u("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
                // JWT payload: Kullanıcı bilgileri + zaman damgaları
                // Girdi değerlerindeki " karakterleri temizlenir (JSON bozulmasını önlemek
                // için)
                String payload = b64u(String.format(
                        "{\"sub\":%d,\"email\":\"%s\",\"rol\":\"%s\",\"csrf\":\"%s\",\"iat\":%d,\"exp\":%d}",
                        id, email.replace("\"", ""), rol.replace("\"", ""), csrf.replace("\"", ""), now,
                        now + EXPIRY_SEC));
                // İmza: header.payload birleştirilip HMAC-SHA256 ile imzalanır
                String sig = sign(header + "." + payload);
                // Son token: header.payload.signature
                return header + "." + payload + "." + sig;
            } catch (Exception e) {
                throw new RuntimeException("Token üretilemedi", e);
            }
        }

        /**
         * JWT token doğrula.
         * 1) Token 3 parçadan oluşmalı (header.payload.signature)
         * 2) İmza doğrulanmalı (değiştirilmemiş olmalı)
         * 3) Süresi dolmamış olmalı (exp > şu an)
         * 
         * @return String[4] = {kullanici_id, email, rol, csrf} — geçersizse null
         */
        static String[] verify(String token) {
            if (token == null)
                return null; // Token yoksa geçersiz
            String[] p = token.split("\\.", -1); // Token'ı "." ile 3 parçaya böl
            if (p.length != 3)
                return null; // 3 parça değilse geçersiz format
            try {
                // İmza doğrulama: header.payload yeniden imzalanıp mevcut imza ile
                // karşılaştırılır
                if (!sign(p[0] + "." + p[1]).equals(p[2]))
                    return null; // İmza uyuşmazsa → token değiştirilmiş
                // Payload'ı Base64URL'den çözüp JSON string'e dönüştür
                String json = new String(Base64.getUrlDecoder().decode(pad(p[1])), StandardCharsets.UTF_8);
                // Süre kontrolü: exp değeri şu anki zamandan küçükse token süresi dolmuş
                long exp = Long.parseLong(jNum(json, "exp"));
                if (System.currentTimeMillis() / 1000 > exp)
                    return null; // Süresi dolmuş
                // Payload'dan kullanıcı bilgilerini çıkar ve dizi olarak döndür
                return new String[] { jNum(json, "sub"), jStr(json, "email"), jStr(json, "rol"), jStr(json, "csrf") };
            } catch (Exception e) {
                return null;
            } // Herhangi bir hata → geçersiz token
        }

        // Base64 URL-safe kodlama (padding olmadan)
        private static String b64u(String s) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
        }

        // Base64 padding tamamlama — "=" karakterleriyle 4'ün katına tamamla
        private static String pad(String s) {
            int n = s.length() % 4;
            return n == 0 ? s : s + "====".substring(n);
        }

        // HMAC-SHA256 imzalama — JWT_SECRET ile veriyi imzalar
        private static String sign(String data) throws Exception {
            Mac mac = Mac.getInstance("HmacSHA256"); // HMAC-SHA256 algoritması
            mac.init(new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256")); // Gizli anahtar
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8))); // İmzayı Base64 olarak döndür
        }

        // JSON string'ten belirli bir anahtarın string değerini çıkar ("key":"value"
        // formatı)
        private static String jStr(String json, String key) {
            String s = "\"" + key + "\":\"";
            int i = json.indexOf(s);
            if (i < 0)
                return ""; // Anahtar bulunamadı
            int start = i + s.length(); // Değer başlangıcı
            return json.substring(start, json.indexOf("\"", start)); // Kapanış tırnağına kadar oku
        }

        // JSON string'ten belirli bir anahtarın sayısal değerini çıkar ("key":123
        // formatı)
        private static String jNum(String json, String key) {
            String s = "\"" + key + "\":";
            int i = json.indexOf(s);
            if (i < 0)
                return "0"; // Anahtar bulunamadı → varsayılan 0
            int st = i + s.length(), en = st;
            // Rakam ve eksi işareti devam ettiği sürece oku
            while (en < json.length() && (Character.isDigit(json.charAt(en)) || json.charAt(en) == '-'))
                en++;
            return json.substring(st, en);
        }
    }

    // ═══════════════════════════════════════════════════
    // RATE LIMITER — Kaba kuvvet (brute-force) saldırı koruması
    // IP bazlı başarısız giriş denemelerini sayıp, belirli sınır aşılınca engeller
    // ═══════════════════════════════════════════════════
    static class RateLimiter {
        private static final int MAX_FAIL = 5; // Maksimum başarısız giriş sayısı
        private static final long WIN_MS = 15L * 60 * 1000; // Pencere süresi: 15 dakika (milisaniye)
        // IP adresi → [hataSayısı, ilkHataZamanı] eşlemesi (thread-safe)
        private static final ConcurrentHashMap<String, long[]> map = new ConcurrentHashMap<>();

        // Belirtilen IP engelli mi kontrol et
        static boolean blocked(String ip) {
            long[] v = map.get(ip);
            if (v == null)
                return false; // Hiç hata kaydı yok → engellenmemiş
            // Pencere süresi aşıldıysa kaydı temizle
            if (System.currentTimeMillis() - v[1] > WIN_MS) {
                map.remove(ip);
                return false;
            }
            return v[0] >= MAX_FAIL; // Hata sayısı >= 5 ise engellenmiş
        }

        // Kalan bekleme süresini saniye olarak hesapla (kullanıcıya gösterilir)
        static long waitSec(String ip) {
            long[] v = map.get(ip);
            if (v == null)
                return 0;
            return Math.max(0, (WIN_MS - (System.currentTimeMillis() - v[1])) / 1000);
        }

        // Başarısız giriş kaydet — hata sayısını artırır
        static void fail(String ip) {
            long now = System.currentTimeMillis();
            map.compute(ip, (k, v) -> {
                // Yeni kayıt veya süresi dolmuş → sıfırdan başlat
                if (v == null || now - v[1] > WIN_MS)
                    return new long[] { 1, now };
                v[0]++;
                return v; // Mevcut kaydın sayısını artır
            });
        }

        // Başarılı giriş — IP'nin hata kaydını sıfırla
        static void ok(String ip) {
            map.remove(ip);
        }
    }

    // ═══════════════════════════════════════════════════
    // CORS + YARDIMCI METOTLAR
    // CORS (Cross-Origin Resource Sharing): Farklı domain'lerden gelen isteklere
    // izin verir
    // ═══════════════════════════════════════════════════
    /**
     * Tüm API yanıtlarına CORS header'ları ekler.
     * "*" = herhangi bir domain'den erişime izin verir (production'da kısıtlanmalı)
     */
    private static void corsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Hangi domainlerden istek kabul edilir
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE"); // İzin verilen
                                                                                                        // HTTP
                                                                                                        // metotları
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-CSRF-Token"); // İzin
                                                                                                                  // verilen
                                                                                                                  // header'lar
        ex.getResponseHeaders().set("Access-Control-Expose-Headers", "Authorization"); // Frontend'in okuyabileceği
                                                                                       // header'lar
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8"); // Yanıt içerik tipi
    }

    /**
     * HTTP yanıt gönderme yardımcısı.
     * CORS header'ları ekler, JSON içeriği UTF-8 olarak kodlar ve gönderir.
     * 
     * @param ex   HTTP değişim nesnesi
     * @param code HTTP durum kodu (200, 400, 401, 403, 404, 500 vb.)
     * @param json JSON formatında yanıt gövdesi
     */
    private static void sendResponse(HttpExchange ex, int code, String json) throws IOException {
        corsHeaders(ex); // Her yanıta CORS header'ı ekle
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8); // JSON'u UTF-8 byte dizisine çevir
        ex.sendResponseHeaders(code, bytes.length); // Durum kodu ve içerik uzunluğunu gönder
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close(); // Yanıt gövdesini yaz ve akışı kapat
    }

    /**
     * HTTP istek gövdesini (body) okur ve String olarak döndürür.
     * POST/PUT isteklerinde gönderilen JSON verisini almak için kullanılır.
     * NOT: InputStream sadece bir kez okunabilir — tekrar çağrılamaz!
     */
    private static String readBody(HttpExchange ex) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null)
            sb.append(line); // Satır satır oku
        return sb.toString();
    }

    /**
     * Basit JSON ayrıştırıcı — JSON string'ten belirtilen anahtarın değerini
     * çıkarır.
     * Tam bir JSON parser değildir; sadece düz (nested olmayan) JSON için çalışır.
     * 
     * @param json JSON formatındaki string (isteğin body'si)
     * @param key  Aranacak anahtar adı (ör: "email", "sifre")
     * @return Anahtarın değeri veya bulunamazsa null
     */
    private static String jsonValue(String json, String key) {
        String search = "\"" + key + "\""; // Anahtarı tırnak içinde ara
        int idx = json.indexOf(search);
        if (idx == -1)
            return null; // Bulunamazsa null
        int colon = json.indexOf(":", idx); // ":" karakterini bul
        int start = colon + 1;
        // Değerden önceki boşlukları atla
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t'))
            start++;
        if (json.charAt(start) == '\"') {
            // String değer: tırnak içinde (ör: "email":"test@mail.com")
            start++;
            int end = json.indexOf("\"", start); // Kapanış tırnağını bul
            return json.substring(start, end);
        } else {
            // Sayısal değer: tırnak olmadan (ör: "id":5)
            int end = start;
            // Virgül, süslü parantez veya boşluk gelene kadar oku
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ' '
                    && json.charAt(end) != '\n' && json.charAt(end) != '\r')
                end++;
            return json.substring(start, end).trim();
        }
    }

    /**
     * Şifre hash'leme — SHA-256 algoritması ile tek yönlü hash üretir.
     * Kullanıcı şifreleri veritabanına açık metin yerine hash olarak kaydedilir.
     * NOT: Salt (tuz) kullanılmıyor — production'da bcrypt veya Argon2 tercih
     * edilmeli.
     * 
     * @param password Açık metin şifre
     * @return 64 karakterlik hex hash string'i
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256"); // SHA-256 algoritmasını al
            byte[] hash = d.digest(password.getBytes(StandardCharsets.UTF_8)); // Şifreyi hash'le
            StringBuilder sb = new StringBuilder();
            // Byte dizisini hex string'e çevir (ör: 0x4a → "4a")
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1)
                    sb.append('0');
                sb.append(h);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // ─── Kimlik Doğrulama ve Yetki Kontrol Yardımcıları
    // ───────────────────────────

    /**
     * JWT token'ı doğrular ve kullanıcı bilgilerini döndürür.
     * POST/PUT/DELETE isteklerinde ek olarak CSRF token kontrolü yapar.
     * 
     * @return String[4] = {kullanici_id, email, rol, csrf} — geçersizse null
     */
    private static String[] authUser(HttpExchange ex) {
        // Authorization header'ından Bearer token'ı al
        String h = ex.getRequestHeaders().getFirst("Authorization");
        if (h == null || !h.startsWith("Bearer "))
            return null; // Header yoksa veya format yanlışsa
        String[] u = JwtUtil.verify(h.substring(7)); // "Bearer " önekini kaldırıp token'ı doğrula
        if (u == null)
            return null; // Token geçersiz veya süresi dolmuş

        // CSRF Koruması: Sadece durumu değiştiren (state-changing) isteklerde kontrol
        // et
        // GET istekleri veri okuma yaptığı için CSRF kontrolü gerekmez
        String method = ex.getRequestMethod();
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            String csrfHeader = ex.getRequestHeaders().getFirst("X-CSRF-Token"); // Frontend'den gelen CSRF token
            // JWT içindeki CSRF ile header'daki CSRF eşleşmeli
            if (csrfHeader == null || !csrfHeader.equals(u[3])) {
                System.out.println("⚠️ CSRF Koruması Devreye Girdi: Token eksik veya hatalı!");
                return null; // CSRF uyuşmazsa istek reddedilir
            }
        }
        return u; // Doğrulanmış kullanıcı bilgileri
    }

    /**
     * Sadece admin rolüne sahip kullanıcıların erişmesine izin verir.
     * 
     * @return true = admin yetkilendirildi, false = yetkisiz (yanıt gönderildi)
     */
    private static boolean requireAdmin(HttpExchange ex) throws IOException {
        String[] u = authUser(ex); // Önce kimlik doğrula
        if (u == null) {
            sendResponse(ex, 401, errJson("Giriş yapmanız gerekiyor!", "UNAUTHORIZED"));
            return false;
        }
        if (!"admin".equals(u[2])) {
            sendResponse(ex, 403, errJson("Bu işlem için yetkiniz yok!", "FORBIDDEN"));
            return false;
        }
        return true;
    }

    /**
     * Giriş yapmış herhangi bir kullanıcının erişmesine izin verir (rol fark
     * etmez).
     * 
     * @return true = kimlik doğrulandı, false = giriş yapılmamış
     */
    private static boolean requireAuth(HttpExchange ex) throws IOException {
        if (authUser(ex) == null) {
            sendResponse(ex, 401, errJson("Giriş yapmanız gerekiyor!", "UNAUTHORIZED"));
            return false;
        }
        return true;
    }

    /**
     * Belirtilen rollerden birine sahip kullanıcıların erişmesine izin verir.
     * 
     * @param roles İzin verilen rol listesi (ör: "admin", "antrenor")
     * @return true = yetkilendirildi, false = yetkisiz
     */
    private static boolean requireRole(HttpExchange ex, String... roles) throws IOException {
        String[] u = authUser(ex);
        if (u == null) {
            sendResponse(ex, 401, errJson("Giriş yapmanız gerekiyor!", "UNAUTHORIZED"));
            return false;
        }
        for (String r : roles)
            if (r.equals(u[2]))
                return true; // Kullanıcının rolü izin verilenlerden biriyse OK
        sendResponse(ex, 403, errJson("Bu işlem için yetkiniz yok!", "FORBIDDEN"));
        return false;
    }

    /**
     * Standart hata JSON yanıtı üretir.
     * 
     * @param msg Kullanıcıya gösterilecek hata mesajı
     * @param kod Hata kodu (UNAUTHORIZED, FORBIDDEN, ERROR vb.)
     * @return JSON formatında hata yanıtı
     */
    private static String errJson(String msg, String kod) {
        return "{\"basarili\":false,\"mesaj\":\"" + msg + "\",\"kod\":\"" + kod + "\"}";
    }

    // ═══════════════════════════════════════════════════
    // KAYIT — POST /api/kayit (Public — token gerekmez)
    // Yeni kullanıcı kaydı oluşturur (kullanicilar tablosuna INSERT)
    // ═══════════════════════════════════════════════════
    static class KayitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS preflight
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            } // Sadece POST kabul

            String body = readBody(ex); // İstek gövdesini oku
            String ad = jsonValue(body, "ad"); // Ad
            String soyad = jsonValue(body, "soyad"); // Soyad
            String email = jsonValue(body, "email"); // E-posta
            String sifre = jsonValue(body, "sifre"); // Şifre (açık metin)
            String rol = jsonValue(body, "rol"); // Rol (genelde boş gelir)
            String telefon = jsonValue(body, "telefon"); // Telefon (opsiyonel)
            String cinsiyet = jsonValue(body, "cinsiyet"); // Cinsiyet (opsiyonel)
            String dogumTarihi = jsonValue(body, "dogum_tarihi"); // Doğum tarihi (opsiyonel)

            // Zorunlu alan kontrolü
            if (ad == null || soyad == null || email == null || sifre == null) {
                sendResponse(ex, 400, errJson("Eksik alanlar!", "BAD_REQUEST"));
                return;
            }
            if (sifre.length() < 6) { // Şifre uzunluk kontrolü
                sendResponse(ex, 400, errJson("Şifre en az 6 karakter olmalıdır!", "BAD_REQUEST"));
                return;
            }
            if (!email.contains("@") || !email.contains(".")) { // Basit email format kontrolü
                sendResponse(ex, 400, errJson("Geçersiz email formatı!", "BAD_REQUEST"));
                return;
            }
            // İlk kayıtta her zaman 'kullanici' rolü atanır; abonelik alınınca 'uye' olur
            if (rol == null || rol.isEmpty() || "uye".equals(rol))
                rol = "kullanici";
            String sifreHash = hashPassword(sifre); // Şifreyi SHA-256 ile hashle

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                // E-posta tekrarı kontrolü — aynı email ile kayıt engellenir
                PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM kullanicilar WHERE email=?");
                check.setString(1, email.toLowerCase()); // Küçük harfe çevirerek karşılaştır
                ResultSet rs = check.executeQuery();
                boolean emailVar = rs.next() && rs.getInt(1) > 0; // Zaten var mı?
                rs.close();
                check.close();
                if (emailVar) {
                    sendResponse(ex, 409, errJson("Bu email zaten kayıtlı!", "CONFLICT"));
                    return;
                } // 409 Conflict

                // 'kullanici' rolünü bul; yoksa otomatik ekle (migration çalışmamış olabilir)
                PreparedStatement rolStmt = conn.prepareStatement("SELECT role_id FROM roller WHERE rol_adi=?");
                rolStmt.setString(1, rol);
                ResultSet rolRs = rolStmt.executeQuery();
                int rolId = -1;
                if (rolRs.next())
                    rolId = rolRs.getInt("role_id"); // Rol ID'sini al
                rolRs.close();
                rolStmt.close();
                if (rolId < 0) {
                    // 'kullanici' rolü tabloda yok → otomatik oluştur
                    PreparedStatement insRol = conn.prepareStatement(
                            "INSERT INTO roller(rol_adi) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
                    insRol.setString(1, rol);
                    insRol.executeUpdate();
                    ResultSet rk = insRol.getGeneratedKeys(); // Oluşturulan role_id
                    if (rk.next())
                        rolId = rk.getInt(1);
                    rk.close();
                    insRol.close();
                    System.out.println("✅ '" + rol + "' rolü roller tablosuna otomatik eklendi. role_id=" + rolId);
                }

                // Kullanıcı kaydını veritabanına ekle
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO kullanicilar (ad,soyad,email,sifre_hash,telefon,cinsiyet,dogum_tarihi,rol_id,durum) VALUES(?,?,?,?,?,?,?,?,N'aktif')",
                        Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, ad);
                stmt.setString(2, soyad); // Ad, soyad
                stmt.setString(3, email.toLowerCase());
                stmt.setString(4, sifreHash); // Email (küçük harf), şifre hash
                stmt.setString(5, telefon != null && !telefon.isEmpty() ? telefon : null); // Telefon (null olabilir)
                stmt.setString(6, cinsiyet != null && !cinsiyet.isEmpty() ? cinsiyet : null); // Cinsiyet (null
                                                                                              // olabilir)
                if (dogumTarihi != null && !dogumTarihi.isEmpty())
                    stmt.setDate(7, java.sql.Date.valueOf(dogumTarihi)); // Doğum tarihi
                else
                    stmt.setNull(7, java.sql.Types.DATE); // Doğum tarihi verilmediyse NULL
                stmt.setInt(8, rolId); // Rol ID
                stmt.executeUpdate(); // INSERT çalıştır

                ResultSet gk = stmt.getGeneratedKeys(); // Oluşturulan kullanici_id
                int yeniId = -1;
                if (gk.next())
                    yeniId = gk.getInt(1);
                gk.close();
                stmt.close();

                // 'kullanici' rolünde kayıt edilir; uyeler tablosuna EKLENMEZ.
                // Abonelik satın alınca uyeler tablosuna eklenip rol 'uye' yapılır.

                System.out.println("✅ Kayıt: " + ad + " " + soyad + " (" + email + ")"); // Konsola log
                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Kayıt başarılı!\"}");
            } catch (SQLException e) {
                System.out.println("❌ Kayıt hatası: " + e.getMessage());
                sendResponse(ex, 500, errJson("Veritabanı hatası: " + e.getMessage().replace("\"", "'"), "DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // GİRİŞ — POST /api/giris (Public + Rate Limiting)
    // Kullanıcı email+şifre ile giriş yapar, JWT token döndürülür
    // ═══════════════════════════════════════════════════
    static class GirisHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            } // POST zorunlu

            String clientIp = ex.getRemoteAddress().getAddress().getHostAddress(); // İstemcinin IP adresi

            // Rate limit kontrolü — 5 başarısız denemeden sonra 15 dk engellenir
            if (RateLimiter.blocked(clientIp)) {
                long wait = RateLimiter.waitSec(clientIp); // Kalan bekleme süresi
                System.out.println("🛑 Rate limit: " + clientIp);
                sendResponse(ex, 429, String.format(
                        "{\"basarili\":false,\"mesaj\":\"Çok fazla başarısız deneme. %d saniye bekleyin!\",\"kod\":\"RATE_LIMITED\"}",
                        wait));
                return;
            }

            String body = readBody(ex); // İstek gövdesini oku
            String email = jsonValue(body, "email"); // E-posta
            String sifre = jsonValue(body, "sifre"); // Şifre (açık metin)

            if (email == null || sifre == null) {
                sendResponse(ex, 400, errJson("Email ve şifre gerekli!", "BAD_REQUEST"));
                return;
            } // Zorunlu alan

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                // Kullanıcıyı email ile bul (rol bilgisiyle birlikte)
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT k.kullanici_id,k.ad,k.soyad,k.email,k.telefon,k.sifre_hash,k.durum,r.rol_adi " +
                                "FROM kullanicilar k JOIN roller r ON k.rol_id=r.role_id WHERE k.email=?");
                stmt.setString(1, email.toLowerCase()); // Küçük harfe çevirerek ara
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) { // Kullanıcı bulunamadı
                    rs.close();
                    stmt.close();
                    RateLimiter.fail(clientIp); // Başarısız deneme kaydet
                    sendResponse(ex, 401, errJson("Email veya şifre yanlış!", "INVALID_CREDENTIALS"));
                    return;
                }

                // DB'den kullanıcı bilgilerini oku
                int id = rs.getInt("kullanici_id"); // Kullanıcı ID
                String ad = rs.getString("ad"); // Ad
                String soyad = rs.getString("soyad"); // Soyad
                String dbEmail = rs.getString("email"); // DB'deki email
                String telefon = rs.getString("telefon"); // Telefon
                String kayitliHash = rs.getString("sifre_hash"); // DB'deki şifre hash
                String durum = rs.getString("durum"); // Hesap durumu
                String rolAdi = rs.getString("rol_adi"); // Rol adı
                rs.close();
                stmt.close();

                // Hesap aktif değilse girişi engelle
                if (!"aktif".equals(durum)) {
                    RateLimiter.fail(clientIp);
                    sendResponse(ex, 401, errJson("Bu hesap devre dışı bırakılmış!", "ACCOUNT_DISABLED"));
                    return;
                }

                // Şifre doğrulama — girilen şifrenin hash'i DB'deki hash ile karşılaştırılır
                if (!hashPassword(sifre).equals(kayitliHash)) {
                    RateLimiter.fail(clientIp); // Başarısız deneme kaydet
                    System.out.println("🔐 Başarısız giriş: " + email + " | IP: " + clientIp);
                    sendResponse(ex, 401, errJson("Email veya şifre yanlış!", "INVALID_CREDENTIALS"));
                    return;
                }

                // ✅ Başarılı giriş — rate limiter'ı sıfırla
                RateLimiter.ok(clientIp);

                // Son giriş tarihini güncelle
                PreparedStatement upd = conn.prepareStatement(
                        "UPDATE kullanicilar SET son_giris=GETDATE() WHERE kullanici_id=?");
                upd.setInt(1, id);
                upd.executeUpdate();
                upd.close();

                String csrf = UUID.randomUUID().toString(); // CSRF token üret
                String token = JwtUtil.generate(id, dbEmail, rolAdi, csrf); // JWT token üret

                // Başarılı giriş yanıtı — token + kullanıcı bilgileri
                String json = String.format(
                        "{\"basarili\":true,\"mesaj\":\"Giriş başarılı!\",\"token\":\"%s\",\"csrfToken\":\"%s\"," +
                                "\"kullanici\":{\"id\":%d,\"ad\":\"%s\",\"soyad\":\"%s\",\"email\":\"%s\",\"rol\":\"%s\",\"telefon\":\"%s\"}}",
                        token, csrf, id, ad, soyad, dbEmail, rolAdi, telefon != null ? telefon : "");

                System.out.println("✅ Giriş: " + ad + " " + soyad + " (" + rolAdi + ")"); // Konsola log
                sendResponse(ex, 200, json);

            } catch (SQLException e) {
                System.out.println("❌ Giriş hatası: " + e.getMessage());
                sendResponse(ex, 500, errJson("Sunucu hatası!", "SERVER_ERROR"));
            }
        }
    }

    // ===================================================================
    // DOGRULA — GET /api/dogrula (Oturum yenileme / Token refresh)
    // Mevcut JWT token'ı doğrular ve yeni token üretir (session uzatma)
    // ===================================================================
    static class DogrulaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            String[] u = authUser(ex); // JWT'den kullanıcı bilgilerini çöz
            if (u == null) {
                // Token geçersiz veya süresi dolmuş — 401 Unauthorized
                sendResponse(ex, 401, "{\"basarili\":false,\"mesaj\":\"Token gecersiz veya suresi dolmus!\"}");
                return;
            }
            try {
                int kullaniciId = Integer.parseInt(u[0]); // Kullanıcı ID
                String email = u[1]; // E-posta
                String rol = u[2]; // Rol adı

                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                // Kullanıcının güncel bilgilerini DB'den çek (token'daki bilgi eski olabilir)
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT k.kullanici_id,k.ad,k.soyad,k.email,r.rol_adi,k.telefon " +
                                "FROM kullanicilar k JOIN roller r ON k.rol_id=r.role_id " +
                                "WHERE k.kullanici_id=?");
                ps.setInt(1, kullaniciId); // Kullanıcı ID parametresi
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    // Kullanıcı bulundu — yeni CSRF token ve JWT üret
                    String csrf = UUID.randomUUID().toString(); // Yeni CSRF token
                    String newToken = JwtUtil.generate( // Yeni JWT token üret
                            rs.getInt("kullanici_id"),
                            rs.getString("email"),
                            rs.getString("rol_adi"), csrf);
                    String telefon = rs.getString("telefon"); // Telefon bilgisi
                    // Kullanıcı bilgilerini ve yeni token'ı JSON olarak döndür
                    String json = String.format(
                            "{\"basarili\":true,\"token\":\"%s\",\"csrfToken\":\"%s\"," +
                                    "\"kullanici\":{\"id\":%d,\"ad\":\"%s\",\"soyad\":\"%s\",\"email\":\"%s\",\"rol\":\"%s\",\"telefon\":\"%s\"}}",
                            newToken, csrf, rs.getInt("kullanici_id"),
                            rs.getString("ad"), rs.getString("soyad"),
                            rs.getString("email"), rs.getString("rol_adi"),
                            telefon != null ? telefon : "");
                    rs.close();
                    ps.close();
                    sendResponse(ex, 200, json); // Başarılı yanıt
                } else {
                    rs.close();
                    ps.close();
                    // Token geçerli ama kullanıcı DB'de yok (silinmiş olabilir)
                    sendResponse(ex, 404, "{\"basarili\":false,\"mesaj\":\"Kullanici bulunamadi!\"}");
                }
            } catch (Exception e) {
                sendResponse(ex, 500, errJson(e.getMessage(), "ERROR")); // Genel hata
            }
        }
    }

    // ===================================================================
    // UYELER — GET /api/uyeler [Admin]
    // Tüm üyeleri istatistikleriyle birlikte JSON olarak döndürür
    // ===================================================================
    static class UyelerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi kontrolü

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                Statement stmt = conn.createStatement();

                // 1. Üye istatistiklerini hesapla (dashboard kartları için)
                ResultSet rsStats = stmt.executeQuery(
                        "SELECT " +
                                "  (SELECT COUNT(*) FROM uyeler) AS toplam, " + // Toplam üye sayısı
                                "  (SELECT COUNT(DISTINCT uye_id) FROM uye_abonelikleri WHERE durum=N'aktif' AND bitis_tarihi >= CAST(GETDATE() AS DATE)) AS aktif, "
                                + // Aktif aboneliği olan üye sayısı
                                "  (SELECT COUNT(DISTINCT uye_id) FROM uye_abonelikleri WHERE durum=N'suresi_doldu' OR bitis_tarihi < CAST(GETDATE() AS DATE)) AS suresiDolan, "
                                + // Süresi dolmuş üye sayısı
                                "  (SELECT COUNT(*) FROM kullanicilar k JOIN roller r ON k.rol_id=r.role_id WHERE r.rol_adi=N'uye' AND MONTH(k.kayit_tarihi)=MONTH(GETDATE()) AND YEAR(k.kayit_tarihi)=YEAR(GETDATE())) AS buAyYeni"); // Bu
                                                                                                                                                                                                                                       // ay
                                                                                                                                                                                                                                       // kaydolan
                                                                                                                                                                                                                                       // yeni
                                                                                                                                                                                                                                       // üye
                                                                                                                                                                                                                                       // sayısı

                // İstatistik değerlerini oku
                int sToplam = 0, sAktif = 0, sDolan = 0, sYeni = 0;
                if (rsStats.next()) {
                    sToplam = rsStats.getInt("toplam"); // Toplam üye
                    sAktif = rsStats.getInt("aktif"); // Aktif üye
                    sDolan = rsStats.getInt("suresiDolan"); // Süresi dolan
                    sYeni = rsStats.getInt("buAyYeni"); // Bu ay yeni kayıt
                }
                rsStats.close();

                // 2. Tüm üyelerin detaylı listesini getir
                // LEFT JOIN ile abonelik bilgisi olmayan üyeler de listelenir
                // ROW_NUMBER() ile her üyenin sadece en son aboneliği alınır (rn=1)
                ResultSet rs = stmt.executeQuery(
                        "SELECT k.kullanici_id,k.ad,k.soyad,k.email,k.telefon,k.cinsiyet," +
                                "k.dogum_tarihi,r.rol_adi,k.durum,k.kayit_tarihi," +
                                "u.uyelik_no," + // Üyelik numarası
                                "p.plan_adi AS abonelik_plan," + // Abonelik plan adı
                                "a.bitis_tarihi AS abonelik_bitis," + // Abonelik bitiş tarihi
                                "a.durum AS abonelik_durum " + // Abonelik durumu
                                "FROM kullanicilar k " +
                                "JOIN roller r ON k.rol_id=r.role_id " + // Rol bilgisi için JOIN
                                "LEFT JOIN uyeler u ON k.kullanici_id=u.kullanici_id " + // Üye profili
                                "LEFT JOIN (SELECT uye_id,plan_id,bitis_tarihi,durum," + // Son abonelik (window
                                                                                         // function)
                                "ROW_NUMBER()OVER(PARTITION BY uye_id ORDER BY baslangic_tarihi DESC) AS rn " +
                                "FROM uye_abonelikleri) a ON u.uye_id=a.uye_id AND a.rn=1 " + // Sadece en son abonelik
                                "LEFT JOIN uye_planlari p ON a.plan_id=p.plan_id " + // Plan adını getir
                                "WHERE r.rol_adi=N'uye' ORDER BY k.kullanici_id"); // Sadece 'uye' rolündekiler

                // JSON yanıtı oluştur — önce stats sonra üye dizisi
                StringBuilder json = new StringBuilder("{\"stats\":{");
                json.append(String.format("\"toplam\":%d,\"aktif\":%d,\"suresiDolan\":%d,\"buAyYeni\":%d",
                        sToplam, sAktif, sDolan, sYeni));
                json.append("},\"uyeler\":[");

                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    // Her alan için null kontrolü — null ise boş string kullan
                    String telefon = rs.getString("telefon");
                    String cinsiyet = rs.getString("cinsiyet");
                    String uyelikNo = rs.getString("uyelik_no");
                    String abPlan = rs.getString("abonelik_plan");
                    String abBitis = rs.getString("abonelik_bitis");
                    String abDurum = rs.getString("abonelik_durum");
                    Timestamp kayitTs = rs.getTimestamp("kayit_tarihi");
                    String kayitTarihi = kayitTs != null ? kayitTs.toString().substring(0, 10) : ""; // Sadece tarih
                                                                                                     // kısmı
                                                                                                     // (YYYY-MM-DD)
                    java.sql.Date dogum = rs.getDate("dogum_tarihi");
                    String dogumStr = dogum != null ? dogum.toString() : ""; // Doğum tarihi
                    // Üye bilgilerini JSON nesnesine dönüştür
                    json.append(String.format(
                            "{\"id\":%d,\"ad\":\"%s\",\"soyad\":\"%s\",\"email\":\"%s\"," +
                                    "\"telefon\":\"%s\",\"cinsiyet\":\"%s\",\"dogumTarihi\":\"%s\"," +
                                    "\"uyelikNo\":\"%s\"," +
                                    "\"abonelikPlan\":\"%s\",\"abonelikBitis\":\"%s\",\"abonelikDurum\":\"%s\"," +
                                    "\"rol\":\"%s\",\"durum\":\"%s\",\"kayitTarihi\":\"%s\"}",
                            rs.getInt("kullanici_id"), rs.getString("ad"), rs.getString("soyad"), rs.getString("email"),
                            telefon != null ? telefon : "", cinsiyet != null ? cinsiyet : "", dogumStr,
                            uyelikNo != null ? uyelikNo : "",
                            abPlan != null ? abPlan : "", abBitis != null ? abBitis : "",
                            abDurum != null ? abDurum : "",
                            rs.getString("rol_adi"), rs.getString("durum"), kayitTarihi));
                    first = false;
                }
                json.append("]}");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString()); // JSON yanıtını gönder
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}"); // SQL hatası
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // KULLANICILAR — GET /api/kullanicilar [Admin]
    // Tüm kullanıcıları (admin, üye, antrenör) basit liste olarak döndürür
    // ═══════════════════════════════════════════════════
    static class KullanicilarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi kontrolü

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                Statement stmt = conn.createStatement();
                // Tüm kullanıcıları rol bilgisiyle birlikte getir
                ResultSet rs = stmt.executeQuery(
                        "SELECT k.kullanici_id, k.ad, k.soyad, k.email, r.rol_adi, k.durum " +
                                "FROM kullanicilar k JOIN roller r ON k.rol_id=r.role_id " +
                                "ORDER BY k.kullanici_id"); // ID sırasına göre sırala

                // JSON dizisi oluştur
                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    // Her kullanıcı için JSON nesnesi — özel karakterler escape edilir
                    json.append(String.format(
                            "{\"id\":%d,\"ad\":\"%s\",\"soyad\":\"%s\",\"email\":\"%s\",\"rol\":\"%s\",\"durum\":\"%s\"}",
                            rs.getInt("kullanici_id"), rs.getString("ad").replace("\"", "\\\""),
                            rs.getString("soyad").replace("\"", "\\\""),
                            rs.getString("email").replace("\"", "\\\""), rs.getString("rol_adi"),
                            rs.getString("durum")));
                    first = false;
                }
                json.append("]");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString()); // JSON yanıtını gönder
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}"); // SQL hatası
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ÜYE GÜNCELLE — POST /api/uye-guncelle [Admin]
    // ═══════════════════════════════════════════════════
    static class UyeGuncelleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            // OPTIONS isteği → CORS preflight yanıtı gönder (tarayıcı ön kontrolü)
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (!requireAdmin(ex))
                return; // Sadece admin yetkisi gerekli — değilse 403 döner
            // Sadece POST metodu kabul edilir
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            }

            String body = readBody(ex); // İstek gövdesini (JSON) oku
            String idStr = jsonValue(body, "id"); // Güncellenecek üyenin ID'si
            String ad = jsonValue(body, "ad"); // Yeni ad
            String soyad = jsonValue(body, "soyad"); // Yeni soyad
            String email = jsonValue(body, "email"); // Yeni email
            String telefon = jsonValue(body, "telefon"); // Yeni telefon
            String durum = jsonValue(body, "durum"); // Yeni durum (aktif/pasif)

            // Zorunlu alanların kontrolü — eksikse 400 Bad Request döndür
            if (idStr == null || ad == null || soyad == null || email == null) {
                sendResponse(ex, 400, errJson("Eksik alanlar!", "BAD_REQUEST"));
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısını al
                // Kullanicilar tablosunda ilgili kaydı güncelle
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE kullanicilar SET ad=?,soyad=?,email=?,telefon=?,durum=? WHERE kullanici_id=?");
                stmt.setString(1, ad);
                stmt.setString(2, soyad); // Ad ve soyadı set et
                stmt.setString(3, email.toLowerCase()); // Email'i küçük harfe çevirerek set et
                stmt.setString(4, telefon != null ? telefon : ""); // Telefon null ise boş string ata
                stmt.setString(5, durum != null ? durum : "aktif"); // Durum null ise varsayılan "aktif"
                stmt.setInt(6, Integer.parseInt(idStr)); // WHERE koşulu: kullanici_id
                int n = stmt.executeUpdate();
                stmt.close(); // Sorguyu çalıştır ve kaç satır etkilendiğini al

                // Etkilenen satır varsa başarılı, yoksa üye bulunamadı
                if (n > 0) {
                    System.out.println("✅ Üye güncellendi: " + ad + " " + soyad);
                    sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Üye güncellendi!\"}");
                } else {
                    sendResponse(ex, 404, errJson("Üye bulunamadı!", "NOT_FOUND"));
                }
            } catch (SQLException e) {
                System.out.println("❌ Güncelleme hatası: " + e.getMessage()); // Hatayı konsola logla
                sendResponse(ex, 500, errJson("Veritabanı hatası", "DB_ERROR")); // 500 hatası döndür
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // PROFİL GÜNCELLE — POST /api/profil-guncelle [Giriş Yapmış Herkes]
    // ═══════════════════════════════════════════════════
    static class ProfilGuncelleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            // OPTIONS isteği → CORS preflight yanıtı
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (!requireAuth(ex))
                return; // Herhangi bir giriş yapmış kullanıcı gerekli
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            }

            String[] u = authUser(ex); // JWT'den kullanıcı bilgilerini al
            int kId = Integer.parseInt(u[0]); // Giriş yapan kullanıcının ID'si

            String body = readBody(ex); // İstek gövdesini oku
            String ad = jsonValue(body, "ad"); // Yeni ad
            String soyad = jsonValue(body, "soyad"); // Yeni soyad
            String telefon = jsonValue(body, "telefon"); // Yeni telefon

            // Zorunlu alanların kontrolü
            if (ad == null || soyad == null) {
                sendResponse(ex, 400, errJson("Eksik alanlar!", "BAD_REQUEST"));
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısını al
                // Kullanıcı kendi profilini günceller — sadece ad, soyad ve telefon değişebilir
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE kullanicilar SET ad=?,soyad=?,telefon=? WHERE kullanici_id=?");
                stmt.setString(1, ad);
                stmt.setString(2, soyad); // Ad ve soyadı set et
                stmt.setString(3, telefon != null ? telefon : ""); // Telefon null ise boş string
                stmt.setInt(4, kId); // WHERE: kendi kullanıcı ID'si
                int n = stmt.executeUpdate();
                stmt.close(); // Sorguyu çalıştır

                // Başarılı güncelleme veya kullanıcı bulunamadı
                if (n > 0) {
                    System.out.println("✅ Profil güncellendi: ID=" + kId);
                    sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Profil güncellendi!\"}");
                } else {
                    sendResponse(ex, 404, errJson("Kullanıcı bulunamadı!", "NOT_FOUND"));
                }
            } catch (SQLException e) {
                sendResponse(ex, 500, errJson("Veritabanı hatası", "DB_ERROR")); // SQL hatası
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ÜYE SİL — POST /api/uye-sil [Admin]
    // ═══════════════════════════════════════════════════
    static class UyeSilHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            // OPTIONS isteği → CORS preflight yanıtı
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (!requireAdmin(ex))
                return; // Sadece admin yetkisi gerekli
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            }

            String idStr = jsonValue(readBody(ex), "id"); // Silinecek üyenin kullanıcı ID'sini al
            if (idStr == null) {
                sendResponse(ex, 400, errJson("ID gerekli!", "BAD_REQUEST"));
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısını al
                int uid = Integer.parseInt(idStr); // String ID'yi integer'a çevir

                // Önce uyeler tablosunda bu kullanıcıya ait uye_id'yi bul
                PreparedStatement fu = conn.prepareStatement("SELECT uye_id FROM uyeler WHERE kullanici_id=?");
                fu.setInt(1, uid);
                ResultSet ur = fu.executeQuery();
                int uyeId = -1;
                if (ur.next())
                    uyeId = ur.getInt("uye_id"); // Üye ID'sini al (yoksa -1 kalır)
                ur.close();
                fu.close();

                // Üye kaydı varsa, bağlı tüm verileri sırasıyla sil (FK bütünlüğü için sıra
                // önemli)
                if (uyeId > 0) {
                    // 1. Ödemeleri sil — üyenin aboneliklerine bağlı ödeme kayıtları
                    PreparedStatement d1 = conn.prepareStatement(
                            "DELETE FROM odemeler WHERE abonelik_id IN(SELECT abonelik_id FROM uye_abonelikleri WHERE uye_id=?)");
                    d1.setInt(1, uyeId);
                    d1.executeUpdate();
                    d1.close();
                    // 2. Ders rezervasyonlarını sil
                    PreparedStatement d2 = conn.prepareStatement("DELETE FROM sinif_rezervasyonlari WHERE uye_id=?");
                    d2.setInt(1, uyeId);
                    d2.executeUpdate();
                    d2.close();
                    // 3. Abonelik kayıtlarını sil
                    PreparedStatement d3 = conn.prepareStatement("DELETE FROM uye_abonelikleri WHERE uye_id=?");
                    d3.setInt(1, uyeId);
                    d3.executeUpdate();
                    d3.close();
                    // 4. Üye kaydını sil
                    PreparedStatement d4 = conn.prepareStatement("DELETE FROM uyeler WHERE uye_id=?");
                    d4.setInt(1, uyeId);
                    d4.executeUpdate();
                    d4.close();
                }

                // 5. Giriş/çıkış kayıtlarını sil
                PreparedStatement dg = conn.prepareStatement("DELETE FROM giris_cikis_kayitlari WHERE kullanici_id=?");
                dg.setInt(1, uid);
                dg.executeUpdate();
                dg.close();
                // 6. Son olarak kullanıcı kaydını sil
                PreparedStatement dk = conn.prepareStatement("DELETE FROM kullanicilar WHERE kullanici_id=?");
                dk.setInt(1, uid);
                int n = dk.executeUpdate();
                dk.close();

                // Başarılı silme veya üye bulunamadı
                if (n > 0) {
                    System.out.println("✅ Üye silindi: ID=" + uid);
                    sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Üye başarıyla silindi!\"}");
                } else {
                    sendResponse(ex, 404, errJson("Üye bulunamadı!", "NOT_FOUND"));
                }
            } catch (Exception e) {
                System.out.println("❌ Silme hatası: " + e.getMessage()); // Hatayı logla
                sendResponse(ex, 500, errJson("Veritabanı hatası: " + e.getMessage().replace("\"", "'"), "DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // İSTATİSTİKLER — GET /api/istatistikler [Admin]
    // ═══════════════════════════════════════════════════
    static class IstatistiklerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS preflight
            String[] u = authUser(ex); // JWT'den kullanıcı bilgilerini al
            if (u == null) {
                sendResponse(ex, 401, errJson("Yetkisiz", "AUTH"));
                return;
            } // Giriş yapılmamış
            boolean isAdmin = "admin".equals(u[2]); // Kullanıcı admin mi kontrol et

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısını al
                if (isAdmin) {
                    // ── ADMİN İSTATİSTİKLERİ ──
                    Statement stmt = conn.createStatement();
                    // Süresi dolmuş aktif abonelikleri otomatik "suresi_doldu" olarak işaretle
                    stmt.executeUpdate(
                            "UPDATE uye_abonelikleri SET durum = N'suresi_doldu' WHERE durum = N'aktif' AND bitis_tarihi < CAST(GETDATE() AS DATE)");
                    // Toplam üye sayısını hesapla (roller tablosunda rolü 'uye' olanlar)
                    ResultSet r1 = stmt.executeQuery(
                            "SELECT COUNT(*) cnt FROM kullanicilar k JOIN roller r ON k.rol_id=r.role_id WHERE r.rol_adi=N'uye'");
                    int toplamUye = r1.next() ? r1.getInt(1) : 0;
                    r1.close();
                    // Toplam tamamlanmış ödeme gelirini hesapla
                    ResultSet r2 = stmt
                            .executeQuery("SELECT ISNULL(SUM(miktar),0) sm FROM odemeler WHERE durum=N'tamamlandi'");
                    double gelir = r2.next() ? r2.getDouble(1) : 0;
                    r2.close(); // Toplam gelir
                    // Aktif abonelik sayısını hesapla
                    ResultSet r3 = stmt.executeQuery("SELECT COUNT(*) cnt FROM uye_abonelikleri WHERE durum=N'aktif'");
                    int aktifAb = r3.next() ? r3.getInt(1) : 0;
                    r3.close(); // Aktif abonelik sayısı
                    // Süresi dolmuş abonelik sayısını hesapla
                    ResultSet r4 = stmt
                            .executeQuery("SELECT COUNT(*) cnt FROM uye_abonelikleri WHERE durum=N'suresi_doldu'");
                    int dolanAb = r4.next() ? r4.getInt(1) : 0;
                    r4.close(); // Süresi dolan abonelik sayısı
                    // İstatistikleri JSON olarak döndür
                    sendResponse(ex, 200,
                            String.format(
                                    "{\"toplamUye\":%d,\"buAyGelir\":%.2f,\"aktifAbonelik\":%d,\"suresiDolan\":%d}",
                                    toplamUye, gelir, aktifAb, dolanAb));
                    stmt.close();
                } else {
                    // ── ÜYE İSTATİSTİKLERİ ──
                    int kid = Integer.parseInt(u[0]); // Giriş yapan kullanıcının ID'si
                    // Üyenin aktif planını, kalan gün sayısını ve katıldığı ders sayısını getir
                    PreparedStatement ps = conn.prepareStatement(
                            "SELECT TOP 1 p.plan_adi, DATEDIFF(DAY, GETDATE(), a.bitis_tarihi) as kalan_gun, " +
                                    "(SELECT COUNT(*) FROM sinif_rezervasyonlari r JOIN uyeler u2 ON r.uye_id=u2.uye_id WHERE u2.kullanici_id=? AND r.durum=N'aktif') as ders_sayisi "
                                    +
                                    "FROM uye_abonelikleri a JOIN uye_planlari p ON a.plan_id=p.plan_id " +
                                    "JOIN uyeler u ON a.uye_id=u.uye_id WHERE u.kullanici_id=? AND a.durum=N'aktif' ORDER BY a.bitis_tarihi DESC");
                    ps.setInt(1, kid);
                    ps.setInt(2, kid); // Parametre olarak kullanıcı ID'si
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        // Aktif plan varsa bilgilerini döndür
                        sendResponse(ex, 200, String.format("{\"aktifPlan\":\"%s\",\"kalanGun\":%d,\"katilanDers\":%d}",
                                rs.getString("plan_adi"), rs.getInt("kalan_gun"), rs.getInt("ders_sayisi")));
                    } else {
                        // Aktif plan yoksa varsayılan değerler döndür
                        sendResponse(ex, 200, "{\"aktifPlan\":\"Yok\",\"kalanGun\":0,\"katilanDers\":0}");
                    }
                    rs.close();
                    ps.close();
                }
            } catch (Exception e) {
                sendResponse(ex, 500, errJson(e.getMessage(), "ERROR"));
            } // Genel hata yakalama
        }
    }

    // ═══════════════════════════════════════════════════
    // ÖDEMELER — GET /api/odemeler [Admin=tümü | Üye=kendi]
    // Admin tüm ödemeleri görür, üye sadece kendi ödemelerini görür
    // ═══════════════════════════════════════════════════
    static class OdemelerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            String[] u = authUser(ex); // JWT doğrulama
            if (u == null) {
                sendResponse(ex, 401, errJson("Giriş yapmanız gerekiyor!", "UNAUTHORIZED"));
                return;
            }

            String rol = u[2]; // Kullanıcının rolü
            // Sadece admin, uye ve kullanici rolleri erişebilir
            if (!"admin".equals(rol) && !"uye".equals(rol) && !"kullanici".equals(rol)) {
                sendResponse(ex, 403, errJson("Bu işlem için yetkiniz yok!", "FORBIDDEN"));
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                PreparedStatement stmt;
                // Temel ödeme sorgusu — odemeler + abonelik + üye + kullanıcı + plan tabloları
                // JOIN
                String baseQ = "SELECT o.odeme_id,k.ad,k.soyad,p.plan_adi,o.miktar," +
                        "o.odeme_yontemi,o.odeme_tarihi,o.durum " +
                        "FROM odemeler o " +
                        "JOIN uye_abonelikleri a ON o.abonelik_id=a.abonelik_id " +
                        "JOIN uyeler u ON a.uye_id=u.uye_id " +
                        "JOIN kullanicilar k ON u.kullanici_id=k.kullanici_id " +
                        "JOIN uye_planlari p ON a.plan_id=p.plan_id ";

                if ("admin".equals(rol)) {
                    stmt = conn.prepareStatement(baseQ + "ORDER BY o.odeme_tarihi DESC"); // Admin: tüm ödemeler
                } else {
                    stmt = conn.prepareStatement(baseQ + "WHERE u.kullanici_id=? ORDER BY o.odeme_tarihi DESC"); // Üye:
                                                                                                                 // kendi
                                                                                                                 // ödemeleri
                    stmt.setInt(1, Integer.parseInt(u[0])); // Kullanıcı ID filtresi
                }

                ResultSet rs = stmt.executeQuery();
                StringBuilder json = new StringBuilder("["); // JSON dizisi oluştur
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    // Ödeme yöntemi kodunu kullanıcı dostu etikete çevir
                    String yon = rs.getString("odeme_yontemi");
                    String yL = "Bilinmiyor";
                    if ("kredi_karti".equals(yon))
                        yL = "Kredi Kartı";
                    else if ("nakit".equals(yon))
                        yL = "Nakit";
                    else if ("havale".equals(yon))
                        yL = "Havale";
                    else if ("online".equals(yon))
                        yL = "Online";
                    Timestamp ts = rs.getTimestamp("odeme_tarihi");
                    String tarih = ts != null ? ts.toString().substring(0, 10) : ""; // Sadece tarih kısmı
                    // JSON nesnesi oluştur
                    json.append(String.format(
                            "{\"id\":%d,\"uye\":\"%s %s\",\"plan\":\"%s\",\"miktar\":%.2f," +
                                    "\"yontem\":\"%s\",\"tarih\":\"%s\",\"durum\":\"%s\"}",
                            rs.getInt("odeme_id"), rs.getString("ad"), rs.getString("soyad"),
                            rs.getString("plan_adi"), rs.getDouble("miktar"), yL, tarih, rs.getString("durum")));
                    first = false;
                }
                json.append("]");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString()); // Yanıtı gönder
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}"); // SQL hatası
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ABONELİKLER — GET /api/abonelikler [Admin=tümü | Üye=kendi]
    // Abonelik listesi döndürür. Önce çoklu abonelik temizliği ve süre kontrolü
    // yapar.
    // ═══════════════════════════════════════════════════
    static class AboneliklerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            String[] u = authUser(ex); // JWT doğrulama
            if (u == null) {
                sendResponse(ex, 401, errJson("Giriş yapmanız gerekiyor!", "UNAUTHORIZED"));
                return;
            }

            String rol = u[2]; // Kullanıcının rolü
            // Sadece admin, uye ve kullanici rolleri erişebilir
            if (!"admin".equals(rol) && !"uye".equals(rol) && !"kullanici".equals(rol)) {
                sendResponse(ex, 403, errJson("Bu işlem için yetkiniz yok!", "FORBIDDEN"));
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı

                // ADIM 1: Her üye için en yüksek abonelik_id'li kaydı bul, diğerlerini 'iptal'
                // yap.
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
                // Temel abonelik sorgusu — abonelik + üye + kullanıcı + plan JOIN
                String baseQ = "SELECT a.abonelik_id,k.ad,k.soyad,p.plan_adi," +
                        "a.baslangic_tarihi,a.bitis_tarihi,a.otomatik_yenile,a.durum " +
                        "FROM uye_abonelikleri a " +
                        "JOIN uyeler u ON a.uye_id=u.uye_id " +
                        "JOIN kullanicilar k ON u.kullanici_id=k.kullanici_id " +
                        "JOIN uye_planlari p ON a.plan_id=p.plan_id ";

                if ("admin".equals(rol)) {
                    stmt = conn.prepareStatement(baseQ + "ORDER BY a.baslangic_tarihi DESC"); // Admin: tüm abonelikler
                } else {
                    stmt = conn.prepareStatement(baseQ + "WHERE u.kullanici_id=? ORDER BY a.baslangic_tarihi DESC"); // Üye:
                                                                                                                     // kendi
                    stmt.setInt(1, Integer.parseInt(u[0])); // Kullanıcı ID filtresi
                }

                ResultSet rs = stmt.executeQuery();
                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    json.append(String.format(
                            "{\"id\":%d,\"uye\":\"%s %s\",\"plan\":\"%s\"," +
                                    "\"baslangic\":\"%s\",\"bitis\":\"%s\",\"otomatik\":%s,\"durum\":\"%s\"}",
                            rs.getInt("abonelik_id"), rs.getString("ad"), rs.getString("soyad"),
                            rs.getString("plan_adi"), rs.getString("baslangic_tarihi"), rs.getString("bitis_tarihi"),
                            rs.getBoolean("otomatik_yenile") ? "true" : "false", rs.getString("durum")));
                    first = false;
                }
                json.append("]");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString());
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // PLANLAR — GET /api/planlar (Public — landing sayfası için)
    // Token gerekmez. Üyelik paketlerini ve aktif üye sayılarını döndürür.
    // ═══════════════════════════════════════════════════
    static class PlanlarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı

                // Lazy Cleanup — her üye için yalnızca en son aboneliği tut, diğerlerini iptal
                // et
                conn.createStatement().executeUpdate(
                        "WITH CTE AS (SELECT abonelik_id, ROW_NUMBER() OVER(PARTITION BY uye_id ORDER BY abonelik_id DESC) as rn "
                                +
                                "FROM uye_abonelikleri) " +
                                "UPDATE uye_abonelikleri SET durum=N'iptal' WHERE abonelik_id IN (SELECT abonelik_id FROM CTE WHERE rn > 1) AND durum <> N'iptal'");

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT p.plan_id,p.plan_adi,p.fiyat,p.sure_ay,p.aciklama,p.ozellikler,p.durum," +
                                "(SELECT COUNT(*) FROM uye_abonelikleri a WHERE a.plan_id=p.plan_id AND a.durum=N'aktif')AS aktifUye "
                                +
                                "FROM uye_planlari p ORDER BY p.fiyat DESC");
                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    String oz = rs.getString("ozellikler");
                    if (oz == null)
                        oz = "[]";
                    json.append(String.format(
                            "{\"id\":%d,\"ad\":\"%s\",\"fiyat\":%.2f,\"sureAy\":%d," +
                                    "\"aciklama\":\"%s\",\"ozellikler\":%s,\"durum\":\"%s\",\"aktifUye\":%d}",
                            rs.getInt("plan_id"), rs.getString("plan_adi"), rs.getDouble("fiyat"), rs.getInt("sure_ay"),
                            rs.getString("aciklama") != null ? rs.getString("aciklama").replace("\"", "'") : "",
                            oz, rs.getString("durum"), rs.getInt("aktifUye")));
                    first = false;
                }
                json.append("]");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString());
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // DERSLER — GET /api/dersler [Hepsi (giriş yapmış)]
    // Ders listesi + haftalık program + rezervasyonları döndürür
    // Antrenör ise sadece kendi derslerini görür
    // ═══════════════════════════════════════════════════
    static class DerslerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            String[] u = authUser(ex); // JWT doğrulama
            if (u == null) {
                sendResponse(ex, 401, "Auth Error");
                return;
            }
            int kId = Integer.parseInt(u[0]); // Kullanıcı ID
            String role = u[2]; // Rol

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı

                // Antrenör ise kendi antrenor_id'sini bul (filtreleme için)
                Integer antrenorId = null;
                if ("antrenor".equals(role)) {
                    PreparedStatement psA = conn
                            .prepareStatement("SELECT antrenor_id FROM antrenorler WHERE kullanici_id=?");
                    psA.setInt(1, kId);
                    ResultSet rsA = psA.executeQuery();
                    if (rsA.next())
                        antrenorId = rsA.getInt("antrenor_id"); // Antrenör ID
                    rsA.close();
                    psA.close();
                }

                // 1. Dersler — siniflar tablosundan ders bilgileri
                String qDersler = "SELECT s.ders_id,s.ders_adi,k.ad+' '+k.soyad AS antrenor," +
                        "s.kontenjan,s.sure_dakika,s.kategori,s.durum " +
                        "FROM siniflar s " +
                        "LEFT JOIN antrenorler a ON s.antrenor_id=a.antrenor_id " +
                        "LEFT JOIN kullanicilar k ON a.kullanici_id=k.kullanici_id";
                if (antrenorId != null)
                    qDersler += " WHERE s.antrenor_id=" + antrenorId;

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(qDersler);
                StringBuilder dJ = new StringBuilder("[");
                boolean f = true;
                while (rs.next()) {
                    if (!f)
                        dJ.append(",");
                    f = false;
                    dJ.append(String.format(
                            "{\"id\":%d,\"ders\":\"%s\",\"antrenor\":\"%s\",\"kontenjan\":%d,\"sure\":%d,\"kategori\":\"%s\",\"durum\":\"%s\"}",
                            rs.getInt("ders_id"), rs.getString("ders_adi"),
                            rs.getString("antrenor") != null ? rs.getString("antrenor") : "",
                            rs.getInt("kontenjan"), rs.getInt("sure_dakika"),
                            rs.getString("kategori") != null ? rs.getString("kategori") : "", rs.getString("durum")));
                }
                dJ.append("]");
                rs.close();

                // 2. Haftalık program — sinif_programlari tablosu (gün sırasına göre)
                String qProg = "SELECT sp.program_id,s.ders_adi,sp.gun," +
                        "CONVERT(VARCHAR(5),sp.baslangic_saati,108)AS bSaat," +
                        "CONVERT(VARCHAR(5),sp.bitis_saati,108)AS bsSaat," +
                        "sp.salon,sp.durum FROM sinif_programlari sp " +
                        "JOIN siniflar s ON sp.ders_id=s.ders_id ";
                if (antrenorId != null)
                    qProg += " WHERE s.antrenor_id=" + antrenorId;
                qProg += " ORDER BY CASE sp.gun WHEN N'Pazartesi' THEN 1 WHEN N'Salı' THEN 2 WHEN N'Çarşamba' THEN 3 " +
                        "WHEN N'Perşembe' THEN 4 WHEN N'Cuma' THEN 5 WHEN N'Cumartesi' THEN 6 WHEN N'Pazar' THEN 7 END";

                ResultSet r2 = stmt.executeQuery(qProg);
                StringBuilder pJ = new StringBuilder("[");
                f = true;
                while (r2.next()) {
                    if (!f)
                        pJ.append(",");
                    f = false;
                    pJ.append(String.format(
                            "{\"id\":%d,\"ders\":\"%s\",\"gun\":\"%s\",\"saat\":\"%s\u2013%s\",\"salon\":\"%s\",\"durum\":\"%s\"}",
                            r2.getInt("program_id"), r2.getString("ders_adi"), r2.getString("gun"),
                            r2.getString("bSaat"), r2.getString("bsSaat"),
                            r2.getString("salon") != null ? r2.getString("salon") : "", r2.getString("durum")));
                }
                pJ.append("]");
                r2.close();

                // 3. Aktif rezervasyonlar — sinif_rezervasyonlari tablosu
                String qRez = "SELECT r.rezervasyon_id,k.ad+' '+k.soyad AS uye,s.ders_adi," +
                        "CONVERT(VARCHAR(10),r.r_tarih,120)AS tarih," +
                        "CONVERT(VARCHAR(5),sp.baslangic_saati,108)+'-'+CONVERT(VARCHAR(5),sp.bitis_saati,108)AS saat,"
                        +
                        "r.durum FROM sinif_rezervasyonlari r " +
                        "JOIN uyeler u ON r.uye_id=u.uye_id " +
                        "JOIN kullanicilar k ON u.kullanici_id=k.kullanici_id " +
                        "JOIN sinif_programlari sp ON r.program_id=sp.program_id " +
                        "JOIN siniflar s ON sp.ders_id=s.ders_id ";
                if (antrenorId != null)
                    qRez += " WHERE s.antrenor_id=" + antrenorId;
                qRez += " ORDER BY r.r_tarih DESC";

                ResultSet r3 = stmt.executeQuery(qRez);
                StringBuilder rJ = new StringBuilder("[");
                f = true;
                while (r3.next()) {
                    if (!f)
                        rJ.append(",");
                    f = false;
                    rJ.append(String.format(
                            "{\"id\":%d,\"uye\":\"%s\",\"ders\":\"%s\",\"tarih\":\"%s\",\"saat\":\"%s\",\"durum\":\"%s\"}",
                            r3.getInt("rezervasyon_id"), r3.getString("uye"), r3.getString("ders_adi"),
                            r3.getString("tarih"), r3.getString("saat"), r3.getString("durum")));
                }
                rJ.append("]");
                r3.close();
                stmt.close();

                sendResponse(ex, 200, "{\"dersler\":" + dJ + ",\"program\":" + pJ + ",\"rezervasyonlar\":" + rJ + "}");
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ANTRENÖRLER — GET /api/antrenorler-detay [Admin | Antrenör]
    // Antrenör listesini uzmanlık, deneyim, ders sayısı, öğrenci sayısı ile
    // döndürür
    // ═══════════════════════════════════════════════════
    static class AntrenorlerDetayHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireRole(ex, "admin", "antrenor"))
                return; // Yetki kontrolü

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                Statement stmt = conn.createStatement();
                // Antrenör bilgileri + verdikleri ders sayısı + öğrenci sayısı (alt sorgu ile)
                ResultSet rs = stmt.executeQuery(
                        "SELECT a.antrenor_id,k.ad+' '+k.soyad AS isim,k.email," +
                                "a.uzmanlik,a.deneyim_yili,a.sertifikalar,a.biyografi,a.durum," +
                                "(SELECT COUNT(*) FROM siniflar s WHERE s.antrenor_id=a.antrenor_id) AS dersCount, " +
                                "(SELECT COUNT(DISTINCT r.uye_id) FROM sinif_rezervasyonlari r " +
                                " JOIN sinif_programlari sp ON r.program_id=sp.program_id " +
                                " JOIN siniflar s ON sp.ders_id=s.ders_id " +
                                " WHERE s.antrenor_id=a.antrenor_id AND r.durum=N'aktif') AS studentCount " +
                                "FROM antrenorler a JOIN kullanicilar k ON a.kullanici_id=k.kullanici_id");
                StringBuilder json = new StringBuilder("[");
                boolean f = true;
                while (rs.next()) {
                    if (!f)
                        json.append(",");
                    f = false;
                    json.append(String.format(
                            "{\"id\":%d,\"isim\":\"%s\",\"email\":\"%s\"," +
                                    "\"uzmanlik\":\"%s\",\"deneyim\":%d,\"sertifikalar\":\"%s\"," +
                                    "\"biyografi\":\"%s\",\"durum\":\"%s\",\"dersCount\":%d,\"studentCount\":%d}",
                            rs.getInt("antrenor_id"), rs.getString("isim"),
                            rs.getString("email") != null ? rs.getString("email") : "",
                            rs.getString("uzmanlik") != null ? rs.getString("uzmanlik").replace("\"", "'") : "",
                            rs.getInt("deneyim_yili"),
                            rs.getString("sertifikalar") != null ? rs.getString("sertifikalar").replace("\"", "'") : "",
                            rs.getString("biyografi") != null ? rs.getString("biyografi").replace("\"", "'") : "",
                            rs.getString("durum"), rs.getInt("dersCount"), rs.getInt("studentCount")));
                }
                json.append("]");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString());
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // GİRİŞ/ÇIKIŞ — GET /api/giris-cikis [Admin | Antrenör]
    // Salına giriş/çıkış kayıtları + aktif rezervasyonları birleştirir (UNION ALL)
    // İstatistik + kayıt listesi JSON olarak döndürür
    // ═══════════════════════════════════════════════════
    static class GirisCikisHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            String[] u = authUser(ex); // JWT doğrulama
            if (u == null) {
                sendResponse(ex, 401, "Auth Error");
                return;
            }
            int kId = Integer.parseInt(u[0]); // Kullanıcı ID
            String role = u[2]; // Rol

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı

                Integer antrenorId = null;
                if ("antrenor".equals(role)) {
                    PreparedStatement psA = conn
                            .prepareStatement("SELECT antrenor_id FROM antrenorler WHERE kullanici_id=?");
                    psA.setInt(1, kId);
                    ResultSet rsA = psA.executeQuery();
                    if (rsA.next())
                        antrenorId = rsA.getInt("antrenor_id");
                    rsA.close();
                    psA.close();
                }

                Statement stmt = conn.createStatement();
                // 1. İstatistikleri hesapla
                String qStats = "SELECT " +
                        "  (SELECT COUNT(*) FROM giris_cikis_kayitlari WHERE CAST(giris_saat AS DATE) = CAST(GETDATE() AS DATE)) + "
                        +
                        "  (SELECT COUNT(*) FROM sinif_rezervasyonlari WHERE CAST(r_tarih AS DATE) = CAST(GETDATE() AS DATE) AND durum=N'aktif') AS bugunGiris, "
                        +
                        "  (SELECT COUNT(*) FROM giris_cikis_kayitlari WHERE durum=N'giris') + " +
                        "  (SELECT COUNT(*) FROM sinif_rezervasyonlari WHERE CAST(r_tarih AS DATE) = CAST(GETDATE() AS DATE) AND durum=N'aktif') AS iceride, "
                        +
                        "  (SELECT COUNT(*) FROM giris_cikis_kayitlari WHERE durum=N'cikis') AS cikisYapan, " +
                        "  (SELECT COUNT(DISTINCT giris_turu) FROM giris_cikis_kayitlari) AS turSayisi";

                // Antrenör ise sadece kendi derslerine girenleri görsün (istatistiklerde de)
                if (antrenorId != null) {
                    qStats = "SELECT " +
                            "  (SELECT COUNT(*) FROM sinif_rezervasyonlari r JOIN sinif_programlari sp ON r.program_id=sp.program_id JOIN siniflar s ON sp.ders_id=s.ders_id WHERE s.antrenor_id="
                            + antrenorId
                            + " AND CAST(r.r_tarih AS DATE)=CAST(GETDATE() AS DATE) AND r.durum=N'aktif') AS bugunGiris, "
                            +
                            "  (SELECT COUNT(*) FROM sinif_rezervasyonlari r JOIN sinif_programlari sp ON r.program_id=sp.program_id JOIN siniflar s ON sp.ders_id=s.ders_id WHERE s.antrenor_id="
                            + antrenorId
                            + " AND CAST(r.r_tarih AS DATE)=CAST(GETDATE() AS DATE) AND r.durum=N'aktif') AS iceride, "
                            +
                            "  0 AS cikisYapan, " +
                            "  1 AS turSayisi";
                }

                ResultSet rsStats = stmt.executeQuery(qStats);
                int bugunGiris = 0, iceride = 0, cikisYapan = 0, turSayisi = 0;
                if (rsStats.next()) {
                    bugunGiris = rsStats.getInt("bugunGiris");
                    iceride = rsStats.getInt("iceride");
                    cikisYapan = rsStats.getInt("cikisYapan");
                    turSayisi = rsStats.getInt("turSayisi");
                }
                rsStats.close();

                // 2. Kayıtları getir
                String qLogs = "SELECT * FROM (" +
                        "  SELECT k.ad + ' ' + k.soyad AS isim, " +
                        "         FORMAT(g.giris_saat, 'HH:mm') AS giris, " +
                        "         ISNULL(FORMAT(g.cikis_saat, 'HH:mm'), '') AS cikis, " +
                        "         g.giris_turu AS turu, " +
                        "         g.durum, " +
                        "         g.giris_saat AS srt, " +
                        "         N'Giriş Kaydı' AS aciklama, " +
                        "         0 AS ant_id " +
                        "  FROM giris_cikis_kayitlari g " +
                        "  JOIN kullanicilar k ON g.kullanici_id = k.kullanici_id " +
                        "  UNION ALL " +
                        "  SELECT k.ad + ' ' + k.soyad AS isim, " +
                        "         FORMAT(r.olusturma_tarihi, 'HH:mm') AS giris, " +
                        "         '' AS cikis, " +
                        "         'normal' AS turu, " +
                        "         'giris' AS durum, " +
                        "         r.olusturma_tarihi AS srt, " +
                        "         s.ders_adi AS aciklama, " +
                        "         s.antrenor_id AS ant_id " +
                        "  FROM sinif_rezervasyonlari r " +
                        "  JOIN uyeler u ON r.uye_id = u.uye_id " +
                        "  JOIN kullanicilar k ON u.kullanici_id = k.kullanici_id " +
                        "  JOIN sinif_programlari sp ON r.program_id = sp.program_id " +
                        "  JOIN siniflar s ON sp.ders_id = s.ders_id " +
                        "  WHERE r.durum = N'aktif' AND CAST(r.r_tarih AS DATE) = CAST(GETDATE() AS DATE) " +
                        ") AS combined ";
                if (antrenorId != null)
                    qLogs += " WHERE ant_id=" + antrenorId;
                qLogs += " ORDER BY srt DESC";

                ResultSet rs = stmt.executeQuery(qLogs);

                StringBuilder json = new StringBuilder("{\"stats\":{");
                json.append(String.format("\"bugunGiris\":%d,\"iceride\":%d,\"cikisYapan\":%d,\"turSayisi\":%d",
                        bugunGiris, iceride, cikisYapan, turSayisi));
                json.append("},\"kayitlar\":[");

                boolean f = true;
                while (rs.next()) {
                    if (!f)
                        json.append(",");
                    f = false;
                    String aciklama = rs.getString("aciklama");
                    String isim = rs.getString("isim");
                    if (aciklama != null && !aciklama.equals("Giriş Kaydı")) {
                        isim += " (" + aciklama + ")";
                    }
                    json.append(String.format(
                            "{\"uye\":\"%s\",\"giris\":\"%s\",\"cikis\":\"%s\",\"turu\":\"%s\",\"durum\":\"%s\"}",
                            isim, rs.getString("giris"), rs.getString("cikis"), rs.getString("turu"),
                            rs.getString("durum")));
                }
                json.append("]}");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString());
            } catch (Exception e) {
                sendResponse(ex, 500, errJson(e.getMessage(), "ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // EKİPMAN — GET /api/ekipman [Admin | Antrenör]
    // Ekipman listesi + bakım geçmişi JSON olarak döndürür
    // ═══════════════════════════════════════════════════
    static class EkipmanHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireRole(ex, "admin", "antrenor"))
                return; // Yetki kontrolü

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                Statement stmt = conn.createStatement();

                // 1. Ekipman listesi — ekipman tablosu

                ResultSet rs = stmt.executeQuery(
                        "SELECT e.ekipman_id,e.ekipman_adi,e.kategori,e.miktar," +
                                "CONVERT(VARCHAR(10),e.satin_alma_tarihi,120)AS satinAlma," +
                                "e.satin_alma_fiyati,e.durum FROM ekipman e ORDER BY e.ekipman_id");
                StringBuilder eJ = new StringBuilder("[");
                boolean f = true;
                while (rs.next()) {
                    if (!f)
                        eJ.append(",");
                    f = false;
                    eJ.append(String.format(
                            "{\"id\":%d,\"ad\":\"%s\",\"kategori\":\"%s\",\"adet\":%d,\"satinAlma\":\"%s\",\"fiyat\":%.2f,\"durum\":\"%s\"}",
                            rs.getInt("ekipman_id"), rs.getString("ekipman_adi"),
                            rs.getString("kategori") != null ? rs.getString("kategori") : "",
                            rs.getInt("miktar"), rs.getString("satinAlma") != null ? rs.getString("satinAlma") : "",
                            rs.getDouble("satin_alma_fiyati"), rs.getString("durum")));
                }
                eJ.append("]");
                rs.close();

                ResultSet r2 = stmt.executeQuery(
                        "SELECT b.bakim_id,e.ekipman_adi," +
                                "CONVERT(VARCHAR(10),b.bakim_tarihi,120)AS tarih," +
                                "b.maliyet,b.yapan_kisi,b.aciklama," +
                                "CONVERT(VARCHAR(10),b.sonraki_bakim,120)AS sonraki,b.durum " +
                                "FROM ekipman_bakimi b JOIN ekipman e ON b.ekipman_id=e.ekipman_id " +
                                "ORDER BY b.bakim_tarihi DESC");
                StringBuilder bJ = new StringBuilder("[");
                f = true;
                while (r2.next()) {
                    if (!f)
                        bJ.append(",");
                    f = false;
                    bJ.append(String.format(
                            "{\"id\":%d,\"ekipman\":\"%s\",\"tarih\":\"%s\",\"maliyet\":%.2f," +
                                    "\"yapan\":\"%s\",\"aciklama\":\"%s\",\"sonraki\":\"%s\",\"durum\":\"%s\"}",
                            r2.getInt("bakim_id"), r2.getString("ekipman_adi"), r2.getString("tarih"),
                            r2.getDouble("maliyet"),
                            r2.getString("yapan_kisi") != null ? r2.getString("yapan_kisi") : "",
                            r2.getString("aciklama") != null ? r2.getString("aciklama").replace("\"", "'") : "",
                            r2.getString("sonraki") != null ? r2.getString("sonraki") : "",
                            r2.getString("durum")));
                }
                bJ.append("]");
                r2.close();
                stmt.close();

                sendResponse(ex, 200, "{\"ekipman\":" + eJ + ",\"bakim\":" + bJ + "}");
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // TEST — GET /api/test (Public — token gerekmez)
    // Veritabanı bağlantısını test eder ve sonuç döndürür
    // ═══════════════════════════════════════════════════
    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            corsHeaders(ex); // CORS header ekle
            boolean ok = DatabaseBaglanti.baglantiTest(); // DB bağlantı testi
            sendResponse(ex, ok ? 200 : 500,
                    ok ? "{\"basarili\":true,\"mesaj\":\"SQL Server bağlantısı aktif!\"}"
                            : "{\"basarili\":false,\"mesaj\":\"SQL Server bağlantısı başarısız!\"}");
        }
    }

    // ═══════════════════════════════════════════════════
    // STATIC FILE HANDLER — Frontend dosyalarını sun (HTML, CSS, JS, görseller)
    // view/ klasöründeki statik dosyaları HTTP üzerinden sunar
    // ═══════════════════════════════════════════════════
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try {
                String path = ex.getRequestURI().getPath(); // İstenen dosya yolu
                if (path.equals("/") || path.isEmpty())
                    path = "/index.html"; // Kök istek → index.html

                // Güvenlik: path traversal koruması — ".." içeren istekleri engelle
                if (path.contains("..")) {
                    String msg = "403 — Erişim reddedildi";
                    ex.sendResponseHeaders(403, msg.getBytes().length);
                    ex.getResponseBody().write(msg.getBytes());
                    ex.getResponseBody().close();
                    return;
                }

                File file = new File("view" + path); // view/ klasöründe dosyayı ara
                if (!file.exists() || !file.isFile()) {
                    file = new File("View" + path); // Fallback: View/ (büyük harf)
                }
                if (!file.exists() || !file.isFile()) {
                    // SPA fallback: Bilinmeyen yollar için index.html döndür
                    // Bu, tarayıcı yeniden yüklemesinde hash-routing sorunlarını çözer
                    File indexFallback = new File("view/index.html");
                    if (!indexFallback.exists())
                        indexFallback = new File("View/index.html");
                    if (indexFallback.exists() && !path.contains(".")) {
                        // Uzantısız yol isteği → SPA route olarak index.html döndür
                        file = indexFallback;
                        path = "/index.html";
                    } else {
                        // Dosya bulunamadı → 404 döndür
                        String msg = "404 — Dosya bulunamadı: " + path;
                        ex.sendResponseHeaders(404, msg.getBytes().length);
                        ex.getResponseBody().write(msg.getBytes());
                        ex.getResponseBody().close();
                        return;
                    }
                }

                // Dosya uzantısına göre MIME türü belirle
                String mime = "application/octet-stream"; // Varsayılan: bilinmeyen dosya
                if (path.endsWith(".html"))
                    mime = "text/html; charset=UTF-8";
                else if (path.endsWith(".css"))
                    mime = "text/css; charset=UTF-8";
                else if (path.endsWith(".js"))
                    mime = "application/javascript; charset=UTF-8";
                else if (path.endsWith(".png"))
                    mime = "image/png";
                else if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
                    mime = "image/jpeg";
                else if (path.endsWith(".svg"))
                    mime = "image/svg+xml";
                else if (path.endsWith(".ico"))
                    mime = "image/x-icon";
                else if (path.endsWith(".woff2"))
                    mime = "font/woff2";

                ex.getResponseHeaders().set("Content-Type", mime); // İçerik tipi
                ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // CORS
                // Statik dosyalar için cache - Geliştirme aşamasında kapalı
                ex.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
                ex.getResponseHeaders().set("Pragma", "no-cache");
                ex.getResponseHeaders().set("Expires", "0");
                // Güvenlik header'ları — iframe ile gömülmeyi ve cross-origin sorunlarını önler
                ex.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
                ex.getResponseHeaders().set("X-Frame-Options", "SAMEORIGIN");

                byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath()); // Dosyayı oku
                ex.sendResponseHeaders(200, bytes.length); // 200 OK + dosya boyutu
                OutputStream os = ex.getResponseBody();
                os.write(bytes);
                os.close(); // Dosya içeriğini gönder
            } catch (Exception e) {
                // Hata durumunda exchange'i düzgün kapatarak thread'in takılmasını önle
                System.out.println("⚠️ Static dosya hatası: " + e.getMessage());
                try {
                    if (!ex.getResponseHeaders().isEmpty()) {
                        String errMsg = "500 — Sunucu hatası";
                        ex.sendResponseHeaders(500, errMsg.getBytes().length);
                        ex.getResponseBody().write(errMsg.getBytes());
                    }
                    ex.getResponseBody().close();
                } catch (Exception ignored) {
                    /* exchange zaten kapatılmış olabilir */ }
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ŞİFREMİ UNUTTUM — POST /api/sifremi-unuttum (Public)
    // E-posta adresine şifre sıfırlama linki gönderir (UUID token + 1 saat
    // geçerlilik)
    // ═══════════════════════════════════════════════════
    static class SifremiUnuttumHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            }

            String body = readBody(ex); // İstek gövdesini oku
            String email = jsonValue(body, "email"); // E-posta adresi
            if (email == null) {
                sendResponse(ex, 400, errJson("E-posta gerekli!", "BAD_REQUEST"));
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement stmt = conn.prepareStatement("SELECT kullanici_id FROM kullanicilar WHERE email=?");
                stmt.setString(1, email.toLowerCase());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int kId = rs.getInt("kullanici_id");
                    String resetToken = UUID.randomUUID().toString();
                    PreparedStatement upd = conn.prepareStatement(
                            "UPDATE kullanicilar SET sifre_token=?, token_gecerli=DATEADD(hour, 1, GETDATE()) WHERE kullanici_id=?");
                    upd.setString(1, resetToken);
                    upd.setInt(2, kId);
                    upd.executeUpdate();
                    upd.close();

                    // Gerçekten Mail Gönder (Thread içinde atıyoruz ki API'yi bekletmesin)
                    new Thread(() -> {
                        try {
                            MailSender.sendResetMail(email, resetToken);
                        } catch (Throwable t) {
                            System.err.println(
                                    "⚠️ E-posta kütüphanesi (javax.mail) bulunamadı veya yüklenemedi. Link konsola yazdırıldı.");
                            System.err.println("Hata Detayı: " + t.getMessage());
                        }
                    }).start();

                    sendResponse(ex, 200, String.format(
                            "{\"basarili\":true,\"mesaj\":\"Şifre sıfırlama linki e-postanıza gönderildi.\",\"token\":\"%s\"}",
                            resetToken));
                } else {
                    sendResponse(ex, 200,
                            "{\"basarili\":true,\"mesaj\":\"Eğer bu e-posta sistemimize kayıtlıysa sıfırlama linki gönderilmiştir.\"}");
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                sendResponse(ex, 500, errJson("Veritabanı hatası!", "DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ŞİFRE SIFIRLA — POST /api/sifre-sifirla (Public)
    // Token + yeni şifre ile şifre sıfırlama işlemi. Token 1 saat geçerlidir.
    // ═══════════════════════════════════════════════════
    static class SifreSifirlaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            }

            String body = readBody(ex); // İstek gövdesi
            String token = jsonValue(body, "token"); // Sıfırlama tokenı
            String yeniSifre = jsonValue(body, "yeni_sifre"); // Yeni şifre

            if (token == null || yeniSifre == null) {
                sendResponse(ex, 400, errJson("Token ve yeni şifre gerekli!", "BAD_REQUEST"));
                return;
            }
            if (yeniSifre.length() < 6) {
                sendResponse(ex, 400, errJson("Şifre en az 6 karakter olmalıdır!", "BAD_REQUEST"));
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT kullanici_id FROM kullanicilar WHERE sifre_token=? AND token_gecerli > GETDATE()");
                stmt.setString(1, token);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int kId = rs.getInt("kullanici_id");
                    String sifreHash = hashPassword(yeniSifre);
                    PreparedStatement upd = conn.prepareStatement(
                            "UPDATE kullanicilar SET sifre_hash=?, sifre_token=NULL, token_gecerli=NULL WHERE kullanici_id=?");
                    upd.setString(1, sifreHash);
                    upd.setInt(2, kId);
                    upd.executeUpdate();
                    upd.close();

                    System.out.println("✅ Şifre sıfırlandı: ID=" + kId);
                    sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Şifreniz başarıyla sıfırlandı!\"}");
                } else {
                    sendResponse(ex, 400, errJson("Geçersiz veya süresi dolmuş token!", "INVALID_TOKEN"));
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                sendResponse(ex, 500, errJson("Veritabanı hatası!", "DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // MAİL GÖNDERİM YARDIMCISI (Javax.Mail / SMTP)
    // ═══════════════════════════════════════════════════
    // ═══════════════════════════════════════════════════
    // ABONELIK SATIN AL — POST /api/abonelik-satin-al [Üye]
    // Bekleyen ödeme kaydı oluşturur (odemeler tablosuna beklemede olarak ekler)
    // ═══════════════════════════════════════════════════
    static class AbonelikSatinAlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            }
            String[] u = authUser(ex);
            System.out.println("?? /api/abonelik-satin-al istegi - auth: " + (u == null ? "NULL" : u[2]));
            if (u == null) {
                sendResponse(ex, 401, errJson("Giriş yapmanız gerekiyor!", "UNAUTHORIZED"));
                return;
            }

            String body = readBody(ex);
            String planId = jsonValue(body, "plan_id");
            System.out.println("?? body: " + body + " | planId: " + planId);
            if (planId == null) {
                sendResponse(ex, 400, errJson("Plan ID gerekli!", "BAD_REQUEST"));
                return;
            }

            int kullaniciId = Integer.parseInt(u[0]);
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();

                // Plan bilgilerini al
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT plan_id,plan_adi,fiyat,sure_ay FROM uye_planlari WHERE plan_id=? AND durum=N'aktif'");
                ps.setInt(1, Integer.parseInt(planId));
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    rs.close();
                    ps.close();
                    sendResponse(ex, 404, errJson("Plan bulunamadı!", "NOT_FOUND"));
                    return;
                }
                int pId = rs.getInt("plan_id");
                String pAdi = rs.getString("plan_adi");
                double fiyat = rs.getDouble("fiyat");
                int sureAy = rs.getInt("sure_ay");
                rs.close();
                ps.close();

                // Üye uyeler tablosunda var mı? (kayit anında ekleniyor ama kontrol edelim)
                PreparedStatement uq = conn.prepareStatement(
                        "SELECT uye_id FROM uyeler WHERE kullanici_id=?");
                uq.setInt(1, kullaniciId);
                ResultSet ur = uq.executeQuery();
                int uyeId = -1;
                if (ur.next())
                    uyeId = ur.getInt("uye_id");
                ur.close();
                uq.close();

                // uyeler'de yoksa ekle (eski kullanıcılar için güvenlik)
                if (uyeId < 0) {
                    Statement cs = conn.createStatement();
                    ResultSet cr = cs.executeQuery("SELECT COUNT(*) AS cnt FROM uyeler");
                    int cnt = cr.next() ? cr.getInt("cnt") : 0;
                    cr.close();
                    cs.close();
                    String uNo = String.format("FZ-%d-%03d", java.time.Year.now().getValue(), cnt + 1);
                    PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO uyeler(kullanici_id,uyelik_no) VALUES(?,?)",
                            java.sql.Statement.RETURN_GENERATED_KEYS);
                    ins.setInt(1, kullaniciId);
                    ins.setString(2, uNo);
                    ins.executeUpdate();
                    ResultSet gk = ins.getGeneratedKeys();
                    if (gk.next())
                        uyeId = gk.getInt(1);
                    gk.close();
                    ins.close();

                    // Kullanıcı rolünü 'uye' ye yükselt (ilk kez uyeler tablosuna ekleniyor)
                    PreparedStatement rolUpd = conn.prepareStatement(
                            "UPDATE kullanicilar SET rol_id = (SELECT role_id FROM roller WHERE rol_adi=N'uye') WHERE kullanici_id=?");
                    rolUpd.setInt(1, kullaniciId);
                    rolUpd.executeUpdate();
                    rolUpd.close();
                    System.out.println("✅ Kullanıcı rolü 'uye' ye yükseltildi: kullanici_id=" + kullaniciId);
                }

                // Mevcut tÜM aktif veya pasif abonelikleri iptal et (bir üye = bir plan kuralı)
                PreparedStatement cancelOld = conn.prepareStatement(
                        "UPDATE uye_abonelikleri SET durum=N'iptal' " +
                                "WHERE uye_id=? AND durum IN (N'aktif', N'pasif', N'suresi_doldu')");
                cancelOld.setInt(1, uyeId);
                int cancelledCount = cancelOld.executeUpdate();
                cancelOld.close();
                System.out.println("?? SAtın Alma Öncesi Temizlik: uye_id=" + uyeId + " - " + cancelledCount
                        + " kayıt 'iptal' yapıldı.");

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
                if (abk.next())
                    abonelikId = abk.getInt(1);
                abk.close();
                ab.close();

                // Bekleyen ödeme kaydı oluştur
                PreparedStatement od = conn.prepareStatement(
                        "INSERT INTO odemeler(abonelik_id,miktar,odeme_yontemi,durum,aciklama) VALUES(?,?,N'online',N'beklemede',?)");
                od.setInt(1, abonelikId);
                od.setDouble(2, fiyat);
                od.setString(3, pAdi + " planı - bekleyen ödeme");
                od.executeUpdate();
                od.close();

                System.out.println("✅ Abonelik satın alma isteği: kullanici_id=" + kullaniciId + " plan=" + pAdi);
                sendResponse(ex, 200, String.format(
                        "{\"basarili\":true,\"mesaj\":\"Plan seçildi! Ödeme bekleniyor.\"," +
                                "\"abonelik_id\":%d,\"plan\":\"%s\",\"fiyat\":%.2f}",
                        abonelikId, pAdi, fiyat));

            } catch (Exception e) {
                System.out.println("❌ Abonelik satın alma hatası: " + e.getMessage());
                sendResponse(ex, 500, errJson("Veritabanı hatası: " + e.getMessage().replace("\"", "'"), "DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ODEME YAP — POST /api/odeme-yap [Üye]
    // Ödemeyi tamamlar: abonelik aktif edilir, ödeme tamamlandi yapılır
    // ═══════════════════════════════════════════════════
    static class OdemeYapHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            }
            String[] u = authUser(ex);
            System.out.println(">> /api/odeme-yap istegi - auth: " + (u == null ? "NULL" : u[2]));
            if (u == null) {
                sendResponse(ex, 401, errJson("Giriş yapmanız gerekiyor!", "UNAUTHORIZED"));
                return;
            }
            // Admin, uye ve kullanici ödeme yapabilir
            if (!"uye".equals(u[2]) && !"admin".equals(u[2]) && !"kullanici".equals(u[2])) {
                sendResponse(ex, 403, errJson("Bu işlem için yetkiniz yok!", "FORBIDDEN"));
                return;
            }

            String body = readBody(ex);
            String abonelikId = jsonValue(body, "abonelik_id");
            String yontem = jsonValue(body, "odeme_yontemi");
            if (abonelikId == null) {
                sendResponse(ex, 400, errJson("Abonelik ID gerekli!", "BAD_REQUEST"));
                return;
            }
            if (yontem == null || yontem.isEmpty())
                yontem = "online";

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
                    cr.close();
                    chk.close();
                    sendResponse(ex, 403, errJson("Bu aboneliğe erişim yetkiniz yok!", "FORBIDDEN"));
                    return;
                }
                String abonelikDurum = cr.getString("durum");
                cr.close();
                chk.close();
                if (!"pasif".equals(abonelikDurum)) {
                    sendResponse(ex, 400, errJson("Bu abonelik zaten aktif veya iptal edilmiş!", "INVALID_STATE"));
                    return;
                }

                // Aboneliği aktif yap
                PreparedStatement upd = conn.prepareStatement(
                        "UPDATE uye_abonelikleri SET durum=N'aktif' WHERE abonelik_id=?");
                upd.setInt(1, Integer.parseInt(abonelikId));
                upd.executeUpdate();
                upd.close();

                // Ödemeyi tamamlandi yap + yöntemi güncelle
                PreparedStatement pod = conn.prepareStatement(
                        "UPDATE odemeler SET durum=N'tamamlandi', odeme_yontemi=?, odeme_tarihi=GETDATE() " +
                                "WHERE abonelik_id=? AND durum=N'beklemede'");
                pod.setString(1, yontem);
                pod.setInt(2, Integer.parseInt(abonelikId));
                pod.executeUpdate();
                pod.close();

                // Kullanıcının rolünü 'uye' ye yükselt (eğer hala 'kullanici' ise)
                PreparedStatement rolUpd = conn.prepareStatement(
                        "UPDATE kullanicilar SET rol_id = (SELECT role_id FROM roller WHERE rol_adi=N'uye') " +
                                "WHERE kullanici_id=? AND rol_id = (SELECT role_id FROM roller WHERE rol_adi=N'kullanici')");
                rolUpd.setInt(1, kullaniciId);
                int updated = rolUpd.executeUpdate();
                rolUpd.close();
                if (updated > 0)
                    System.out.println("✅ Kullanıcı rolü 'uye' ye yükseltildi: kullanici_id=" + kullaniciId);

                System.out.println("✅ Ödeme tamamlandı: abonelik_id=" + abonelikId + " kullanici=" + kullaniciId);
                sendResponse(ex, 200,
                        "{\"basarili\":true,\"mesaj\":\"\u00d6deme başarıyla tamamlandı! Aboneliğiniz aktif edildi.\"}");

            } catch (Exception e) {
                System.out.println("❌ Ödeme hatası: " + e.getMessage());
                sendResponse(ex, 500, errJson("Veritabanı hatası: " + e.getMessage().replace("\"", "'"), "DB_ERROR"));
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // AYLIK GELİR — GET /api/aylik-gelir [Admin]
    // Son 12 ayın gelir verilerini odemeler tablosundan çeker
    // ═══════════════════════════════════════════════════
    static class AylikGelirHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (!requireAdmin(ex))
                return;

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();

                // Son 12 ayın gelir verilerini getir (odemeler tablosundan)
                ResultSet rs = stmt.executeQuery(
                        "WITH Son12Ay AS (" +
                                "  SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 " +
                                "  UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 " +
                                "  UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11" +
                                ") " +
                                "SELECT YEAR(DATEADD(MONTH, -n, GETDATE())) AS yil, " +
                                "       MONTH(DATEADD(MONTH, -n, GETDATE())) AS ay, " +
                                "       ISNULL((SELECT SUM(o.miktar) FROM odemeler o " +
                                "         WHERE o.durum = N'tamamlandi' " +
                                "           AND MONTH(o.odeme_tarihi) = MONTH(DATEADD(MONTH, -n, GETDATE())) " +
                                "           AND YEAR(o.odeme_tarihi) = YEAR(DATEADD(MONTH, -n, GETDATE()))), 0) AS toplam "
                                +
                                "FROM Son12Ay " +
                                "ORDER BY yil ASC, ay ASC");

                StringBuilder json = new StringBuilder("{\"aylar\":[");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    json.append(String.format(
                            "{\"yil\":%d,\"ay\":%d,\"toplam\":%.2f}",
                            rs.getInt("yil"), rs.getInt("ay"), rs.getDouble("toplam")));
                    first = false;
                }
                json.append("]}");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString());

            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // AYLIK ÜYE BÜYÜMESİ — GET /api/aylik-uye [Admin]
    // Son 12 ayın kümülatif üye sayısını çeker
    // ═══════════════════════════════════════════════════
    static class AylikUyeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (!requireAdmin(ex))
                return;

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();

                // Son 12 ayın kümülatif üye verilerini getir
                ResultSet rs = stmt.executeQuery(
                        "WITH Son12Ay AS (" +
                                "  SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 " +
                                "  UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 " +
                                "  UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11" +
                                ") " +
                                "SELECT YEAR(DATEADD(MONTH, -n, GETDATE())) AS yil, " +
                                "       MONTH(DATEADD(MONTH, -n, GETDATE())) AS ay, " +
                                "       (SELECT COUNT(*) FROM kullanicilar k " +
                                "        JOIN roller r ON k.rol_id = r.role_id " +
                                "        WHERE r.rol_adi = N'uye' " +
                                "        AND k.kayit_tarihi < DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()) - n + 1, 0)) AS toplam "
                                +
                                "FROM Son12Ay " +
                                "ORDER BY yil ASC, ay ASC");

                StringBuilder json = new StringBuilder("{\"aylar\":[");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    json.append(String.format(
                            "{\"yil\":%d,\"ay\":%d,\"toplam\":%d}",
                            rs.getInt("yil"), rs.getInt("ay"), rs.getInt("toplam")));
                    first = false;
                }
                json.append("]}");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString());

            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // AYLIK DEVAMSIZ ÜYE — GET /api/aylik-devamsiz [Admin]
    // Son 12 ayın devamsız üye sayısını çeker
    // ═══════════════════════════════════════════════════
    static class AylikDevamsizHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (!requireAdmin(ex))
                return;

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir();
                Statement stmt = conn.createStatement();

                // Son 12 ayın devamsız üye verilerini getir
                // Mantık: O ay içinde aboneliği aktif olan (başlangıç < ay sonu VE bitiş >= ay
                // başı)
                // ANCAK o ay içinde hiç giriş kaydı olmayan üyeler
                ResultSet rs = stmt.executeQuery(
                        "WITH Son12Ay AS (" +
                                "  SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 " +
                                "  UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 " +
                                "  UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11" +
                                "), " +
                                "Aylar AS (" +
                                "  SELECT YEAR(DATEADD(MONTH, -n, GETDATE())) AS yil, " +
                                "         MONTH(DATEADD(MONTH, -n, GETDATE())) AS ay, " +
                                "         DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()) - n, 0) AS ay_basi, " +
                                "         DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()) - n + 1, 0) AS ay_sonu " +
                                "  FROM Son12Ay" +
                                ") " +
                                "SELECT A.yil, A.ay, " +
                                "  (SELECT COUNT(DISTINCT ua.uye_id) " +
                                "   FROM uye_abonelikleri ua " +
                                "   WHERE ua.baslangic_tarihi < A.ay_sonu AND ua.bitis_tarihi >= A.ay_basi " +
                                "   AND ua.uye_id NOT IN (" +
                                "       SELECT DISTINCT kullanici_id " +
                                "       FROM giris_cikis_kayitlari gck " +
                                "       WHERE gck.giris_saat >= A.ay_basi AND gck.giris_saat < A.ay_sonu" +
                                "   )) AS toplam " +
                                "FROM Aylar A " +
                                "ORDER BY A.yil ASC, A.ay ASC");

                StringBuilder json = new StringBuilder("{\"aylar\":[");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    json.append(String.format(
                            "{\"yil\":%d,\"ay\":%d,\"toplam\":%d}",
                            rs.getInt("yil"), rs.getInt("ay"), rs.getInt("toplam")));
                    first = false;
                }
                json.append("]}");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString());

            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═════════════════════════════════════════════════
    // RAPOR: ÖDEME YÖNTEMİ DAĞILIMI — GET /api/rapor-odeme [Admin]
    // Ödeme yöntemlerine göre (kredi kartı, nakit, havale, online) gruplandırılmış
    // ödeme sayısı
    // ═════════════════════════════════════════════════
    static class RaporOdemeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Sadece admin erişebilir
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                Statement stmt = conn.createStatement();
                // Ödeme yöntemlerine göre grupla ve her birinden kaç adet olduğunu say
                ResultSet rs = stmt
                        .executeQuery("SELECT odeme_yontemi, COUNT(*) AS adet FROM odemeler GROUP BY odeme_yontemi");
                StringBuilder json = new StringBuilder("{\"yontemler\":[");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    String y = rs.getString("odeme_yontemi"); // Ödeme yöntemi adı
                    // null ise varsayılan "Nakit" olarak göster
                    json.append(String.format("{\"yontem\":\"%s\",\"adet\":%d}", (y == null ? "Nakit" : y),
                            rs.getInt("adet")));
                    first = false;
                }
                json.append("]}");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString()); // JSON yanıtı gönder
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═════════════════════════════════════════════════
    // RAPOR: DERS KATILIM DAĞILIMI — GET /api/rapor-ders [Admin]
    // Her derse kaç rezervasyon yapıldığını gösterir
    // ═════════════════════════════════════════════════
    static class RaporDersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Sadece admin erişebilir
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                Statement stmt = conn.createStatement();
                // Her ders için rezervasyon sayısını hesapla (LEFT JOIN: rezervasyonu olmayan
                // dersler de görünür)
                ResultSet rs = stmt.executeQuery(
                        "SELECT d.ders_adi, COUNT(r.rezervasyon_id) AS katilim " +
                                "FROM dersler d " +
                                "LEFT JOIN rezervasyonlar r ON d.ders_id = r.ders_id " +
                                "GROUP BY d.ders_adi");
                // Sonuçları JSON dizisine dönüştür
                StringBuilder json = new StringBuilder("{\"dersler\":[");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    // Her ders için ders adı ve katılım sayısını JSON'a ekle
                    json.append(String.format("{\"ders\":\"%s\",\"katilim\":%d}", rs.getString("ders_adi"),
                            rs.getInt("katilim")));
                    first = false;
                }
                json.append("]}");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString()); // JSON yanıtı gönder
            } catch (SQLException e) {
                sendResponse(ex, 500, "{\"hata\":\"" + e.getMessage().replace("\"", "'") + "\"}");
            }
        }
    }

    // ═════════════════════════════════════════════════
    // MAİL GÖNDERİM SINIFI — Şifre sıfırlama e-postası gönderir
    // Gmail SMTP sunucusu üzerinden JavaMail API kullanılarak e-posta gönderilir
    // ═════════════════════════════════════════════════
    static class MailSender {
        // Gmail SMTP gönderici e-posta adresi
        static final String username = "fitzonedestek@gmail.com";
        // Gmail Uygulama Şifresi (App Password) — normal şifre değil, Google hesap
        // ayarlarından üretilir
        static final String password = "srtrfiadbvtqldlk";

        // Şifre sıfırlama e-postası gönderen metot
        // @param toEmail Alıcı e-posta adresi
        // @param token Şifre sıfırlama tokenı (UUID — URL'ye eklenir)
        public static void sendResetMail(String toEmail, String token) {
            // SMTP sunucu ayarlarını yapılandır
            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com"); // Gmail SMTP sunucusu
            prop.put("mail.smtp.port", "587"); // SMTP portu (TLS için 587)
            prop.put("mail.smtp.auth", "true"); // Kimlik doğrulama gerekli
            prop.put("mail.smtp.starttls.enable", "true"); // TLS şifreleme aktif

            // SMTP oturumu oluştur — kimlik doğrulama bilgileriyle
            Session session = Session.getInstance(prop,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password); // Gmail giriş bilgileri
                        }
                    });

            try {
                // E-posta mesajını oluştur (MIME formatında)
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username, "FitZone Pro")); // Gönderen adı ve adresi
                message.setRecipients(
                        Message.RecipientType.TO, // Alıcı türü: TO (doğrudan alıcı)
                        InternetAddress.parse(toEmail) // Alıcı e-posta adresini parse et
                );
                message.setSubject("FitZone Pro - Şifre Sıfırlama İsteği"); // E-posta konusu

                // Şifre sıfırlama linkini oluştur (token URL'ye eklenir)
                String resetLink = BASE_URL + "/?resetToken=" + token;

                // HTML formatında e-posta içeriği — kullanıcıya gönderilecek şifre sıfırlama
                // maili
                String htmlContent = "<div style='font-family:sans-serif;max-width:600px;margin:auto;padding:20px;border:1px solid #ddd;border-radius:10px;'>"
                        + "<h2 style='color:#0ea5e9;'>FitZone Pro</h2>"
                        + "<p>Merhaba,</p>"
                        + "<p>Hesabınız için bir şifre sıfırlama isteği aldık. Şifrenizi sıfırlamak için aşağıdaki butona tıklayın:</p>"
                        + "<div style='text-align:center;margin:30px 0;'>"
                        + "<a href='" + resetLink
                        + "' style='background:#6366f1;color:white;padding:12px 24px;text-decoration:none;border-radius:8px;font-weight:bold;'>Şifremi Sıfırla</a>"
                        + "</div>"
                        + "<p style='font-size:12px;color:#777;'>Bu isteği siz yapmadıysanız, bu e-postayı dikkate almayınız.</p>"
                        + "</div>";

                message.setContent(htmlContent, "text/html; charset=utf-8"); // İçeriği HTML olarak ayarla

                Transport.send(message); // E-postayı SMTP üzerinden gönder
                System.out.println("✅ E-posta başarıyla gönderildi: " + toEmail); // Başarı logu
            } catch (Exception e) {
                // Gmail bağlantı hatası veya yetkilendirme sorunu — konsola logla
                System.out.println("❌ E-posta gönderim hatası (Gmail bağlanamadı veya yetki yok): " + e.getMessage());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // YENİ EKLEME İŞLEMLERİ (Antrenör, Ekipman, Ders)
    // ══════════════════════════════════════════════════════════════════════

    // ════════════════════════════════════════════════════════════════════
    // ANTRENÖR EKLE — POST /api/antrenor-ekle [Admin]
    // Yeni antrenör kaydı oluşturur: kullanicilar + antrenorler tablolarına ekleme
    // yapar
    // ════════════════════════════════════════════════════════════════════
    static class AntrenorEkleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi kontrolü
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            }

            String body = readBody(ex); // İstek gövdesini oku
            String ad = jsonValue(body, "ad"); // Antrenör adı
            String soyad = jsonValue(body, "soyad"); // Antrenör soyadı
            String email = jsonValue(body, "email"); // E-posta adresi
            String telefon = jsonValue(body, "telefon"); // Telefon numarası
            String uzmanlik = jsonValue(body, "uzmanlik"); // Uzmanlık alanı
            String deneyim = jsonValue(body, "deneyim"); // Deneyim yılı
            String sertif = jsonValue(body, "sertifikalar"); // Sertifika bilgisi
            String sifre = jsonValue(body, "sifre"); // Giriş şifresi

            // Zorunlu alanların kontrolü
            if (ad == null || soyad == null || email == null || sifre == null) {
                sendResponse(ex, 400, errJson("Eksik alanlar!", "BAD_REQUEST"));
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı

                // 1) Kullanicilar tablosuna ekle (rol_id=3 → antrenör rolü)
                String sifreHash = hashPassword(sifre); // Şifreyi SHA-256 ile hashle

                String q1 = "INSERT INTO kullanicilar (ad, soyad, email, sifre_hash, telefon, rol_id, durum) VALUES (?,?,?,?,?,3,'aktif')";
                PreparedStatement ps1 = conn.prepareStatement(q1, Statement.RETURN_GENERATED_KEYS); // Auto-generated
                                                                                                    // ID'yi al
                ps1.setString(1, ad); // Ad
                ps1.setString(2, soyad); // Soyad
                ps1.setString(3, email.toLowerCase()); // Email (küçük harf)
                ps1.setString(4, sifreHash); // Şifre hash
                ps1.setString(5, telefon != null ? telefon : ""); // Telefon (null ise boş)
                ps1.executeUpdate(); // INSERT sorgusunu çalıştır

                // Oluşturulan kullanıcının auto-generated ID'sini al
                ResultSet rs = ps1.getGeneratedKeys();
                int newUserId = -1;
                if (rs.next()) {
                    newUserId = rs.getInt(1);
                } // Yeni kullanıcı ID
                ps1.close();

                if (newUserId != -1) {
                    // 2) Antrenorler tablosuna uzmanlık bilgilerini ekle
                    String q2 = "INSERT INTO antrenorler (kullanici_id, uzmanlik, deneyim_yili, sertifikalar) VALUES (?,?,?,?)";
                    PreparedStatement ps2 = conn.prepareStatement(q2);
                    ps2.setInt(1, newUserId); // Kullanıcı ID (FK)
                    ps2.setString(2, uzmanlik != null ? uzmanlik : ""); // Uzmanlık alanı
                    ps2.setInt(3, (deneyim != null && !deneyim.isEmpty()) ? Integer.parseInt(deneyim) : 0); // Deneyim
                                                                                                            // yılı
                    ps2.setString(4, sertif != null ? sertif : ""); // Sertifikalar
                    ps2.executeUpdate();
                    ps2.close(); // INSERT'i çalıştır

                    sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Antrenör eklendi!\"}");
                } else {
                    sendResponse(ex, 500, errJson("Kullanıcı ID alınamadı", "DB_ERROR"));
                }
            } catch (SQLException e) {
                System.out.println("❌ Antrenör Ekleme hatası: " + e.getMessage());
                // UNIQUE constraint ihlali — e-posta zaten kayıtlı
                if (e.getMessage().contains("UNIQUE")) {
                    sendResponse(ex, 409, errJson("Bu e-posta zaten kayıtlı!", "CONFLICT"));
                } else {
                    sendResponse(ex, 500, errJson("Veritabanı hatası", "DB_ERROR"));
                }
            }
        }
    }

    // ═════════════════════════════════════════════════
    // EKİPMAN EKLE — POST /api/ekipman-ekle [Admin]
    // Yeni ekipman kaydı oluşturur (ekipman tablosuna INSERT)
    // ═════════════════════════════════════════════════
    static class EkipmanEkleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi kontrolü
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            }

            String body = readBody(ex); // İstek gövdesini oku
            String ad = jsonValue(body, "ad"); // Ekipman adı
            String kategori = jsonValue(body, "kategori"); // Ekipman kategorisi
            String adet = jsonValue(body, "adet"); // Adet sayısı
            String fiyat = jsonValue(body, "fiyat"); // Satın alma fiyatı
            String satinAlma = jsonValue(body, "satinAlma"); // Satın alma tarihi

            // Ekipman adı zorunlu alan
            if (ad == null) {
                sendResponse(ex, 400, errJson("Ekipman adı zorunlu!", "BAD_REQUEST"));
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                // Yeni ekipman kaydı oluştur — varsayılan durum "calisiyor"
                String q = "INSERT INTO ekipman (ekipman_adi, kategori, miktar, satin_alma_fiyati, satin_alma_tarihi, durum) VALUES (?,?,?,?,?, 'calisiyor')";
                PreparedStatement ps = conn.prepareStatement(q);
                ps.setString(1, ad); // Ekipman adı
                ps.setString(2, kategori != null ? kategori : "Diğer"); // Kategori (null ise "Diğer")
                ps.setInt(3, (adet != null && !adet.isEmpty()) ? Integer.parseInt(adet) : 1); // Adet (null ise 1)
                ps.setDouble(4, (fiyat != null && !fiyat.isEmpty()) ? Double.parseDouble(fiyat) : 0.0); // Fiyat (null
                                                                                                        // ise 0)

                // Satın alma tarihi varsa set et, yoksa NULL olarak bırak
                if (satinAlma != null && !satinAlma.isEmpty()) {
                    ps.setDate(5, java.sql.Date.valueOf(satinAlma)); // Tarih değerini ata
                } else {
                    ps.setNull(5, java.sql.Types.DATE); // NULL olarak ata
                }

                ps.executeUpdate();
                ps.close(); // INSERT sorgusunu çalıştır

                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Ekipman başarıyla eklendi!\"}");
            } catch (SQLException e) {
                System.out.println("❌ Ekipman Ekleme hatası: " + e.getMessage());
                sendResponse(ex, 500, errJson("Veritabanı hatası", "DB_ERROR"));
            }
        }
    }

    // ═════════════════════════════════════════════════
    // DERS EKLE — POST /api/ders-ekle [Admin]
    // Yeni ders kaydı oluşturur (siniflar tablosuna INSERT)
    // ═════════════════════════════════════════════════
    static class DersEkleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi kontrolü
            if (!"POST".equals(ex.getRequestMethod())) {
                sendResponse(ex, 405, errJson("Sadece POST", "METHOD_NOT_ALLOWED"));
                return;
            }

            String body = readBody(ex); // İstek gövdesini oku
            String dersAd = jsonValue(body, "dersAd"); // Ders adı
            String antrenorId = jsonValue(body, "antrenorId"); // Antrenör ID'si (FK)
            String kategori = jsonValue(body, "kategori"); // Ders kategorisi
            String kontenjan = jsonValue(body, "kontenjan"); // Maks öğrenci sayısı
            String sure = jsonValue(body, "sure"); // Ders süresi (dk)

            // Zorunlu alanların kontrolü
            if (dersAd == null || antrenorId == null) {
                sendResponse(ex, 400, errJson("Eksik alanlar!", "BAD_REQUEST"));
                return;
            }

            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                // Yeni ders kaydı oluştur — varsayılan durum "aktif"
                String q = "INSERT INTO siniflar (ders_adi, antrenor_id, kategori, kontenjan, sure_dakika, durum) VALUES (?,?,?,?,?, 'aktif')";
                PreparedStatement ps = conn.prepareStatement(q);
                ps.setString(1, dersAd); // Ders adı
                ps.setInt(2, Integer.parseInt(antrenorId)); // Antrenör ID (FK)
                ps.setString(3, kategori != null ? kategori : "Kardio"); // Kategori (varsayılan: Kardio)
                ps.setInt(4, (kontenjan != null && !kontenjan.isEmpty()) ? Integer.parseInt(kontenjan) : 20); // Kontenjan
                                                                                                              // (varsayılan:
                                                                                                              // 20)
                ps.setInt(5, (sure != null && !sure.isEmpty()) ? Integer.parseInt(sure) : 60); // Süre dk (varsayılan:
                                                                                               // 60)

                ps.executeUpdate();
                ps.close(); // INSERT sorgusunu çalıştır

                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Ders başarıyla eklendi!\"}");
            } catch (NumberFormatException e) {
                // Sayısal alanlarda geçersiz format hatası
                System.out.println("❌ Ders Ekleme hatası (ID Formatı yanlış): " + e.getMessage());
                sendResponse(ex, 400, errJson("Geçersiz sayısal veri!", "BAD_REQUEST"));
            } catch (SQLException e) {
                System.out.println("❌ Ders Ekleme hatası: " + e.getMessage());
                sendResponse(ex, 500, errJson("Veritabanı hatası", "DB_ERROR"));
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // DÜZENLE VE SİL HANDLER'LARI (DERS / ANTRENÖR / EKİPMAN)
    // ════════════════════════════════════════════════════════════════

    // DERS GÜNCELLE — POST /api/ders-guncelle [Admin]
    static class DersGuncelleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi kontrolü
            try {
                String body = readBody(ex); // İstek gövdesini oku
                int id = Integer.parseInt(jsonValue(body, "id")); // Güncellenecek ders ID'si
                String dersAd = jsonValue(body, "dersAd"); // Yeni ders adı
                int antrenorId = Integer.parseInt(jsonValue(body, "antrenorId")); // Yeni antrenör ID
                String kategori = jsonValue(body, "kategori"); // Yeni kategori
                int kontenjan = Integer.parseInt(jsonValue(body, "kontenjan")); // Yeni kontenjan
                int sure = Integer.parseInt(jsonValue(body, "sure")); // Yeni süre (dk)
                String durum = jsonValue(body, "durum"); // Yeni durum

                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                // Siniflar tablosundaki ders kaydını güncelle
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE siniflar SET ders_adi=?, antrenor_id=?, kategori=?, kontenjan=?, sure_dakika=?, durum=? WHERE ders_id=?");
                ps.setString(1, dersAd);
                ps.setInt(2, antrenorId);
                ps.setString(3, kategori); // Ders bilgilerini set et
                ps.setInt(4, kontenjan);
                ps.setInt(5, sure);
                ps.setString(6, durum);
                ps.setInt(7, id); // Kalan alanlar ve WHERE
                ps.executeUpdate();
                ps.close(); // UPDATE sorgusunu çalıştır

                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Ders güncellendi!\"}");
            } catch (Exception e) {
                sendResponse(ex, 500, errJson("Hata: " + e.getMessage(), "ERROR"));
            }
        }
    }

    // DERS SİL — POST /api/ders-sil [Admin]
    static class DersSilHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi kontrolü
            try {
                String body = readBody(ex); // İstek gövdesini oku
                int id = Integer.parseInt(jsonValue(body, "id")); // Silinecek ders ID'si
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                PreparedStatement ps = conn.prepareStatement("DELETE FROM siniflar WHERE ders_id=?"); // Silme sorgusu
                ps.setInt(1, id); // Ders ID'sini parametre olarak ver
                ps.executeUpdate();
                ps.close(); // Sorguyu çalıştır
                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Ders silindi!\"}");
            } catch (Exception e) {
                sendResponse(ex, 500, errJson("Hata: " + e.getMessage(), "ERROR"));
            }
        }
    }

    // EKİPMAN GÜNCELLE — POST /api/ekipman-guncelle [Admin]
    static class EkipmanGuncelleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi kontrolü
            try {
                String body = readBody(ex); // İstek gövdesini oku
                int id = Integer.parseInt(jsonValue(body, "id")); // Güncellenecek ekipman ID
                String ad = jsonValue(body, "ad"); // Yeni ekipman adı
                String kategori = jsonValue(body, "kategori"); // Yeni kategori
                int adet = Integer.parseInt(jsonValue(body, "adet")); // Yeni adet
                double fiyat = Double.parseDouble(jsonValue(body, "fiyat")); // Yeni fiyat
                String durum = jsonValue(body, "durum"); // Yeni durum

                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                // Ekipman tablosundaki kaydı güncelle
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE ekipman SET ekipman_adi=?, kategori=?, miktar=?, satin_alma_fiyati=?, durum=? WHERE ekipman_id=?");
                ps.setString(1, ad);
                ps.setString(2, kategori);
                ps.setInt(3, adet); // Ad, kategori, adet
                ps.setDouble(4, fiyat);
                ps.setString(5, durum);
                ps.setInt(6, id); // Fiyat, durum, WHERE ID
                ps.executeUpdate();
                ps.close(); // UPDATE çalıştır

                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Ekipman güncellendi!\"}");
            } catch (Exception e) {
                sendResponse(ex, 500, errJson("Hata: " + e.getMessage(), "ERROR"));
            }
        }
    }

    // EKİPMAN SİL — POST /api/ekipman-sil [Admin]
    static class EkipmanSilHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi kontrolü
            try {
                String body = readBody(ex); // İstek gövdesini oku
                int id = Integer.parseInt(jsonValue(body, "id")); // Silinecek ekipman ID
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                PreparedStatement ps = conn.prepareStatement("DELETE FROM ekipman WHERE ekipman_id=?"); // Silme sorgusu
                ps.setInt(1, id); // ID parametresini set et
                ps.executeUpdate();
                ps.close(); // Sorguyu çalıştır
                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Ekipman silindi!\"}");
            } catch (Exception e) {
                sendResponse(ex, 500, errJson("Hata: " + e.getMessage(), "ERROR"));
            }
        }
    }

    // ANTRENÖR GÜNCELLE — POST /api/antrenor-guncelle [Admin]
    // İki tabloda güncelleme yapar: kullanicilar (kişisel bilgi) + antrenorler
    // (uzmanlık bilgisi)
    static class AntrenorGuncelleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi kontrolü
            try {
                String body = readBody(ex); // İstek gövdesini oku
                int id = Integer.parseInt(jsonValue(body, "id")); // Antrenör ID
                String ad = jsonValue(body, "ad"); // Yeni ad
                String soyad = jsonValue(body, "soyad"); // Yeni soyad
                String email = jsonValue(body, "email"); // Yeni email
                String telefon = jsonValue(body, "telefon"); // Yeni telefon
                String uzmanlik = jsonValue(body, "uzmanlik"); // Yeni uzmanlık alanı
                int deneyim = Integer.parseInt(jsonValue(body, "deneyim")); // Yeni deneyim yılı
                String sertifikalar = jsonValue(body, "sertifikalar"); // Yeni sertifikalar
                String durum = jsonValue(body, "durum"); // Yeni durum

                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı

                // Önce antrenorler tablosundan kullanici_id'yi bul
                PreparedStatement ps0 = conn
                        .prepareStatement("SELECT kullanici_id FROM antrenorler WHERE antrenor_id=?");
                ps0.setInt(1, id); // Antrenör ID ile sorgula
                ResultSet rs0 = ps0.executeQuery();
                int kid = -1; // Kullanıcı ID (bulunamazsa -1)
                if (rs0.next())
                    kid = rs0.getInt(1); // Kullanıcı ID'sini al
                rs0.close();
                ps0.close();

                // Kullanıcı kaydı varsa kişisel bilgileri güncelle
                if (kid != -1) {
                    PreparedStatement psk = conn.prepareStatement(
                            "UPDATE kullanicilar SET ad=?, soyad=?, email=?, telefon=? WHERE kullanici_id=?");
                    psk.setString(1, ad);
                    psk.setString(2, soyad);
                    psk.setString(3, email);
                    psk.setString(4, telefon);
                    psk.setInt(5, kid);
                    psk.executeUpdate();
                    psk.close(); // Kullanıcı tablosunu güncelle
                }

                // Antrenör tablosundaki uzmanlık bilgilerini güncelle
                PreparedStatement psa = conn.prepareStatement(
                        "UPDATE antrenorler SET uzmanlik=?, deneyim_yili=?, sertifikalar=?, durum=? WHERE antrenor_id=?");
                psa.setString(1, uzmanlik);
                psa.setInt(2, deneyim);
                psa.setString(3, sertifikalar);
                psa.setString(4, durum);
                psa.setInt(5, id);
                psa.executeUpdate();
                psa.close(); // Antrenör tablosunu güncelle

                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Antrenör güncellendi!\"}");
            } catch (Exception e) {
                sendResponse(ex, 500, errJson("Hata: " + e.getMessage(), "ERROR"));
            }
        }
    }

    // ANTRENÖR SİL — POST /api/antrenor-sil [Admin]
    // Antrenörü siler: eğer kullanicilar tablosunda da kaydı varsa oradan silince
    // CASCADE kurallarıyla antrenorler kaydı da silinir
    static class AntrenorSilHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi kontrolü
            try {
                String body = readBody(ex); // İstek gövdesini oku
                int id = Integer.parseInt(jsonValue(body, "id")); // Silinecek antrenör ID
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı

                // Önce antrenorler tablosundan kullanici_id'yi bul
                PreparedStatement ps0 = conn
                        .prepareStatement("SELECT kullanici_id FROM antrenorler WHERE antrenor_id=?");
                ps0.setInt(1, id); // Antrenör ID ile sorgula
                ResultSet rs0 = ps0.executeQuery();
                int kid = -1; // Kullanıcı ID (bulunamazsa -1)
                if (rs0.next())
                    kid = rs0.getInt(1); // Kullanıcı ID'sini al
                rs0.close();
                ps0.close();

                if (kid != -1) {
                    // Kullanıcı kaydı varsa — kullanicilar tablosundan sil (CASCADE ile antrenorler
                    // de silinir)
                    PreparedStatement psk = conn.prepareStatement("DELETE FROM kullanicilar WHERE kullanici_id=?");
                    psk.setInt(1, kid);
                    psk.executeUpdate();
                    psk.close(); // CASCADE kurallarıyla bağlı kayıtlar da silinir
                } else {
                    // Kullanıcı kaydı yoksa doğrudan antrenorler tablosundan sil
                    PreparedStatement psa = conn.prepareStatement("DELETE FROM antrenorler WHERE antrenor_id=?");
                    psa.setInt(1, id);
                    psa.executeUpdate();
                    psa.close();
                }

                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Antrenör silindi!\"}");
            } catch (Exception e) {
                sendResponse(ex, 500, errJson("Hata: " + e.getMessage(), "ERROR"));
            }
        }
    }

    // ═════════════════════════════════════════════════
    // REZERVASYON YAP — POST /api/rezervasyon-yap [Üye]
    // Üye bir ders seansına kayıt olur (sinif_rezervasyonlari tablosuna INSERT)
    // ═════════════════════════════════════════════════
    static class RezervasyonYapHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireRole(ex, "uye"))
                return; // Sadece üye rolü gerekli
            try {
                String body = readBody(ex); // İstek gövdesini oku
                int programId = Integer.parseInt(jsonValue(body, "program_id")); // Ders programı ID'si
                String[] u = authUser(ex); // JWT'den kullanıcı bilgisi
                int kid = Integer.parseInt(u[0]); // Kullanıcı ID
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı

                // 1) Aktif abonelik kontrolü
                PreparedStatement psAb = conn.prepareStatement(
                        "SELECT a.abonelik_id FROM uye_abonelikleri a " +
                                "JOIN uyeler u ON a.uye_id = u.uye_id " +
                                "WHERE u.kullanici_id=? AND a.durum=N'aktif'");
                psAb.setInt(1, kid);
                ResultSet rsAb = psAb.executeQuery();
                if (!rsAb.next()) {
                    rsAb.close();
                    psAb.close();
                    sendResponse(ex, 403,
                            "{\"hata\":\"Aktif aboneliğiniz bulunmuyor. Lütfen önce bir plan satın alın ve ödemesini yapın.\"}");
                    return;
                }
                rsAb.close();
                psAb.close();

                // 2) Kontenjan kontrolü — ders dolu mu?
                PreparedStatement psK = conn.prepareStatement(
                        "SELECT s.kontenjan, (SELECT COUNT(*) FROM sinif_rezervasyonlari r WHERE r.program_id=sp.program_id AND r.durum=N'aktif' AND r.r_tarih=CAST(GETDATE() AS DATE)) AS dolu "
                                +
                                "FROM sinif_programlari sp JOIN siniflar s ON sp.ders_id=s.ders_id WHERE sp.program_id=?");
                psK.setInt(1, programId); // Program ID parametresi
                ResultSet rsK = psK.executeQuery();
                if (rsK.next()) {
                    // Dolu sayısı kontenjanı geçtiyse reddedilir
                    if (rsK.getInt("dolu") >= rsK.getInt("kontenjan")) {
                        rsK.close();
                        psK.close();
                        sendResponse(ex, 400, "{\"hata\":\"Bu dersin kontenjanı dolmuştur!\"}");
                        return;
                    }
                }
                rsK.close();
                psK.close();

                // 3) Üyenin uye_id'sini bul (uyeler tablosundan)
                PreparedStatement psU = conn.prepareStatement("SELECT uye_id FROM uyeler WHERE kullanici_id=?");
                psU.setInt(1, kid);
                ResultSet rsU = psU.executeQuery();
                if (!rsU.next()) {
                    sendResponse(ex, 403, "{\"hata\":\"Profil yok\"}");
                    return;
                } // Üye profili yoksa hata
                int uid = rsU.getInt(1);
                rsU.close();
                psU.close(); // Üye ID'sini al

                // 4) Rezervasyon kaydını oluştur (bugünün tarihiyle, durum: aktif)
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO sinif_rezervasyonlari (uye_id, program_id, r_tarih, durum) VALUES (?, ?, CAST(GETDATE() AS DATE), N'aktif')");
                ps.setInt(1, uid);
                ps.setInt(2, programId); // Üye ID ve Program ID
                ps.executeUpdate();
                ps.close(); // INSERT çalıştır
                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Derse kayıt olundu!\"}");
            } catch (Exception e) {
                // UNIQUE constraint ihlali — aynı derse zaten kayıtlı
                if (e.getMessage() != null
                        && (e.getMessage().contains("UNIQUE") || e.getMessage().contains("uq_rezervasyon")))
                    sendResponse(ex, 400, "{\"hata\":\"Bu derse zaten kayıtlısınız!\"}");
                else
                    sendResponse(ex, 500, errJson(e.getMessage(), "ERROR")); // Genel hata
            }
        }
    }

    // ═════════════════════════════════════════════════
    // REZERVASYON İPTAL — POST /api/rezervasyon-iptal [Giriş yapmış herkes]
    // Mevcut rezervasyonu iptal eder (durum: 'iptal')
    // ═════════════════════════════════════════════════
    static class RezervasyonIptalHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAuth(ex))
                return; // Herhangi bir giriş yapmış kullanıcı gerekli
            try {
                String body = readBody(ex); // İstek gövdesini oku
                int rezId = Integer.parseInt(jsonValue(body, "rezervasyon_id")); // İptal edilecek rezervasyon ID
                System.out.println("API: Rezervasyon \u0130ptali -> ID:" + rezId); // Debug logu
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                // Rezervasyon durumunu 'iptal' olarak güncelle
                PreparedStatement ps = conn
                        .prepareStatement("UPDATE sinif_rezervasyonlari SET durum=N'iptal' WHERE rezervasyon_id=?");
                ps.setInt(1, rezId); // Rezervasyon ID parametresi
                int affected = ps.executeUpdate();
                ps.close(); // UPDATE çalıştır
                System.out.println("API: Rezervasyon \u0130ptal Edildi. Etkilenen sat\u0131r: " + affected); // Sonuç
                                                                                                             // logu
                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Rezervasyon iptal edildi.\"}");
            } catch (Exception e) {
                e.printStackTrace(); // Hata yığın izini yazdır
                sendResponse(ex, 500, errJson(e.getMessage(), "ERROR"));
            }
        }
    }

    // PROGRAM (SEANS) EKLE — POST /api/program-ekle [Admin]
    // Bir derse yeni seans/program slotu ekler (gün, saat, salon bilgisiyle)
    static class ProgramEkleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi
            try {
                String body = readBody(ex); // İstek gövdesini oku
                int dersId = Integer.parseInt(jsonValue(body, "ders_id")); // Ders ID (FK)
                String gun = jsonValue(body, "gun"); // Gün (Pazartesi, Salı...)
                String bas = jsonValue(body, "baslangic"); // Başlangıç saati
                String bit = jsonValue(body, "bitis"); // Bitiş saati
                String salon = jsonValue(body, "salon"); // Salon adı

                System.out.println(
                        "API: Seans Ekleme -> Ders:" + dersId + " G\u00fcn:" + gun + " Saat:" + bas + "-" + bit);

                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO sinif_programlari (ders_id, gun, baslangic_saati, bitis_saati, salon, durum) VALUES (?, ?, ?, ?, ?, N'aktif')");
                ps.setInt(1, dersId);
                ps.setString(2, gun); // Ders ID ve gün
                // Saat formatı HH:mm ise HH:mm:00 olarak dönüştür (SQL Server TIME formatı)
                ps.setString(3, bas.contains(":") && bas.length() == 5 ? bas + ":00" : bas);
                ps.setString(4, bit.contains(":") && bit.length() == 5 ? bit + ":00" : bit);
                ps.setString(5, salon); // Salon
                int affected = ps.executeUpdate();
                ps.close(); // INSERT çalıştır

                System.out.println("API: Seans Eklendi. Etkilenen sat\u0131r: " + affected);
                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Program slotu eklendi.\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(ex, 500, errJson(e.getMessage(), "ERROR"));
            }
        }
    }

    // PROGRAM (SEANS) SİL — POST /api/program-sil [Admin]
    static class ProgramSilHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi
            try {
                String body = readBody(ex); // İstek gövdesini oku
                int pid = Integer.parseInt(jsonValue(body, "program_id")); // Silinecek program ID
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                PreparedStatement ps = conn.prepareStatement("DELETE FROM sinif_programlari WHERE program_id=?");
                ps.setInt(1, pid); // Program ID parametresi
                ps.executeUpdate();
                ps.close(); // DELETE çalıştır
                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Program slotu silindi.\"}");
            } catch (Exception e) {
                sendResponse(ex, 500, errJson(e.getMessage(), "ERROR"));
            }
        }
    }

    // ÜYE AKTİVİTE — GET /api/uye-aktivite [Giriş yapmış üye]
    // Bu haftanın günlerine göre üyenin toplam ders süresini (dakika) döndürür
    static class UyeAktiviteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            String[] u = authUser(ex);
            if (u == null) {
                sendResponse(ex, 401, "Auth Error");
                return;
            } // Yetki kontrolü
            int kid = Integer.parseInt(u[0]); // Kullanıcı ID
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                // Haftanın 7 günü için üyenin aktif rezervasyonlarının ders sürelerini topla
                PreparedStatement stmt = conn.prepareStatement(
                        "SET DATEFIRST 1; WITH Gunler AS (SELECT 1 AS d UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7) "
                                +
                                "SELECT g.d, ISNULL(SUM(s.sure), 0) as toplam_dk FROM Gunler g " +
                                "LEFT JOIN sinif_rezervasyonlari r ON DATEPART(dw, r.r_tarih) = g.d " +
                                "JOIN uyeler u ON r.uye_id = u.uye_id AND u.kullanici_id = ? " +
                                "JOIN sinif_programlari sp ON r.program_id = sp.program_id " +
                                "JOIN siniflar s ON sp.ders_id = s.ders_id WHERE r.durum=N'aktif' AND r.r_tarih >= DATEADD(DAY, 1-DATEPART(dw, GETDATE()), CAST(GETDATE() AS DATE)) "
                                +
                                "GROUP BY g.d ORDER BY g.d");
                stmt.setInt(1, kid);
                ResultSet rs = stmt.executeQuery(); // Kullanıcı ID parametresi
                // Sonuçları JSON dizisi olarak oluştur [dk1, dk2, ..., dk7]
                StringBuilder json = new StringBuilder("{\"aktivite\":[");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        json.append(",");
                    json.append(rs.getInt("toplam_dk"));
                    first = false;
                }
                json.append("]}");
                rs.close();
                stmt.close();
                sendResponse(ex, 200, json.toString());
            } catch (Exception e) {
                sendResponse(ex, 500, errJson(e.getMessage(), "ERROR"));
            }
        }
    }

    // ABONELİK GÜNCELLE — POST /api/abonelik-guncelle [Admin]
    // Abonelik tarihlerini günceller (başlangıç ve bitiş tarihi)
    static class AbonelikGuncelleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            if (!requireAdmin(ex))
                return; // Admin yetkisi
            try {
                String body = readBody(ex); // İstek gövdesini oku
                int aid = Integer.parseInt(jsonValue(body, "abonelik_id")); // Abonelik ID
                String bas = jsonValue(body, "baslangic"); // Yeni başlangıç tarihi
                String bit = jsonValue(body, "bitis"); // Yeni bitiş tarihi
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                // Abonelik tarihlerini güncelle
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE uye_abonelikleri SET baslangic_tarihi=?, bitis_tarihi=? WHERE abonelik_id=?");
                ps.setString(1, bas);
                ps.setString(2, bit);
                ps.setInt(3, aid); // Parametreleri set et
                ps.executeUpdate();
                ps.close(); // UPDATE çalıştır
                sendResponse(ex, 200, "{\"basarili\":true,\"mesaj\":\"Abonelik güncellendi!\"}");
            } catch (Exception e) {
                sendResponse(ex, 500, errJson(e.getMessage(), "ERROR"));
            }
        }
    }

    // KAMUYA AÇIK İSTATİSTİKLER — GET /api/public-istatistikler
    // Giriş yapmadan erişilebilir — ana sayfada gösterilen genel istatistikler
    static class PublicIstatistiklerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                corsHeaders(ex);
                ex.sendResponseHeaders(204, -1);
                return;
            } // CORS
            try {
                Connection conn = DatabaseBaglanti.baglantiGetir(); // DB bağlantısı
                Statement stmt = conn.createStatement();

                // Aktif abonelik sayısını getir
                ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM uye_abonelikleri WHERE durum=N'aktif'");
                int aktifUye = rs1.next() ? rs1.getInt(1) : 0;
                rs1.close();

                // Toplam ders sayısını getir
                ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM siniflar");
                int dersSayisi = rs2.next() ? rs2.getInt(1) : 0;
                rs2.close();

                // JSON olarak döndür
                sendResponse(ex, 200, String.format("{\"aktifUye\":%d,\"dersSayisi\":%d}", aktifUye, dersSayisi));
                stmt.close();
            } catch (Exception e) {
                sendResponse(ex, 500, errJson(e.getMessage(), "ERROR"));
            }
        }
    }
}
