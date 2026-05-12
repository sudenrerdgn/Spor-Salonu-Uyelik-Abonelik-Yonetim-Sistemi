package Model; // Bu sınıf Model paketine aittir — veritabanı varlıklarını temsil eden sınıflar bu pakette yer alır

/**
 * Antrenor — Spor salonunda ders veren antrenörleri temsil eden model sınıfı.
 * Veritabanındaki "antrenorler" ve "kullanicilar" tablolarından gelen verileri birleştirir.
 * Her antrenör aynı zamanda bir kullanıcıdır (kullanicilar tablosunda rol_id=3 olarak kaydedilir).
 */
public class Antrenor {
    private int antrenorId;              // Antrenörün benzersiz kimlik numarası (Primary Key — antrenorler tablosu)
    private String ad;                   // Antrenörün adı (kullanicilar tablosundan gelir)
    private String soyad;                // Antrenörün soyadı (kullanicilar tablosundan gelir)
    private String email;                // Antrenörün e-posta adresi — sisteme giriş yapmak için kullanılır
    private String telefon;              // Antrenörün telefon numarası (iletişim bilgisi)
    private String uzmanlik;             // Uzmanlık alanı — örn: "Fonksiyonel Fitness", "Crossfit", "Yoga"
    private int deneyimYili;             // Deneyim süresi (yıl cinsinden) — antrenörün kaç yıllık tecrübesi olduğu
    private String sertifikalar;         // Sahip olduğu sertifikalar — örn: "ACE CPT", "CSCS" (virgülle ayrılmış)
    private String biyografi;            // Antrenör hakkında kısa özgeçmiş bilgisi
    private int dersCount;               // Bu antrenörün verdiği toplam ders sayısı (siniflar tablosundan hesaplanır)
    private int ogrenciCount;            // Bu antrenörün derslerine kayıtlı benzersiz öğrenci sayısı
    private String durum;                // Antrenörün durumu — "aktif" veya "pasif" (pasif ise ders veremez)

    // Boş Constructor — Parametresiz yapıcı metot
    // ResultSet'ten veri okurken veya JSON'dan nesne oluştururken kullanılır
    public Antrenor() {}

    // Parametreli Constructor — Tüm alanları parametre olarak alan yapıcı metot
    // Veritabanı sorgusunun sonucunu doğrudan nesneye dönüştürmek için kullanılır
    public Antrenor(int antrenorId, String ad, String soyad, String email,
                    String telefon, String uzmanlik, int deneyimYili,
                    String sertifikalar, String biyografi, int dersCount,
                    int ogrenciCount, String durum) {
        this.antrenorId = antrenorId;       // Antrenör ID'sini ata
        this.ad = ad;                       // Adı ata
        this.soyad = soyad;                 // Soyadı ata
        this.email = email;                 // E-posta adresini ata
        this.telefon = telefon;             // Telefon numarasını ata
        this.uzmanlik = uzmanlik;           // Uzmanlık alanını ata
        this.deneyimYili = deneyimYili;     // Deneyim yılını ata
        this.sertifikalar = sertifikalar;   // Sertifika bilgisini ata
        this.biyografi = biyografi;         // Biyografi metnini ata
        this.dersCount = dersCount;         // Ders sayısını ata
        this.ogrenciCount = ogrenciCount;   // Öğrenci sayısını ata
        this.durum = durum;                 // Durumu ata (aktif/pasif)
    }

    // ─── Getter ve Setter Metotları ───
    // Her private alan için okuma (get) ve yazma (set) metotları tanımlanır

    public int getAntrenorId() { return antrenorId; }                                      // Antrenör ID'sini döndür
    public void setAntrenorId(int antrenorId) { this.antrenorId = antrenorId; }             // Antrenör ID'sini güncelle

    public String getAd() { return ad; }                                                    // Adı döndür
    public void setAd(String ad) { this.ad = ad; }                                          // Adı güncelle

    public String getSoyad() { return soyad; }                                              // Soyadı döndür
    public void setSoyad(String soyad) { this.soyad = soyad; }                              // Soyadı güncelle

    public String getEmail() { return email; }                                              // E-posta adresini döndür
    public void setEmail(String email) { this.email = email; }                              // E-posta adresini güncelle

    public String getTelefon() { return telefon; }                                          // Telefon numarasını döndür
    public void setTelefon(String telefon) { this.telefon = telefon; }                      // Telefon numarasını güncelle

    public String getUzmanlik() { return uzmanlik; }                                        // Uzmanlık alanını döndür
    public void setUzmanlik(String uzmanlik) { this.uzmanlik = uzmanlik; }                  // Uzmanlık alanını güncelle

    public int getDeneyimYili() { return deneyimYili; }                                     // Deneyim yılını döndür
    public void setDeneyimYili(int deneyimYili) { this.deneyimYili = deneyimYili; }          // Deneyim yılını güncelle

    public String getSertifikalar() { return sertifikalar; }                                // Sertifikaları döndür
    public void setSertifikalar(String sertifikalar) { this.sertifikalar = sertifikalar; }  // Sertifikaları güncelle

    public String getBiyografi() { return biyografi; }                                      // Biyografiyi döndür
    public void setBiyografi(String biyografi) { this.biyografi = biyografi; }               // Biyografiyi güncelle

    public int getDersCount() { return dersCount; }                                         // Ders sayısını döndür
    public void setDersCount(int dersCount) { this.dersCount = dersCount; }                 // Ders sayısını güncelle

    public int getOgrenciCount() { return ogrenciCount; }                                   // Öğrenci sayısını döndür
    public void setOgrenciCount(int ogrenciCount) { this.ogrenciCount = ogrenciCount; }     // Öğrenci sayısını güncelle

    public String getDurum() { return durum; }                                              // Durumu döndür (aktif/pasif)
    public void setDurum(String durum) { this.durum = durum; }                              // Durumu güncelle

    // Yardımcı metot — Ad ve soyadı birleştirerek tam isim döndürür
    // UI'da antrenör listelerken "Ad Soyad" formatında göstermek için kullanılır
    public String getAdSoyad() {
        return ad + " " + soyad; // Ad ve soyadı boşlukla birleştir
    }

    // toString() metodu — Nesneyi okunabilir metin formatında döndürür (debug ve loglama için)
    @Override
    public String toString() {
        return "Antrenor{" +
                "antrenorId=" + antrenorId +            // Antrenör ID
                ", ad='" + ad + '\'' +                  // Ad
                ", soyad='" + soyad + '\'' +            // Soyad
                ", uzmanlik='" + uzmanlik + '\'' +      // Uzmanlık alanı
                ", deneyimYili=" + deneyimYili +        // Deneyim yılı
                ", durum='" + durum + '\'' +            // Durum
                '}';
    }
}
