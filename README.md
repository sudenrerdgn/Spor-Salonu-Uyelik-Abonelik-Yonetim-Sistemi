🏋️ Spor Salonu Üyelik ve Abonelik Yönetim Sistemi

## 📌 Proje Hakkında

Bu proje, spor salonları için geliştirilen bir **üyelik ve abonelik yönetim sistemi**dir.  
Sistem sayesinde spor salonu yöneticileri üyeleri, abonelik paketlerini ve ödemeleri kolay bir şekilde yönetebilir.

Proje, **İleri Web Uygulamaları** dersi kapsamında geliştirilmektedir ve **MVC mimarisi** kullanılarak tasarlanmıştır.

---

# 🎯 Projenin Amacı

Bu projenin amacı:

- Spor salonu üyelik süreçlerini dijital ortama taşımak
- Üye kayıt ve abonelik işlemlerini kolaylaştırmak
- Yönetici paneli üzerinden sistem yönetimi sağlamak
- Modern web teknolojileri ile güvenli bir web uygulaması geliştirmek

---

# ⚙️ Kullanılan Teknolojiler

Projede aşağıdaki teknolojiler kullanılacaktır:

### Backend
- Java Spring Boot

### Frontend
- HTML
- CSS
- JavaScript

### Veritabanı
- PostgreSQL

### Diğer
- GitHub
- AWS

---

# 👥 Kullanıcı Rollerı

Sistemde iki farklı kullanıcı rolü bulunmaktadır:

### Admin
- Üye ekleme / silme / düzenleme
- Abonelik paketlerini yönetme
- Ödeme kayıtlarını görüntüleme
- Sistem raporlarını inceleme

### Antrenör
- Öğrencilerini görüntüleme
- Sorumlu olduğu dersleri/programları görüntüleme

  
### Üye
- Kendi abonelik bilgilerini görüntüleme
- Profil bilgilerini güncelleme

---

# 🧩 Sistem Özellikleri

### 👤 Üye Yönetimi
- Yeni üye ekleme
- Üye bilgilerini düzenleme
- Üye silme
- Üye listeleme

### 💳 Abonelik Yönetimi
- Abonelik paketi oluşturma
- Paket güncelleme
- Paket silme
- Paket fiyat ve süre yönetimi

### 💰 Ödeme Yönetimi
- Üye ödeme kayıtları
- Ödeme geçmişi
- Ödeme takibi

### 🔐 Kimlik Doğrulama
- Kullanıcı kayıt sistemi
- Giriş sistemi
- Şifre hashleme
- Rol bazlı yetkilendirme
- "Beni Hatırla" ve "Şifremi Unuttum" özelliği

---

# 🗄️ Veritabanı Tasarımı

Sistem, ilişkisel bir veritabanı kullanmaktadır.

Temel tablolar:

- ROLLER
- UYELER
- KULLANICILAR
- ANTRENORLER
- UYELER
- SINIFLAR
- SINIF_PROGRAMLARI
- SINIF_REZERVASYONLARI
- UYE_ABONELIKLERI
- UYE_PLANLARI
- GIRIS_CIKIS_KAYITLARI
- ODEMELER
- EKIPMAN
- EKIPMAN_BAKIM

Toplam **13 ilişkili tablo** bulunmaktadır.

---

# 📊 Yönetim Paneli

Admin paneli üzerinden aşağıdaki işlemler yapılabilir:

- Dashboard (istatistikler)
- Üye yönetimi
- Abonelik paketleri
- Ödeme takibi
- Sistem logları

---

# 🔒 Güvenlik

Projede aşağıdaki güvenlik önlemleri uygulanacaktır:

- SQL Injection koruması
- XSS koruması
- CSRF koruması
- Güvenli şifre hashleme
- Yetkisiz erişim engelleme

---

# ☁️ Deployment

Proje **AWS Cloud** ortamında yayınlanacaktır.
