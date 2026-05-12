package Model; // Bu sınıf Model paketine aittir

import java.util.Date; // Tarih işlemleri için Java Date sınıfını içe aktar

/**
 * Bildirim — Kullanıcılara gönderilen sistem bildirimlerini temsil eden model sınıfı.
 * Veritabanındaki "bildirimler" tablosuna karşılık gelir.
 */
public class Bildirim {
    private int bildirimId;              // Bildirimin benzersiz kimlik numarası (Primary Key)
    private int kullaniciId;             // Bildirimin gönderildiği kullanıcının ID'si (Foreign Key → kullanicilar)
    private String baslik;               // Bildirim başlığı — kısa ve açıklayıcı
    private String mesaj;                // Bildirim mesajı — detaylı açıklama metni
    private String tip;                  // Bildirim tipi — "bilgi", "uyari", "hata" veya "basari"
    private boolean okunduMu;            // Bildirim okundu mu? true=okundu, false=okunmadı
    private Date olusturmaTarihi;        // Bildirimin oluşturulma tarihi

    // Boş Constructor
    public Bildirim() {
    }

    // Parametreli Constructor — Tüm alanları alarak nesne oluşturur
    public Bildirim(int bildirimId, int kullaniciId, String baslik, String mesaj,
            String tip, boolean okunduMu, Date olusturmaTarihi) {
        this.bildirimId = bildirimId;           // Bildirim ID'sini ata
        this.kullaniciId = kullaniciId;         // Kullanıcı ID'sini ata
        this.baslik = baslik;                   // Başlık metnini ata
        this.mesaj = mesaj;                     // Mesaj metnini ata
        this.tip = tip;                         // Bildirim tipini ata
        this.okunduMu = okunduMu;               // Okunma durumunu ata
        this.olusturmaTarihi = olusturmaTarihi; // Oluşturma tarihini ata
    }

    // ─── Getter ve Setter Metotları ───
    public int getBildirimId() { return bildirimId; }                    // Bildirim ID'sini döndür
    public void setBildirimId(int bildirimId) { this.bildirimId = bildirimId; } // Bildirim ID'sini güncelle

    public int getKullaniciId() { return kullaniciId; }                  // Kullanıcı ID'sini döndür
    public void setKullaniciId(int kullaniciId) { this.kullaniciId = kullaniciId; }

    public String getBaslik() { return baslik; }                         // Başlığı döndür
    public void setBaslik(String baslik) { this.baslik = baslik; }

    public String getMesaj() { return mesaj; }                           // Mesajı döndür
    public void setMesaj(String mesaj) { this.mesaj = mesaj; }

    public String getTip() { return tip; }                               // Bildirim tipini döndür
    public void setTip(String tip) { this.tip = tip; }

    public boolean isOkunduMu() { return okunduMu; }                     // Okunma durumunu döndür
    public void setOkunduMu(boolean okunduMu) { this.okunduMu = okunduMu; }

    public Date getOlusturmaTarihi() { return olusturmaTarihi; }         // Oluşturma tarihini döndür
    public void setOlusturmaTarihi(Date olusturmaTarihi) { this.olusturmaTarihi = olusturmaTarihi; }

    // toString() — Debug ve loglama amacıyla nesneyi metin formatında döndürür
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
