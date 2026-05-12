package Model; // Bu sınıf Model paketine aittir

import java.util.Date; // Tarih işlemleri için

/**
 * GirisCikis — Üyelerin salona giriş/çıkış kayıtlarını temsil eden model sınıfı.
 * Veritabanındaki "giris_cikis_kayitlari" tablosuna karşılık gelir.
 * Üyenin salona ne zaman girdiğini, ne zaman çıktığını ve giriş türünü takip eder.
 */
public class GirisCikis {
    private int kayitId;                 // Giriş/çıkış kaydının benzersiz kimlik numarası (Primary Key)
    private int uyeId;                   // Giriş yapan üyenin ID'si (Foreign Key → uyeler)
    private String uyeAdi;               // Üyenin tam adı (görüntüleme amaçlı)
    private String girisSaati;           // Giriş saati — örn: "07:30" (HH:mm formatında)
    private String cikisSaati;           // Çıkış saati — örn: "09:15" veya null (üye hala içerideyse)
    private String girisTuru;            // Giriş türü — "normal" (manuel), "qr" (QR kod) veya "kart" (kart okuyucu)
    private String durum;                // Kayıt durumu — "giris" (hala içeride) veya "cikis" (çıkış yapılmış)
    private Date tarih;                  // Giriş/çıkışın yapıldığı tarih

    // Boş Constructor
    public GirisCikis() {}

    // Parametreli Constructor — Tüm alanları alarak nesne oluşturur
    public GirisCikis(int kayitId, int uyeId, String uyeAdi,
                      String girisSaati, String cikisSaati,
                      String girisTuru, String durum, Date tarih) {
        this.kayitId = kayitId;           // Kayıt ID'sini ata
        this.uyeId = uyeId;              // Üye ID'sini ata
        this.uyeAdi = uyeAdi;            // Üye adını ata
        this.girisSaati = girisSaati;    // Giriş saatini ata
        this.cikisSaati = cikisSaati;    // Çıkış saatini ata
        this.girisTuru = girisTuru;      // Giriş türünü ata
        this.durum = durum;              // Durumu ata
        this.tarih = tarih;              // Tarihi ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getKayitId() { return kayitId; }                                    // Kayıt ID'sini döndür
    public void setKayitId(int kayitId) { this.kayitId = kayitId; }

    public int getUyeId() { return uyeId; }                                        // Üye ID'sini döndür
    public void setUyeId(int uyeId) { this.uyeId = uyeId; }

    public String getUyeAdi() { return uyeAdi; }                                  // Üye adını döndür
    public void setUyeAdi(String uyeAdi) { this.uyeAdi = uyeAdi; }

    public String getGirisSaati() { return girisSaati; }                           // Giriş saatini döndür
    public void setGirisSaati(String girisSaati) { this.girisSaati = girisSaati; }

    public String getCikisSaati() { return cikisSaati; }                           // Çıkış saatini döndür
    public void setCikisSaati(String cikisSaati) { this.cikisSaati = cikisSaati; }

    public String getGirisTuru() { return girisTuru; }                             // Giriş türünü döndür
    public void setGirisTuru(String girisTuru) { this.girisTuru = girisTuru; }

    public String getDurum() { return durum; }                                     // Durumu döndür
    public void setDurum(String durum) { this.durum = durum; }

    public Date getTarih() { return tarih; }                                       // Tarihi döndür
    public void setTarih(Date tarih) { this.tarih = tarih; }

    // Yardımcı metot — Üye şu an salonda mı kontrolü
    // Durum "giris" ise üye hala içeridedir
    public boolean isIceride() {
        return "giris".equals(durum); // Durum "giris" ise true döndür (üye içeride)
    }

    // toString() — Debug ve loglama amacıyla
    @Override
    public String toString() {
        return "GirisCikis{" +
                "kayitId=" + kayitId +
                ", uyeAdi='" + uyeAdi + '\'' +
                ", girisSaati='" + girisSaati + '\'' +
                ", cikisSaati='" + cikisSaati + '\'' +
                ", girisTuru='" + girisTuru + '\'' +
                ", durum='" + durum + '\'' +
                '}';
    }
}
