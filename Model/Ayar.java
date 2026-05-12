package Model; // Bu sınıf Model paketine aittir

import java.util.Date; // Tarih işlemleri için Java Date sınıfını içe aktar

/**
 * Ayar — Sistemin genel yapılandırma ayarlarını temsil eden model sınıfı.
 * Veritabanındaki "ayarlar" tablosuna karşılık gelir.
 * Salon adı, çalışma saatleri, tema gibi sistem genelindeki konfigürasyon değerlerini saklar.
 */
public class Ayar {
    private int ayarId;                  // Ayarın benzersiz kimlik numarası (Primary Key)
    private String ayarAdi;              // Ayar adı — örn: "salon_adi", "calisma_saatleri", "tema", "max_uye_sayisi"
    private String ayarDegeri;           // Ayarın değeri — her ayar için farklı tipte değer tutabilir (string olarak saklanır)
    private String kategori;             // Ayar kategorisi — "genel", "bildirim" veya "guvenlik" olabilir (gruplama amaçlı)
    private Date guncellemeTarihi;       // Ayarın son güncellenme tarihi — ne zaman değiştirildiğini takip etmek için

    // Boş Constructor — Parametresiz yapıcı metot
    // Veritabanından veri okurken boş nesne oluşturup sonra setter ile doldurmak için kullanılır
    public Ayar() {}

    // Parametreli Constructor — Tüm alanları alarak nesne oluşturan yapıcı metot
    public Ayar(int ayarId, String ayarAdi, String ayarDegeri,
                String kategori, Date guncellemeTarihi) {
        this.ayarId = ayarId;                       // Ayar ID'sini ata
        this.ayarAdi = ayarAdi;                     // Ayar adını ata
        this.ayarDegeri = ayarDegeri;               // Ayar değerini ata
        this.kategori = kategori;                   // Kategori bilgisini ata
        this.guncellemeTarihi = guncellemeTarihi;   // Güncelleme tarihini ata
    }

    // ─── Getter ve Setter Metotları ───

    public int getAyarId() { return ayarId; }                                              // Ayar ID'sini döndür
    public void setAyarId(int ayarId) { this.ayarId = ayarId; }                            // Ayar ID'sini güncelle

    public String getAyarAdi() { return ayarAdi; }                                         // Ayar adını döndür
    public void setAyarAdi(String ayarAdi) { this.ayarAdi = ayarAdi; }                     // Ayar adını güncelle

    public String getAyarDegeri() { return ayarDegeri; }                                   // Ayar değerini döndür
    public void setAyarDegeri(String ayarDegeri) { this.ayarDegeri = ayarDegeri; }          // Ayar değerini güncelle

    public String getKategori() { return kategori; }                                       // Kategoriyi döndür
    public void setKategori(String kategori) { this.kategori = kategori; }                 // Kategoriyi güncelle

    public Date getGuncellemeTarihi() { return guncellemeTarihi; }                          // Güncelleme tarihini döndür
    public void setGuncellemeTarihi(Date guncellemeTarihi) { this.guncellemeTarihi = guncellemeTarihi; } // Güncelleme tarihini güncelle

    // toString() metodu — Debug ve loglama amacıyla nesneyi metin formatında döndürür
    @Override
    public String toString() {
        return "Ayar{" +
                "ayarId=" + ayarId +                    // Ayar ID
                ", ayarAdi='" + ayarAdi + '\'' +        // Ayar adı
                ", ayarDegeri='" + ayarDegeri + '\'' +  // Ayar değeri
                ", kategori='" + kategori + '\'' +      // Kategori
                '}';
    }
}
