package Model; // Bu sınıf Model paketine aittir

import java.util.Date; // Tarih işlemleri için

/**
 * SistemLog — Sistemde yapılan işlemlerin log (kayıt) kaydını temsil eden model sınıfı.
 * Veritabanındaki "sistem_loglari" tablosuna karşılık gelir.
 * Kim, ne zaman, ne yaptı gibi audit trail (denetim izleme) bilgilerini saklar.
 */
public class SistemLog {
    private int logId;                   // Log kaydının benzersiz kimlik numarası (Primary Key)
    private int kullaniciId;             // İşlemi yapan kullanıcının ID'si (Foreign Key → kullanicilar)
    private String kullaniciAdi;         // İşlemi yapan kullanıcının adı (görüntüleme amaçlı)
    private String islem;                // Yapılan işlem türü — "giris", "cikis", "uye_ekleme", "odeme_alma" vb.
    private String detay;                // İşlemin detay açıklaması — ne yapıldığının metin olarak kaydı
    private String ipAdresi;             // İşlemin yapıldığı IP adresi — güvenlik takibi için
    private Date islemTarihi;            // İşlemin yapıldığı tarih ve saat

    // Boş Constructor
    public SistemLog() {}

    // Parametreli Constructor — Tüm log bilgilerini alarak nesne oluşturur
    public SistemLog(int logId, int kullaniciId, String kullaniciAdi,
                     String islem, String detay, String ipAdresi, Date islemTarihi) {
        this.logId = logId;                   // Log ID'sini ata
        this.kullaniciId = kullaniciId;       // Kullanıcı ID'sini ata
        this.kullaniciAdi = kullaniciAdi;     // Kullanıcı adını ata
        this.islem = islem;                   // İşlem türünü ata
        this.detay = detay;                   // İşlem detayını ata
        this.ipAdresi = ipAdresi;             // IP adresini ata
        this.islemTarihi = islemTarihi;       // İşlem tarihini ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getLogId() { return logId; }                                        // Log ID döndür
    public void setLogId(int logId) { this.logId = logId; }

    public int getKullaniciId() { return kullaniciId; }                            // Kullanıcı ID döndür
    public void setKullaniciId(int kullaniciId) { this.kullaniciId = kullaniciId; }

    public String getKullaniciAdi() { return kullaniciAdi; }                       // Kullanıcı adı döndür
    public void setKullaniciAdi(String kullaniciAdi) { this.kullaniciAdi = kullaniciAdi; }

    public String getIslem() { return islem; }                                     // İşlem türünü döndür
    public void setIslem(String islem) { this.islem = islem; }

    public String getDetay() { return detay; }                                     // Detayı döndür
    public void setDetay(String detay) { this.detay = detay; }

    public String getIpAdresi() { return ipAdresi; }                               // IP adresini döndür
    public void setIpAdresi(String ipAdresi) { this.ipAdresi = ipAdresi; }

    public Date getIslemTarihi() { return islemTarihi; }                            // İşlem tarihini döndür
    public void setIslemTarihi(Date islemTarihi) { this.islemTarihi = islemTarihi; }

    // toString() — Debug ve loglama amacıyla
    @Override
    public String toString() {
        return "SistemLog{" +
                "logId=" + logId +
                ", kullaniciAdi='" + kullaniciAdi + '\'' +
                ", islem='" + islem + '\'' +
                ", islemTarihi=" + islemTarihi +
                '}';
    }
}
