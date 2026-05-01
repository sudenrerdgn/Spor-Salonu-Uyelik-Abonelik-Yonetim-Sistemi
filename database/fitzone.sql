USE master;
GO

-- Varsa sil, yeniden oluştur
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'fitzone_db')
BEGIN
    ALTER DATABASE fitzone_db SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE fitzone_db;
END
GO

CREATE DATABASE fitzone_db
    COLLATE Turkish_CI_AS;
GO

USE fitzone_db;
GO

-- ══════════════════════════════════════════
-- TABLO YAPILARI (13 İlişkili Tablo)
-- ══════════════════════════════════════════

-- ──────────────────────────────────────────
-- 1. ROLLER
-- ──────────────────────────────────────────
CREATE TABLE roller (
    role_id   INT IDENTITY(1,1) PRIMARY KEY,
    rol_adi   NVARCHAR(50) NOT NULL UNIQUE
);
GO

-- ──────────────────────────────────────────
-- 2. KULLANICILAR
-- ──────────────────────────────────────────
CREATE TABLE kullanicilar (
    kullanici_id    INT IDENTITY(1,1) PRIMARY KEY,
    ad              NVARCHAR(100)  NOT NULL,
    soyad           NVARCHAR(100)  NOT NULL,
    email           NVARCHAR(255)  NOT NULL UNIQUE,
    sifre_hash      NVARCHAR(255)  NOT NULL,
    telefon         NVARCHAR(20),
    dogum_tarihi    DATE,
    cinsiyet        NVARCHAR(20)   CHECK (cinsiyet IN (N'Erkek', N'Kadın', N'Belirtilmemiş')),
    kayit_tarihi    DATETIME2      DEFAULT GETDATE(),
    son_giris       DATETIME2,
    rol_id          INT            NOT NULL REFERENCES roller(role_id),
    durum           NVARCHAR(20)   DEFAULT N'aktif' CHECK (durum IN (N'aktif', N'pasif', N'askida')),
    beni_hatirla    BIT            DEFAULT 0,
    sifre_token     NVARCHAR(255),
    token_gecerli   DATETIME2
);
GO

-- ──────────────────────────────────────────
-- 3. UYELER
-- ──────────────────────────────────────────
CREATE TABLE uyeler (
    uye_id          INT IDENTITY(1,1) PRIMARY KEY,
    kullanici_id    INT NOT NULL UNIQUE REFERENCES kullanicilar(kullanici_id) ON DELETE CASCADE,
    uyelik_no       NVARCHAR(20)  NOT NULL UNIQUE,
    acil_iletisim   NVARCHAR(150),
    saglik_notu     NVARCHAR(MAX),
    fotograf_url    NVARCHAR(500),
    kayit_tarihi    DATETIME2 DEFAULT GETDATE()
);
GO

-- ──────────────────────────────────────────
-- 4. UYE_PLANLARI
-- ──────────────────────────────────────────
CREATE TABLE uye_planlari (
    plan_id     INT IDENTITY(1,1) PRIMARY KEY,
    plan_adi    NVARCHAR(100)  NOT NULL UNIQUE,
    fiyat       DECIMAL(10,2)  NOT NULL,
    sure_ay     INT            NOT NULL,
    aciklama    NVARCHAR(MAX),
    ozellikler  NVARCHAR(MAX),
    durum       NVARCHAR(20)   DEFAULT N'aktif' CHECK (durum IN (N'aktif', N'pasif'))
);
GO

-- ──────────────────────────────────────────
-- 5. UYE_ABONELIKLERI
-- ──────────────────────────────────────────
CREATE TABLE uye_abonelikleri (
    abonelik_id         INT IDENTITY(1,1) PRIMARY KEY,
    uye_id              INT  NOT NULL REFERENCES uyeler(uye_id),
    plan_id             INT  NOT NULL REFERENCES uye_planlari(plan_id),
    baslangic_tarihi    DATE NOT NULL,
    bitis_tarihi        DATE NOT NULL,
    otomatik_yenile     BIT  DEFAULT 0,
    durum               NVARCHAR(20) DEFAULT N'aktif' CHECK (durum IN (N'aktif', N'pasif', N'iptal', N'suresi_doldu')),
    olusturma_tarihi    DATETIME2 DEFAULT GETDATE()
);
GO

-- ──────────────────────────────────────────
-- 6. ODEMELER
-- ──────────────────────────────────────────
CREATE TABLE odemeler (
    odeme_id        INT IDENTITY(1,1) PRIMARY KEY,
    abonelik_id     INT            NOT NULL REFERENCES uye_abonelikleri(abonelik_id),
    miktar          DECIMAL(10,2)  NOT NULL,
    odeme_tarihi    DATETIME2      DEFAULT GETDATE(),
    odeme_yontemi   NVARCHAR(50)   CHECK (odeme_yontemi IN (N'nakit', N'kredi_karti', N'havale', N'online')),
    durum           NVARCHAR(20)   DEFAULT N'tamamlandi' CHECK (durum IN (N'tamamlandi', N'beklemede', N'basarisiz', N'iade')),
    aciklama        NVARCHAR(MAX)
);
GO

-- ──────────────────────────────────────────
-- 7. ANTRENORLER
-- ──────────────────────────────────────────
CREATE TABLE antrenorler (
    antrenor_id     INT IDENTITY(1,1) PRIMARY KEY,
    kullanici_id    INT REFERENCES kullanicilar(kullanici_id) ON DELETE SET NULL,
    uzmanlik        NVARCHAR(255),
    deneyim_yili    INT DEFAULT 0,
    sertifikalar    NVARCHAR(MAX),
    biyografi       NVARCHAR(MAX),
    durum           NVARCHAR(20) DEFAULT N'aktif' CHECK (durum IN (N'aktif', N'pasif', N'izinli'))
);
GO

-- ──────────────────────────────────────────
-- 8. SINIFLAR (Dersler)
-- ──────────────────────────────────────────
CREATE TABLE siniflar (
    ders_id         INT IDENTITY(1,1) PRIMARY KEY,
    ders_adi        NVARCHAR(150) NOT NULL,
    antrenor_id     INT REFERENCES antrenorler(antrenor_id) ON DELETE SET NULL,
    kontenjan       INT NOT NULL DEFAULT 20,
    sure_dakika     INT NOT NULL DEFAULT 60,
    kategori        NVARCHAR(100),
    aciklama        NVARCHAR(MAX),
    durum           NVARCHAR(20) DEFAULT N'aktif' CHECK (durum IN (N'aktif', N'pasif'))
);
GO

-- ──────────────────────────────────────────
-- 9. SINIF_PROGRAMLARI
-- ──────────────────────────────────────────
CREATE TABLE sinif_programlari (
    program_id          INT IDENTITY(1,1) PRIMARY KEY,
    ders_id             INT NOT NULL REFERENCES siniflar(ders_id) ON DELETE CASCADE,
    gun                 NVARCHAR(15) NOT NULL CHECK (gun IN (N'Pazartesi', N'Salı', N'Çarşamba', N'Perşembe', N'Cuma', N'Cumartesi', N'Pazar')),
    baslangic_saati     TIME NOT NULL,
    bitis_saati         TIME NOT NULL,
    salon               NVARCHAR(50),
    durum               NVARCHAR(20) DEFAULT N'aktif' CHECK (durum IN (N'aktif', N'pasif', N'iptal'))
);
GO

-- ──────────────────────────────────────────
-- 10. SINIF_REZERVASYONLARI
-- ──────────────────────────────────────────
CREATE TABLE sinif_rezervasyonlari (
    rezervasyon_id      INT IDENTITY(1,1) PRIMARY KEY,
    uye_id              INT NOT NULL REFERENCES uyeler(uye_id),
    program_id          INT NOT NULL REFERENCES sinif_programlari(program_id),
    r_tarih             DATE NOT NULL,
    durum               NVARCHAR(20) DEFAULT N'aktif' CHECK (durum IN (N'aktif', N'iptal', N'tamamlandi')),
    olusturma_tarihi    DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT uq_rezervasyon UNIQUE (uye_id, program_id, r_tarih)
);
GO

-- ──────────────────────────────────────────
-- 11. GIRIS_CIKIS_KAYITLARI
-- ──────────────────────────────────────────
CREATE TABLE giris_cikis_kayitlari (
    g_c_id          INT IDENTITY(1,1) PRIMARY KEY,
    kullanici_id    INT NOT NULL REFERENCES kullanicilar(kullanici_id),
    giris_saat      DATETIME2   NOT NULL DEFAULT GETDATE(),
    cikis_saat      DATETIME2,
    giris_turu      NVARCHAR(30) DEFAULT N'normal' CHECK (giris_turu IN (N'normal', N'qr', N'kart')),
    durum           NVARCHAR(20) DEFAULT N'giris'   CHECK (durum IN (N'giris', N'cikis'))
);
GO

-- ──────────────────────────────────────────
-- 12. EKIPMAN
-- ──────────────────────────────────────────
CREATE TABLE ekipman (
    ekipman_id          INT IDENTITY(1,1) PRIMARY KEY,
    ekipman_adi         NVARCHAR(150)  NOT NULL,
    kategori            NVARCHAR(100),
    miktar              INT            NOT NULL DEFAULT 1,
    satin_alma_tarihi   DATE,
    satin_alma_fiyati   DECIMAL(10,2),
    durum               NVARCHAR(30)   DEFAULT N'calisiyor' CHECK (durum IN (N'calisiyor', N'bakimda', N'arizali', N'hurdaya_ayrildi')),
    aciklama            NVARCHAR(MAX)
);
GO

-- ──────────────────────────────────────────
-- 13. EKIPMAN_BAKIMI
-- ──────────────────────────────────────────
CREATE TABLE ekipman_bakimi (
    bakim_id        INT IDENTITY(1,1) PRIMARY KEY,
    ekipman_id      INT NOT NULL REFERENCES ekipman(ekipman_id) ON DELETE CASCADE,
    bakim_tarihi    DATE           NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    maliyet         DECIMAL(10,2),
    yapan_kisi      NVARCHAR(150),
    aciklama        NVARCHAR(MAX),
    sonraki_bakim   DATE,
    durum           NVARCHAR(30)   DEFAULT N'tamamlandi' CHECK (durum IN (N'tamamlandi', N'beklemede', N'devam_ediyor'))
);
GO


-- ══════════════════════════════════════════
-- İNDEKSLER (Performans Optimizasyonu)
-- ══════════════════════════════════════════
CREATE INDEX idx_kullanicilar_email      ON kullanicilar(email);
CREATE INDEX idx_kullanicilar_rol        ON kullanicilar(rol_id);
CREATE INDEX idx_kullanicilar_durum      ON kullanicilar(durum);
CREATE INDEX idx_uyeler_kullanici        ON uyeler(kullanici_id);
CREATE INDEX idx_abonelik_uye            ON uye_abonelikleri(uye_id);
CREATE INDEX idx_abonelik_plan           ON uye_abonelikleri(plan_id);
CREATE INDEX idx_abonelik_durum          ON uye_abonelikleri(durum);
CREATE INDEX idx_abonelik_bitis          ON uye_abonelikleri(bitis_tarihi);
CREATE INDEX idx_odeme_abonelik          ON odemeler(abonelik_id);
CREATE INDEX idx_odeme_tarih             ON odemeler(odeme_tarihi);
CREATE INDEX idx_sinif_antrenor          ON siniflar(antrenor_id);
CREATE INDEX idx_program_ders            ON sinif_programlari(ders_id);
CREATE INDEX idx_rezervasyon_uye         ON sinif_rezervasyonlari(uye_id);
CREATE INDEX idx_rezervasyon_program     ON sinif_rezervasyonlari(program_id);
CREATE INDEX idx_giris_kullanici         ON giris_cikis_kayitlari(kullanici_id);
CREATE INDEX idx_giris_saat              ON giris_cikis_kayitlari(giris_saat);
CREATE INDEX idx_ekipman_durum           ON ekipman(durum);
CREATE INDEX idx_bakim_ekipman           ON ekipman_bakimi(ekipman_id);
GO


-- ══════════════════════════════════════════
-- SEED DATA (Örnek Veriler)
-- ══════════════════════════════════════════

-- ── 1. ROLLER ─────────────────────────────
INSERT INTO roller (rol_adi) VALUES
  (N'admin'),
  (N'uye'),
  (N'antrenor'),
  (N'resepsiyonist');
GO

-- ── 2. KULLANICILAR ───────────────────────
INSERT INTO kullanicilar (ad, soyad, email, sifre_hash, telefon, dogum_tarihi, cinsiyet, rol_id, durum) VALUES
  (N'Admin',   N'Yönetici', N'admin@fitzone.com',  N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0500000000', '1985-01-01', N'Erkek', 1, N'aktif'),
  (N'Ahmet',   N'Yılmaz',   N'ahmet@mail.com',     N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0532111111', '1990-05-12', N'Erkek', 2, N'aktif'),
  (N'Fatma',   N'Kaya',     N'fatma@mail.com',     N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0533222222', '1995-08-20', N'Kadın', 2, N'aktif'),
  (N'Can',     N'Öztürk',   N'can@mail.com',       N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0534333333', '1988-11-03', N'Erkek', 2, N'aktif'),
  (N'Selin',   N'Arslan',   N'selin@mail.com',     N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0535444444', '1992-03-15', N'Kadın', 2, N'aktif'),
  (N'Emre',    N'Demir',    N'emre@mail.com',      N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0536555555', '1997-07-22', N'Erkek', 2, N'aktif'),
  (N'Zeynep',  N'Şahin',    N'zeynep@mail.com',    N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0537666666', '1993-12-08', N'Kadın', 2, N'pasif'),
  (N'Murat',   N'Çelik',    N'murat@mail.com',     N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0538777777', '1986-09-30', N'Erkek', 2, N'aktif'),
  (N'Ayşe',    N'Yıldız',   N'ayse@mail.com',      N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0539888888', '1994-04-17', N'Kadın', 2, N'aktif'),
  (N'Kemal',   N'Antrenör', N'kemal@fitzone.com',  N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0541999999', '1984-06-25', N'Erkek', 3, N'aktif'),
  (N'Deniz',   N'Koç',      N'deniz@fitzone.com',  N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0542000000', '1989-02-14', N'Kadın', 3, N'aktif'),
  (N'Elif',    N'Aydın',    N'elif@fitzone.com',   N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', N'0543111111', '1996-10-05', N'Kadın', 4, N'aktif');

GO

-- ── 3. UYELER ─────────────────────────────
INSERT INTO uyeler (kullanici_id, uyelik_no, acil_iletisim) VALUES
  (2,  N'FZ-2026-001', N'Anne: 0500111111'),
  (3,  N'FZ-2026-002', N'Eş: 0500222222'),
  (4,  N'FZ-2026-003', N'Baba: 0500333333'),
  (5,  N'FZ-2026-004', N'Kardeş: 0500444444'),
  (6,  N'FZ-2026-005', NULL),
  (7,  N'FZ-2026-006', N'Eş: 0500666666'),
  (8,  N'FZ-2026-007', N'Anne: 0500777777'),
  (9,  N'FZ-2026-008', N'Baba: 0500888888');
GO

-- ── 4. UYE_PLANLARI ───────────────────────
INSERT INTO uye_planlari (plan_adi, fiyat, sure_ay, aciklama, ozellikler) VALUES
  (N'Basic',    199.00, 1,  N'1 aylık temel üyelik',    N'["Fitness alanı","Duş & soyunma"]'),
  (N'Silver',   350.00, 3,  N'3 aylık gümüş üyelik',    N'["Fitness alanı","2 grup dersi/hafta","Duş & soyunma"]'),
  (N'Gold',     550.00, 6,  N'6 aylık altın üyelik',     N'["Fitness alanı","Sınırsız grup dersi","1 PT seansı/ay","Havuz"]'),
  (N'Platinum', 850.00, 12, N'12 aylık platin üyelik',   N'["Fitness alanı","Sınırsız her şey","3 PT seansı/ay","Havuz & Sauna","Öncelikli rezervasyon"]');
GO

-- ── 5. UYE_ABONELIKLERI ───────────────────
INSERT INTO uye_abonelikleri (uye_id, plan_id, baslangic_tarihi, bitis_tarihi, durum) VALUES
  (1, 4, '2026-01-15', '2027-01-15', N'aktif'),
  (2, 3, '2026-02-01', '2026-08-01', N'aktif'),
  (3, 2, '2026-01-01', '2026-04-01', N'aktif'),
  (4, 4, '2025-12-01', '2026-12-01', N'aktif'),
  (5, 1, '2026-03-01', '2026-04-01', N'aktif'),
  (6, 3, '2025-11-01', '2026-02-01', N'suresi_doldu'),
  (7, 2, '2026-02-15', '2026-05-15', N'aktif'),
  (8, 4, '2026-01-20', '2027-01-20', N'aktif');
GO

-- ── 6. ODEMELER ───────────────────────────
INSERT INTO odemeler (abonelik_id, miktar, odeme_yontemi, durum) VALUES
  (1, 850.00,  N'kredi_karti', N'tamamlandi'),
  (2, 550.00,  N'nakit',       N'tamamlandi'),
  (3, 350.00,  N'havale',      N'tamamlandi'),
  (4, 850.00,  N'kredi_karti', N'tamamlandi'),
  (5, 199.00,  N'nakit',       N'tamamlandi'),
  (6, 550.00,  N'online',      N'iade'),
  (7, 350.00,  N'kredi_karti', N'tamamlandi'),
  (8, 850.00,  N'kredi_karti', N'tamamlandi');
GO

-- ── 7. ANTRENORLER ────────────────────────
INSERT INTO antrenorler (kullanici_id, uzmanlik, deneyim_yili, sertifikalar, biyografi) VALUES
  (10, N'Fonksiyonel Fitness, Crossfit', 8, N'ACE CPT, CSCS',         N'8 yıllık deneyimli antrenör.'),
  (11, N'Yoga, Pilates, Aqua Aerobik',  5, N'RYT-200, STOTT Pilates', N'5 yıldır yoga ve pilates eğitmeni.');
GO

-- ── 8. SINIFLAR ───────────────────────────
INSERT INTO siniflar (ders_adi, antrenor_id, kontenjan, sure_dakika, kategori, aciklama) VALUES
  (N'Yoga Flow',           2, 15, 60, N'Esneklik', N'Akış temelli yoga dersi, tüm seviyeler.'),
  (N'Kickboks',            1, 20, 60, N'Kardio',   N'Yüksek yoğunluklu kickboks antrenmanı.'),
  (N'Aqua Aerobik',        2, 15, 45, N'Kardio',   N'Havuzda düşük etkili aerobik.'),
  (N'Fonksiyonel Fitness', 1, 20, 60, N'Güç',      N'Vücut ağırlığı ve fonksiyonel hareketler.'),
  (N'Pilates',             2, 12, 50, N'Esneklik', N'Mat pilates, çekirdek güçlendirme.');
GO

-- ── 9. SINIF_PROGRAMLARI ──────────────────
INSERT INTO sinif_programlari (ders_id, gun, baslangic_saati, bitis_saati, salon) VALUES
  (1, N'Pazartesi',  '08:00', '09:00', N'Salon A'),
  (2, N'Pazartesi',  '10:00', '11:00', N'Salon B'),
  (3, N'Salı',       '14:00', '14:45', N'Havuz'),
  (4, N'Çarşamba',   '18:00', '19:00', N'Salon C'),
  (5, N'Perşembe',   '09:00', '09:50', N'Salon A'),
  (1, N'Cuma',       '08:00', '09:00', N'Salon A'),
  (2, N'Cumartesi',  '10:00', '11:00', N'Salon B');
GO

-- ── 10. SINIF_REZERVASYONLARI ─────────────
INSERT INTO sinif_rezervasyonlari (uye_id, program_id, r_tarih, durum) VALUES
  (1, 1, '2026-03-10', N'tamamlandi'),
  (2, 2, '2026-03-10', N'aktif'),
  (3, 3, '2026-03-11', N'aktif'),
  (1, 4, '2026-03-12', N'aktif'),
  (4, 5, '2026-03-13', N'aktif'),
  (5, 1, '2026-03-14', N'aktif');
GO

-- ── 11. GIRIS_CIKIS_KAYITLARI ─────────────
INSERT INTO giris_cikis_kayitlari (kullanici_id, giris_saat, cikis_saat, giris_turu, durum) VALUES
  (2, DATEADD(HOUR, -3, GETDATE()), DATEADD(HOUR, -1, GETDATE()), N'kart',   N'cikis'),
  (3, DATEADD(HOUR, -2, GETDATE()), NULL,                          N'qr',     N'giris'),
  (4, DATEADD(HOUR, -5, GETDATE()), DATEADD(HOUR, -3, GETDATE()), N'normal', N'cikis'),
  (5, DATEADD(HOUR, -1, GETDATE()), NULL,                          N'kart',   N'giris');
GO

-- ── 12. EKIPMAN ───────────────────────────
INSERT INTO ekipman (ekipman_adi, kategori, miktar, satin_alma_tarihi, satin_alma_fiyati, durum) VALUES
  (N'Koşu Bandı',           N'Kardio',    8, '2024-01-15', 15000.00, N'calisiyor'),
  (N'Eliptik Bisiklet',     N'Kardio',    4, '2024-01-15', 12000.00, N'calisiyor'),
  (N'Ağırlık Sehpası',      N'Güç',      10, '2024-02-01', 800.00,   N'calisiyor'),
  (N'Dumbbell Seti',        N'Güç',       5, '2024-02-01', 3500.00,  N'calisiyor'),
  (N'Kürek Çekme Makinesi', N'Kardio',    2, '2024-03-10', 9000.00,  N'bakimda'),
  (N'Smith Machine',        N'Güç',       2, '2024-03-10', 18000.00, N'calisiyor'),
  (N'Yoga Matı',            N'Esneklik', 20, '2024-04-01', 150.00,   N'calisiyor');
GO

-- ── 13. EKIPMAN_BAKIMI ────────────────────
INSERT INTO ekipman_bakimi (ekipman_id, bakim_tarihi, maliyet, yapan_kisi, aciklama, sonraki_bakim, durum) VALUES
  (5, '2026-03-10', 1200.00, N'Teknik Servis A', N'Motor yağlama ve kemer değişimi.', '2026-06-10', N'tamamlandi'),
  (1, '2026-02-20', 500.00,  N'Teknik Servis B', N'Yıllık rutin bakım.',              '2026-08-20', N'tamamlandi');
GO


-- ══════════════════════════════════════════
-- DOĞRULAMA SORGUSU
-- ══════════════════════════════════════════
SELECT 'roller'                 AS tablo, COUNT(*) AS kayit FROM roller               UNION ALL
SELECT 'kullanicilar',                    COUNT(*)          FROM kullanicilar          UNION ALL
SELECT 'uyeler',                          COUNT(*)          FROM uyeler                UNION ALL
SELECT 'uye_planlari',                    COUNT(*)          FROM uye_planlari          UNION ALL
SELECT 'uye_abonelikleri',                COUNT(*)          FROM uye_abonelikleri      UNION ALL
SELECT 'odemeler',                        COUNT(*)          FROM odemeler              UNION ALL
SELECT 'antrenorler',                     COUNT(*)          FROM antrenorler           UNION ALL
SELECT 'siniflar',                        COUNT(*)          FROM siniflar              UNION ALL
SELECT 'sinif_programlari',               COUNT(*)          FROM sinif_programlari     UNION ALL
SELECT 'sinif_rezervasyonlari',           COUNT(*)          FROM sinif_rezervasyonlari UNION ALL
SELECT 'giris_cikis_kayitlari',           COUNT(*)          FROM giris_cikis_kayitlari UNION ALL
SELECT 'ekipman',                         COUNT(*)          FROM ekipman               UNION ALL
SELECT 'ekipman_bakimi',                  COUNT(*)          FROM ekipman_bakimi;
GO