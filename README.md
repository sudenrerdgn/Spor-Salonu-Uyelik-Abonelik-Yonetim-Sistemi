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
- SQL Server Management Studio 22

### Diğer
- GitHub
- AWS

---

# 👥 Kullanıcı Rollerı

Sistemde iki farklı kullanıcı rolü bulunmaktadır:

### Admin
- Üye/Antrenör ekleme / silme / düzenleme
- Abonelik paketlerini yönetme
- Ödeme kayıtlarını görüntüleme
- Sistem raporlarını inceleme
- Dersleri yönetme


### Antrenör
- Öğrencilerini görüntüleme
- Sorumlu olduğu dersleri/programları görüntüleme

  
### Üye
- Kendi abonelik bilgilerini görüntüleme
- Abonelik planı alma/ödeme
- Profil bilgilerini güncelleme

---

# 🧩 Sistem Özellikleri

### 👤 Üye Yönetimi
- Yeni üye ekleme
- Üye bilgilerini düzenleme
- Üye silme
- Üye listeleme

### 💳 Abonelik Yönetimi
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

---

# 🔌 API Endpoints

Sistemde sunulan RESTful API endpointleri ve yetki seviyeleri aşağıdaki gibidir:

### 🌍 Public (Token Gerekmez)
- `POST /api/kayit` - Yeni üye kaydı
- `POST /api/giris` - Giriş (JWT token döner)
- `POST /api/sifremi-unuttum` - Şifremi unuttum e-posta isteği
- `POST /api/sifre-sifirla` - Şifre sıfırlama işlemi
- `GET /api/planlar` - Üyelik planları (Landing sayfası için)
- `GET /api/test` - Veritabanı ve sunucu bağlantı testi

### 🔐 Kimlik Doğrulama
- `GET /api/dogrula` - Token doğrulama ve oturum yenileme (refresh)

### 👑 Admin
- `GET /api/uyeler` - Tüm üyeleri listeler
- `GET /api/kullanicilar` - Sistemdeki tüm kullanıcıları listeler
- `POST /api/uye-guncelle` - Üye bilgilerini günceller
- `POST /api/uye-sil` - Üyeyi siler
- `GET /api/istatistikler` - Dashboard için genel istatistikleri getirir
- `GET /api/aylik-gelir` - Aylık gelir istatistiklerini getirir
- `GET /api/aylik-uye` - Aylık üye kayıt istatistiklerini getirir
- `GET /api/aylik-devamsiz` - Devamsız üye istatistiklerini getirir
- `POST /api/antrenor-ekle` - Yeni antrenör ekler
- `POST /api/antrenor-guncelle` - Antrenör bilgilerini günceller
- `POST /api/antrenor-sil` - Antrenörü siler
- `POST /api/ekipman-ekle` - Yeni ekipman ekler
- `POST /api/ekipman-guncelle` - Ekipman bilgilerini günceller
- `POST /api/ekipman-sil` - Ekipmanı siler
- `POST /api/ders-ekle` - Yeni ders ekler
- `POST /api/ders-guncelle` - Ders bilgilerini günceller
- `POST /api/ders-sil` - Dersi siler

### 🧑‍💼 Admin | Üye (Kendi Verisi)
- `GET /api/odemeler` - Tüm ödemeleri (Admin) veya üyenin kendi ödemelerini getirir
- `GET /api/abonelikler` - Tüm abonelikleri (Admin) veya üyenin kendi aboneliklerini getirir
- `POST /api/profil-guncelle` - Kullanıcı profilini günceller
- `POST /api/abonelik-satin-al` - Yeni abonelik satın alma işlemi
- `POST /api/odeme-yap` - Ödeme yapma işlemi

### 🏋️‍♂️ Admin | Antrenör
- `GET /api/antrenorler-detay` - Antrenör detaylı listesi
- `GET /api/giris-cikis` - Üye giriş-çıkış kayıtlarını listeler
- `GET /api/ekipman` - Salondaki ekipman listesi ve durumları

### 👥 Tüm Giriş Yapmış Kullanıcılar
- `GET /api/dersler` - Salondaki ders programları ve listesi

