package Model;

import java.util.Date;

public class Katilim {
    private int katilimId;
    private int uyeId;
    private String uyeAdi;
    private int dersId;
    private String dersAdi;
    private Date tarih;
    private String saat;             // 08:00–09:00
    private String gun;              // Pazartesi, Salı, Çarşamba vb.
    private String salon;            // Salon A, Salon B, Havuz
    private String durum;            // aktif, iptal, tamamlandi

    // Boş Constructor
    public Katilim() {}

    // Parametreli Constructor
    public Katilim(int katilimId, int uyeId, String uyeAdi, int dersId,
                   String dersAdi, Date tarih, String saat, String gun,
                   String salon, String durum) {
        this.katilimId = katilimId;
        this.uyeId = uyeId;
        this.uyeAdi = uyeAdi;
        this.dersId = dersId;
        this.dersAdi = dersAdi;
        this.tarih = tarih;
        this.saat = saat;
        this.gun = gun;
        this.salon = salon;
        this.durum = durum;
    }

    // Getter ve Setter'lar
    public int getKatilimId() { return katilimId; }
    public void setKatilimId(int katilimId) { this.katilimId = katilimId; }

    public int getUyeId() { return uyeId; }
    public void setUyeId(int uyeId) { this.uyeId = uyeId; }

    public String getUyeAdi() { return uyeAdi; }
    public void setUyeAdi(String uyeAdi) { this.uyeAdi = uyeAdi; }

    public int getDersId() { return dersId; }
    public void setDersId(int dersId) { this.dersId = dersId; }

    public String getDersAdi() { return dersAdi; }
    public void setDersAdi(String dersAdi) { this.dersAdi = dersAdi; }

    public Date getTarih() { return tarih; }
    public void setTarih(Date tarih) { this.tarih = tarih; }

    public String getSaat() { return saat; }
    public void setSaat(String saat) { this.saat = saat; }

    public String getGun() { return gun; }
    public void setGun(String gun) { this.gun = gun; }

    public String getSalon() { return salon; }
    public void setSalon(String salon) { this.salon = salon; }

    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }

    @Override
    public String toString() {
        return "Katilim{" +
                "katilimId=" + katilimId +
                ", uyeAdi='" + uyeAdi + '\'' +
                ", dersAdi='" + dersAdi + '\'' +
                ", tarih=" + tarih +
                ", durum='" + durum + '\'' +
                '}';
    }
}
