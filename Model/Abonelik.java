package Model; // Bu sınıf Model paketine aittir — veritabanı tablolarını temsil eden Java nesneleri burada tanımlanır

import java.util.Date; // Tarih işlemleri için Java Date sınıfını içe aktar

/**
 * Abonelik — Üyelerin sahip olduğu abonelik bilgilerini temsil eden model sınıfı.
 * Veritabanındaki "uye_abonelikleri" tablosuna karşılık gelir.
 * Her üye en fazla bir aktif aboneliğe sahip olabilir.
 */
public class Abonelik {
    private int abonelikId;             // Aboneliğin benzersiz kimlik numarası (Primary Key — veritabanında auto-increment)
    private int uyeId;                  // Bu aboneliğin ait olduğu üyenin kimlik numarası (Foreign Key → uyeler tablosu)
    private String uyeAdi;              // Üyenin tam adı (ad + soyad birleşimi — görüntüleme amaçlı)
    private String plan;                // Abonelik planı adı — Platinum, Gold, Silver veya Basic olabilir
    private Date baslangicTarihi;       // Aboneliğin başlama tarihi — plan satın alındığında set edilir
    private Date bitisTarihi;           // Aboneliğin bitiş tarihi — plan süresine göre hesaplanır (1, 3, 6 veya 12 ay sonra)
    private boolean otomatikYenileme;   // Otomatik yenileme aktif mi? true ise süre dolduğunda abonelik otomatik uzatılır
    private String durum;               // Aboneliğin mevcut durumu — "aktif", "pasif", "suresi_doldu" veya "iptal" değerlerini alabilir

    // Boş Constructor — Parametresiz yapıcı metot
    // Java Bean standartları gereği ve veritabanından veri okurken (ResultSet→Nesne dönüşümü) kullanılır
    public Abonelik() {}

    // Parametreli Constructor — Tüm alanları alarak nesne oluşturan yapıcı metot
    // Veritabanından okunan verileri doğrudan nesneye atamak için kullanılır
    public Abonelik(int abonelikId, int uyeId, String uyeAdi, String plan,
                    Date baslangicTarihi, Date bitisTarihi,
                    boolean otomatikYenileme, String durum) {
        this.abonelikId = abonelikId;             // Gelen abonelik ID'sini ata
        this.uyeId = uyeId;                       // Gelen üye ID'sini ata
        this.uyeAdi = uyeAdi;                     // Gelen üye adını ata
        this.plan = plan;                         // Gelen plan adını ata (Platinum/Gold/Silver/Basic)
        this.baslangicTarihi = baslangicTarihi;   // Gelen başlangıç tarihini ata
        this.bitisTarihi = bitisTarihi;           // Gelen bitiş tarihini ata
        this.otomatikYenileme = otomatikYenileme; // Gelen otomatik yenileme durumunu ata
        this.durum = durum;                       // Gelen durumu ata
    }

    // ─── Getter ve Setter Metotları ───
    // Java Bean standartları gereği her private alan için getter (okuma) ve setter (yazma) metotları tanımlanır
    // Bu metotlar sayesinde dışarıdan alanlara kontrollü erişim sağlanır

    public int getAbonelikId() { return abonelikId; }                              // Abonelik ID'sini döndür
    public void setAbonelikId(int abonelikId) { this.abonelikId = abonelikId; }     // Abonelik ID'sini güncelle

    public int getUyeId() { return uyeId; }                                        // Üye ID'sini döndür
    public void setUyeId(int uyeId) { this.uyeId = uyeId; }                        // Üye ID'sini güncelle

    public String getUyeAdi() { return uyeAdi; }                                   // Üye adını döndür
    public void setUyeAdi(String uyeAdi) { this.uyeAdi = uyeAdi; }                 // Üye adını güncelle

    public String getPlan() { return plan; }                                        // Plan adını döndür (Platinum/Gold/Silver/Basic)
    public void setPlan(String plan) { this.plan = plan; }                          // Plan adını güncelle

    public Date getBaslangicTarihi() { return baslangicTarihi; }                    // Başlangıç tarihini döndür
    public void setBaslangicTarihi(Date baslangicTarihi) { this.baslangicTarihi = baslangicTarihi; } // Başlangıç tarihini güncelle

    public Date getBitisTarihi() { return bitisTarihi; }                            // Bitiş tarihini döndür
    public void setBitisTarihi(Date bitisTarihi) { this.bitisTarihi = bitisTarihi; } // Bitiş tarihini güncelle

    public boolean isOtomatikYenileme() { return otomatikYenileme; }                // Otomatik yenileme durumunu döndür (true/false)
    public void setOtomatikYenileme(boolean otomatikYenileme) { this.otomatikYenileme = otomatikYenileme; } // Otomatik yenileme durumunu güncelle

    public String getDurum() { return durum; }                                      // Abonelik durumunu döndür (aktif/pasif/suresi_doldu/iptal)
    public void setDurum(String durum) { this.durum = durum; }                      // Abonelik durumunu güncelle

    // toString() metodu — Nesneyi okunabilir bir metin olarak döndürür
    // Debug (hata ayıklama) ve loglama amacıyla kullanılır
    // System.out.println(abonelik) dediğinizde bu metot çağrılır
    @Override
    public String toString() {
        return "Abonelik{" +
                "abonelikId=" + abonelikId +       // Abonelik ID bilgisi
                ", uyeAdi='" + uyeAdi + '\'' +     // Üye adı bilgisi
                ", plan='" + plan + '\'' +          // Plan adı bilgisi
                ", durum='" + durum + '\'' +        // Durum bilgisi
                ", otomatikYenileme=" + otomatikYenileme + // Otomatik yenileme bilgisi
                '}';
    }
}
