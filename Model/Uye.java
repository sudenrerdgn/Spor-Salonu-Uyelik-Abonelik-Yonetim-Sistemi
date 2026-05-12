package Model; // Bu sınıf Model paketine aittir

import java.util.Date; // Tarih işlemleri için

/**
 * Uye — Spor salonuna üye olmuş kişileri temsil eden model sınıfı.
 * Veritabanındaki "uyeler" ve "kullanicilar" tablolarından gelen verileri birleştirir.
 * Bir kullanıcı abonelik satın aldığında "uye" rolüne yükseltilir ve uyeler tablosuna eklenir.
 */
public class Uye {
    private int uyeId;                   // Üyenin benzersiz kimlik numarası (Primary Key — uyeler tablosu)
    private String ad;                   // Üyenin adı
    private String soyad;                // Üyenin soyadı
    private String email;                // Üyenin e-posta adresi
    private String telefon;              // Üyenin telefon numarası
    private String cinsiyet;             // Üyenin cinsiyeti — "Erkek" veya "Kadın"
    private String uyelikNo;             // Benzersiz üyelik numarası — örn: "FZ-2026-001" (otomatik oluşturulur)
    private String plan;                 // Mevcut abonelik planı — "platinum", "gold", "silver" veya "basic"
    private String durum;                // Üyelik durumu — "aktif", "pasif" veya "suresi_doldu"
    private Date kayitTarihi;            // Üyelik kayıt tarihi — sisteme ilk kayıt olunan tarih
    private Date abonelikBaslangic;      // Aktif aboneliğin başlangıç tarihi
    private Date abonelikBitis;          // Aktif aboneliğin bitiş tarihi
    private String odemeYontemi;         // Son kullanılan ödeme yöntemi — "kredi_karti", "nakit", "havale", "online"
    private double odemeTutari;          // Son ödeme tutarı (TL cinsinden)
    private String acilDurumKisi;        // Acil durumda aranacak kişi bilgisi
    private String saglikNotu;           // Sağlık notu — varsa alerjiler, kronik hastalıklar vb.

    // Boş Constructor
    public Uye() {}

    // Parametreli Constructor — Temel üye bilgilerini alarak nesne oluşturur
    public Uye(int uyeId, String ad, String soyad, String email, String telefon,
               String cinsiyet, String uyelikNo, String plan, String durum,
               Date kayitTarihi, Date abonelikBaslangic, Date abonelikBitis,
               String odemeYontemi, double odemeTutari) {
        this.uyeId = uyeId;                         // Üye ID'sini ata
        this.ad = ad;                               // Adı ata
        this.soyad = soyad;                         // Soyadı ata
        this.email = email;                         // E-posta ata
        this.telefon = telefon;                     // Telefonu ata
        this.cinsiyet = cinsiyet;                   // Cinsiyeti ata
        this.uyelikNo = uyelikNo;                   // Üyelik numarasını ata
        this.plan = plan;                           // Planı ata
        this.durum = durum;                         // Durumu ata
        this.kayitTarihi = kayitTarihi;             // Kayıt tarihini ata
        this.abonelikBaslangic = abonelikBaslangic; // Abonelik başlangıcını ata
        this.abonelikBitis = abonelikBitis;         // Abonelik bitişini ata
        this.odemeYontemi = odemeYontemi;           // Ödeme yöntemini ata
        this.odemeTutari = odemeTutari;             // Ödeme tutarını ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getUyeId() { return uyeId; }                                         // Üye ID döndür
    public void setUyeId(int uyeId) { this.uyeId = uyeId; }

    public String getAd() { return ad; }                                             // Ad döndür
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }                                       // Soyad döndür
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getEmail() { return email; }                                       // E-posta döndür
    public void setEmail(String email) { this.email = email; }

    public String getTelefon() { return telefon; }                                   // Telefon döndür
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getCinsiyet() { return cinsiyet; }                                 // Cinsiyet döndür
    public void setCinsiyet(String cinsiyet) { this.cinsiyet = cinsiyet; }

    public String getUyelikNo() { return uyelikNo; }                                // Üyelik numarası döndür
    public void setUyelikNo(String uyelikNo) { this.uyelikNo = uyelikNo; }

    public String getPlan() { return plan; }                                         // Plan adı döndür
    public void setPlan(String plan) { this.plan = plan; }

    public String getDurum() { return durum; }                                       // Durum döndür
    public void setDurum(String durum) { this.durum = durum; }

    public Date getKayitTarihi() { return kayitTarihi; }                             // Kayıt tarihi döndür
    public void setKayitTarihi(Date kayitTarihi) { this.kayitTarihi = kayitTarihi; }

    public Date getAbonelikBaslangic() { return abonelikBaslangic; }                 // Abonelik başlangıcı döndür
    public void setAbonelikBaslangic(Date abonelikBaslangic) { this.abonelikBaslangic = abonelikBaslangic; }

    public Date getAbonelikBitis() { return abonelikBitis; }                         // Abonelik bitişi döndür
    public void setAbonelikBitis(Date abonelikBitis) { this.abonelikBitis = abonelikBitis; }

    public String getOdemeYontemi() { return odemeYontemi; }                         // Ödeme yöntemi döndür
    public void setOdemeYontemi(String odemeYontemi) { this.odemeYontemi = odemeYontemi; }

    public double getOdemeTutari() { return odemeTutari; }                           // Ödeme tutarı döndür
    public void setOdemeTutari(double odemeTutari) { this.odemeTutari = odemeTutari; }

    public String getAcilDurumKisi() { return acilDurumKisi; }                       // Acil durum kişisi döndür
    public void setAcilDurumKisi(String acilDurumKisi) { this.acilDurumKisi = acilDurumKisi; }

    public String getSaglikNotu() { return saglikNotu; }                             // Sağlık notu döndür
    public void setSaglikNotu(String saglikNotu) { this.saglikNotu = saglikNotu; }

    // Yardımcı metot — Ad ve soyadı birleştirerek tam isim döndürür
    public String getAdSoyad() {
        return ad + " " + soyad; // Ad ve soyadı boşlukla birleştir
    }

    // toString() — Debug ve loglama amacıyla
    @Override
    public String toString() {
        return "Uye{" +
                "uyeId=" + uyeId +
                ", ad='" + ad + '\'' +
                ", soyad='" + soyad + '\'' +
                ", email='" + email + '\'' +
                ", uyelikNo='" + uyelikNo + '\'' +
                ", plan='" + plan + '\'' +
                ", durum='" + durum + '\'' +
                '}';
    }
}
