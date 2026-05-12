package Model; // Bu sınıf Model paketine aittir

import java.util.Date; // Tarih işlemleri için

/**
 * Ekipman — Spor salonundaki ekipmanları temsil eden model sınıfı.
 * Veritabanındaki "ekipman" tablosuna karşılık gelir.
 * Koşu Bandı, Eliptik Bisiklet gibi aletlerin bilgilerini tutar.
 */
public class Ekipman {
    private int ekipmanId;               // Ekipmanın benzersiz kimlik numarası (Primary Key)
    private String ad;                   // Ekipman adı — örn: "Koşu Bandı", "Eliptik Bisiklet", "Dumbbell Set"
    private String kategori;             // Ekipman kategorisi — "Kardio", "Güç" veya "Esneklik"
    private int adet;                    // Ekipmandan kaç adet bulunduğu (stok miktarı)
    private Date satinAlmaTarihi;        // Ekipmanın satın alındığı tarih
    private double fiyat;                // Ekipmanın satın alma fiyatı (TL cinsinden)
    private String durum;                // Ekipmanın mevcut durumu — "calisiyor", "bakimda" veya "arizali"

    // Boş Constructor
    public Ekipman() {}

    // Parametreli Constructor — Tüm ekipman bilgilerini alarak nesne oluşturur
    public Ekipman(int ekipmanId, String ad, String kategori, int adet,
                   Date satinAlmaTarihi, double fiyat, String durum) {
        this.ekipmanId = ekipmanId;             // Ekipman ID'sini ata
        this.ad = ad;                           // Ekipman adını ata
        this.kategori = kategori;               // Kategoriyi ata
        this.adet = adet;                       // Adet sayısını ata
        this.satinAlmaTarihi = satinAlmaTarihi; // Satın alma tarihini ata
        this.fiyat = fiyat;                     // Fiyatı ata
        this.durum = durum;                     // Durumu ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getEkipmanId() { return ekipmanId; }                                   // Ekipman ID'sini döndür
    public void setEkipmanId(int ekipmanId) { this.ekipmanId = ekipmanId; }

    public String getAd() { return ad; }                                               // Ekipman adını döndür
    public void setAd(String ad) { this.ad = ad; }

    public String getKategori() { return kategori; }                                   // Kategoriyi döndür
    public void setKategori(String kategori) { this.kategori = kategori; }

    public int getAdet() { return adet; }                                              // Adet sayısını döndür
    public void setAdet(int adet) { this.adet = adet; }

    public Date getSatinAlmaTarihi() { return satinAlmaTarihi; }                       // Satın alma tarihini döndür
    public void setSatinAlmaTarihi(Date satinAlmaTarihi) { this.satinAlmaTarihi = satinAlmaTarihi; }

    public double getFiyat() { return fiyat; }                                         // Fiyatı döndür
    public void setFiyat(double fiyat) { this.fiyat = fiyat; }

    public String getDurum() { return durum; }                                         // Durumu döndür
    public void setDurum(String durum) { this.durum = durum; }

    // toString() — Debug ve loglama amacıyla
    @Override
    public String toString() {
        return "Ekipman{" +
                "ekipmanId=" + ekipmanId +
                ", ad='" + ad + '\'' +
                ", kategori='" + kategori + '\'' +
                ", adet=" + adet +
                ", fiyat=" + fiyat +
                ", durum='" + durum + '\'' +
                '}';
    }
}
