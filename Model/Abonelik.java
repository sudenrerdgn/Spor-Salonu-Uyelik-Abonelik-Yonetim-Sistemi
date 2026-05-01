package Model;

import java.util.Date;

public class Abonelik {
    private int abonelikId;
    private int uyeId;
    private String uyeAdi;
    private String plan;             // Platinum, Gold, Silver, Basic
    private Date baslangicTarihi;
    private Date bitisTarihi;
    private boolean otomatikYenileme;
    private String durum;            // aktif, pasif, suresi_doldu, iptal

    // Boş Constructor
    public Abonelik() {}

    // Parametreli Constructor
    public Abonelik(int abonelikId, int uyeId, String uyeAdi, String plan,
                    Date baslangicTarihi, Date bitisTarihi,
                    boolean otomatikYenileme, String durum) {
        this.abonelikId = abonelikId;
        this.uyeId = uyeId;
        this.uyeAdi = uyeAdi;
        this.plan = plan;
        this.baslangicTarihi = baslangicTarihi;
        this.bitisTarihi = bitisTarihi;
        this.otomatikYenileme = otomatikYenileme;
        this.durum = durum;
    }

    // Getter ve Setter'lar
    public int getAbonelikId() { return abonelikId; }
    public void setAbonelikId(int abonelikId) { this.abonelikId = abonelikId; }

    public int getUyeId() { return uyeId; }
    public void setUyeId(int uyeId) { this.uyeId = uyeId; }

    public String getUyeAdi() { return uyeAdi; }
    public void setUyeAdi(String uyeAdi) { this.uyeAdi = uyeAdi; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public Date getBaslangicTarihi() { return baslangicTarihi; }
    public void setBaslangicTarihi(Date baslangicTarihi) { this.baslangicTarihi = baslangicTarihi; }

    public Date getBitisTarihi() { return bitisTarihi; }
    public void setBitisTarihi(Date bitisTarihi) { this.bitisTarihi = bitisTarihi; }

    public boolean isOtomatikYenileme() { return otomatikYenileme; }
    public void setOtomatikYenileme(boolean otomatikYenileme) { this.otomatikYenileme = otomatikYenileme; }

    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }

    @Override
    public String toString() {
        return "Abonelik{" +
                "abonelikId=" + abonelikId +
                ", uyeAdi='" + uyeAdi + '\'' +
                ", plan='" + plan + '\'' +
                ", durum='" + durum + '\'' +
                ", otomatikYenileme=" + otomatikYenileme +
                '}';
    }
}
