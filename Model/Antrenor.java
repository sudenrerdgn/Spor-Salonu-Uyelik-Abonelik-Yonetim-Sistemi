package Model;

public class Antrenor {
    private int antrenorId;
    private String ad;
    private String soyad;
    private String email;
    private String telefon;
    private String uzmanlik;         // Fonksiyonel Fitness, Crossfit
    private int deneyimYili;
    private String sertifikalar;     // ACE CPT, CSCS
    private String biyografi;
    private int dersCount;
    private int ogrenciCount;
    private String durum;            // aktif, pasif

    // Boş Constructor
    public Antrenor() {}

    // Parametreli Constructor
    public Antrenor(int antrenorId, String ad, String soyad, String email,
                    String telefon, String uzmanlik, int deneyimYili,
                    String sertifikalar, String biyografi, int dersCount,
                    int ogrenciCount, String durum) {
        this.antrenorId = antrenorId;
        this.ad = ad;
        this.soyad = soyad;
        this.email = email;
        this.telefon = telefon;
        this.uzmanlik = uzmanlik;
        this.deneyimYili = deneyimYili;
        this.sertifikalar = sertifikalar;
        this.biyografi = biyografi;
        this.dersCount = dersCount;
        this.ogrenciCount = ogrenciCount;
        this.durum = durum;
    }

    // Getter ve Setter'lar
    public int getAntrenorId() { return antrenorId; }
    public void setAntrenorId(int antrenorId) { this.antrenorId = antrenorId; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getUzmanlik() { return uzmanlik; }
    public void setUzmanlik(String uzmanlik) { this.uzmanlik = uzmanlik; }

    public int getDeneyimYili() { return deneyimYili; }
    public void setDeneyimYili(int deneyimYili) { this.deneyimYili = deneyimYili; }

    public String getSertifikalar() { return sertifikalar; }
    public void setSertifikalar(String sertifikalar) { this.sertifikalar = sertifikalar; }

    public String getBiyografi() { return biyografi; }
    public void setBiyografi(String biyografi) { this.biyografi = biyografi; }

    public int getDersCount() { return dersCount; }
    public void setDersCount(int dersCount) { this.dersCount = dersCount; }

    public int getOgrenciCount() { return ogrenciCount; }
    public void setOgrenciCount(int ogrenciCount) { this.ogrenciCount = ogrenciCount; }

    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }

    public String getAdSoyad() {
        return ad + " " + soyad;
    }

    @Override
    public String toString() {
        return "Antrenor{" +
                "antrenorId=" + antrenorId +
                ", ad='" + ad + '\'' +
                ", soyad='" + soyad + '\'' +
                ", uzmanlik='" + uzmanlik + '\'' +
                ", deneyimYili=" + deneyimYili +
                ", durum='" + durum + '\'' +
                '}';
    }
}
