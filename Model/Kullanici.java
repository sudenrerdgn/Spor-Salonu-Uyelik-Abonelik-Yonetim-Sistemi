package Model;

import java.util.Date;

public class Kullanici {
    private int kullaniciId;
    private String ad;
    private String soyad;
    private String email;
    private String sifre;
    private String rolAdi;       // admin, uye, antrenor
    private boolean aktifMi;
    private Date olusturmaTarihi;

    // Boş Constructor
    public Kullanici() {}

    // Parametreli Constructor
    public Kullanici(int kullaniciId, String ad, String soyad, String email,
                     String sifre, String rolAdi, boolean aktifMi, Date olusturmaTarihi) {
        this.kullaniciId = kullaniciId;
        this.ad = ad;
        this.soyad = soyad;
        this.email = email;
        this.sifre = sifre;
        this.rolAdi = rolAdi;
        this.aktifMi = aktifMi;
        this.olusturmaTarihi = olusturmaTarihi;
    }

    // Getter ve Setter'lar
    public int getKullaniciId() { return kullaniciId; }
    public void setKullaniciId(int kullaniciId) { this.kullaniciId = kullaniciId; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSifre() { return sifre; }
    public void setSifre(String sifre) { this.sifre = sifre; }

    public String getRolAdi() { return rolAdi; }
    public void setRolAdi(String rolAdi) { this.rolAdi = rolAdi; }

    public boolean isAktifMi() { return aktifMi; }
    public void setAktifMi(boolean aktifMi) { this.aktifMi = aktifMi; }

    public Date getOlusturmaTarihi() { return olusturmaTarihi; }
    public void setOlusturmaTarihi(Date olusturmaTarihi) { this.olusturmaTarihi = olusturmaTarihi; }

    public String getAdSoyad() {
        return ad + " " + soyad;
    }

    @Override
    public String toString() {
        return "Kullanici{" +
                "kullaniciId=" + kullaniciId +
                ", ad='" + ad + '\'' +
                ", soyad='" + soyad + '\'' +
                ", email='" + email + '\'' +
                ", rolAdi='" + rolAdi + '\'' +
                ", aktifMi=" + aktifMi +
                '}';
    }
}
