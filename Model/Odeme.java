package Model;

import java.util.Date;

public class Odeme {
    private int odemeId;
    private int uyeId;
    private String uyeAdi;
    private String plan; // Platinum, Gold, Silver, Basic
    private double miktar;
    private String odemeYontemi; // Kredi Kartı, Nakit, Havale, Online
    private Date odemeTarihi;
    private String durum; // tamamlandi, beklemede, basarisiz, iade

    // Boş Constructor
    public Odeme() {
    }

    // Parametreli Constructor
    public Odeme(int odemeId, int uyeId, String uyeAdi, String plan,
            double miktar, String odemeYontemi, Date odemeTarihi, String durum) {
        this.odemeId = odemeId;
        this.uyeId = uyeId;
        this.uyeAdi = uyeAdi;
        this.plan = plan;
        this.miktar = miktar;
        this.odemeYontemi = odemeYontemi;
        this.odemeTarihi = odemeTarihi;
        this.durum = durum;
    }

    // Getter ve Setter'lar
    public int getOdemeId() {
        return odemeId;
    }

    public void setOdemeId(int odemeId) {
        this.odemeId = odemeId;
    }

    public int getUyeId() {
        return uyeId;
    }

    public void setUyeId(int uyeId) {
        this.uyeId = uyeId;
    }

    public String getUyeAdi() {
        return uyeAdi;
    }

    public void setUyeAdi(String uyeAdi) {
        this.uyeAdi = uyeAdi;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public double getMiktar() {
        return miktar;
    }

    public void setMiktar(double miktar) {
        this.miktar = miktar;
    }

    public String getOdemeYontemi() {
        return odemeYontemi;
    }

    public void setOdemeYontemi(String odemeYontemi) {
        this.odemeYontemi = odemeYontemi;
    }

    public Date getOdemeTarihi() {
        return odemeTarihi;
    }

    public void setOdemeTarihi(Date odemeTarihi) {
        this.odemeTarihi = odemeTarihi;
    }

    public String getDurum() {
        return durum;
    }

    public void setDurum(String durum) {
        this.durum = durum;
    }

    @Override
    public String toString() {
        return "Odeme{" +
                "odemeId=" + odemeId +
                ", uyeAdi='" + uyeAdi + '\'' +
                ", miktar=" + miktar +
                ", odemeYontemi='" + odemeYontemi + '\'' +
                ", durum='" + durum + '\'' +
                '}';
    }
}
