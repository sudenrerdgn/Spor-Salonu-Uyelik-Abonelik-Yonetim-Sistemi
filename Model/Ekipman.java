package Model;

import java.util.Date;

public class Ekipman {
    private int ekipmanId;
    private String ad;               // Koşu Bandı, Eliptik Bisiklet vb.
    private String kategori;         // Kardio, Güç, Esneklik
    private int adet;
    private Date satinAlmaTarihi;
    private double fiyat;
    private String durum;            // calisiyor, bakimda, arizali

    // Boş Constructor
    public Ekipman() {}

    // Parametreli Constructor
    public Ekipman(int ekipmanId, String ad, String kategori, int adet,
                   Date satinAlmaTarihi, double fiyat, String durum) {
        this.ekipmanId = ekipmanId;
        this.ad = ad;
        this.kategori = kategori;
        this.adet = adet;
        this.satinAlmaTarihi = satinAlmaTarihi;
        this.fiyat = fiyat;
        this.durum = durum;
    }

    // Getter ve Setter'lar
    public int getEkipmanId() { return ekipmanId; }
    public void setEkipmanId(int ekipmanId) { this.ekipmanId = ekipmanId; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public int getAdet() { return adet; }
    public void setAdet(int adet) { this.adet = adet; }

    public Date getSatinAlmaTarihi() { return satinAlmaTarihi; }
    public void setSatinAlmaTarihi(Date satinAlmaTarihi) { this.satinAlmaTarihi = satinAlmaTarihi; }

    public double getFiyat() { return fiyat; }
    public void setFiyat(double fiyat) { this.fiyat = fiyat; }

    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }

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
