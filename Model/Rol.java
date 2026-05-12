package Model; // Bu sınıf Model paketine aittir

/**
 * Rol — Sistemdeki kullanıcı rollerini temsil eden model sınıfı.
 * Veritabanındaki "roller" tablosuna karşılık gelir.
 * Roller: admin (yönetici), uye (üye), antrenor (eğitmen), kullanici (henüz üye olmamış)
 */
public class Rol {
    private int rolId;                   // Rolün benzersiz kimlik numarası (Primary Key)
    private String rolAdi;               // Rol adı — "admin", "uye", "antrenor" veya "kullanici"
    private String aciklama;             // Rolün açıklaması — ne yetkilere sahip olduğunu belirtir

    // Boş Constructor
    public Rol() {}

    // Parametreli Constructor — Tüm rol bilgilerini alarak nesne oluşturur
    public Rol(int rolId, String rolAdi, String aciklama) {
        this.rolId = rolId;              // Rol ID'sini ata
        this.rolAdi = rolAdi;            // Rol adını ata
        this.aciklama = aciklama;        // Açıklamayı ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getRolId() { return rolId; }                                   // Rol ID'sini döndür
    public void setRolId(int rolId) { this.rolId = rolId; }

    public String getRolAdi() { return rolAdi; }                              // Rol adını döndür
    public void setRolAdi(String rolAdi) { this.rolAdi = rolAdi; }

    public String getAciklama() { return aciklama; }                          // Açıklamayı döndür
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }

    // toString() — Debug ve loglama amacıyla
    @Override
    public String toString() {
        return "Rol{" +
                "rolId=" + rolId +
                ", rolAdi='" + rolAdi + '\'' +
                ", aciklama='" + aciklama + '\'' +
                '}';
    }
}
