package Model; // Bu sınıf Model paketine aittir

/**
 * UyelikPaketi — Spor salonunun sunduğu abonelik paketlerini temsil eden model sınıfı.
 * Veritabanındaki "uye_planlari" tablosuna karşılık gelir.
 * Platinum, Gold, Silver, Basic gibi planların fiyat, süre ve özelliklerini tanımlar.
 */
public class UyelikPaketi {
    private int paketId;                 // Paketin benzersiz kimlik numarası (Primary Key)
    private String paketAdi;             // Paket adı — "Platinum", "Gold", "Silver" veya "Basic"
    private double aylikFiyat;           // Aylık fiyat (TL) — örn: 850, 550, 350, 199
    private int sureyAy;                 // Paket süresi (ay) — 12 ay (yıllık), 6 ay, 3 ay, 1 ay
    private String ozellikler;           // Paketin sunduğu özellikler — JSON formatında (Fitness alanı, Havuz, Sauna vb.)
    private int aktifUyeSayisi;          // Bu paketi aktif olarak kullanan üye sayısı (hesaplanmış değer)
    private String ikon;                 // Paket ikonu (emoji) — UI'da görsel gösterim için (💎, ⭐, 🥈, 🔰)
    private String renk;                 // Paket rengi (hex kodu) — UI'da kart arka plan rengi (#a78bfa, #fbbf24 vb.)
    private boolean aktifMi;             // Paket aktif mi? false ise yeni üye bu paketi seçemez

    // Boş Constructor
    public UyelikPaketi() {}

    // Parametreli Constructor — Tüm paket bilgilerini alarak nesne oluşturur
    public UyelikPaketi(int paketId, String paketAdi, double aylikFiyat, int sureyAy,
                        String ozellikler, int aktifUyeSayisi, String ikon,
                        String renk, boolean aktifMi) {
        this.paketId = paketId;                   // Paket ID'sini ata
        this.paketAdi = paketAdi;                 // Paket adını ata
        this.aylikFiyat = aylikFiyat;             // Aylık fiyatı ata
        this.sureyAy = sureyAy;                   // Süreyi ata (ay cinsinden)
        this.ozellikler = ozellikler;             // Özellikleri ata
        this.aktifUyeSayisi = aktifUyeSayisi;     // Aktif üye sayısını ata
        this.ikon = ikon;                         // İkonu ata
        this.renk = renk;                         // Rengi ata
        this.aktifMi = aktifMi;                   // Aktiflik durumunu ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getPaketId() { return paketId; }                                       // Paket ID döndür
    public void setPaketId(int paketId) { this.paketId = paketId; }

    public String getPaketAdi() { return paketAdi; }                                  // Paket adı döndür
    public void setPaketAdi(String paketAdi) { this.paketAdi = paketAdi; }

    public double getAylikFiyat() { return aylikFiyat; }                              // Aylık fiyat döndür
    public void setAylikFiyat(double aylikFiyat) { this.aylikFiyat = aylikFiyat; }

    public int getSureyAy() { return sureyAy; }                                       // Süre döndür (ay)
    public void setSureyAy(int sureyAy) { this.sureyAy = sureyAy; }

    public String getOzellikler() { return ozellikler; }                              // Özellikleri döndür
    public void setOzellikler(String ozellikler) { this.ozellikler = ozellikler; }

    public int getAktifUyeSayisi() { return aktifUyeSayisi; }                         // Aktif üye sayısı döndür
    public void setAktifUyeSayisi(int aktifUyeSayisi) { this.aktifUyeSayisi = aktifUyeSayisi; }

    public String getIkon() { return ikon; }                                          // İkon döndür
    public void setIkon(String ikon) { this.ikon = ikon; }

    public String getRenk() { return renk; }                                          // Renk döndür
    public void setRenk(String renk) { this.renk = renk; }

    public boolean isAktifMi() { return aktifMi; }                                    // Aktiflik durumu döndür
    public void setAktifMi(boolean aktifMi) { this.aktifMi = aktifMi; }

    // toString() — Debug ve loglama amacıyla
    @Override
    public String toString() {
        return "UyelikPaketi{" +
                "paketId=" + paketId +
                ", paketAdi='" + paketAdi + '\'' +
                ", aylikFiyat=" + aylikFiyat +
                ", sureyAy=" + sureyAy +
                ", aktifUyeSayisi=" + aktifUyeSayisi +
                '}';
    }
}
