package Model;

public class Ders {
    private int dersId;
    private String dersAdi;          // Yoga Flow, Kickboks, Aqua Aerobik vb.
    private String antrenorAdi;
    private int antrenorId;
    private String kategori;         // Esneklik, Kardio, Güç
    private int kontenjan;           // 15, 20 vb.
    private int sureDakika;          // 60, 45, 50
    private String salon;            // Salon A, Salon B, Havuz, Salon C
    private String ikon;             // 🧘, 🥊, 🏊, 🏋️, 🤸
    private String durum;            // aktif, pasif

    // Boş Constructor
    public Ders() {}

    // Parametreli Constructor
    public Ders(int dersId, String dersAdi, String antrenorAdi, int antrenorId,
                String kategori, int kontenjan, int sureDakika,
                String salon, String ikon, String durum) {
        this.dersId = dersId;
        this.dersAdi = dersAdi;
        this.antrenorAdi = antrenorAdi;
        this.antrenorId = antrenorId;
        this.kategori = kategori;
        this.kontenjan = kontenjan;
        this.sureDakika = sureDakika;
        this.salon = salon;
        this.ikon = ikon;
        this.durum = durum;
    }

    // Getter ve Setter'lar
    public int getDersId() { return dersId; }
    public void setDersId(int dersId) { this.dersId = dersId; }

    public String getDersAdi() { return dersAdi; }
    public void setDersAdi(String dersAdi) { this.dersAdi = dersAdi; }

    public String getAntrenorAdi() { return antrenorAdi; }
    public void setAntrenorAdi(String antrenorAdi) { this.antrenorAdi = antrenorAdi; }

    public int getAntrenorId() { return antrenorId; }
    public void setAntrenorId(int antrenorId) { this.antrenorId = antrenorId; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public int getKontenjan() { return kontenjan; }
    public void setKontenjan(int kontenjan) { this.kontenjan = kontenjan; }

    public int getSureDakika() { return sureDakika; }
    public void setSureDakika(int sureDakika) { this.sureDakika = sureDakika; }

    public String getSalon() { return salon; }
    public void setSalon(String salon) { this.salon = salon; }

    public String getIkon() { return ikon; }
    public void setIkon(String ikon) { this.ikon = ikon; }

    public String getDurum() { return durum; }
    public void setDurum(String durum) { this.durum = durum; }

    @Override
    public String toString() {
        return "Ders{" +
                "dersId=" + dersId +
                ", dersAdi='" + dersAdi + '\'' +
                ", antrenorAdi='" + antrenorAdi + '\'' +
                ", kategori='" + kategori + '\'' +
                ", kontenjan=" + kontenjan +
                ", sureDakika=" + sureDakika +
                '}';
    }
}
