package Model;

import java.util.Date;

public class Bildirim {
    private int bildirimId;
    private int kullaniciId;
    private String baslik;
    private String mesaj;
    private String tip; // bilgi, uyari, hata, basari
    private boolean okunduMu;
    private Date olusturmaTarihi;

    // Boş Constructor
    public Bildirim() {
    }

    // Parametreli Constructor
    public Bildirim(int bildirimId, int kullaniciId, String baslik, String mesaj,
            String tip, boolean okunduMu, Date olusturmaTarihi) {
        this.bildirimId = bildirimId;
        this.kullaniciId = kullaniciId;
        this.baslik = baslik;
        this.mesaj = mesaj;
        this.tip = tip;
        this.okunduMu = okunduMu;
        this.olusturmaTarihi = olusturmaTarihi;
    }

    // Getter ve Setter'lar
    public int getBildirimId() {
        return bildirimId;
    }

    public void setBildirimId(int bildirimId) {
        this.bildirimId = bildirimId;
    }

    public int getKullaniciId() {
        return kullaniciId;
    }

    public void setKullaniciId(int kullaniciId) {
        this.kullaniciId = kullaniciId;
    }

    public String getBaslik() {
        return baslik;
    }

    public void setBaslik(String baslik) {
        this.baslik = baslik;
    }

    public String getMesaj() {
        return mesaj;
    }

    public void setMesaj(String mesaj) {
        this.mesaj = mesaj;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public boolean isOkunduMu() {
        return okunduMu;
    }

    public void setOkunduMu(boolean okunduMu) {
        this.okunduMu = okunduMu;
    }

    public Date getOlusturmaTarihi() {
        return olusturmaTarihi;
    }

    public void setOlusturmaTarihi(Date olusturmaTarihi) {
        this.olusturmaTarihi = olusturmaTarihi;
    }

    @Override
    public String toString() {
        return "Bildirim{" +
                "bildirimId=" + bildirimId +
                ", baslik='" + baslik + '\'' +
                ", tip='" + tip + '\'' +
                ", okunduMu=" + okunduMu +
                '}';
    }
}
