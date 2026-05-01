package Model;

import java.util.Date;

public class GirisCikis {
    private int kayitId;
    private int uyeId;
    private String uyeAdi;
    private String girisSaati;       // 07:30
    private String cikisSaati;       // 09:15 veya null (hala içeride)
    private String girisTuru;        // normal, qr, kart
    private String durum;            // giris, cikis
    private Date tarih;

    // Boş Constructor
    public GirisCikis() {}

    // Parametreli Constructor
    public GirisCikis(int kayitId, int uyeId, String uyeAdi,
                      String girisSaati, String cikisSaati,
                      String girisTuru, String durum, Date tarih) {
        this.kayitId = kayitId;
        this.uyeId = uyeId;
        this.uyeAdi = uyeAdi;
        this.girisSaati = girisSaati;
        this.cikisSaati = cikisSaati;
        this.girisTuru = girisTuru;
        this.durum = durum;
        this.tarih = tarih;
    }

    // Getter ve Setter'lar
    public int getKayitId() { return kayitId; }
    public void setKayitId(int kayitId) { this.kayitId = kayitId; }

    public int getUyeId() { return uyeId; }
    public void setUyeId(int uyeId) { this.uyeId = uyeId; }

    public String getUyeAdi() { return uyeAdi; }
    public void setUyeAdi(String uyeAdi) { this.uyeAdi = uyeAdi; }

    public String getGirisSaati() { return girisSaati; }
    public void setGirisSaati(String girisSaati) { this.girisSaati = girisSaati; }

    public String getCikisSaati() { return cikisSaati; }
    public void setCikisSaati(String cikisSaati) { this.cikisSaati = cikisSaati; }

    public String getGirisTuru() { return girisTuru; }
    public void setGirisTuru(String girisTuru) { this.girisTuru = girisTuru; }

    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }

    public Date getTarih() { return tarih; }
    public void setTarih(Date tarih) { this.tarih = tarih; }

    public boolean isIceride() {
        return "giris".equals(durum);
    }

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
