package Model;

import java.util.Date;

public class EkipmanBakim {
    private int bakimId;
    private int ekipmanId;
    private String ekipmanAdi;       // Kürek Çekme Makinesi, Koşu Bandı
    private Date bakimTarihi;
    private double maliyet;
    private String yapanKisi;        // Teknik Servis A, Teknik Servis B
    private String aciklama;
    private Date sonrakiBakimTarihi;
    private String durum;            // tamamlandi, devam_ediyor, planli

    // Boş Constructor
    public EkipmanBakim() {}

    // Parametreli Constructor
    public EkipmanBakim(int bakimId, int ekipmanId, String ekipmanAdi,
                        Date bakimTarihi, double maliyet, String yapanKisi,
                        String aciklama, Date sonrakiBakimTarihi, String durum) {
        this.bakimId = bakimId;
        this.ekipmanId = ekipmanId;
        this.ekipmanAdi = ekipmanAdi;
        this.bakimTarihi = bakimTarihi;
        this.maliyet = maliyet;
        this.yapanKisi = yapanKisi;
        this.aciklama = aciklama;
        this.sonrakiBakimTarihi = sonrakiBakimTarihi;
        this.durum = durum;
    }

    // Getter ve Setter'lar
    public int getBakimId() { return bakimId; }
    public void setBakimId(int bakimId) { this.bakimId = bakimId; }

    public int getEkipmanId() { return ekipmanId; }
    public void setEkipmanId(int ekipmanId) { this.ekipmanId = ekipmanId; }

    public String getEkipmanAdi() { return ekipmanAdi; }
    public void setEkipmanAdi(String ekipmanAdi) { this.ekipmanAdi = ekipmanAdi; }

    public Date getBakimTarihi() { return bakimTarihi; }
    public void setBakimTarihi(Date bakimTarihi) { this.bakimTarihi = bakimTarihi; }

    public double getMaliyet() { return maliyet; }
    public void setMaliyet(double maliyet) { this.maliyet = maliyet; }

    public String getYapanKisi() { return yapanKisi; }
    public void setYapanKisi(String yapanKisi) { this.yapanKisi = yapanKisi; }

    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }

    public Date getSonrakiBakimTarihi() { return sonrakiBakimTarihi; }
    public void setSonrakiBakimTarihi(Date sonrakiBakimTarihi) { this.sonrakiBakimTarihi = sonrakiBakimTarihi; }

    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }

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
