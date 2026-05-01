package Model;

import java.util.Date;

public class Uye {
    private int uyeId;
    private String ad;
    private String soyad;
    private String email;
    private String telefon;
    private String cinsiyet;         // Erkek, Kadın
    private String uyelikNo;        // FZ-2026-001
    private String plan;             // platinum, gold, silver, basic
    private String durum;            // aktif, pasif, suresi_doldu
    private Date kayitTarihi;
    private Date abonelikBaslangic;
    private Date abonelikBitis;
    private String odemeYontemi;     // kredi_karti, nakit, havale, online
    private double odemeTutari;
    private String acilDurumKisi;
    private String saglikNotu;

    // Boş Constructor
    public Uye() {}

    // Parametreli Constructor
    public Uye(int uyeId, String ad, String soyad, String email, String telefon,
               String cinsiyet, String uyelikNo, String plan, String durum,
               Date kayitTarihi, Date abonelikBaslangic, Date abonelikBitis,
               String odemeYontemi, double odemeTutari) {
        this.uyeId = uyeId;
        this.ad = ad;
        this.soyad = soyad;
        this.email = email;
        this.telefon = telefon;
        this.cinsiyet = cinsiyet;
        this.uyelikNo = uyelikNo;
        this.plan = plan;
        this.durum = durum;
        this.kayitTarihi = kayitTarihi;
        this.abonelikBaslangic = abonelikBaslangic;
        this.abonelikBitis = abonelikBitis;
        this.odemeYontemi = odemeYontemi;
        this.odemeTutari = odemeTutari;
    }

    // Getter ve Setter'lar
    public int getUyeId() { return uyeId; }
    public void setUyeId(int uyeId) { this.uyeId = uyeId; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getCinsiyet() { return cinsiyet; }
    public void setCinsiyet(String cinsiyet) { this.cinsiyet = cinsiyet; }

    public String getUyelikNo() { return uyelikNo; }
    public void setUyelikNo(String uyelikNo) { this.uyelikNo = uyelikNo; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }

    public Date getKayitTarihi() { return kayitTarihi; }
    public void setKayitTarihi(Date kayitTarihi) { this.kayitTarihi = kayitTarihi; }

    public Date getAbonelikBaslangic() { return abonelikBaslangic; }
    public void setAbonelikBaslangic(Date abonelikBaslangic) { this.abonelikBaslangic = abonelikBaslangic; }

    public Date getAbonelikBitis() { return abonelikBitis; }
    public void setAbonelikBitis(Date abonelikBitis) { this.abonelikBitis = abonelikBitis; }

    public String getOdemeYontemi() { return odemeYontemi; }
    public void setOdemeYontemi(String odemeYontemi) { this.odemeYontemi = odemeYontemi; }

    public double getOdemeTutari() { return odemeTutari; }
    public void setOdemeTutari(double odemeTutari) { this.odemeTutari = odemeTutari; }

    public String getAcilDurumKisi() { return acilDurumKisi; }
    public void setAcilDurumKisi(String acilDurumKisi) { this.acilDurumKisi = acilDurumKisi; }

    public String getSaglikNotu() { return saglikNotu; }
    public void setSaglikNotu(String saglikNotu) { this.saglikNotu = saglikNotu; }

    public String getAdSoyad() {
        return ad + " " + soyad;
    }

    @Override
    public String toString() {
        return "Uye{" +
                "uyeId=" + uyeId +
                ", ad='" + ad + '\'' +
                ", soyad='" + soyad + '\'' +
                ", email='" + email + '\'' +
                ", uyelikNo='" + uyelikNo + '\'' +
                ", plan='" + plan + '\'' +
                ", durum='" + durum + '\'' +
                '}';
    }
}
