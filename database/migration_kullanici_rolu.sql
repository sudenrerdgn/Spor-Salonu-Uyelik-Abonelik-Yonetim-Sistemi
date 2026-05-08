-- ══════════════════════════════════════════════════════
-- MİGRASYON: 'kullanici' Rolü Ekleme + Mevcut Hatalı Kayıtları Düzeltme
-- Çalıştırma: SQL Server Management Studio'da fitzone_db'yi
--             seçip bu scripti çalıştırın.
-- ══════════════════════════════════════════════════════

USE fitzone_db;
GO

-- ─── ADIM 1: 'kullanici' rolü yoksa ekle ──────────────
IF NOT EXISTS (SELECT 1 FROM roller WHERE rol_adi = N'kullanici')
BEGIN
    INSERT INTO roller (rol_adi) VALUES (N'kullanici');
    PRINT '✅ kullanici rolü roller tablosuna eklendi.';
END
ELSE
BEGIN
    PRINT 'ℹ️  kullanici rolü zaten mevcut.';
END
GO

-- ─── ADIM 2: Hatalı Kayıtları Düzelt ─────────────────
-- 'uye' rolünde olan ama uyeler tablosunda KAYDI OLMAYAN
-- kullanıcılar aslında 'kullanici' rolünde olmalıydı.
-- (Plan almadan önce kayıt olanlar)

DECLARE @kullaniciRolId INT = (SELECT role_id FROM roller WHERE rol_adi = N'kullanici');
DECLARE @uyeRolId       INT = (SELECT role_id FROM roller WHERE rol_adi = N'uye');

UPDATE kullanicilar
SET rol_id = @kullaniciRolId
WHERE rol_id = @uyeRolId
  AND kullanici_id NOT IN (SELECT kullanici_id FROM uyeler);

PRINT CAST(@@ROWCOUNT AS VARCHAR) + ' kayıt uye → kullanici rolüne düzeltildi.';
GO

-- ─── ADIM 3: Doğrulama ────────────────────────────────
SELECT r.rol_adi, COUNT(*) AS adet
FROM kullanicilar k
JOIN roller r ON k.rol_id = r.role_id
GROUP BY r.rol_adi
ORDER BY r.rol_adi;
GO
