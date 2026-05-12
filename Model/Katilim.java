package Model; // Bu sınıf Model paketine aittir

import java.util.Date; // Tarih işlemleri için

/**
 * Katilim — Üyelerin derslere katılım kayıtlarını temsil eden model sınıfı.
 * Veritabanındaki "sinif_rezervasyonlari" tablosuna karşılık gelir.
 * Hangi üyenin hangi derse, ne zaman katıldığını takip eder.
 */
public class Katilim {
    private int katilimId;               // Katılım kaydının benzersiz kimlik numarası (Primary Key)
    private int uyeId;                   // Katılan üyenin ID'si (Foreign Key → uyeler)
    private String uyeAdi;               // Üyenin tam adı (görüntüleme amaçlı)
    private int dersId;                  // Katılınan dersin ID'si (Foreign Key → siniflar)
    private String dersAdi;              // Dersin adı (görüntüleme amaçlı)
    private Date tarih;                  // Katılım tarihi
    private String saat;                 // Ders saati — örn: "08:00–09:00"
    private String gun;                  // Ders günü — "Pazartesi", "Salı", "Çarşamba" vb.
    private String salon;                // Dersin yapıldığı salon — "Salon A", "Salon B", "Havuz"
    private String durum;                // Katılım durumu — "aktif", "iptal" veya "tamamlandi"

    // Boş Constructor
    public Katilim() {}

    // Parametreli Constructor — Tüm katılım bilgilerini alarak nesne oluşturur
    public Katilim(int katilimId, int uyeId, String uyeAdi, int dersId,
                   String dersAdi, Date tarih, String saat, String gun,
                   String salon, String durum) {
        this.katilimId = katilimId;     // Katılım ID'sini ata
        this.uyeId = uyeId;            // Üye ID'sini ata
        this.uyeAdi = uyeAdi;          // Üye adını ata
        this.dersId = dersId;          // Ders ID'sini ata
        this.dersAdi = dersAdi;        // Ders adını ata
        this.tarih = tarih;            // Tarihi ata
        this.saat = saat;              // Saati ata
        this.gun = gun;                // Günü ata
        this.salon = salon;            // Salonu ata
        this.durum = durum;            // Durumu ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getKatilimId() { return katilimId; }                                 // Katılım ID'sini döndür
    public void setKatilimId(int katilimId) { this.katilimId = katilimId; }

    public int getUyeId() { return uyeId; }                                         // Üye ID'sini döndür
    public void setUyeId(int uyeId) { this.uyeId = uyeId; }

    public String getUyeAdi() { return uyeAdi; }                                   // Üye adını döndür
    public void setUyeAdi(String uyeAdi) { this.uyeAdi = uyeAdi; }

    public int getDersId() { return dersId; }                                       // Ders ID'sini döndür
    public void setDersId(int dersId) { this.dersId = dersId; }

    public String getDersAdi() { return dersAdi; }                                  // Ders adını döndür
    public void setDersAdi(String dersAdi) { this.dersAdi = dersAdi; }

    public Date getTarih() { return tarih; }                                        // Tarihi döndür
    public void setTarih(Date tarih) { this.tarih = tarih; }

    public String getSaat() { return saat; }                                        // Saati döndür
    public void setSaat(String saat) { this.saat = saat; }

    public String getGun() { return gun; }                                          // Günü döndür
    public void setGun(String gun) { this.gun = gun; }

    public String getSalon() { return salon; }                                      // Salonu döndür
    public void setSalon(String salon) { this.salon = salon; }

    public String getDurum() { return durum; }                                      // Durumu döndür
    public void setDurum(String durum) { this.durum = durum; }

    // toString() — Debug ve loglama amacıyla
    @Override
    public String toString() {
        return "Katilim{" +
                "katilimId=" + katilimId +
                ", uyeAdi='" + uyeAdi + '\'' +
                ", dersAdi='" + dersAdi + '\'' +
                ", tarih=" + tarih +
                ", durum='" + durum + '\'' +
                '}';
    }
}
