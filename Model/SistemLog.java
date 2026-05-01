package Model;

import java.util.Date;

public class SistemLog {
    private int logId;
    private int kullaniciId;
    private String kullaniciAdi;
    private String islem;            // giris, cikis, uye_ekleme, odeme_alma vb.
    private String detay;
    private String ipAdresi;
    private Date islemTarihi;

    // Boş Constructor
    public SistemLog() {}

    // Parametreli Constructor
    public SistemLog(int logId, int kullaniciId, String kullaniciAdi,
                     String islem, String detay, String ipAdresi, Date islemTarihi) {
        this.logId = logId;
        this.kullaniciId = kullaniciId;
        this.kullaniciAdi = kullaniciAdi;
        this.islem = islem;
        this.detay = detay;
        this.ipAdresi = ipAdresi;
        this.islemTarihi = islemTarihi;
    }

    // Getter ve Setter'lar
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getKullaniciId() { return kullaniciId; }
    public void setKullaniciId(int kullaniciId) { this.kullaniciId = kullaniciId; }

    public String getKullaniciAdi() { return kullaniciAdi; }
    public void setKullaniciAdi(String kullaniciAdi) { this.kullaniciAdi = kullaniciAdi; }

    public String getIslem() { return islem; }
    public void setIslem(String islem) { this.islem = islem; }

    public String getDetay() { return detay; }
    public void setDetay(String detay) { this.detay = detay; }

    public String getIpAdresi() { return ipAdresi; }
    public void setIpAdresi(String ipAdresi) { this.ipAdresi = ipAdresi; }

    public Date getIslemTarihi() { return islemTarihi; }
    public void setIslemTarihi(Date islemTarihi) { this.islemTarihi = islemTarihi; }

    @Override
    public String toString() {
        return "SistemLog{" +
                "logId=" + logId +
                ", kullaniciAdi='" + kullaniciAdi + '\'' +
                ", islem='" + islem + '\'' +
                ", islemTarihi=" + islemTarihi +
                '}';
    }
}
