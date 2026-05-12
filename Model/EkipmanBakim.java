package Model; // Bu sınıf Model paketine aittir

import java.util.Date; // Tarih işlemleri için

/**
 * EkipmanBakim — Ekipman bakım kayıtlarını temsil eden model sınıfı.
 * Veritabanındaki "ekipman_bakimi" tablosuna karşılık gelir.
 * Hangi ekipmanın ne zaman bakıma alındığını, maliyetini ve bir sonraki bakım tarihini takip eder.
 */
public class EkipmanBakim {
    private int bakimId;                 // Bakım kaydının benzersiz kimlik numarası (Primary Key)
    private int ekipmanId;               // Bakımı yapılan ekipmanın ID'si (Foreign Key → ekipman)
    private String ekipmanAdi;           // Ekipman adı — örn: "Kürek Çekme Makinesi", "Koşu Bandı"
    private Date bakimTarihi;            // Bakımın yapıldığı tarih
    private double maliyet;              // Bakım maliyeti (TL cinsinden)
    private String yapanKisi;            // Bakımı yapan kişi/firma — örn: "Teknik Servis A"
    private String aciklama;             // Bakım açıklaması — yapılan işlemlerin detayı
    private Date sonrakiBakimTarihi;     // Bir sonraki planlı bakım tarihi
    private String durum;                // Bakım durumu — "tamamlandi", "devam_ediyor" veya "planli"

    // Boş Constructor
    public EkipmanBakim() {}

    // Parametreli Constructor — Tüm bakım bilgilerini alarak nesne oluşturur
    public EkipmanBakim(int bakimId, int ekipmanId, String ekipmanAdi,
                        Date bakimTarihi, double maliyet, String yapanKisi,
                        String aciklama, Date sonrakiBakimTarihi, String durum) {
        this.bakimId = bakimId;                       // Bakım ID'sini ata
        this.ekipmanId = ekipmanId;                   // Ekipman ID'sini ata
        this.ekipmanAdi = ekipmanAdi;                 // Ekipman adını ata
        this.bakimTarihi = bakimTarihi;               // Bakım tarihini ata
        this.maliyet = maliyet;                       // Maliyeti ata
        this.yapanKisi = yapanKisi;                   // Yapan kişiyi ata
        this.aciklama = aciklama;                     // Açıklamayı ata
        this.sonrakiBakimTarihi = sonrakiBakimTarihi; // Sonraki bakım tarihini ata
        this.durum = durum;                           // Durumu ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getBakimId() { return bakimId; }                                          // Bakım ID döndür
    public void setBakimId(int bakimId) { this.bakimId = bakimId; }

    public int getEkipmanId() { return ekipmanId; }                                     // Ekipman ID döndür
    public void setEkipmanId(int ekipmanId) { this.ekipmanId = ekipmanId; }

    public String getEkipmanAdi() { return ekipmanAdi; }                                // Ekipman adını döndür
    public void setEkipmanAdi(String ekipmanAdi) { this.ekipmanAdi = ekipmanAdi; }

    public Date getBakimTarihi() { return bakimTarihi; }                                // Bakım tarihini döndür
    public void setBakimTarihi(Date bakimTarihi) { this.bakimTarihi = bakimTarihi; }

    public double getMaliyet() { return maliyet; }                                      // Maliyeti döndür
    public void setMaliyet(double maliyet) { this.maliyet = maliyet; }

    public String getYapanKisi() { return yapanKisi; }                                  // Yapan kişiyi döndür
    public void setYapanKisi(String yapanKisi) { this.yapanKisi = yapanKisi; }

    public String getAciklama() { return aciklama; }                                    // Açıklamayı döndür
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }

    public Date getSonrakiBakimTarihi() { return sonrakiBakimTarihi; }                  // Sonraki bakım tarihini döndür
    public void setSonrakiBakimTarihi(Date sonrakiBakimTarihi) { this.sonrakiBakimTarihi = sonrakiBakimTarihi; }

    public String getDurum() { return durum; }                                          // Durumu döndür
    public void setDurum(String durum) { this.durum = durum; }

    // toString() — Debug ve loglama amacıyla
    @Override
    public String toString() {
        return "EkipmanBakim{" +
                "bakimId=" + bakimId +
                ", ekipmanAdi='" + ekipmanAdi + '\'' +
                ", bakimTarihi=" + bakimTarihi +
                ", maliyet=" + maliyet +
                ", durum='" + durum + '\'' +
                '}';
    }
}
