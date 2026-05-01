package Model;

import java.util.Date;

public class Ayar {
    private int ayarId;
    private String ayarAdi;          // salon_adi, calisma_saatleri, tema vb.
    private String ayarDegeri;
    private String kategori;         // genel, bildirim, guvenlik
    private Date guncellemeTarihi;

    // Boş Constructor
    public Ayar() {}

    // Parametreli Constructor
    public Ayar(int ayarId, String ayarAdi, String ayarDegeri,
                String kategori, Date guncellemeTarihi) {
        this.ayarId = ayarId;
        this.ayarAdi = ayarAdi;
        this.ayarDegeri = ayarDegeri;
        this.kategori = kategori;
        this.guncellemeTarihi = guncellemeTarihi;
    }

    // Getter ve Setter'lar
    public int getAyarId() { return ayarId; }
    public void setAyarId(int ayarId) { this.ayarId = ayarId; }

    public String getAyarAdi() { return ayarAdi; }
    public void setAyarAdi(String ayarAdi) { this.ayarAdi = ayarAdi; }

    public String getAyarDegeri() { return ayarDegeri; }
    public void setAyarDegeri(String ayarDegeri) { this.ayarDegeri = ayarDegeri; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public Date getGuncellemeTarihi() { return guncellemeTarihi; }
    public void setGuncellemeTarihi(Date guncellemeTarihi) { this.guncellemeTarihi = guncellemeTarihi; }

    @Override
    public String toString() {
        return "Ayar{" +
                "ayarId=" + ayarId +
                ", ayarAdi='" + ayarAdi + '\'' +
                ", ayarDegeri='" + ayarDegeri + '\'' +
                ", kategori='" + kategori + '\'' +
                '}';
    }
}
