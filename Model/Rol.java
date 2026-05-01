package Model;

public class Rol {
    private int rolId;
    private String rolAdi;       // admin, uye, antrenor
    private String aciklama;

    // Boş Constructor
    public Rol() {}

    // Parametreli Constructor
    public Rol(int rolId, String rolAdi, String aciklama) {
        this.rolId = rolId;
        this.rolAdi = rolAdi;
        this.aciklama = aciklama;
    }

    // Getter ve Setter'lar
    public int getRolId() { return rolId; }
    public void setRolId(int rolId) { this.rolId = rolId; }

    public String getRolAdi() { return rolAdi; }
    public void setRolAdi(String rolAdi) { this.rolAdi = rolAdi; }

    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }

    @Override
    public String toString() {
        return "Rol{" +
                "rolId=" + rolId +
                ", rolAdi='" + rolAdi + '\'' +
                ", aciklama='" + aciklama + '\'' +
                '}';
    }
}
