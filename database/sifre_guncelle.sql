-- ============================================================
-- SIFRE HASH GUNCELLEME SCRIPTI
-- SQL Server Management Studio'da calistirin
-- Tum sifreleri admin1234 SHA-256 hash'i ile gunceller
-- ============================================================
USE fitzone;
GO

UPDATE kullanicilar 
SET sifre_hash = 'ac9689e2272427085e35b9d3e3e8bed88cb3434828b43b86fc0596cad4c6e270';
GO

-- Dogrulama
SELECT kullanici_id, ad, soyad, email, LEFT(sifre_hash, 20) AS hash_baslangic, rol_id, durum
FROM kullanicilar;
GO
