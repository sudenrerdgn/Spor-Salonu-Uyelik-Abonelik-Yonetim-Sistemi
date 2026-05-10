-- ============================================================
-- ANTRENÖR ŞIFRE GÜNCELLEME SCRIPTI
-- fitzone_db üzerinde çalıştırın
-- ============================================================

USE fitzone_db;
GO

-- Kemal Antrenör → Şifre: kemal123
UPDATE kullanicilar
SET sifre_hash = '4f4a570a34099bb2734bd493cdb78edf1c9eb808d70679c9f654de76b62705cf'
WHERE email = 'kemal@fitzone.com';
GO

-- Deniz Koç → Şifre: deniz123
UPDATE kullanicilar
SET sifre_hash = 'b561fab2d5c85b3cabe69a20bad81be36501217a0b3991bf9685540a58968e3e'
WHERE email = 'deniz@fitzone.com';
GO

-- Doğrulama
SELECT ad, soyad, email, rol_id FROM kullanicilar WHERE email IN ('kemal@fitzone.com', 'deniz@fitzone.com');
GO
