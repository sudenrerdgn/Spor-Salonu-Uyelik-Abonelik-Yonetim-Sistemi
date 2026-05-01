package Model;

public class UyelikPaketi {
    private int paketId;
    private String paketAdi;         // Platinum, Gold, Silver, Basic
    private double aylikFiyat;       // 850, 550, 350, 199
    private int sureyAy;             // 12, 6, 3, 1
    private String ozellikler;       // Fitness alanı, Havuz, Sauna vb.
    private int aktifUyeSayisi;
    private String ikon;             // 💎, ⭐, 🥈, 🔰
    private String renk;             // #a78bfa, #fbbf24, #94a3b8, #67e8f9
    private boolean aktifMi;

    // Boş Constructor
    public UyelikPaketi() {}

    // Parametreli Constructor
    public UyelikPaketi(int paketId, String paketAdi, double aylikFiyat, int sureyAy,
                        String ozellikler, int aktifUyeSayisi, String ikon,
                        String renk, boolean aktifMi) {
        this.paketId = paketId;
        this.paketAdi = paketAdi;
        this.aylikFiyat = aylikFiyat;
        this.sureyAy = sureyAy;
        this.ozellikler = ozellikler;
        this.aktifUyeSayisi = aktifUyeSayisi;
        this.ikon = ikon;
        this.renk = renk;
        this.aktifMi = aktifMi;
    }

    // Getter ve Setter'lar
    public int getPaketId() { return paketId; }
    public void setPaketId(int paketId) { this.paketId = paketId; }

    public String getPaketAdi() { return paketAdi; }
    public void setPaketAdi(String paketAdi) { this.paketAdi = paketAdi; }

    public double getAylikFiyat() { return aylikFiyat; }
    public void setAylikFiyat(double aylikFiyat) { this.aylikFiyat = aylikFiyat; }

    public int getSureyAy() { return sureyAy; }
    public void setSureyAy(int sureyAy) { this.sureyAy = sureyAy; }

    public String getOzellikler() { return ozellikler; }
    public void setOzellikler(String ozellikler) { this.ozellikler = ozellikler; }

    public int getAktifUyeSayisi() { return aktifUyeSayisi; }
    public void setAktifUyeSayisi(int aktifUyeSayisi) { this.aktifUyeSayisi = aktifUyeSayisi; }

    public String getIkon() { return ikon; }
    public void setIkon(String ikon) { this.ikon = ikon; }

    public String getRenk() { return renk; }
    public void setRenk(String renk) { this.renk = renk; }

    public boolean isAktifMi() { return aktifMi; }
    public void setAktifMi(boolean aktifMi) { this.aktifMi = aktifMi; }

    @Override
    public String toString() {
        return "UyelikPaketi{" +
                "paketId=" + paketId +
                ", paketAdi='" + paketAdi + '\'' +
                ", aylikFiyat=" + aylikFiyat +
                ", sureyAy=" + sureyAy +
                ", aktifUyeSayisi=" + aktifUyeSayisi +
                '}';
    }
}
