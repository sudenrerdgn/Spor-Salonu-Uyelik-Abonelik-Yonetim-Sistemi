package Model; // Bu sınıf Model paketine aittir

/**
 * Ders — Spor salonunda verilen dersleri temsil eden model sınıfı.
 * Veritabanındaki "siniflar" tablosuna karşılık gelir.
 * Yoga, Kickboks, Aqua Aerobik gibi ders bilgilerini tutar.
 */
public class Ders {
    private int dersId;                  // Dersin benzersiz kimlik numarası (Primary Key — siniflar tablosu)
    private String dersAdi;              // Ders adı — örn: "Yoga Flow", "Kickboks", "Aqua Aerobik"
    private String antrenorAdi;          // Dersi veren antrenörün adı (JOIN sorgusuyla gelir)
    private int antrenorId;              // Dersi veren antrenörün ID'si (Foreign Key → antrenorler)
    private String kategori;             // Ders kategorisi — "Esneklik", "Kardio" veya "Güç"
    private int kontenjan;               // Derse katılabilecek maksimum öğrenci sayısı (örn: 15, 20)
    private int sureDakika;              // Ders süresi dakika cinsinden (örn: 45, 60, 50)
    private String salon;                // Dersin yapıldığı salon — "Salon A", "Salon B", "Havuz", "Salon C"
    private String ikon;                 // Ders ikonu (emoji) — UI'da görsel gösterim için (🧘, 🥊, 🏊)
    private String durum;                // Dersin durumu — "aktif" veya "pasif" (pasif ise dersi görmezler)

    // Boş Constructor — Parametresiz yapıcı metot
    public Ders() {}

    // Parametreli Constructor — Tüm ders bilgilerini alarak nesne oluşturur
    public Ders(int dersId, String dersAdi, String antrenorAdi, int antrenorId,
                String kategori, int kontenjan, int sureDakika,
                String salon, String ikon, String durum) {
        this.dersId = dersId;               // Ders ID'sini ata
        this.dersAdi = dersAdi;             // Ders adını ata
        this.antrenorAdi = antrenorAdi;     // Antrenör adını ata
        this.antrenorId = antrenorId;       // Antrenör ID'sini ata
        this.kategori = kategori;           // Kategoriyi ata
        this.kontenjan = kontenjan;         // Kontenjanı ata
        this.sureDakika = sureDakika;       // Süreyi ata
        this.salon = salon;                 // Salonu ata
        this.ikon = ikon;                   // İkonu ata
        this.durum = durum;                 // Durumu ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getDersId() { return dersId; }                                   // Ders ID'sini döndür
    public void setDersId(int dersId) { this.dersId = dersId; }                 // Ders ID'sini güncelle

    public String getDersAdi() { return dersAdi; }                              // Ders adını döndür
    public void setDersAdi(String dersAdi) { this.dersAdi = dersAdi; }          // Ders adını güncelle

    public String getAntrenorAdi() { return antrenorAdi; }                      // Antrenör adını döndür
    public void setAntrenorAdi(String antrenorAdi) { this.antrenorAdi = antrenorAdi; }

    public int getAntrenorId() { return antrenorId; }                           // Antrenör ID'sini döndür
    public void setAntrenorId(int antrenorId) { this.antrenorId = antrenorId; }

    public String getKategori() { return kategori; }                            // Kategoriyi döndür
    public void setKategori(String kategori) { this.kategori = kategori; }

    public int getKontenjan() { return kontenjan; }                             // Kontenjanı döndür
    public void setKontenjan(int kontenjan) { this.kontenjan = kontenjan; }

    public int getSureDakika() { return sureDakika; }                           // Süreyi döndür (dakika)
    public void setSureDakika(int sureDakika) { this.sureDakika = sureDakika; }

    public String getSalon() { return salon; }                                  // Salon bilgisini döndür
    public void setSalon(String salon) { this.salon = salon; }

    public String getIkon() { return ikon; }                                    // İkonu döndür
    public void setIkon(String ikon) { this.ikon = ikon; }

    public String getDurum() { return durum; }                                  // Durumu döndür
    public void setDurum(String durum) { this.durum = durum; }

    // toString() — Debug ve loglama amacıyla
    @Override
    public String toString() {
        return "Ders{" +
                "dersId=" + dersId +
                ", dersAdi='" + dersAdi + '\'' +
                ", antrenorAdi='" + antrenorAdi + '\'' +
                ", kategori='" + kategori + '\'' +
                ", kontenjan=" + kontenjan +
                ", sureDakika=" + sureDakika +
                '}';
    }
}
