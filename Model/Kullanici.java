package Model; // Bu sınıf Model paketine aittir

import java.util.Date; // Tarih işlemleri için

/**
 * Kullanici — Sisteme kayıtlı tüm kullanıcıları temsil eden model sınıfı.
 * Veritabanındaki "kullanicilar" tablosuna karşılık gelir.
 * Admin, üye, antrenör ve kullanıcı rollerinin hepsi bu tabloda saklanır.
 */
public class Kullanici {
    private int kullaniciId;             // Kullanıcının benzersiz kimlik numarası (Primary Key — auto-increment)
    private String ad;                   // Kullanıcının adı
    private String soyad;                // Kullanıcının soyadı
    private String email;                // Kullanıcının e-posta adresi — giriş yapmak için kullanılır (UNIQUE)
    private String sifre;                // Şifre hash'i (SHA-256) — açık metin değil, hash olarak saklanır
    private String rolAdi;               // Kullanıcının rolü — "admin", "uye", "antrenor" veya "kullanici"
    private boolean aktifMi;             // Hesap aktif mi? false ise kullanıcı giriş yapamaz
    private Date olusturmaTarihi;        // Hesabın oluşturulma tarihi — kayıt tarihi

    // Boş Constructor
    public Kullanici() {}

    // Parametreli Constructor — Tüm kullanıcı bilgilerini alarak nesne oluşturur
    public Kullanici(int kullaniciId, String ad, String soyad, String email,
                     String sifre, String rolAdi, boolean aktifMi, Date olusturmaTarihi) {
        this.kullaniciId = kullaniciId;         // Kullanıcı ID'sini ata
        this.ad = ad;                           // Adı ata
        this.soyad = soyad;                     // Soyadı ata
        this.email = email;                     // E-posta adresini ata
        this.sifre = sifre;                     // Şifre hash'ini ata
        this.rolAdi = rolAdi;                   // Rol adını ata
        this.aktifMi = aktifMi;                 // Aktiflik durumunu ata
        this.olusturmaTarihi = olusturmaTarihi; // Oluşturma tarihini ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getKullaniciId() { return kullaniciId; }                              // Kullanıcı ID'sini döndür
    public void setKullaniciId(int kullaniciId) { this.kullaniciId = kullaniciId; }

    public String getAd() { return ad; }                                             // Adı döndür
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }                                       // Soyadı döndür
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getEmail() { return email; }                                       // E-posta döndür
    public void setEmail(String email) { this.email = email; }

    public String getSifre() { return sifre; }                                       // Şifre hash'ini döndür
    public void setSifre(String sifre) { this.sifre = sifre; }

    public String getRolAdi() { return rolAdi; }                                     // Rol adını döndür
    public void setRolAdi(String rolAdi) { this.rolAdi = rolAdi; }

    public boolean isAktifMi() { return aktifMi; }                                   // Aktiflik durumunu döndür
    public void setAktifMi(boolean aktifMi) { this.aktifMi = aktifMi; }

    public Date getOlusturmaTarihi() { return olusturmaTarihi; }                     // Oluşturma tarihini döndür
    public void setOlusturmaTarihi(Date olusturmaTarihi) { this.olusturmaTarihi = olusturmaTarihi; }

    // Yardımcı metot — Ad ve soyadı birleştirerek tam isim döndürür
    public String getAdSoyad() {
        return ad + " " + soyad; // Ad ve soyadı boşlukla birleştir
    }

    // toString() — Debug ve loglama amacıyla
    @Override
    public String toString() {
        return "Kullanici{" +
                "kullaniciId=" + kullaniciId +
                ", ad='" + ad + '\'' +
                ", soyad='" + soyad + '\'' +
                ", email='" + email + '\'' +
                ", rolAdi='" + rolAdi + '\'' +
                ", aktifMi=" + aktifMi +
                '}';
    }
}
