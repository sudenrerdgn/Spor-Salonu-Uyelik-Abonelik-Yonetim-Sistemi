package Model; // Bu sınıf Model paketine aittir

import java.util.Date; // Tarih işlemleri için

/**
 * Odeme — Üyelerin yaptığı ödemeleri temsil eden model sınıfı.
 * Veritabanındaki "odemeler" tablosuna karşılık gelir.
 * Abonelik satın alma sonucunda oluşan ödeme kayıtlarını tutar.
 */
public class Odeme {
    private int odemeId;                 // Ödemenin benzersiz kimlik numarası (Primary Key)
    private int uyeId;                   // Ödemeyi yapan üyenin ID'si (Foreign Key → uyeler)
    private String uyeAdi;               // Üyenin tam adı (görüntüleme amaçlı)
    private String plan;                 // Ödenen plan — "Platinum", "Gold", "Silver" veya "Basic"
    private double miktar;               // Ödeme miktarı (TL cinsinden)
    private String odemeYontemi;         // Ödeme yöntemi — "Kredi Kartı", "Nakit", "Havale" veya "Online"
    private Date odemeTarihi;            // Ödemenin yapıldığı tarih
    private String durum;                // Ödeme durumu — "tamamlandi", "beklemede", "basarisiz" veya "iade"

    // Boş Constructor
    public Odeme() {
    }

    // Parametreli Constructor — Tüm ödeme bilgilerini alarak nesne oluşturur
    public Odeme(int odemeId, int uyeId, String uyeAdi, String plan,
            double miktar, String odemeYontemi, Date odemeTarihi, String durum) {
        this.odemeId = odemeId;               // Ödeme ID'sini ata
        this.uyeId = uyeId;                  // Üye ID'sini ata
        this.uyeAdi = uyeAdi;               // Üye adını ata
        this.plan = plan;                     // Plan adını ata
        this.miktar = miktar;                 // Miktarı ata
        this.odemeYontemi = odemeYontemi;     // Ödeme yöntemini ata
        this.odemeTarihi = odemeTarihi;       // Ödeme tarihini ata
        this.durum = durum;                   // Durumu ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getOdemeId() { return odemeId; }                                       // Ödeme ID'sini döndür
    public void setOdemeId(int odemeId) { this.odemeId = odemeId; }

    public int getUyeId() { return uyeId; }                                           // Üye ID'sini döndür
    public void setUyeId(int uyeId) { this.uyeId = uyeId; }

    public String getUyeAdi() { return uyeAdi; }                                     // Üye adını döndür
    public void setUyeAdi(String uyeAdi) { this.uyeAdi = uyeAdi; }

    public String getPlan() { return plan; }                                           // Plan adını döndür
    public void setPlan(String plan) { this.plan = plan; }

    public double getMiktar() { return miktar; }                                       // Miktarı döndür
    public void setMiktar(double miktar) { this.miktar = miktar; }

    public String getOdemeYontemi() { return odemeYontemi; }                           // Ödeme yöntemini döndür
    public void setOdemeYontemi(String odemeYontemi) { this.odemeYontemi = odemeYontemi; }

    public Date getOdemeTarihi() { return odemeTarihi; }                               // Ödeme tarihini döndür
    public void setOdemeTarihi(Date odemeTarihi) { this.odemeTarihi = odemeTarihi; }

    public String getDurum() { return durum; }                                         // Durumu döndür
    public void setDurum(String durum) { this.durum = durum; }

    // toString() — Debug ve loglama amacıyla
    @Override
    public String toString() {
        return "Odeme{" +
                "odemeId=" + odemeId +
                ", uyeAdi='" + uyeAdi + '\'' +
                ", miktar=" + miktar +
                ", odemeYontemi='" + odemeYontemi + '\'' +
                ", durum='" + durum + '\'' +
                '}';
    }
}
