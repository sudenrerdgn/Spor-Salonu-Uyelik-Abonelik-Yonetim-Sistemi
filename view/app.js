// ═══════════════════════════════════════════
// FitZone Pro — app.js
// Sadece Frontend Demo Verileri
// Backend bağlantısı yapılacak
// ═══════════════════════════════════════════

// ─── XSS KORUMASI (Global Fetch Wrapper) ───
const originalFetch = window.fetch;
window.fetch = async (...args) => {
  const response = await originalFetch(...args);
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    const originalJson = response.json.bind(response);
    response.json = async () => {
      const json = await originalJson();
      const sanitize = (obj) => {
        if (typeof obj === 'string') {
          return obj.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
        }
        if (Array.isArray(obj)) return obj.map(sanitize);
        if (typeof obj === 'object' && obj !== null) {
          const newObj = {};
          for (const key in obj) newObj[key] = sanitize(obj[key]);
          return newObj;
        }
        return obj;
      };
      return sanitize(json);
    };
  }
  return response;
};

// ─── DEMO VERİLER ───
const planColors = {
  platinum: { class: 'platinum', icon: '💎', price: '₺850', color: '#a78bfa', bg: 'rgba(139,92,246,.15)' },
  gold:     { class: 'gold',     icon: '⭐', price: '₺550', color: '#fbbf24', bg: 'rgba(251,191,36,.15)' },
  silver:   { class: 'silver',   icon: '🥈', price: '₺350', color: '#94a3b8', bg: 'rgba(148,163,184,.15)' },
  basic:    { class: 'basic',    icon: '🔰', price: '₺199', color: '#67e8f9', bg: 'rgba(34,211,238,.1)' }
};

const avatarColors = [
  'linear-gradient(135deg,#8b5cf6,#06b6d4)',
  'linear-gradient(135deg,#f472b6,#8b5cf6)',
  'linear-gradient(135deg,#22d3ee,#6366f1)',
  'linear-gradient(135deg,#fb923c,#f472b6)',
  'linear-gradient(135deg,#4ade80,#06b6d4)',
  'linear-gradient(135deg,#fbbf24,#f472b6)',
];

// ─── ÜYE LİSTESİ (localStorage destekli) ───
const defaultMembers = [
  { id:1, name:'Ahmet Yılmaz',   email:'ahmet@mail.com',  uyelikNo:'FZ-2026-001', plan:'platinum', start:'2026-01-15', end:'2027-01-15', payment:'₺850', status:'aktif',        odemeYontemi:'kredi_karti' },
  { id:2, name:'Fatma Kaya',     email:'fatma@mail.com',  uyelikNo:'FZ-2026-002', plan:'gold',     start:'2026-02-01', end:'2026-08-01', payment:'₺550', status:'aktif',        odemeYontemi:'nakit' },
  { id:3, name:'Can Öztürk',     email:'can@mail.com',    uyelikNo:'FZ-2026-003', plan:'silver',   start:'2026-01-01', end:'2026-04-01', payment:'₺350', status:'aktif',        odemeYontemi:'havale' },
  { id:4, name:'Selin Arslan',   email:'selin@mail.com',  uyelikNo:'FZ-2026-004', plan:'platinum', start:'2025-12-01', end:'2026-12-01', payment:'₺850', status:'aktif',        odemeYontemi:'kredi_karti' },
  { id:5, name:'Emre Demir',     email:'emre@mail.com',   uyelikNo:'FZ-2026-005', plan:'basic',    start:'2026-03-01', end:'2026-04-01', payment:'₺199', status:'aktif',        odemeYontemi:'nakit' },
  { id:6, name:'Zeynep Şahin',   email:'zeynep@mail.com', uyelikNo:'FZ-2026-006', plan:'gold',     start:'2025-11-01', end:'2026-02-01', payment:'₺550', status:'suresi_doldu', odemeYontemi:'online' },
  { id:7, name:'Murat Çelik',    email:'murat@mail.com',  uyelikNo:'FZ-2026-007', plan:'silver',   start:'2026-02-15', end:'2026-05-15', payment:'₺350', status:'aktif',        odemeYontemi:'kredi_karti' },
  { id:8, name:'Ayşe Yıldız',    email:'ayse@mail.com',   uyelikNo:'FZ-2026-008', plan:'platinum', start:'2026-01-20', end:'2027-01-20', payment:'₺850', status:'aktif',        odemeYontemi:'kredi_karti' },
];
let members = JSON.parse(localStorage.getItem('fitzone_members')) || [...defaultMembers];

let filtered = [...members];

// ─── localStorage kaydetme yardımcıları ───
function saveMembers()         { localStorage.setItem('fitzone_members', JSON.stringify(members)); }
function saveRegisteredUsers() { localStorage.setItem('fitzone_users', JSON.stringify(registeredUsers)); }

// ═══════════════════════════════════════════
// ÜYE TABLOSU
// ═══════════════════════════════════════════

function getInitials(name) {
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
}

function renderMembers(data) {
  const tbody = document.getElementById('membersTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';
  const statusLabel = { aktif:'Aktif', pasif:'Pasif', iptal:'İptal', suresi_doldu:'Süresi Doldu', askida:'Askıda' };
  data.forEach((m, idx) => {
    const color = avatarColors[idx % avatarColors.length];
    const name = m.name || (m.ad + ' ' + m.soyad);
    const durum = m.status || m.durum || 'aktif';
    tbody.innerHTML += `
      <tr>
        <td>
          <div class="member-info">
            <div class="m-avatar" style="background:${color}">${getInitials(name)}</div>
            <div>
              <div class="m-name">${name}</div>
              <div class="m-email">${m.email}</div>
            </div>
          </div>
        </td>
        <td style="color:var(--text-muted);font-size:12px">${m.uyelikNo || ''}</td>
        <td style="color:var(--text-muted);font-size:12px">${m.telefon || ''}</td>
        <td><span class="status-dot ${durum}">${statusLabel[durum] || durum}</span></td>
        <td style="color:var(--text-muted);font-size:12px">${m.kayitTarihi || ''}</td>
        <td>
          <div style="display:flex;gap:6px">
            <div class="icon-btn" style="width:30px;height:30px;border-radius:8px;font-size:11px;cursor:pointer" title="Düzenle" onclick="apiEditMember(${m.id})"><i class="fas fa-pen"></i></div>
            <div class="icon-btn" style="width:30px;height:30px;border-radius:8px;font-size:11px;cursor:pointer" title="Sil" onclick="apiDeleteMember(${m.id})"><i class="fas fa-trash" style="color:#f87171"></i></div>
          </div>
        </td>
      </tr>`;
  });
  const countEl = document.getElementById('memberCount');
  if (countEl) countEl.textContent = `${data.length} üye gösteriliyor`;
}

function filterMembers(q) {
  const source = apiMembers.length > 0 ? apiMembers : members;
  filtered = source.filter(m => {
    const name = m.name || (m.ad + ' ' + m.soyad);
    return name.toLowerCase().includes(q.toLowerCase()) || m.email.toLowerCase().includes(q.toLowerCase());
  });
  const st = document.getElementById('statusFilter').value;
  if (st !== 'hepsi') filtered = filtered.filter(m => (m.status || m.durum) === st);
  renderMembers(filtered);
}

function filterByStatus(st) {
  const q = document.getElementById('searchInput').value;
  const source = apiMembers.length > 0 ? apiMembers : members;
  filtered = source.filter(m => {
    const name = m.name || (m.ad + ' ' + m.soyad);
    return name.toLowerCase().includes(q.toLowerCase()) || m.email.toLowerCase().includes(q.toLowerCase());
  });
  if (st !== 'hepsi') filtered = filtered.filter(m => (m.status || m.durum) === st);
  renderMembers(filtered);
}

function deleteMember(id) {
  members = members.filter(m => m.id !== id);
  filterByStatus(document.getElementById('statusFilter')?.value || 'hepsi');
  const te = document.getElementById('totalMembers');
  const se = document.getElementById('sidebarMemberCount');
  if (te) te.textContent = members.length;
  if (se) se.textContent = members.length;
  showToast('Üye silindi.');
}

// ═══════════════════════════════════════════
// API — SQL Server üzerinden üye yönetimi
// ═══════════════════════════════════════════
const API_URL = 'http://13.53.225.142:8080';
let apiMembers = []; // SQL Server'dan gelen üye listesi

// ═══════════════════════════════════════════
// TOKEN YÖNETİMİ — Auth helpers
// ═══════════════════════════════════════════
function getToken()    { return localStorage.getItem('fitzone_token'); }
function setToken(t)   { localStorage.setItem('fitzone_token', t); }
function clearToken()  {
  localStorage.removeItem('fitzone_token');
  localStorage.removeItem('fitzone_user');
  localStorage.removeItem('fitzone_csrf');
}
function getSavedUser() {
  try { return JSON.parse(localStorage.getItem('fitzone_user')); } catch { return null; }
}

/** loadPublicStats — Açılış sayfası istatistiklerini yükler */
function loadPublicStats() {
    fetch('/api/public-istatistikler')
        .then(res => res.json())
        .then(data => {
            const el1 = document.getElementById('aktifUyeStat');
            const el2 = document.getElementById('dersSayisiStat');
            if (el1) el1.textContent = (data.aktifUye || 0) + '+';
            if (el2) el2.textContent = data.dersSayisi || 0;
        })
        .catch(err => console.log('Kamu istatistikleri yüklenemedi.'));
}

// ═══ SAYFA YÜKLENİNCE ═══
document.addEventListener('DOMContentLoaded', () => {
    loadPublicStats();
    // Mevcut init kodları...
    checkSession();
});

/**
 * apiFetch — Authorization header'lı fetch wrapper
 * 401/403 alınırsa otomatik çıkış yapılır.
 */
function apiFetch(path, options = {}) {
  const token = getToken();
  const csrf = localStorage.getItem('fitzone_csrf');
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': 'Bearer ' + token } : {}),
    ...(csrf ? { 'X-CSRF-Token': csrf } : {}),
    ...(options.headers || {})
  };
  return fetch(API_URL + path, { ...options, headers })
    .then(res => {
      if (res.status === 401) {
        clearToken();
        forcedLogout();
        throw new Error('AUTH_ERROR');
      }
      if (res.status === 403) {
        throw new Error('AUTH_ERROR');
      }
      return res.json();
    });
}

/**
 * checkSession — Sayfa yenilenince mevcut token'ı doğrula.
 * Geçerliyse loginAs() ile paneli aç, değilse landing sayfası kalır.
 */
async function checkSession() {
  const token = getToken();
  if (!token) return false;
  try {
    const csrf = localStorage.getItem('fitzone_csrf');
    const data = await fetch(API_URL + '/api/dogrula', {
      headers: { 
        'Authorization': 'Bearer ' + token, 
        'Content-Type': 'application/json',
        ...(csrf ? { 'X-CSRF-Token': csrf } : {})
      }
    }).then(r => r.json());
    if (data.basarili) {
      if (data.token) setToken(data.token);
      if (data.csrfToken) localStorage.setItem('fitzone_csrf', data.csrfToken);
      const k = data.kullanici;
      const saved = getSavedUser() || {};
      // 'kullanici' rolü henüz aboneliği olmayan kayıtlı kullanıcıdır; 'uye' paneliyle gösterilir
      const effectiveRol = (k.rol === 'kullanici') ? 'uye' : (k.rol || saved.rol || 'uye');
      const user = {
        id:    k.id,
        ad:    k.ad    || saved.ad    || '',
        soyad: k.soyad || saved.soyad || '',
        email: k.email || saved.email || '',
        rol:   k.rol   || saved.rol   || 'kullanici',
        telefon: k.telefon || saved.telefon || '',
        name:  (k.ad||saved.ad||'') + ' ' + (k.soyad||saved.soyad||'')
      };
      loginAs(effectiveRol, user);
      return true;
    }
  } catch(e) { /* network error */ }
  clearToken();
  return false;
}

/** forcedLogout — token hatası nedeniyle çıkış */
function forcedLogout() {
  showToast('⚠️ Oturum süresi doldu, lütfen tekrar giriş yapın!');
  setTimeout(() => logout(), 1200);
}

function loadMembersFromAPI() {
  return apiFetch('/api/uyeler')
    .then(data => {
      const uyeler = data.uyeler || (Array.isArray(data) ? data : []);
      apiMembers = uyeler.map(u => ({
        id: u.id, ad: u.ad, soyad: u.soyad,
        name: u.ad + ' ' + u.soyad,
        email: u.email, telefon: u.telefon || '',
        cinsiyet: u.cinsiyet || '', rol: u.rol,
        durum: u.durum, kayitTarihi: u.kayitTarihi || '',
        uyelikNo: u.uyelikNo || '', dogumTarihi: u.dogumTarihi || '',
        abonelikPlan: u.abonelikPlan || '', abonelikBitis: u.abonelikBitis || '',
        abonelikDurum: u.abonelikDurum || ''
      }));
      if (data.stats) window.uyelerPageStats = data.stats;
      return apiMembers;
    })
    .catch(e => { if(e.message!=='AUTH_ERROR') console.log('Üye API yok, fallback'); return []; });
}

function apiDeleteMember(id) {
  const m = apiMembers.find(u => u.id === id);
  if (!m) { showToast('Üye bulunamadı!'); return; }
  document.getElementById('deleteId').value = id;
  document.getElementById('deleteConfirmName').textContent = m.name || (m.ad + ' ' + m.soyad);
  document.getElementById('deleteMemberModal').classList.add('open');
}

function closeDeleteModal() {
  document.getElementById('deleteMemberModal').classList.remove('open');
}

function submitDeleteMember() {
  const id = document.getElementById('deleteId').value;
  closeDeleteModal();
  apiFetch('/api/uye-sil', {
    method: 'POST',
    body: JSON.stringify({ id: String(id) })
  })
  .then(data => {
    showToast(data.mesaj);
    if (data.basarili) refreshMemberViews();
  })
  .catch(e => { if(e.message!=='AUTH_ERROR') showToast('Sunucu bağlantısı hatası!'); });
}

function apiEditMember(id) {
  const m = apiMembers.find(u => u.id === id);
  if (!m) { showToast('Üye bulunamadı!'); return; }
  document.getElementById('editId').value = id;
  document.getElementById('editAd').value = m.ad || '';
  document.getElementById('editSoyad').value = m.soyad || '';
  document.getElementById('editEmail').value = m.email || '';
  document.getElementById('editTelefon').value = m.telefon || '';
  document.getElementById('editDurum').value = m.durum || 'aktif';
  document.getElementById('editMemberModal').classList.add('open');
}

function closeEditModal() {
  document.getElementById('editMemberModal').classList.remove('open');
}

function submitEditMember() {
  const id = document.getElementById('editId').value;
  const ad = document.getElementById('editAd').value.trim();
  const soyad = document.getElementById('editSoyad').value.trim();
  const email = document.getElementById('editEmail').value.trim();
  const telefon = document.getElementById('editTelefon').value.trim();
  const durum = document.getElementById('editDurum').value;

  if (!ad || !soyad || !email) {
    showToast('Ad, soyad ve e-posta zorunludur!');
    return;
  }

  closeEditModal();
  apiFetch('/api/uye-guncelle', {
    method: 'POST',
    body: JSON.stringify({ id: String(id), ad, soyad, email, telefon: telefon || '', durum: durum || 'aktif' })
  })
  .then(data => {
    showToast(data.mesaj);
    if (data.basarili) refreshMemberViews();
  })
  .catch(e => { if(e.message!=='AUTH_ERROR') showToast('Sunucu bağlantısı hatası!'); });
}

function refreshMemberViews() {
  loadMembersFromAPI().then(data => {
    renderMembers(data);
    renderUyelerPage(data);
    const te = document.getElementById('totalMembers');
    const se = document.getElementById('sidebarMemberCount');
    if (te) te.textContent = data.length;
    if (se) se.textContent = data.length;
    // Üyeler sayfası stat kartlarını da güncelle
    loadDashboardStats();
  });
}

// ═══════════════════════════════════════════
// SON ÖDEMELER
// ═══════════════════════════════════════════
function renderRecentPayments() {
  const container = document.getElementById('recentPayments');
  const payments = [
    { name:'Ahmet Yılmaz',  plan:'Platinum', method:'Kredi Kartı', date:'12 Mar', amount:850,  icon:'fa-crown',       iconColor:'#a78bfa', iconBg:'rgba(139,92,246,.15)', type:'pos' },
    { name:'Fatma Kaya',    plan:'Gold',     method:'Nakit',       date:'11 Mar', amount:550,  icon:'fa-star',        iconColor:'#fbbf24', iconBg:'rgba(251,191,36,.15)', type:'pos' },
    { name:'Can Öztürk',    plan:'Silver',   method:'Havale',      date:'10 Mar', amount:350,  icon:'fa-medal',       iconColor:'#94a3b8', iconBg:'rgba(148,163,184,.15)', type:'pos' },
    { name:'Zeynep Şahin',  plan:'İade',     method:'Online',      date:'10 Mar', amount:550,  icon:'fa-rotate-left', iconColor:'#f87171', iconBg:'rgba(239,68,68,.15)',   type:'neg' },
  ];
  container.innerHTML = '';
  payments.forEach(p => {
    container.innerHTML += `
      <div class="payment-item">
        <div class="pay-icon" style="background:${p.iconBg};color:${p.iconColor}"><i class="fas ${p.icon}"></i></div>
        <div><div class="pay-name">${p.name}</div><div class="pay-date">${p.plan} — ${p.method} — ${p.date}</div></div>
        <div class="pay-amount ${p.type}">${p.type==='neg'?'-':'+'}₺${p.amount}</div>
      </div>`;
  });
}

// ═══════════════════════════════════════════
// ABONELİK PLANLARI
// ═══════════════════════════════════════════
function renderPlanCards() {
  const container = document.getElementById('planCards');
  const plans = [
    { icon:'💎', name:'Platinum', members:3,  sureAy:12, price:'₺850/ay', color:'#a78bfa', bg:'rgba(139,92,246,.15)' },
    { icon:'⭐', name:'Gold',     members:2,  sureAy:6,  price:'₺550/ay', color:'#fbbf24', bg:'rgba(251,191,36,.15)' },
    { icon:'🥈', name:'Silver',   members:2,  sureAy:3,  price:'₺350/ay', color:'#94a3b8', bg:'rgba(148,163,184,.15)' },
    { icon:'🔰', name:'Basic',    members:1,  sureAy:1,  price:'₺199/ay', color:'#67e8f9', bg:'rgba(34,211,238,.1)' },
  ];
  container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
  plans.forEach(p => {
    container.innerHTML += `
      <div class="plan-card">
        <div class="plan-left">
          <div class="plan-icon" style="background:${p.bg};color:${p.color};font-size:18px;">${p.icon}</div>
          <div><div class="plan-name">${p.name}</div><div class="plan-members">${p.members} aktif üye · ${p.sureAy} ay</div></div>
        </div>
        <div class="plan-price" style="color:${p.color}">${p.price}</div>
      </div>`;
  });
  container.innerHTML += '</div>';
}

// ═══════════════════════════════════════════
// BUGÜNKÜ DERSLER (siniflar + sinif_programlari)
// ═══════════════════════════════════════════
function renderClassCards() {
  const container = document.getElementById('classCards');
  const classes = [
    { icon:'🧘', name:'Yoga Flow',           time:'08:00–09:00', salon:'Salon A', trainer:'Deniz Koç',      capacity:'12/15', status:'Açık', statusColor:'var(--accent-cyan)', statusBg:'rgba(0,212,255,.1)' },
    { icon:'🥊', name:'Kickboks',            time:'10:00–11:00', salon:'Salon B', trainer:'Kemal Antrenör', capacity:'18/20', status:'Dolu', statusColor:'var(--accent-orange)', statusBg:'rgba(251,146,60,.1)' },
    { icon:'🏊', name:'Aqua Aerobik',        time:'14:00–14:45', salon:'Havuz',   trainer:'Deniz Koç',      capacity:'8/15',  status:'Açık', statusColor:'#4ade80', statusBg:'rgba(74,222,128,.1)' },
    { icon:'🏋️', name:'Fonksiyonel Fitness', time:'18:00–19:00', salon:'Salon C', trainer:'Kemal Antrenör', capacity:'15/20', status:'Açık', statusColor:'var(--accent-cyan)', statusBg:'rgba(0,212,255,.1)' },
  ];
  container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
  classes.forEach(c => {
    container.innerHTML += `
      <div class="plan-card">
        <div class="plan-left">
          <div class="plan-icon" style="background:rgba(0,212,255,.1);color:var(--accent-cyan);font-size:17px;">${c.icon}</div>
          <div><div class="plan-name">${c.name}</div><div class="plan-members">${c.time} — ${c.salon} — ${c.trainer} — ${c.capacity}</div></div>
        </div>
        <div style="font-size:11px;background:${c.statusBg};color:${c.statusColor};padding:4px 10px;border-radius:20px;font-weight:600;">${c.status}</div>
      </div>`;
  });
  container.innerHTML += '</div>';
}

// ═══════════════════════════════════════════
// ANTRENÖRLER
// ═══════════════════════════════════════════
function renderTrainerCards() {
  const container = document.getElementById('trainerCards');
  const trainers = [
    { name:'Kemal Antrenör', uzmanlik:'Fonksiyonel Fitness, Crossfit', deneyim:8, sertifika:'ACE CPT, CSCS',         dersCount:2, durum:'aktif' },
    { name:'Deniz Koç',      uzmanlik:'Yoga, Pilates, Aqua Aerobik',  deneyim:5, sertifika:'RYT-200, STOTT Pilates', dersCount:3, durum:'aktif' },
  ];
  container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
  trainers.forEach((t, idx) => {
    const color = avatarColors[idx % avatarColors.length];
    container.innerHTML += `
      <div class="plan-card">
        <div class="plan-left">
          <div class="m-avatar" style="background:${color};width:38px;height:38px;border-radius:10px;display:grid;place-items:center;font-size:13px;font-weight:700;flex-shrink:0;">${getInitials(t.name)}</div>
          <div>
            <div class="plan-name">${t.name}</div>
            <div class="plan-members">${t.uzmanlik}</div>
            <div class="plan-members">${t.deneyim} yıl · ${t.dersCount} ders · ${t.sertifika}</div>
          </div>
        </div>
        <div style="font-size:11px;background:rgba(74,222,128,.1);color:#4ade80;padding:4px 10px;border-radius:20px;font-weight:600;">Aktif</div>
      </div>`;
  });
  container.innerHTML += '</div>';
}

// ═══════════════════════════════════════════
// GİRİŞ / ÇIKIŞ KAYITLARI
// ═══════════════════════════════════════════
function renderAccessLogs() {
  const container = document.getElementById('accessLogs');
  const turuLabel = { normal:'Normal', qr:'QR Kod', kart:'Kart' };
  const turuIcon  = { normal:'fa-door-open', qr:'fa-qrcode', kart:'fa-id-badge' };
  const logs = [
    { name:'Ahmet Yılmaz',  giris:'07:30', cikis:'09:15', turu:'kart',   isInside:false },
    { name:'Fatma Kaya',    giris:'08:00', cikis:null,     turu:'qr',     isInside:true },
    { name:'Can Öztürk',    giris:'06:00', cikis:'08:00', turu:'normal', isInside:false },
    { name:'Selin Arslan',  giris:'09:30', cikis:null,     turu:'kart',   isInside:true },
  ];
  container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
  logs.forEach(l => {
    container.innerHTML += `
      <div class="plan-card">
        <div class="plan-left">
          <div class="plan-icon" style="background:${l.isInside?'rgba(74,222,128,.1)':'rgba(148,163,184,.1)'};color:${l.isInside?'#4ade80':'#94a3b8'};font-size:16px;"><i class="fas ${turuIcon[l.turu]}"></i></div>
          <div>
            <div class="plan-name">${l.name}</div>
            <div class="plan-members">Giriş: ${l.giris} · Çıkış: ${l.cikis||'İçeride'} · ${turuLabel[l.turu]}</div>
          </div>
        </div>
        <div style="font-size:11px;background:${l.isInside?'rgba(74,222,128,.1)':'rgba(148,163,184,.1)'};color:${l.isInside?'#4ade80':'#94a3b8'};padding:4px 10px;border-radius:20px;font-weight:600;">${l.isInside?'İçeride':'Çıktı'}</div>
      </div>`;
  });
  container.innerHTML += '</div>';
}

// ═══════════════════════════════════════════
// EKİPMAN DURUMU (ekipman + ekipman_bakimi)
// ═══════════════════════════════════════════
function renderEquipmentCards() {
  const container = document.getElementById('equipmentCards');
  const durumStyle = {
    calisiyor: { text:'Çalışıyor', color:'#4ade80', bg:'rgba(74,222,128,.1)' },
    bakimda:   { text:'Bakımda',   color:'#fbbf24', bg:'rgba(251,191,36,.1)' },
    arizali:   { text:'Arızalı',   color:'#f87171', bg:'rgba(239,68,68,.1)' }
  };
  const categoryIcons = { 'Kardio':'🏃', 'Güç':'💪', 'Esneklik':'🧘' };
  const equipment = [
    { name:'Koşu Bandı',           kategori:'Kardio',    adet:8,  durum:'calisiyor', sonBakim:'2026-02-20' },
    { name:'Eliptik Bisiklet',     kategori:'Kardio',    adet:4,  durum:'calisiyor', sonBakim:null },
    { name:'Ağırlık Sehpası',      kategori:'Güç',       adet:10, durum:'calisiyor', sonBakim:null },
    { name:'Dumbbell Seti',        kategori:'Güç',       adet:5,  durum:'calisiyor', sonBakim:null },
    { name:'Kürek Çekme Makinesi', kategori:'Kardio',    adet:2,  durum:'bakimda',   sonBakim:'2026-03-10' },
    { name:'Smith Machine',        kategori:'Güç',       adet:2,  durum:'calisiyor', sonBakim:null },
    { name:'Yoga Matı',            kategori:'Esneklik',  adet:20, durum:'calisiyor', sonBakim:null },
  ];
  container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
  equipment.forEach(e => {
    const d = durumStyle[e.durum];
    const icon = categoryIcons[e.kategori] || '🔧';
    container.innerHTML += `
      <div class="plan-card">
        <div class="plan-left">
          <div class="plan-icon" style="background:${d.bg};color:${d.color};font-size:17px;">${icon}</div>
          <div>
            <div class="plan-name">${e.name}</div>
            <div class="plan-members">${e.kategori} · ${e.adet} adet${e.sonBakim ? ' · Son bakım: '+e.sonBakim : ''}</div>
          </div>
        </div>
        <div style="font-size:11px;background:${d.bg};color:${d.color};padding:4px 10px;border-radius:20px;font-weight:600;">${d.text}</div>
      </div>`;
  });
  container.innerHTML += '</div>';

  // Uye Dashboard (Demo)
  const uyeContainer = document.getElementById('uyeEquipmentStatus');
  if (uyeContainer) {
    uyeContainer.innerHTML = '';
    equipment.forEach(e => {
      const d = durumStyle[e.durum];
      const icon = categoryIcons[e.kategori] || '🔧';
      uyeContainer.innerHTML += `
        <div class="glass-card" style="padding:15px; background:rgba(255,255,255,0.03); display:flex; align-items:center; gap:12px; border:1px solid rgba(255,255,255,0.05);">
          <div style="font-size:24px;">${icon}</div>
          <div style="flex:1;">
            <div style="font-size:14px; font-weight:600; color:var(--text-primary);">${e.name}</div>
            <div style="font-size:12px; color:var(--text-muted);">${e.kategori}</div>
          </div>
          <div style="font-size:10px; padding:4px 8px; border-radius:12px; background:${d.bg}; color:${d.color}; font-weight:700;">${d.text}</div>
        </div>`;
    });
  }
}

// ═══════════════════════════════════════════
// MODAL
// ═══════════════════════════════════════════
function openModal() {
  document.getElementById('modalOverlay').classList.add('open');
  document.getElementById('fDate').value = new Date().toISOString().split('T')[0];
}
function closeModal() {
  document.getElementById('modalOverlay').classList.remove('open');
}

function addMember() {
  const name = `${document.getElementById('fName').value.trim()} ${document.getElementById('fSurname').value.trim()}`.trim();
  const email = document.getElementById('fEmail').value.trim();
  const plan = document.getElementById('fPlan').value;
  const start = document.getElementById('fDate').value;
  if (!name || name === '' || !email || !start) { showToast('Lütfen zorunlu alanları doldurun!'); return; }

  const planMap = { '4':'platinum', '3':'gold', '2':'silver', '1':'basic' };
  const planKey = planMap[plan] || 'basic';
  const p = planColors[planKey];

  const endDate = new Date(start);
  const sureMap = { '4':12, '3':6, '2':3, '1':1 };
  endDate.setMonth(endDate.getMonth() + (sureMap[plan] || 1));

  const newId = Date.now();
  const uyelikNo = `FZ-2026-${String(members.length + 1).padStart(3, '0')}`;

  members.unshift({
    id: newId, name, email, uyelikNo,
    plan: planKey, start,
    end: endDate.toISOString().split('T')[0],
    payment: p.price, status: 'aktif',
    odemeYontemi: document.getElementById('fPayMethod').value
  });

  filterByStatus(document.getElementById('statusFilter').value);
  document.getElementById('totalMembers').textContent = members.length;
  document.getElementById('sidebarMemberCount').textContent = members.length;
  closeModal();
  showToast(`${name} başarıyla eklendi! ✓`);
  ['fName','fSurname','fEmail','fPhone','fEmergency','fHealthNote'].forEach(id => document.getElementById(id).value = '');
}

// ═══════════════════════════════════════════
// TOAST
// ═══════════════════════════════════════════
function showToast(msg) {
  const t = document.getElementById('toast');
  document.getElementById('toastMsg').textContent = msg;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 3000);
}

// ═══════════════════════════════════════════
// GRAFİK
// ═══════════════════════════════════════════
const ayAdlari = ['Oca','Şub','Mar','Nis','May','Haz','Tem','Ağu','Eyl','Eki','Kas','Ara'];

const chartData = {
  gelir: {
    labels: ['Oca','Şub','Mar','Nis','May','Haz','Tem'],
    data: [0, 0, 0, 0, 0, 0, 0],
    color1: 'rgba(139,92,246,0.8)', color2: 'rgba(0,212,255,0.2)',
    label: 'Aylık Gelir (₺)'
  },
  uye: {
    labels: ['Oca','Şub','Mar','Nis','May','Haz','Tem'],
    data: [0, 0, 0, 0, 0, 0, 0],
    color1: 'rgba(0,212,255,0.8)', color2: 'rgba(139,92,246,0.2)',
    label: 'Toplam Üye'
  },
  devamsiz: {
    labels: ['Oca','Şub','Mar','Nis','May','Haz','Tem'],
    data: [0, 0, 0, 0, 0, 0, 0],
    color1: 'rgba(244,114,182,0.8)', color2: 'rgba(251,146,60,0.2)',
    label: 'Devamsız Üye'
  }
};

let chart;
let gelirDataLoaded = false;
let uyeDataLoaded = false;
let devamsizDataLoaded = false;

function loadGelirChartData() {
  if (gelirDataLoaded) return Promise.resolve();
  return apiFetch('/api/aylik-gelir')
    .then(data => {
      if (data && data.aylar && data.aylar.length > 0) {
        chartData.gelir.labels = data.aylar.map(a => ayAdlari[a.ay - 1] + ' ' + a.yil);
        chartData.gelir.data = data.aylar.map(a => a.toplam);
        gelirDataLoaded = true;
      }
    })
    .catch(e => { if(e.message!=='AUTH_ERROR') console.log('Aylık gelir API yok'); });
}

function loadUyeChartData() {
  if (uyeDataLoaded) return Promise.resolve();
  return apiFetch('/api/aylik-uye')
    .then(data => {
      if (data && data.aylar && data.aylar.length > 0) {
        chartData.uye.labels = data.aylar.map(a => ayAdlari[a.ay - 1] + ' ' + a.yil);
        chartData.uye.data = data.aylar.map(a => a.toplam);
        uyeDataLoaded = true;
      }
    })
    .catch(e => { if(e.message!=='AUTH_ERROR') console.log('Aylık üye API yok'); });
}

function loadDevamsizChartData() {
  if (devamsizDataLoaded) return Promise.resolve();
  return apiFetch('/api/aylik-devamsiz')
    .then(data => {
      if (data && data.aylar && data.aylar.length > 0) {
        chartData.devamsiz.labels = data.aylar.map(a => ayAdlari[a.ay - 1] + ' ' + a.yil);
        chartData.devamsiz.data = data.aylar.map(a => a.toplam);
        devamsizDataLoaded = true;
      }
    })
    .catch(e => { if(e.message!=='AUTH_ERROR') console.log('Aylık devamsız API yok'); });
}

function getChartColors() {
  const isLight = document.documentElement.getAttribute('data-theme') === 'light';
  return {
    grid: isLight ? 'rgba(0,0,0,0.06)' : 'rgba(255,255,255,0.04)',
    tick: isLight ? 'rgba(30,40,80,0.5)' : 'rgba(200,210,255,0.5)',
    tooltipBg: isLight ? 'rgba(255,255,255,0.95)' : 'rgba(15,20,40,0.95)',
    tooltipBorder: isLight ? 'rgba(0,0,0,0.1)' : 'rgba(255,255,255,0.1)',
    tooltipTitle: isLight ? '#1a1d2e' : '#f0f4ff',
    tooltipBody: isLight ? 'rgba(30,40,80,0.7)' : '#94a3b8',
    legendColor: isLight ? 'rgba(30,40,80,0.6)' : 'rgba(200,210,255,0.7)',
  };
}

function initChart(type = 'gelir') {
  const canvas = document.getElementById('mainChart');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  const renderChart = () => {
    const d = chartData[type];
    const cc = getChartColors();
    const grad = ctx.createLinearGradient(0, 0, 0, 220);
    grad.addColorStop(0, d.color1.replace('0.8','0.3'));
    grad.addColorStop(1, 'rgba(0,0,0,0)');
    if (chart) chart.destroy();
    chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: d.labels,
        datasets: [{
          label: d.label, data: d.data,
          borderColor: d.color1, backgroundColor: grad,
          borderWidth: 2.5, pointBackgroundColor: d.color1,
          pointBorderColor: '#fff', pointBorderWidth: 2,
          pointRadius: 5, pointHoverRadius: 7,
          fill: true, tension: 0.45,
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: cc.tooltipBg,
            borderColor: cc.tooltipBorder, borderWidth: 1,
            titleColor: cc.tooltipTitle, bodyColor: cc.tooltipBody,
            padding: 12, cornerRadius: 10
          }
        },
        scales: {
          x: { grid: { color: cc.grid }, ticks: { color: cc.tick, font: { size: 11 } }, border: { display: false } },
          y: { grid: { color: cc.grid }, ticks: { color: cc.tick, font: { size: 11 } }, border: { display: false } }
        }
      }
    });
  };

  // İlgili sekme seçiliyse önce API'den veri yükle
  if (type === 'gelir' && !gelirDataLoaded) {
    loadGelirChartData().then(renderChart);
  } else if (type === 'uye' && !uyeDataLoaded) {
    loadUyeChartData().then(renderChart);
  } else if (type === 'devamsiz' && !devamsizDataLoaded) {
    loadDevamsizChartData().then(renderChart);
  } else {
    renderChart();
  }
}

function switchChart(type, el) {
  document.querySelectorAll('.chart-tab').forEach(t => t.classList.remove('active'));
  el.classList.add('active');
  initChart(type);
}

// ═══════════════════════════════════════════
// TEMA DEĞİŞTİRME (Light / Dark Mode)
// ═══════════════════════════════════════════
function toggleTheme() {
  const html = document.documentElement;
  const current = html.getAttribute('data-theme');
  const newTheme = current === 'light' ? 'dark' : 'light';
  html.setAttribute('data-theme', newTheme);
  localStorage.setItem('fitzone-theme', newTheme);
  updateThemeIcons(newTheme);
  // Grafikleri yeniden çiz (renk uyumu için)
  if (typeof chart !== 'undefined' && chart) {
    const activeTab = document.querySelector('.chart-tab.active');
    if (activeTab) {
      const type = activeTab.textContent.trim().toLowerCase();
      const typeMap = { 'gelir':'gelir', 'üye':'uye', 'devamsız':'devamsiz' };
      initChart(typeMap[type] || 'gelir');
    }
  }
}

function updateThemeIcons(theme) {
  const icons = ['themeIconLanding', 'themeIconTopbar', 'themeIconSidebar'];
  icons.forEach(id => {
    const el = document.getElementById(id);
    if (el) {
      el.className = theme === 'light' ? 'fas fa-moon' : 'fas fa-sun';
    }
  });
  const label = document.getElementById('themeLabel');
  if (label) {
    label.textContent = theme === 'light' ? 'Karanlık Mod' : 'Aydınlık Mod';
  }
}

function loadTheme() {
  const saved = localStorage.getItem('fitzone-theme') || 'dark';
  document.documentElement.setAttribute('data-theme', saved);
  updateThemeIcons(saved);
}

// ═══════════════════════════════════════════
// ROL PROFİLLERİ
// ═══════════════════════════════════════════
const roleProfiles = {
  admin:    { name:'Admin Yönetici',  email:'admin@fitzone.com',  rol:'admin' },
  uye:      { name:'Ahmet Yılmaz',    email:'ahmet@mail.com',     rol:'uye' },
  antrenor: { name:'Kemal Antrenör',  email:'kemal@fitzone.com',  rol:'antrenor' },
};

// ═══════════════════════════════════════════
// DASHBOARD İSTATİSTİKLERİ (API'den)
// ═══════════════════════════════════════════
function loadDashboardStats() {
  apiFetch('/api/istatistikler')
    .then(data => {
      const el1 = document.getElementById('totalMembers');
      const el2 = document.getElementById('monthlyIncome');
      const el3 = document.getElementById('activeMembers');
      const el4 = document.getElementById('expiredMembers');
      const sb  = document.getElementById('sidebarMemberCount');
      if (el1) el1.textContent = data.toplamUye;
      if (el2) el2.textContent = '₺' + Number(data.buAyGelir).toLocaleString('tr-TR');
      if (el3) el3.textContent = data.aktifAbonelik;
      if (el4) el4.textContent = data.suresiDolan;
      if (sb)  sb.textContent  = data.toplamUye;
      // Üyeler sayfası stat kartlarını da güncelle
      const u1 = document.getElementById('uyelerToplamUye');
      const u2 = document.getElementById('uyelerAktifUye');
      const u3 = document.getElementById('uyelerSuresiDolan');
      const u4 = document.getElementById('uyelerBuAyYeni');
      if (u1) u1.textContent = data.toplamUye;
      if (u2) u2.textContent = data.aktifUye;
      if (u3) u3.textContent = data.suresiDolan;
      if (u4) u4.textContent = data.buAyYeniKayit;
    })
    .catch(e => { if(e.message!=='AUTH_ERROR') console.log('İstatistik API yok'); });
}

// ═══════════════════════════════════════════
// API'den render fonksiyonları
// ═══════════════════════════════════════════
function loadRecentPaymentsFromAPI() {
  apiFetch('/api/odemeler').then(data => {
    const container = document.getElementById('recentPayments');
    if (!container) return;
    const iconMap = { 'Platinum':{ icon:'fa-crown', color:'#a78bfa', bg:'rgba(139,92,246,.15)' }, 'Gold':{ icon:'fa-star', color:'#fbbf24', bg:'rgba(251,191,36,.15)' }, 'Silver':{ icon:'fa-medal', color:'#94a3b8', bg:'rgba(148,163,184,.15)' }, 'Basic':{ icon:'fa-shield', color:'#67e8f9', bg:'rgba(34,211,238,.1)' } };
    container.innerHTML = '';
    data.slice(0, 5).forEach(p => {
      const pi = iconMap[p.plan] || { icon:'fa-receipt', color:'#94a3b8', bg:'rgba(148,163,184,.15)' };
      const isIade = p.durum === 'iade';
      if (isIade) { pi.icon = 'fa-rotate-left'; pi.color = '#f87171'; pi.bg = 'rgba(239,68,68,.15)'; }
      container.innerHTML += `<div class="payment-item"><div class="pay-icon" style="background:${pi.bg};color:${pi.color}"><i class="fas ${pi.icon}"></i></div><div><div class="pay-name">${p.uye}</div><div class="pay-date">${p.plan} — ${p.yontem} — ${p.tarih}</div></div><div class="pay-amount ${isIade?'neg':'pos'}">${isIade?'-':'+'}₺${Math.round(p.miktar)}</div></div>`;
    });
  }).catch(e => { if(e.message!=='AUTH_ERROR') renderRecentPayments(); });
}

function loadPlanCardsFromAPI() {
  fetch(API_URL + '/api/planlar').then(r => r.json()).then(data => {  // planlar public
    const container = document.getElementById('planCards');
    if (!container) return;
    const iconMap = { 'Platinum':'💎', 'Gold':'⭐', 'Silver':'🥈', 'Basic':'🔰' };
    const colorMap = { 'Platinum':{ color:'#a78bfa', bg:'rgba(139,92,246,.15)' }, 'Gold':{ color:'#fbbf24', bg:'rgba(251,191,36,.15)' }, 'Silver':{ color:'#94a3b8', bg:'rgba(148,163,184,.15)' }, 'Basic':{ color:'#67e8f9', bg:'rgba(34,211,238,.1)' } };
    container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
    data.forEach(p => {
      const c = colorMap[p.ad] || { color:'#94a3b8', bg:'rgba(148,163,184,.15)' };
      const icon = iconMap[p.ad] || '📋';
      container.innerHTML += `<div class="plan-card"><div class="plan-left"><div class="plan-icon" style="background:${c.bg};color:${c.color};font-size:18px;">${icon}</div><div><div class="plan-name">${p.ad}</div><div class="plan-members">${p.aktifUye} aktif üye · ${p.sureAy} ay</div></div></div><div class="plan-price" style="color:${c.color}">₺${Math.round(p.fiyat)}/ay</div></div>`;
    });
    container.innerHTML += '</div>';
  }).catch(() => renderPlanCards());
}

function loadClassCardsFromAPI() {
  apiFetch('/api/dersler').then(data => {
    const container = document.getElementById('classCards');
    if (!container) return;
    const iconMap = { 'Esneklik':'🧘', 'Kardio':'🥊', 'Güç':'🏋️' };
    container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
    data.dersler.forEach(d => {
      const prog = data.program.find(p => p.ders === d.ders);
      const saat = prog ? prog.saat : '';
      const salon = prog ? prog.salon : '';
      const icon = iconMap[d.kategori] || '📋';
      container.innerHTML += `<div class="plan-card"><div class="plan-left"><div class="plan-icon" style="background:rgba(0,212,255,.1);color:var(--accent-cyan);font-size:17px;">${icon}</div><div><div class="plan-name">${d.ders}</div><div class="plan-members">${saat} — ${salon} — ${d.antrenor} — ${d.kontenjan} kişi</div></div></div><div style="font-size:11px;background:rgba(0,212,255,.1);color:var(--accent-cyan);padding:4px 10px;border-radius:20px;font-weight:600;">Aktif</div></div>`;
    });
    container.innerHTML += '</div>';
  }).catch(e => { if(e.message!=='AUTH_ERROR') renderClassCards(); });
}

function loadTrainerCardsFromAPI() {
  apiFetch('/api/antrenorler-detay').then(data => {
    const container = document.getElementById('trainerCards');
    if (!container) return;
    container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
    data.forEach((t, idx) => {
      const color = avatarColors[idx % avatarColors.length];
      container.innerHTML += `<div class="plan-card"><div class="plan-left"><div class="m-avatar" style="background:${color};width:38px;height:38px;border-radius:10px;display:grid;place-items:center;font-size:13px;font-weight:700;flex-shrink:0;">${getInitials(t.isim)}</div><div><div class="plan-name">${t.isim}</div><div class="plan-members">${t.uzmanlik}</div><div class="plan-members">${t.deneyim} yıl · ${t.dersCount} ders · ${t.sertifikalar}</div></div></div><div style="font-size:11px;background:rgba(74,222,128,.1);color:#4ade80;padding:4px 10px;border-radius:20px;font-weight:600;">Aktif</div></div>`;
    });
    container.innerHTML += '</div>';
  }).catch(e => { if(e.message!=='AUTH_ERROR') renderTrainerCards(); });
}

function loadAccessLogsFromAPI() {
  apiFetch('/api/giris-cikis').then(data => {
    const container = document.getElementById('accessLogs');
    if (!container) return;
    const turuLabel = { normal:'Normal', qr:'QR Kod', kart:'Kart' };
    const turuIcon  = { normal:'fa-door-open', qr:'fa-qrcode', kart:'fa-id-badge' };
    container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
    const kayitlar = data.kayitlar || (Array.isArray(data) ? data : []);
    if (kayitlar.length === 0) { 
      container.innerHTML += '<div style="text-align:center;color:var(--text-muted);padding:20px;font-size:13px;">Bugün giriş kaydı yok</div>'; 
    }
    kayitlar.forEach(l => {
      const isInside = l.durum === 'giris';
      container.innerHTML += `<div class="plan-card"><div class="plan-left"><div class="plan-icon" style="background:${isInside?'rgba(74,222,128,.1)':'rgba(148,163,184,.1)'};color:${isInside?'#4ade80':'#94a3b8'};font-size:16px;"><i class="fas ${turuIcon[l.turu]||'fa-door-open'}"></i></div><div><div class="plan-name">${l.uye}</div><div class="plan-members">Giriş: ${l.giris} · Çıkış: ${l.cikis||'İçeride'} · ${turuLabel[l.turu]||l.turu}</div></div></div><div style="font-size:11px;background:${isInside?'rgba(74,222,128,.1)':'rgba(148,163,184,.1)'};color:${isInside?'#4ade80':'#94a3b8'};padding:4px 10px;border-radius:20px;font-weight:600;">${isInside?'İçeride':'Çıktı'}</div></div>`;
    });
    container.innerHTML += '</div>';
  }).catch(e => { if(e.message!=='AUTH_ERROR') renderAccessLogs(); });
}

function loadEquipmentCardsFromAPI() {
  apiFetch('/api/ekipman').then(data => {
    const durumStyle = { calisiyor:{ text:'Calisiyor', color:'#4ade80', bg:'rgba(74,222,128,.1)' }, bakimda:{ text:'Bakimda', color:'#fbbf24', bg:'rgba(251,191,36,.1)' }, arizali:{ text:'Arizali', color:'#f87171', bg:'rgba(239,68,68,.1)' } };
    const categoryIcons = { 'Kardio':'🏃', 'Guc':'💪', 'Esneklik':'🧘' };
    
    // Admin Dashboard
    const container = document.getElementById('equipmentCards');
    if (container) {
      container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
      data.ekipman.forEach(e => {
        const d = durumStyle[e.durum] || durumStyle.calisiyor;
        const icon = categoryIcons[e.kategori] || '🔧';
        const bakim = data.bakim.find(b => b.ekipman === e.ad);
        container.innerHTML += `<div class="plan-card"><div class="plan-left"><div class="plan-icon" style="background:${d.bg};color:${d.color};font-size:17px;">${icon}</div><div><div class="plan-name">${e.ad}</div><div class="plan-members">${e.kategori} · ${e.adet} adet${bakim ? ' · Son bakim: '+bakim.tarih : ''}</div></div></div><div style="font-size:11px;background:${d.bg};color:${d.color};padding:4px 10px;border-radius:20px;font-weight:600;">${d.text}</div></div>`;
      });
      container.innerHTML += '</div>';
    }

    // Uye Dashboard
    const uyeContainer = document.getElementById('uyeEquipmentStatus');
    if (uyeContainer) {
      uyeContainer.innerHTML = '';
      data.ekipman.forEach(e => {
        const d = durumStyle[e.durum] || durumStyle.calisiyor;
        const icon = categoryIcons[e.kategori] || '🔧';
        uyeContainer.innerHTML += `
          <div class="glass-card" style="padding:15px; background:rgba(255,255,255,0.03); display:flex; align-items:center; gap:12px; border:1px solid rgba(255,255,255,0.05);">
            <div style="font-size:24px;">${icon}</div>
            <div style="flex:1;">
              <div style="font-size:14px; font-weight:600; color:var(--text-primary);">${e.ad}</div>
              <div style="font-size:12px; color:var(--text-muted);">${e.kategori}</div>
            </div>
            <div style="font-size:10px; padding:4px 8px; border-radius:12px; background:${d.bg}; color:${d.color}; font-weight:700;">${d.text}</div>
          </div>`;
      });
    }
  }).catch(e => { if(e.message!=='AUTH_ERROR') renderEquipmentCards(); });
}

// ═══════════════════════════════════════════
// INIT — SAYFA YÜKLENINCE
// ═══════════════════════════════════════════

/** loadAppData — panel açıldıktan sonra veri yükler */
function loadAppData() {
  loadDashboardStats();
  loadMembersFromAPI().then(data => {
    if (data.length > 0) { renderMembers(data); }
    else { renderMembers(members); }
  });
  loadRecentPaymentsFromAPI();
  loadPlanCardsFromAPI();
  loadClassCardsFromAPI();
  loadTrainerCardsFromAPI();
  loadAccessLogsFromAPI();
  loadEquipmentCardsFromAPI();
  initChart();
}

/** initApp — DOMContentLoaded'dan çağrılır; oturum kontrolü yapar */
async function initApp() {
  loadTheme();
  
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.has('resetToken')) {
      const token = urlParams.get('resetToken');
      document.getElementById('resetTokenInput').value = token;
      setTimeout(() => showResetModal(), 500);
  }
  
  // Sayfa yenilemede token kontrol et
  await checkSession();
  // checkSession başarılıysa loginAs → loadAppData zaten çalışacak
}

// ═══════════════════════════════════════════
// SPA NAVİGASYON
// ═══════════════════════════════════════════
let currentPage = 'dashboard';
let raporCharts = {};

function navigateTo(page, navEl) {
  console.log("Navigating to:", page);
  if (page === currentPage) return;
  event && event.preventDefault();
  document.querySelectorAll('.page-section').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  const section = document.getElementById('page-' + page);
  if (section) { section.classList.add('active'); }
  if (navEl) navEl.classList.add('active');
  currentPage = page;
  window.scrollTo(0, 0);
  // Lazy render
  const renderers = {
    'uyeler': renderUyelerPage,
    'abonelikler': renderAboneliklerPage,
    'odemeler': renderOdemelerPage,
    'dersler': renderDerslerPage,
    'antrenorler': renderAntrenorlerPage,
    'giris-cikis': renderGirisCikisPage,
    'ekipman': renderEkipmanPage,
    'raporlar': renderRaporlarPage,
    'ayarlar': renderAyarlarPage,
  };
  if (renderers[page]) renderers[page]();
}

// ═══════════════════════════════════════════
// ÜYELER SAYFASI
// ═══════════════════════════════════════════
let uyelerCachedData = [];

function renderUyelerPage(data) {
  // Eğer data verilmemişse API'den çek
  if (!data) {
    loadMembersFromAPI().then(apiData => {
      uyelerCachedData = apiData;
      updateUyelerStatsUI();
      renderUyelerPageTable(apiData);
    });
    return;
  }
  uyelerCachedData = data;
  updateUyelerStatsUI();
  renderUyelerPageTable(data);
}

function updateUyelerStatsUI() {
  const s = window.uyelerPageStats;
  if (!s) return;
  const t = document.getElementById('uyelerToplamUye');
  const a = document.getElementById('uyelerAktifUye');
  const d = document.getElementById('uyelerSuresiDolan');
  const n = document.getElementById('uyelerBuAyYeni');
  if (t) t.textContent = s.toplam;
  if (a) a.textContent = s.aktif;
  if (d) d.textContent = s.suresiDolan;
  if (n) n.textContent = s.buAyYeni;
}

function renderUyelerPageTable(list) {
  const tbody = document.getElementById('uyelerTableBody');
  if (!tbody) return;
  const statusLabel = { aktif:'Aktif', pasif:'Pasif', suresi_doldu:'Süresi Doldu', askida:'Askıda' };
  const abonelikLabel = { aktif:'Aktif', pasif:'Pasif', iptal:'İptal', suresi_doldu:'Süresi Doldu' };
  tbody.innerHTML = '';
  list.forEach((m, idx) => {
    const color = avatarColors[idx % avatarColors.length];
    const name = m.name || (m.ad + ' ' + m.soyad);
    const durum = m.status || m.durum || 'aktif';
    const abDurum = m.abonelikDurum || '';
    const abClass = abDurum === 'aktif' ? 'aktif' : abDurum === 'suresi_doldu' ? 'suresi_doldu' : 'pasif';
    tbody.innerHTML += `<tr>
      <td><div class="member-info"><div class="m-avatar" style="background:${color}">${getInitials(name)}</div><div><div class="m-name">${name}</div><div class="m-email">${m.email}</div></div></div></td>
      <td style="color:var(--text-muted);font-size:12px">${m.uyelikNo || ''}</td>
      <td style="color:var(--text-muted);font-size:12px">${m.telefon || ''}</td>
      <td style="color:var(--text-muted);font-size:12px">${m.cinsiyet || ''}</td>
      <td style="color:var(--text-muted);font-size:12px">${m.abonelikPlan || '-'}</td>
      <td><span class="status-dot ${durum}">${statusLabel[durum]||durum}</span></td>
      <td style="color:var(--text-muted);font-size:12px">${m.kayitTarihi || ''}</td>
      <td><div style="display:flex;gap:6px"><div class="icon-btn" style="width:30px;height:30px;border-radius:8px;font-size:11px;cursor:pointer" title="Düzenle" onclick="apiEditMember(${m.id})"><i class="fas fa-pen"></i></div><div class="icon-btn" style="width:30px;height:30px;border-radius:8px;font-size:11px;cursor:pointer" title="Sil" onclick="apiDeleteMember(${m.id})"><i class="fas fa-trash" style="color:#f87171"></i></div></div></td></tr>`;
  });
}

function filterUyelerPage(q) {
  let f = uyelerCachedData.filter(m => {
    const name = m.name || (m.ad + ' ' + m.soyad);
    return name.toLowerCase().includes(q.toLowerCase()) || m.email.toLowerCase().includes(q.toLowerCase());
  });
  const st = document.getElementById('uyelerStatusFilter')?.value;
  if (st && st !== 'hepsi') f = f.filter(m => (m.durum || m.status) === st);
  renderUyelerPageTable(f);
}

// ═══════════════════════════════════════════
// ABONELİKLER SAYFASI
// ═══════════════════════════════════════════
const aboneliklerData = [
  { uye:'Ahmet Yılmaz',  plan:'Platinum', baslangic:'2026-01-15', bitis:'2027-01-15', otomatik:true,  durum:'aktif' },
  { uye:'Fatma Kaya',    plan:'Gold',     baslangic:'2026-02-01', bitis:'2026-08-01', otomatik:false, durum:'aktif' },
  { uye:'Can Öztürk',    plan:'Silver',   baslangic:'2026-01-01', bitis:'2026-04-01', otomatik:false, durum:'aktif' },
  { uye:'Selin Arslan',  plan:'Platinum', baslangic:'2025-12-01', bitis:'2026-12-01', otomatik:true,  durum:'aktif' },
  { uye:'Emre Demir',    plan:'Basic',    baslangic:'2026-03-01', bitis:'2026-04-01', otomatik:false, durum:'aktif' },
  { uye:'Zeynep Şahin',  plan:'Gold',     baslangic:'2025-11-01', bitis:'2026-02-01', otomatik:false, durum:'suresi_doldu' },
  { uye:'Murat Çelik',   plan:'Silver',   baslangic:'2026-02-15', bitis:'2026-05-15', otomatik:true,  durum:'aktif' },
  { uye:'Ayşe Yıldız',   plan:'Platinum', baslangic:'2026-01-20', bitis:'2027-01-20', otomatik:true,  durum:'aktif' },
];

let planChartInstance = null;
function renderAboneliklerPage() {
  const planContainer = document.getElementById('abonelikPlanCards');
  if (!planContainer) return;

  // Üye için aktif abonelik banner
  if (currentRole === 'uye') {
    apiFetch('/api/abonelikler').then(abonelikler => {
      const aktif = abonelikler.find(a => a.durum === 'aktif');
      const banner = document.getElementById('aktifAbonelikBanner');
      if (banner && aktif) {
        const pKey = (aktif.plan || '').toLowerCase();
        const p = planColors[pKey] || planColors.basic;
        banner.style.display = '';
        banner.innerHTML = `<div style="display:flex;align-items:center;gap:14px;background:${p.bg};border:1px solid ${p.color}44;border-radius:14px;padding:16px 20px;margin-bottom:18px;"><span style="font-size:28px">${p.icon}</span><div style="flex:1"><div style="font-size:15px;font-weight:700;color:${p.color}">${aktif.plan} Paketi — Aktif</div><div style="font-size:12px;color:var(--text-muted);margin-top:3px">Başlangıç: ${aktif.baslangic} &rarr; Bitiş: ${aktif.bitis}</div></div><span style="font-size:11px;background:rgba(74,222,128,.12);color:#4ade80;padding:5px 14px;border-radius:20px;font-weight:700;">✓ Aktif</span></div>`;
      } else if (banner) { banner.style.display = 'none'; }
      _renderAbonelikTablosu(abonelikler);
    }).catch(() => _renderAbonelikTablosu([]));
  }

  // API'den planları çek
  apiFetch('/api/planlar').then(plans => {
    const iconMap = { 'Platinum':'💎', 'Gold':'⭐', 'Silver':'🥈', 'Basic':'🔰' };
    const colorMap = { 'Platinum':{ color:'#a78bfa', bg:'rgba(139,92,246,.15)' }, 'Gold':{ color:'#fbbf24', bg:'rgba(251,191,36,.15)' }, 'Silver':{ color:'#94a3b8', bg:'rgba(148,163,184,.15)' }, 'Basic':{ color:'#67e8f9', bg:'rgba(34,211,238,.1)' } };
    planContainer.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
    plans.forEach(p => {
      const c = colorMap[p.ad] || { color:'#94a3b8', bg:'rgba(148,163,184,.15)' };
      const icon = iconMap[p.ad] || '📋';
      const oz = Array.isArray(p.ozellikler) ? p.ozellikler.join(' · ') : (typeof p.ozellikler === 'string' ? p.ozellikler : '');
      // Üye rolundeyse "Plan Seç" butonu göster
      const satinAlBtn = (currentRole === 'uye' || currentRole === 'kullanici')
        ? `<button onclick="handlePlanSatinAl(${p.id},'${p.ad}',${p.fiyat})" style="margin-left:auto;background:${c.color}22;color:${c.color};border:1px solid ${c.color}44;border-radius:8px;padding:5px 14px;font-size:11px;font-weight:700;cursor:pointer;white-space:nowrap;transition:all .2s;" onmouseover="this.style.background='${c.color}';this.style.color='#fff'" onmouseout="this.style.background='${c.color}22';this.style.color='${c.color}'">✓ Seç</button>`
        : '';
      planContainer.innerHTML += `<div class="plan-card"><div class="plan-left"><div class="plan-icon" style="background:${c.bg};color:${c.color};font-size:18px;">${icon}</div><div><div class="plan-name">${p.ad}</div><div class="plan-members">${p.aktifUye} aktif üye · ${p.sureAy} ay · ${oz}</div></div></div><div style="display:flex;align-items:center;gap:8px;"><div class="plan-price" style="color:${c.color}">₺${Math.round(p.fiyat)}/ay</div>${satinAlBtn}</div></div>`;
    });
    planContainer.innerHTML += '</div>';

    // Plan Dağılımı Grafiği (Doughnut) — Premium Tasarım (Gradyanlı)
    const ctx = document.getElementById('planChart');
    if (ctx) {
      const c = ctx.getContext('2d');
      if (planChartInstance) planChartInstance.destroy();

      // Gradyan Oluşturucu
      const createGrad = (c1, c2) => {
        const g = c.createLinearGradient(0, 0, 0, 300);
        g.addColorStop(0, c1);
        g.addColorStop(1, c2);
        return g;
      };

      const g1 = createGrad('#8b5cf6', '#6d28d9'); // Violet
      const g2 = createGrad('#f59e0b', '#d97706'); // Amber
      const g3 = createGrad('#64748b', '#334155'); // Slate
      const g4 = createGrad('#06b6d4', '#0891b2'); // Cyan
      const premiumColors = [g1, g2, g3, g4];

      planChartInstance = new Chart(c, {
        type:'doughnut',
        data:{ 
          labels:plans.map(p2=>p2.ad), 
          datasets:[{ 
            data:plans.map(p2=>p2.aktifUye), 
            backgroundColor: premiumColors,
            borderWidth: 0,
            hoverOffset: 15,
            borderRadius: 6,
            spacing: 4
          }] 
        },
        options:{ 
          responsive:true, maintainAspectRatio:false, cutout: '78%',
          plugins:{ 
            legend:{ position:'bottom', labels:{ color:'rgba(241,245,249,0.9)', padding:22, font:{size:13, weight:'500'} } },
            tooltip: {
              backgroundColor: 'rgba(15,23,42,0.95)', padding: 12, cornerRadius: 10,
              titleFont: { size:14, weight:'700' }, bodyFont: { size:13 }
            }
          } 
        }
      });
    }

    // Tabloyu yükle
    if (currentRole === 'admin') {
      apiFetch('/api/abonelikler').then(ab => _renderAbonelikTablosu(ab)).catch(() => _renderAbonelikTablosu([]));
    }
  }).catch(() => {});
}

function _renderAbonelikTablosu(abonelikler) {
  const tbody = document.getElementById('aboneliklerTableBody');
  if (!tbody) return;
  tbody.innerHTML = '';

  const tCount = abonelikler.length;
  const aCount = abonelikler.filter(a => a.durum === 'aktif').length;
  const sCount = abonelikler.filter(a => a.durum !== 'aktif' && a.durum !== 'pasif').length;
  const pCount = new Set(abonelikler.map(a => a.plan)).size;
  const el1 = document.getElementById('abonelikTotalCount');
  const el2 = document.getElementById('abonelikAktifCount');
  const el3 = document.getElementById('abonelikSuresiDolmusCount');
  const el4 = document.getElementById('abonelikPlanSayisi');
  if (el1) el1.textContent = tCount;
  if (el2) el2.textContent = aCount;
  if (el3) el3.textContent = sCount;
  if (el4) el4.textContent = pCount;

  if (abonelikler.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--text-muted);padding:30px">Henüz abonelik yok.</td></tr>';
    return;
  }

  // Aktif / bekleyen abonelikler (kişi başı max 1)
  const aktifler = abonelikler.filter(a => a.durum === 'aktif' || a.durum === 'pasif');
  // Eski abonelikler (iptal, suresi_doldu vs.)
  const eskiler = abonelikler.filter(a => a.durum !== 'aktif' && a.durum !== 'pasif');

  const durumText = { aktif:'Aktif', pasif:'Ödeme Bekliyor', iptal:'Eski Abonelik', suresi_doldu:'Eski Abonelik' };

  const buildRow = (a, idx, isEski) => {
    const color = avatarColors[idx % avatarColors.length];
    const pKey = (a.plan || '').toLowerCase();
    const p = planColors[pKey] || planColors.basic;
    const durumClass = isEski ? 'iptal' : (a.durum === 'aktif' ? 'aktif' : 'pasif');
    const rowOpacity = isEski ? 'opacity:0.5;' : '';
    const actionBtn = currentRole === 'admin'
      ? `<div class="icon-btn" style="width:30px;height:30px;border-radius:8px;font-size:11px;cursor:pointer" title="Tarihleri Düzenle" onclick="openEditAbonelikModal(${a.id},'${a.uye.replace(/'/g,"\\'")}','${a.plan}','${a.baslangic}','${a.bitis}')"><i class="fas fa-pen"></i></div>`
      : '';
    return `<tr style="${rowOpacity}"><td><div class="member-info"><div class="m-avatar" style="background:${color}">${getInitials(a.uye)}</div><div class="m-name">${a.uye}</div></div></td><td><span class="plan-badge ${p.class}">${p.icon} ${a.plan}</span></td><td style="color:var(--text-muted);font-size:12px">${a.baslangic}</td><td style="color:var(--text-muted);font-size:12px">${a.bitis}</td><td style="text-align:center"><i class="fas ${a.otomatik?'fa-check-circle':'fa-times-circle'}" style="color:${a.otomatik?'#4ade80':'#f87171'}"></i></td><td><span class="status-dot ${durumClass}">${durumText[a.durum]||a.durum}</span></td><td><div style="display:flex;gap:6px">${actionBtn}</div></td></tr>`;
  };

  // Önce aktif abonelikler
  aktifler.forEach((a, idx) => { tbody.innerHTML += buildRow(a, idx, false); });

  // Eski abonelikler varsa ayraç çiz + soluk satırlar
  if (eskiler.length > 0) {
    tbody.innerHTML += `<tr><td colspan="7" style="padding:12px 16px 6px;"><div style="display:flex;align-items:center;gap:10px;"><div style="flex:1;height:1px;background:rgba(148,163,184,0.18);"></div><span style="font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.1em;color:var(--text-muted);white-space:nowrap;">📁 Eski Abonelikler (${eskiler.length})</span><div style="flex:1;height:1px;background:rgba(148,163,184,0.18);"></div></div></td></tr>`;
    eskiler.forEach((a, idx) => { tbody.innerHTML += buildRow(a, idx, true); });
  }
}

// ═══════════════════════════════════════════
// ÖDEMELER SAYFASI
// ═══════════════════════════════════════════
const odemelerData = [
  { uye:'Ahmet Yılmaz',  plan:'Platinum', miktar:850,  yontem:'Kredi Kartı', tarih:'2026-03-12', durum:'tamamlandi' },
  { uye:'Fatma Kaya',    plan:'Gold',     miktar:550,  yontem:'Nakit',       tarih:'2026-03-11', durum:'tamamlandi' },
  { uye:'Can Öztürk',    plan:'Silver',   miktar:350,  yontem:'Havale',      tarih:'2026-03-10', durum:'tamamlandi' },
  { uye:'Selin Arslan',  plan:'Platinum', miktar:850,  yontem:'Kredi Kartı', tarih:'2026-03-08', durum:'tamamlandi' },
  { uye:'Emre Demir',    plan:'Basic',    miktar:199,  yontem:'Nakit',       tarih:'2026-03-01', durum:'tamamlandi' },
  { uye:'Zeynep Şahin',  plan:'Gold',     miktar:550,  yontem:'Online',      tarih:'2026-02-28', durum:'iade' },
  { uye:'Murat Çelik',   plan:'Silver',   miktar:350,  yontem:'Kredi Kartı', tarih:'2026-02-15', durum:'tamamlandi' },
  { uye:'Ayşe Yıldız',   plan:'Platinum', miktar:850,  yontem:'Kredi Kartı', tarih:'2026-01-20', durum:'tamamlandi' },
];

function renderOdemelerTable(data) {
  const tbody = document.getElementById('odemelerTableBody');
  if (!tbody) return;
  const durumLabel = { tamamlandi:'Tamamlandı', beklemede:'Beklemede', basarisiz:'Başarısız', iade:'İade' };
  const durumColor = { tamamlandi:'#4ade80', beklemede:'#fbbf24', basarisiz:'#f87171', iade:'#fb923c' };
  tbody.innerHTML = '';
  data.forEach((o, idx) => {
    const color = avatarColors[idx % avatarColors.length];
    tbody.innerHTML += `<tr>
      <td><div class="member-info"><div class="m-avatar" style="background:${color}">${getInitials(o.uye)}</div><div class="m-name">${o.uye}</div></div></td>
      <td style="color:var(--text-muted);font-size:12px">${o.plan}</td>
      <td style="font-family:'Clash Display',sans-serif;font-weight:700;color:${o.durum==='iade'?'#f87171':'#4ade80'}">${o.durum==='iade'?'-':''}₺${o.miktar}</td>
      <td style="color:var(--text-muted);font-size:12px">${o.yontem}</td>
      <td style="color:var(--text-muted);font-size:12px">${o.tarih}</td>
      <td><span style="font-size:11px;background:${durumColor[o.durum]}20;color:${durumColor[o.durum]};padding:4px 10px;border-radius:20px;font-weight:600;">${durumLabel[o.durum]}</span></td></tr>`;
  });
}

let odemelerCachedData = [];
function renderOdemelerPage() {
  // Bekleyen abonelikleri üst bölümde göster (sadece üye)
  if (currentRole === 'uye') {
    apiFetch('/api/abonelikler').then(abonelikler => {
      const bekleyenler = abonelikler.filter(a => a.durum === 'pasif');
      renderBekleyenOdemelerSection(bekleyenler);
    }).catch(() => renderBekleyenOdemelerSection([]));
  } else {
    const bekSec = document.getElementById('bekleyenOdemelerSection');
    if (bekSec) bekSec.style.display = 'none';
  }

  apiFetch('/api/odemeler').then(apiData => {
    odemelerCachedData = apiData;
    const savedUser = getSavedUser();
    const veri = currentRole === 'uye' && savedUser
      ? apiData.filter(o => o.uye === (savedUser.ad + ' ' + savedUser.soyad))
      : apiData;
    renderOdemelerTable(veri);
    const toplamGelir = veri.filter(o => o.durum !== 'iade').reduce((s, o) => s + o.miktar, 0);
    const toplamIslem = veri.length;
    const tamamlanan  = veri.filter(o => o.durum === 'tamamlandi').length;
    const iade        = veri.filter(o => o.durum === 'iade').length;
    const elToplam    = document.getElementById('odemeToplam');
    const elIslem     = document.getElementById('odemeTotalIslem');
    const elTam       = document.getElementById('odemeTamamlanan');
    const elIade      = document.getElementById('odemeIade');
    if (elToplam) elToplam.textContent = '₺' + toplamGelir.toLocaleString('tr-TR');
    if (elIslem)  elIslem.textContent  = toplamIslem;
    if (elTam)    elTam.textContent    = tamamlanan;
    if (elIade)   elIade.textContent   = iade;
  }).catch(() => {
    odemelerCachedData = odemelerData;
    const savedUser = getSavedUser();
    const veri = currentRole === 'uye' && savedUser
      ? odemelerData.filter(o => o.uye === (savedUser.ad + ' ' + savedUser.soyad))
      : odemelerData;
    renderOdemelerTable(veri);
  });
}

function filterOdemeler(st) {
  const savedUser = getSavedUser();
  const source = odemelerCachedData.length > 0 ? odemelerCachedData : odemelerData;
  let veri = currentRole === 'uye' && savedUser
    ? source.filter(o => o.uye === (savedUser.ad + ' ' + savedUser.soyad))
    : source;
  renderOdemelerTable(st === 'hepsi' ? veri : veri.filter(o => o.durum === st));
}

// ═══════════════════════════════════════════
// BEKLEYEN ÖDEMELER SECTION (sadece üye)
// ═══════════════════════════════════════════
function renderBekleyenOdemelerSection(bekleyenler) {
  const container = document.getElementById('bekleyenOdemelerSection');
  if (!container) return;
  if (!bekleyenler || bekleyenler.length === 0) {
    container.style.display = 'none';
    return;
  }
  container.style.display = '';
  const tbody = document.getElementById('bekleyenOdemelerBody');
  if (!tbody) return;
  tbody.innerHTML = '';
  const planIcons = { 'Platinum':'💎', 'Gold':'⭐', 'Silver':'🥈', 'Basic':'🔰' };
  const planColors2 = { 'Platinum':'#a78bfa', 'Gold':'#fbbf24', 'Silver':'#94a3b8', 'Basic':'#67e8f9' };
  bekleyenler.forEach(a => {
    const icon  = planIcons[a.plan]  || '📋';
    const color = planColors2[a.plan] || '#94a3b8';
    tbody.innerHTML += `<tr>
      <td><span style="font-size:18px">${icon}</span> <span class="m-name" style="color:${color}">${a.plan}</span></td>
      <td style="color:var(--text-muted);font-size:12px">${a.baslangic} → ${a.bitis}</td>
      <td><span style="font-size:11px;background:rgba(251,191,36,.1);color:#fbbf24;padding:4px 10px;border-radius:20px;font-weight:600;">Beklemede</span></td>
      <td>
        <select id="yontem_${a.id}" style="background:var(--glass);border:1px solid var(--glass-border);border-radius:8px;padding:5px 8px;color:var(--text-primary);font-family:'DM Sans',sans-serif;font-size:11px;outline:none;cursor:pointer;margin-right:6px;">
          <option value="kredi_karti">Kredi Kart\u0131</option>
          <option value="nakit">Nakit</option>
          <option value="havale">Havale</option>
          <option value="online" selected>Online</option>
        </select>
        <button onclick="handleOdemeYap(${a.id})" style="background:linear-gradient(135deg,#6366f1,#8b5cf6);color:#fff;border:none;border-radius:8px;padding:6px 14px;font-size:12px;font-weight:700;cursor:pointer;">\uD83D\uDCB3 Öde</button>
      </td>
    </tr>`;
  });
}

// Plan Seç — abonelik-satin-al endpoint’ine istek at
// Plan Seç — pasif abonelik + bekleyen ödeme oluştur
function handlePlanSatinAl(planId, planAdi, fiyat) {
  showToast('⏳ Plan seçiliyor...');
  apiFetch('/api/abonelik-satin-al', {
    method: 'POST',
    body: JSON.stringify({ plan_id: String(planId) })
  })
  .then(data => {
    if (data.basarili) {
      showToast('✅ ' + planAdi + ' seçildi! Lütfen ödemenizi tamamlayın.');
      setTimeout(() => {
        const odLink = document.querySelector('[data-page="odemeler"]');
        if (odLink) navigateTo('odemeler', odLink);
        else renderOdemelerPage();
      }, 800);
    } else {
      showToast('❌ ' + (data.mesaj || 'Bir hata oluştu!'));
    }
  })
  .catch(e => {
    if (e.message === 'AUTH_ERROR') showToast('⚠️ Giriş yapmanız gerekiyor!');
    else showToast('❌ Bağlantı hatası: ' + e.message);
  });
}

// Öde butonu — odeme-yap endpoint’ine istek at
// Öde butonu — ödeme onay paneli aç
let odemeOnayYontem = 'online';

function handleOdemeYap(abonelikId) {
  const yontemEl = document.getElementById('yontem_' + abonelikId);
  const yontem   = yontemEl ? yontemEl.value : 'online';
  odemeOnayYontem = yontem;

  document.getElementById('odemeOnayAbonelikId').value = abonelikId;

  apiFetch('/api/abonelikler').then(abonelikler => {
    const ab = abonelikler.find(a => a.id === abonelikId || String(a.id) === String(abonelikId));
    if (ab) {
      document.getElementById('odemeOnayPlan').textContent = ab.plan || '—';
      document.getElementById('odemeOnaySure').textContent = ab.baslangic + ' → ' + ab.bitis;
      const planFiyatMap = { 'Platinum': 850, 'Gold': 550, 'Silver': 350, 'Basic': 199 };
      const fiyat = planFiyatMap[ab.plan] || 0;
      document.getElementById('odemeOnayTutar').textContent = '₺' + fiyat.toLocaleString('tr-TR');
      const savedUser = getSavedUser();
      const aciklama = document.getElementById('odemeHavaleAciklama');
      if (aciklama && savedUser) {
        aciklama.textContent = (savedUser.ad || '') + ' ' + (savedUser.soyad || '');
      }
    }
  }).catch(() => {
    document.getElementById('odemeOnayPlan').textContent = '—';
    document.getElementById('odemeOnaySure').textContent = '—';
    document.getElementById('odemeOnayTutar').textContent = '₺0';
  });

  const yontemConfig = {
    'kredi_karti': { icon: 'fa-credit-card', text: 'Kredi Kartı', color: '#a78bfa' },
    'nakit':       { icon: 'fa-money-bill-wave', text: 'Nakit', color: '#4ade80' },
    'havale':      { icon: 'fa-building-columns', text: 'Havale / EFT', color: '#6366f1' },
    'online':      { icon: 'fa-globe', text: 'Online Ödeme', color: '#22d3ee' }
  };
  const cfg = yontemConfig[yontem] || yontemConfig.online;
  document.getElementById('odemeOnayYontemIcon').className = 'fas ' + cfg.icon;
  document.getElementById('odemeOnayYontemIcon').style.color = cfg.color;
  document.getElementById('odemeOnayYontemText').textContent = cfg.text;

  document.querySelectorAll('.odeme-panel').forEach(p => p.style.display = 'none');
  const panel = document.getElementById('odemePanel_' + yontem);
  if (panel) panel.style.display = '';

  ['odemeKartIsim', 'odemeKartNo', 'odemeKartSKT', 'odemeKartCVV'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });

  const savedUser = getSavedUser();
  if (savedUser && document.getElementById('odemeKartIsim')) {
    document.getElementById('odemeKartIsim').value = (savedUser.ad || '') + ' ' + (savedUser.soyad || '');
  }

  const btn = document.getElementById('odemeOnaySubmitBtn');
  if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fas fa-check-circle"></i> Ödemeyi Onayla'; }

  document.getElementById('odemeOnayModal').classList.add('open');
}

function closeOdemeOnayModal() {
  document.getElementById('odemeOnayModal').classList.remove('open');
}

function submitOdemeOnay() {
  const abonelikId = document.getElementById('odemeOnayAbonelikId').value;
  const yontem = odemeOnayYontem;

  if (yontem === 'kredi_karti') {
    const kartNo = (document.getElementById('odemeKartNo').value || '').replace(/\s/g, '');
    const skt = document.getElementById('odemeKartSKT').value || '';
    const cvv = document.getElementById('odemeKartCVV').value || '';
    const isim = document.getElementById('odemeKartIsim').value.trim();
    if (!isim) { showToast('Kart üzerindeki ismi girin!'); return; }
    if (kartNo.length < 16) { showToast('Geçerli bir kart numarası girin!'); return; }
    if (skt.length < 5) { showToast('Son kullanma tarihini girin! (AA/YY)'); return; }
    if (cvv.length < 3) { showToast('CVV kodunu girin!'); return; }
  }

  const btn = document.getElementById('odemeOnaySubmitBtn');
  if (btn) { btn.disabled = true; btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> İşleniyor...'; }

  apiFetch('/api/odeme-yap', {
    method: 'POST',
    body: JSON.stringify({ abonelik_id: String(abonelikId), odeme_yontemi: yontem })
  })
  .then(data => {
    if (data.basarili) {
      if (btn) { btn.innerHTML = '<i class="fas fa-check"></i> Ödeme Başarılı!'; btn.style.background = 'linear-gradient(135deg,#059669,#10b981)'; }
      showToast('✅ ' + data.mesaj);
      setTimeout(() => {
        closeOdemeOnayModal();
        if (btn) { btn.style.background = ''; }
        // Oturumu yenile: backend 'uye' rolünü atamış olabilir → yeni token al
        checkSession().then(() => {
          renderOdemelerPage();
          setTimeout(() => {
            renderAboneliklerPage();
            loadDashboardStats();
            loadMembersFromAPI().then(d => { if(d.length>0) { renderMembers(d); renderUyelerPage(d); } });
          }, 500);
        });
      }, 1200);
    } else {
      if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fas fa-check-circle"></i> Ödemeyi Onayla'; }
      showToast('❌ ' + (data.mesaj || 'Bir hata oluştu!'));
    }
  })
  .catch(e => {
    if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fas fa-check-circle"></i> Ödemeyi Onayla'; }
    if (e.message === 'AUTH_ERROR') showToast('⚠️ Giriş yapmanız gerekiyor!');
    else showToast('❌ Bağlantı hatası: ' + e.message);
  });
}

function formatCardNumber(input) {
  let value = input.value.replace(/\D/g, '');
  value = value.substring(0, 16);
  let formatted = '';
  for (let i = 0; i < value.length; i++) {
    if (i > 0 && i % 4 === 0) formatted += ' ';
    formatted += value[i];
  }
  input.value = formatted;
}

function formatExpiry(input) {
  let value = input.value.replace(/\D/g, '');
  value = value.substring(0, 4);
  if (value.length >= 2) {
    value = value.substring(0, 2) + '/' + value.substring(2);
  }
  input.value = value;
}


// ═══════════════════════════════════════════
// DERSLER SAYFASI
// ═══════════════════════════════════════════
const derslerDemoData = [
  { ders:'Yoga Flow',           antrenor:'Deniz Koç',      kategori:'Esneklik', kontenjan:15, sure:60, durum:'aktif', icon:'🧘' },
  { ders:'Kickboks',            antrenor:'Kemal Antrenör', kategori:'Kardio',   kontenjan:20, sure:60, durum:'aktif', icon:'🥊' },
  { ders:'Aqua Aerobik',        antrenor:'Deniz Koç',      kategori:'Kardio',   kontenjan:15, sure:45, durum:'aktif', icon:'🏊' },
  { ders:'Fonksiyonel Fitness', antrenor:'Kemal Antrenör', kategori:'Güç',      kontenjan:20, sure:60, durum:'aktif', icon:'🏋️' },
  { ders:'Pilates',             antrenor:'Deniz Koç',      kategori:'Esneklik', kontenjan:12, sure:50, durum:'aktif', icon:'🤸' },
];
const programData = [
  { gun:'Pazartesi',  ders:'Yoga Flow',           saat:'08:00–09:00', salon:'Salon A', durum:'aktif' },
  { gun:'Pazartesi',  ders:'Kickboks',            saat:'10:00–11:00', salon:'Salon B', durum:'aktif' },
  { gun:'Salı',       ders:'Aqua Aerobik',        saat:'14:00–14:45', salon:'Havuz',   durum:'aktif' },
  { gun:'Çarşamba',   ders:'Fonksiyonel Fitness', saat:'18:00–19:00', salon:'Salon C', durum:'aktif' },
  { gun:'Perşembe',   ders:'Pilates',             saat:'09:00–09:50', salon:'Salon A', durum:'aktif' },
  { gun:'Cuma',       ders:'Yoga Flow',           saat:'08:00–09:00', salon:'Salon A', durum:'aktif' },
  { gun:'Cumartesi',  ders:'Kickboks',            saat:'10:00–11:00', salon:'Salon B', durum:'aktif' },
];
const rezervasyonData = [
  { uye:'Ahmet Yılmaz',  ders:'Yoga Flow',           tarih:'2026-03-10', saat:'08:00–09:00', durum:'tamamlandi' },
  { uye:'Fatma Kaya',    ders:'Kickboks',            tarih:'2026-03-10', saat:'10:00–11:00', durum:'aktif' },
  { uye:'Can Öztürk',    ders:'Aqua Aerobik',        tarih:'2026-03-11', saat:'14:00–14:45', durum:'aktif' },
  { uye:'Ahmet Yılmaz',  ders:'Fonksiyonel Fitness', tarih:'2026-03-12', saat:'18:00–19:00', durum:'aktif' },
  { uye:'Selin Arslan',  ders:'Pilates',             tarih:'2026-03-13', saat:'09:00–09:50', durum:'aktif' },
  { uye:'Emre Demir',    ders:'Yoga Flow',           tarih:'2026-03-14', saat:'08:00–09:00', durum:'aktif' },
];

let derslerCachedData = [];
let uyeAktifPlan = null;      // Üyenin aktif abonelik planı ('Basic','Silver','Gold','Platinum' veya null)
let uyeAktifRezCount = 0;     // Üyenin aktif rezervasyon sayısı

// Plan bazlı ders seçim limitleri
const PLAN_DERS_LIMIT = {
  'Basic': 0,
  'Silver': 2,
  'Gold': Infinity,
  'Platinum': Infinity
};

function getDersLimitText(plan) {
  if (!plan) return 'Abonelik yok';
  const limit = PLAN_DERS_LIMIT[plan];
  if (limit === 0) return 'Ders seçilemez';
  if (limit === Infinity) return 'Sınırsız';
  return limit + ' ders';
}

function renderDerslerPage() {
  // Admin butonlarını her render'da güncelle
  ['adminAddProgramBtn', 'adminAddClassBtn'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.style.display = currentRole === 'admin' ? 'flex' : 'none';
  });

  // Üye ise önce aktif abonelik planını ve rezervasyon sayısını al
  const isUye = currentRole === 'uye';
  const planPromise = isUye
    ? apiFetch('/api/abonelikler').then(abonelikler => {
        const aktif = abonelikler.find(a => a.durum === 'aktif');
        uyeAktifPlan = aktif ? aktif.plan : null;
      }).catch(() => { uyeAktifPlan = null; })
    : Promise.resolve();

  planPromise.then(() => {
    apiFetch('/api/dersler').then(data => {
      derslerCachedData = data.dersler;
      const iconMap = { 'Esneklik':'🧘', 'Kardio':'🥊', 'Güç':'🏋️', 'Yüzme':'🏊', 'Pilates':'🤸' };
      const dTb = document.getElementById('derslerTableBody');

      // Üyenin aktif rezervasyon sayısını hesapla
      if (isUye && data.rezervasyonlar) {
        const currentProfile = roleProfiles[currentRole];
        uyeAktifRezCount = data.rezervasyonlar.filter(r =>
          r.uye === currentProfile.name && r.durum === 'aktif'
        ).length;
      }

      // Plan limitleri
      const planLimit = isUye ? (PLAN_DERS_LIMIT[uyeAktifPlan] ?? 0) : Infinity;
      const kalanHak = Math.max(0, planLimit - uyeAktifRezCount);

      // Üye için plan bilgi banner'ı
      const infoBanner = document.getElementById('uyeDersLimitBanner');
      if (isUye && infoBanner) {
        const planIcon = { 'Platinum':'💎', 'Gold':'⭐', 'Silver':'🥈', 'Basic':'🔰' };
        const planColor = { 'Platinum':'#a78bfa', 'Gold':'#fbbf24', 'Silver':'#94a3b8', 'Basic':'#67e8f9' };
        const pIcon = planIcon[uyeAktifPlan] || '❌';
        const pColor = planColor[uyeAktifPlan] || '#f87171';
        const limitText = getDersLimitText(uyeAktifPlan);

        if (!uyeAktifPlan) {
          infoBanner.innerHTML = `<div style="display:flex;align-items:center;gap:12px;background:rgba(248,113,113,.08);border:1px solid rgba(248,113,113,.2);border-radius:12px;padding:14px 18px;">
            <span style="font-size:22px;">❌</span>
            <div style="flex:1;">
              <div style="font-size:13px;font-weight:700;color:#f87171;">Aktif Abonelik Bulunamadı</div>
              <div style="font-size:11px;color:var(--text-muted);margin-top:2px;">Ders seçebilmek için bir abonelik planı satın alın.</div>
            </div>
          </div>`;
        } else if (planLimit === 0) {
          infoBanner.innerHTML = `<div style="display:flex;align-items:center;gap:12px;background:rgba(248,113,113,.08);border:1px solid rgba(248,113,113,.2);border-radius:12px;padding:14px 18px;">
            <span style="font-size:22px;">${pIcon}</span>
            <div style="flex:1;">
              <div style="font-size:13px;font-weight:700;color:${pColor};">${uyeAktifPlan} Plan — Ders Seçim Hakkı Yok</div>
              <div style="font-size:11px;color:var(--text-muted);margin-top:2px;">Basic planda ders seçimi bulunmamaktadır. Ders seçmek için planınızı yükseltin.</div>
            </div>
          </div>`;
        } else if (planLimit === Infinity) {
          infoBanner.innerHTML = `<div style="display:flex;align-items:center;gap:12px;background:${pColor}12;border:1px solid ${pColor}33;border-radius:12px;padding:14px 18px;">
            <span style="font-size:22px;">${pIcon}</span>
            <div style="flex:1;">
              <div style="font-size:13px;font-weight:700;color:${pColor};">${uyeAktifPlan} Plan — Sınırsız Ders Seçimi</div>
              <div style="font-size:11px;color:var(--text-muted);margin-top:2px;">Tüm derslere sınırsız rezervasyon yapabilirsiniz. Aktif rezervasyon: <strong style="color:${pColor};">${uyeAktifRezCount}</strong></div>
            </div>
            <div style="font-size:11px;background:${pColor}22;color:${pColor};padding:5px 12px;border-radius:20px;font-weight:700;">∞ Sınırsız</div>
          </div>`;
        } else {
          const kalanColor = kalanHak > 0 ? '#4ade80' : '#f87171';
          infoBanner.innerHTML = `<div style="display:flex;align-items:center;gap:12px;background:${pColor}12;border:1px solid ${pColor}33;border-radius:12px;padding:14px 18px;">
            <span style="font-size:22px;">${pIcon}</span>
            <div style="flex:1;">
              <div style="font-size:13px;font-weight:700;color:${pColor};">${uyeAktifPlan} Plan — ${limitText} Hakkı</div>
              <div style="font-size:11px;color:var(--text-muted);margin-top:2px;">Aktif rezervasyon: <strong>${uyeAktifRezCount}</strong> / ${planLimit} — Kalan hak: <strong style="color:${kalanColor};">${kalanHak}</strong></div>
            </div>
            <div style="font-size:11px;background:${kalanColor}22;color:${kalanColor};padding:5px 12px;border-radius:20px;font-weight:700;">${kalanHak} / ${planLimit}</div>
          </div>`;
        }
        infoBanner.style.display = '';
      } else if (infoBanner) {
        infoBanner.style.display = 'none';
      }

      if (dTb) {
        dTb.innerHTML = '';
        data.dersler.forEach(d => {
          const icon = iconMap[d.kategori] || '📋';
          let actionBtn = '';
          if (isUye) {
              const prog = data.program.find(p => p.ders === d.ders);
              const targetId = prog ? prog.id : d.id;

              if (!uyeAktifPlan || planLimit === 0) {
                // Basic veya abonelik yok — kilitli buton
                actionBtn = `<button disabled style="padding:6px 12px; font-size:11px; border-radius:8px; background:rgba(148,163,184,.1); border:1px solid rgba(148,163,184,.15); color:#94a3b8; cursor:not-allowed; display:flex; align-items:center; gap:5px;" title="${!uyeAktifPlan ? 'Aktif abonelik gerekli' : 'Basic planda ders seçilemez'}"><i class="fas fa-lock" style="font-size:10px;"></i> ${!uyeAktifPlan ? 'Abonelik Gerekli' : 'Kilitli'}</button>`;
              } else if (prog && planLimit !== Infinity && kalanHak <= 0) {
                // Silver ve hak dolmuş
                actionBtn = `<button disabled style="padding:6px 12px; font-size:11px; border-radius:8px; background:rgba(251,191,36,.08); border:1px solid rgba(251,191,36,.2); color:#fbbf24; cursor:not-allowed; display:flex; align-items:center; gap:5px;" title="Ders seçim hakkınız doldu"><i class="fas fa-exclamation-circle" style="font-size:10px;"></i> Hak Doldu</button>`;
              } else if (prog) {
                // Hak var — aktif buton
                actionBtn = `<button class="btn-primary" style="padding:6px 12px; font-size:11px; border-radius:8px;" onclick="bookClass(${d.id}, ${prog.id})"><i class="fas fa-calendar-plus" style="margin-right:5px;"></i> Rezervasyon Yap</button>`;
              } else {
                // Program yok
                actionBtn = `<button disabled style="padding:6px 12px; font-size:11px; border-radius:8px; background:rgba(148,163,184,.05); border:1px solid rgba(148,163,184,.1); color:#94a3b8; cursor:not-allowed;">Seans Yok</button>`;
              }
          } else {
              actionBtn = `<div style="display:flex;gap:6px;justify-content:flex-end;"><div class="icon-btn" style="width:30px;height:30px;border-radius:8px;font-size:11px;cursor:pointer" title="Düzenle" onclick="openEditClassModal(${d.id})"><i class="fas fa-pen"></i></div><div class="icon-btn" style="width:30px;height:30px;border-radius:8px;font-size:11px;cursor:pointer" title="Sil" onclick="openDeleteModal('ders',${d.id},'${d.ders}')"><i class="fas fa-trash" style="color:#f87171"></i></div></div>`;
          }
          
          dTb.innerHTML += `<tr><td><div style="display:flex;align-items:center;gap:10px"><span style="font-size:18px">${icon}</span><span class="m-name">${d.ders}</span></div></td><td style="color:var(--text-muted);font-size:12px">${d.antrenor}</td><td style="color:var(--text-muted);font-size:12px">${d.kategori}</td><td style="color:var(--text-muted);font-size:12px">${d.kontenjan} kişi</td><td style="color:var(--text-muted);font-size:12px">${d.sure} dk</td><td><span class="status-dot ${d.durum}">${d.durum === 'aktif' ? 'Aktif' : d.durum}</span></td><td style="text-align:right;">${actionBtn}</td></tr>`;
        });
      }
    const pTb = document.getElementById('programTableBody');
    if (pTb) {
      pTb.innerHTML = '';
      data.program.forEach(p => {
        let adminBtn = '';
        if (currentRole === 'admin') {
            adminBtn = `<td style="text-align:right;"><div class="icon-btn" style="width:26px;height:26px;border-radius:6px;font-size:10px;cursor:pointer" title="Sil" onclick="deleteProgram(${p.id})"><i class="fas fa-trash" style="color:#f87171"></i></div></td>`;
        }
        pTb.innerHTML += `<tr><td style="font-weight:600;font-size:13px">${p.gun}</td><td style="color:var(--text-muted);font-size:12px">${p.ders}</td><td style="color:var(--text-muted);font-size:12px">${p.saat}</td><td style="color:var(--text-muted);font-size:12px">${p.salon}</td><td><span class="status-dot aktif">Aktif</span></td>${adminBtn}</tr>`;
      });
      // Header update for admin column
      const pThead = pTb.parentElement.querySelector('thead tr');
      if (currentRole === 'admin' && pThead.children.length === 5) {
          pThead.innerHTML += '<th style="text-align:right;">İŞLEM</th>';
      } else if (currentRole !== 'admin' && pThead.children.length === 6) {
          pThead.removeChild(pThead.lastElementChild);
      }
    }
    const rTb = document.getElementById('rezervasyonTableBody');
    const rDurum = { aktif:'Aktif', iptal:'İptal', tamamlandi:'Tamamlandı' };
    const currentProfile = roleProfiles[currentRole];
    const uyeSutunu = document.getElementById('rezervasyonUyeTh');
    if (uyeSutunu) uyeSutunu.style.display = isUye ? 'none' : '';
    let filteredRez = isUye
      ? data.rezervasyonlar.filter(r => r.uye === currentProfile.name)
      : data.rezervasyonlar;
    if (rTb) {
      rTb.innerHTML = '';
      // Header for cancel button
      const rThead = rTb.parentElement.querySelector('thead tr');
      const hasActionHeader = Array.from(rThead.children).some(th => th.textContent === 'İŞLEM');
      if (isUye && !hasActionHeader) {
          rThead.innerHTML += '<th style="text-align:right;">İŞLEM</th>';
      } else if (!isUye && hasActionHeader) {
          // If we switch from member to admin, remove it?
          // Actually, let's keep it simple: just don't add it if not member
      }

      filteredRez.forEach((r, idx) => {
        const color = avatarColors[idx % avatarColors.length];
        const uyeCell = isUye ? '' : `<td><div class="member-info"><div class="m-avatar" style="background:${color};width:30px;height:30px;font-size:11px;border-radius:8px">${getInitials(r.uye)}</div><div class="m-name">${r.uye}</div></div></td>`;
        
        let cancelBtn = '';
        if (isUye && r.durum === 'aktif') {
            cancelBtn = `<td style="text-align:right;"><button class="btn-ghost" style="padding:4px 8px; font-size:10px; color:#f87171;" onclick="cancelReservation(${r.id})"><i class="fas fa-times"></i> İptal</button></td>`;
        } else if (isUye) {
            cancelBtn = '<td></td>';
        }

        rTb.innerHTML += `<tr>${uyeCell}<td style="color:var(--text-muted);font-size:12px">${r.ders}</td><td style="color:var(--text-muted);font-size:12px">${r.tarih}</td><td style="color:var(--text-muted);font-size:12px">${r.saat}</td><td><span class="status-dot ${r.durum}">${rDurum[r.durum] || r.durum}</span></td>${cancelBtn}</tr>`;
      });
    }
  }).catch(() => {
    // Fallback: demo veriler
    const iconMap = { 'Esneklik':'🧘', 'Kardio':'🥊', 'Güç':'🏋️' };
    const dTb = document.getElementById('derslerTableBody');
    if (dTb) { dTb.innerHTML = ''; derslerDemoData.forEach(d => {
      dTb.innerHTML += `<tr><td><div style="display:flex;align-items:center;gap:10px"><span style="font-size:18px">${d.icon}</span><span class="m-name">${d.ders}</span></div></td><td style="color:var(--text-muted);font-size:12px">${d.antrenor}</td><td style="color:var(--text-muted);font-size:12px">${d.kategori}</td><td style="color:var(--text-muted);font-size:12px">${d.kontenjan} kişi</td><td style="color:var(--text-muted);font-size:12px">${d.sure} dk</td><td><span class="status-dot aktif">Aktif</span></td></tr>`;
    });}
    const pTb = document.getElementById('programTableBody');
    if (pTb) { pTb.innerHTML = ''; programData.forEach(p => {
      pTb.innerHTML += `<tr><td style="font-weight:600;font-size:13px">${p.gun}</td><td style="color:var(--text-muted);font-size:12px">${p.ders}</td><td style="color:var(--text-muted);font-size:12px">${p.saat}</td><td style="color:var(--text-muted);font-size:12px">${p.salon}</td><td><span class="status-dot aktif">Aktif</span></td></tr>`;
    });}
    const rTb = document.getElementById('rezervasyonTableBody');
    const rDurum = { aktif:'Aktif', iptal:'İptal', tamamlandi:'Tamamlandı' };
    const isUye = currentRole === 'uye';
    const currentProfile = roleProfiles[currentRole];
    const uyeSutunu = document.getElementById('rezervasyonUyeTh');
    if (uyeSutunu) uyeSutunu.style.display = isUye ? 'none' : '';
    let filteredRezervasyonlar = isUye ? rezervasyonData.filter(r => r.uye === currentProfile.name) : rezervasyonData;
    if (rTb) { rTb.innerHTML = ''; filteredRezervasyonlar.forEach((r, idx) => {
      const color = avatarColors[idx % avatarColors.length];
      const uyeCell = isUye ? '' : `<td><div class="member-info"><div class="m-avatar" style="background:${color};width:30px;height:30px;font-size:11px;border-radius:8px">${getInitials(r.uye)}</div><div class="m-name">${r.uye}</div></div></td>`;
      rTb.innerHTML += `<tr>${uyeCell}<td style="color:var(--text-muted);font-size:12px">${r.ders}</td><td style="color:var(--text-muted);font-size:12px">${r.tarih}</td><td style="color:var(--text-muted);font-size:12px">${r.saat}</td><td><span class="status-dot ${r.durum}">${rDurum[r.durum]}</span></td></tr>`;
    });}
    });
  }); // planPromise.then end
}

// ═══════════════════════════════════════════
// ANTRENÖRLER SAYFASI
// ═══════════════════════════════════════════
let antrenorlerCachedData = [];
function renderAntrenorlerPage() {
  const container = document.getElementById('antrenorProfileCards');
  if (!container) return;
  apiFetch('/api/antrenorler-detay').then(trainers => {
    antrenorlerCachedData = trainers;
    container.innerHTML = '';
    trainers.forEach((t, idx) => {
      const color = avatarColors[idx % avatarColors.length];
      const certList = t.sertifikalar ? t.sertifikalar.split(',').map(c => c.trim()) : [];
      const certs = certList.map(c => `<span class="cert-badge"><i class="fas fa-certificate" style="font-size:9px"></i> ${c}</span>`).join('');
      const durumColor = t.durum === 'aktif' ? { bg:'rgba(74,222,128,.1)', color:'#4ade80', text:'Aktif' } : { bg:'rgba(148,163,184,.1)', color:'#94a3b8', text:t.durum };
      container.innerHTML += `
        <div class="trainer-profile-card">
          <div class="trainer-header">
            <div class="trainer-avatar" style="background:${color}">${getInitials(t.isim)}</div>
            <div><div class="trainer-name">${t.isim}</div><div class="trainer-specialty">${t.uzmanlik || ''}</div></div>
            <div style="margin-left:auto;font-size:11px;background:${durumColor.bg};color:${durumColor.color};padding:4px 10px;border-radius:20px;font-weight:600;">${durumColor.text}</div>
          </div>
          <div class="trainer-stats">
            <div class="trainer-stat"><div class="trainer-stat-value">${t.deneyim}</div><div class="trainer-stat-label">Yıl Deneyim</div></div>
            <div class="trainer-stat"><div class="trainer-stat-value">${t.dersCount}</div><div class="trainer-stat-label">Aktif Ders</div></div>
            <div class="trainer-stat"><div class="trainer-stat-value">${t.studentCount || 0}</div><div class="trainer-stat-label">Öğrenci</div></div>
          </div>
          <div style="margin-bottom:10px;"><div style="font-size:11px;color:var(--text-muted);margin-bottom:6px;font-weight:600;text-transform:uppercase;letter-spacing:.08em;">Sertifikalar</div><div class="trainer-certs">${certs || '—'}</div></div>
          <div class="trainer-bio" style="margin-bottom:10px;">${t.biyografi || ''}</div>
          <div style="display:flex;gap:10px;justify-content:flex-end;border-top:1px solid rgba(255,255,255,0.05);padding-top:10px;">
                        <div class="icon-btn" style="width:32px;height:32px;border-radius:10px;cursor:pointer;display:flex;align-items:center;justify-content:center;background:rgba(255,255,255,0.05);" onclick="openEditTrainerModal(${t.id})" title="Düzenle"><i class="fas fa-pen" style="font-size:12px;"></i></div>
                        <div class="icon-btn" style="width:32px;height:32px;border-radius:10px;cursor:pointer;display:flex;align-items:center;justify-content:center;background:rgba(239,68,68,0.1);" onclick="openDeleteModal('antrenor',${t.id},'${t.isim}')" title="Sil"><i class="fas fa-trash" style="color:#f87171;font-size:12px;"></i></div>
          </div>
        </div>`;
    });
  }).catch(() => {
    // Fallback: hardcoded demo veriler
    const fallback = [
      { isim:'Kemal Antrenör', uzmanlik:'Fonksiyonel Fitness, Crossfit', deneyim:8, sertifikalar:'ACE CPT, CSCS', biyografi:'8 yıllık deneyimli antrenör.', dersCount:2, durum:'aktif' },
      { isim:'Deniz Koç',      uzmanlik:'Yoga, Pilates, Aqua Aerobik',  deneyim:5, sertifikalar:'RYT-200, STOTT Pilates', biyografi:'5 yıldır yoga ve pilates eğitmeni.', dersCount:3, durum:'aktif' },
    ];
    container.innerHTML = '';
    fallback.forEach((t, idx) => {
      const color = avatarColors[idx % avatarColors.length];
      const certList = t.sertifikalar ? t.sertifikalar.split(',').map(c => c.trim()) : [];
      const certs = certList.map(c => `<span class="cert-badge"><i class="fas fa-certificate" style="font-size:9px"></i> ${c}</span>`).join('');
      container.innerHTML += `<div class="trainer-profile-card"><div class="trainer-header"><div class="trainer-avatar" style="background:${color}">${getInitials(t.isim)}</div><div><div class="trainer-name">${t.isim}</div><div class="trainer-specialty">${t.uzmanlik}</div></div><div style="margin-left:auto;font-size:11px;background:rgba(74,222,128,.1);color:#4ade80;padding:4px 10px;border-radius:20px;font-weight:600;">Aktif</div></div><div class="trainer-stats"><div class="trainer-stat"><div class="trainer-stat-value">${t.deneyim}</div><div class="trainer-stat-label">Yıl Deneyim</div></div><div class="trainer-stat"><div class="trainer-stat-value">${t.dersCount}</div><div class="trainer-stat-label">Aktif Ders</div></div></div><div style="margin-bottom:10px;"><div style="font-size:11px;color:var(--text-muted);margin-bottom:6px;font-weight:600;text-transform:uppercase;letter-spacing:.08em;">Sertifikalar</div><div class="trainer-certs">${certs}</div></div><div class="trainer-bio">${t.biyografi}</div></div>`;
    });
  });
}

// ═══════════════════════════════════════════
// GİRİŞ / ÇIKIŞ SAYFASI
// ═══════════════════════════════════════════
const girisCikisData = [
  { uye:'Ahmet Yılmaz',  giris:'07:30', cikis:'09:15', turu:'kart',   durum:'cikis' },
  { uye:'Fatma Kaya',    giris:'08:00', cikis:null,     turu:'qr',     durum:'giris' },
  { uye:'Can Öztürk',    giris:'06:00', cikis:'08:00', turu:'normal', durum:'cikis' },
  { uye:'Selin Arslan',  giris:'09:30', cikis:null,     turu:'kart',   durum:'giris' },
];

function renderGirisCikisTable(data) {
  const tbody = document.getElementById('girisCikisTableBody');
  if (!tbody) return;
  const turuLabel = { normal:'Normal', qr:'QR Kod', kart:'Kart' };
  tbody.innerHTML = '';
  data.forEach((l, idx) => {
    const color = avatarColors[idx % avatarColors.length];
    const isInside = l.durum === 'giris';
    let sure = '—';
    if (l.cikis) { const [gh,gm]=l.giris.split(':').map(Number); const [ch,cm]=l.cikis.split(':').map(Number); sure=`${ch-gh} sa ${cm-gm>=0?cm-gm:60+(cm-gm)} dk`; }
    else { sure = '<span style="color:#4ade80;font-weight:600">İçeride</span>'; }
    tbody.innerHTML += `<tr>
      <td><div class="member-info"><div class="m-avatar" style="background:${color}">${getInitials(l.uye)}</div><div class="m-name">${l.uye}</div></div></td>
      <td style="color:var(--text-muted);font-size:12px">${l.giris}</td>
      <td style="color:var(--text-muted);font-size:12px">${l.cikis||'—'}</td>
      <td style="color:var(--text-muted);font-size:12px"><i class="fas ${l.turu==='qr'?'fa-qrcode':l.turu==='kart'?'fa-id-badge':'fa-door-open'}" style="margin-right:6px;color:var(--accent-cyan)"></i>${turuLabel[l.turu]}</td>
      <td style="font-size:12px">${sure}</td>
      <td><span style="font-size:11px;background:${isInside?'rgba(74,222,128,.1)':'rgba(148,163,184,.1)'};color:${isInside?'#4ade80':'#94a3b8'};padding:4px 10px;border-radius:20px;font-weight:600;">${isInside?'İçeride':'Çıktı'}</span></td></tr>`;
  });
}window.downloadRaporPDF = function() {
  const element = document.getElementById('page-raporlar');
  if (!element || typeof html2pdf === 'undefined') {
    showToast('PDF modülü yüklenemedi!', true);
    return;
  }
  
  // PDF için stil hazırlığı
  const originalStyle = element.getAttribute('style') || '';
  element.style.backgroundColor = '#000000';
  element.style.color = '#ffffff';
  element.style.padding = '30px';
  element.style.width = '100%';
  
  const opt = {
    margin: 0,
    filename: 'fitzone_rapor.pdf',
    image: { type: 'jpeg', quality: 1.0 },
    html2canvas: { 
        scale: 2, 
        useCORS: true, 
        backgroundColor: '#000000'
    },
    jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
  };
  
  showToast('PDF hazırlanıyor, lütfen bekleyin...', false);
  html2pdf().set(opt).from(element).save().then(() => {
    element.setAttribute('style', originalStyle);
    showToast('Rapor PDF olarak indirildi ✓');
  });
};

let girisCikisCachedData = [];
function renderGirisCikisPage() {
  apiFetch('/api/giris-cikis').then(data => {
    if (data.stats) {
      const cards = document.querySelectorAll('#page-giris-cikis .stat-value');
      if (cards.length >= 4) {
        cards[0].textContent = data.stats.bugunGiris;
        cards[1].textContent = data.stats.iceride;
        cards[2].textContent = data.stats.cikisYapan;
        cards[3].textContent = data.stats.turSayisi;
      }
    }
    const kayitlar = data.kayitlar || data;
    girisCikisCachedData = kayitlar.map(l => ({ uye:l.uye, giris:l.giris, cikis:l.cikis, turu:l.turu, durum:l.durum }));
    renderGirisCikisTable(girisCikisCachedData);
  }).catch(() => { 
    girisCikisCachedData = girisCikisData; 
    renderGirisCikisTable(girisCikisData); 
  });
}
function filterGirisCikis(st) {
  const source = girisCikisCachedData.length > 0 ? girisCikisCachedData : girisCikisData;
  renderGirisCikisTable(st === 'hepsi' ? source : source.filter(l => l.durum === st));
}

// ═══════════════════════════════════════════
// EKİPMAN SAYFASI
// ═══════════════════════════════════════════
const ekipmanData = [
  { ad:'Koşu Bandı',           kategori:'Kardio',   adet:8,  satinAlma:'2024-01-15', fiyat:15000, durum:'calisiyor' },
  { ad:'Eliptik Bisiklet',     kategori:'Kardio',   adet:4,  satinAlma:'2024-01-15', fiyat:12000, durum:'calisiyor' },
  { ad:'Ağırlık Sehpası',      kategori:'Güç',      adet:10, satinAlma:'2024-02-01', fiyat:800,   durum:'calisiyor' },
  { ad:'Dumbbell Seti',        kategori:'Güç',      adet:5,  satinAlma:'2024-02-01', fiyat:3500,  durum:'calisiyor' },
  { ad:'Kürek Çekme Makinesi', kategori:'Kardio',   adet:2,  satinAlma:'2024-03-10', fiyat:9000,  durum:'bakimda' },
  { ad:'Smith Machine',        kategori:'Güç',      adet:2,  satinAlma:'2024-03-10', fiyat:18000, durum:'calisiyor' },
  { ad:'Yoga Matı',            kategori:'Esneklik', adet:20, satinAlma:'2024-04-01', fiyat:150,   durum:'calisiyor' },
];
const bakimData = [
  { ekipman:'Kürek Çekme Makinesi', tarih:'2026-03-10', maliyet:1200, yapan:'Teknik Servis A', aciklama:'Motor yağlama ve kemer değişimi.', sonraki:'2026-06-10', durum:'tamamlandi' },
  { ekipman:'Koşu Bandı',           tarih:'2026-02-20', maliyet:500,  yapan:'Teknik Servis B', aciklama:'Yıllık rutin bakım.',              sonraki:'2026-08-20', durum:'tamamlandi' },
];

function renderEkipmanTable(data) {
  const tbody = document.getElementById('ekipmanTableBody');
  if (!tbody) return;
  const durumStyle = { calisiyor:{text:'Çalışıyor',color:'#4ade80'}, bakimda:{text:'Bakımda',color:'#fbbf24'}, arizali:{text:'Arızalı',color:'#f87171'} };
  const catIcon = { 'Kardio':'🏃', 'Güç':'💪', 'Esneklik':'🧘' };
  tbody.innerHTML = '';
  data.forEach(e => {
    const d = durumStyle[e.durum]||durumStyle.calisiyor;
    tbody.innerHTML += `<tr>
      <td><div style="display:flex;align-items:center;gap:10px"><span style="font-size:17px">${catIcon[e.kategori]||'🔧'}</span><span class="m-name">${e.ad}</span></div></td>
      <td style="color:var(--text-muted);font-size:12px">${e.kategori}</td>
      <td style="color:var(--text-muted);font-size:12px">${e.adet}</td>
      <td style="color:var(--text-muted);font-size:12px">${e.satinAlma}</td>
      <td style="font-family:'Clash Display',sans-serif;font-weight:700;color:var(--text-muted)">₺${e.fiyat.toLocaleString('tr-TR')}</td>
      <td><span style="font-size:11px;background:${d.color}20;color:${d.color};padding:4px 10px;border-radius:20px;font-weight:600;">${d.text}</span></td>
      <td style="text-align:right;"><div style="display:flex;gap:6px;justify-content:flex-end;"><div class="icon-btn" style="width:30px;height:30px;border-radius:8px;font-size:11px;cursor:pointer" title="Düzenle" onclick="openEditEquipmentModal(${e.id})"><i class="fas fa-pen"></i></div><div class="icon-btn" style="width:30px;height:30px;border-radius:8px;font-size:11px;cursor:pointer" title="Sil" onclick="openDeleteModal('ekipman',${e.id},'${e.ad}')"><i class="fas fa-trash" style="color:#f87171"></i></div></div></td></tr>`;
  });
}

let ekipmanCachedData = [];
function renderEkipmanPage() {
  apiFetch('/api/ekipman').then(data => {
    ekipmanCachedData = data.ekipman.map(e => ({ id:e.id, ad:e.ad, kategori:e.kategori, adet:e.adet, satinAlma:e.satinAlma, fiyat:e.fiyat, durum:e.durum }));
    renderEkipmanTable(ekipmanCachedData);
    const bTb = document.getElementById('bakimTableBody');
    if (!bTb) return;
    bTb.innerHTML = '';
    data.bakim.forEach(b => {
      bTb.innerHTML += `<tr>
        <td class="m-name">${b.ekipman}</td>
        <td style="color:var(--text-muted);font-size:12px">${b.tarih}</td>
        <td style="font-family:'Clash Display',sans-serif;font-weight:700;color:#fbbf24">₺${Number(b.maliyet).toLocaleString('tr-TR')}</td>
        <td style="color:var(--text-muted);font-size:12px">${b.yapan}</td>
        <td style="color:var(--text-muted);font-size:12px">${b.aciklama}</td>
        <td style="color:var(--text-muted);font-size:12px">${b.sonraki}</td>
        <td><span style="font-size:11px;background:rgba(74,222,128,.1);color:#4ade80;padding:4px 10px;border-radius:20px;font-weight:600;">Tamamlandı</span></td></tr>`;
    });
  }).catch(() => { ekipmanCachedData = ekipmanData; renderEkipmanTable(ekipmanData); });
}

function filterEkipman(st) {
  const source = ekipmanCachedData.length > 0 ? ekipmanCachedData : ekipmanData;
  renderEkipmanTable(st === 'hepsi' ? source : source.filter(e => e.durum === st));
}

// ═══════════════════════════════════════════
// RAPORLAR SAYFASI
// ═══════════════════════════════════════════
function renderRaporlarPage() {
  Object.values(raporCharts).forEach(c => c.destroy());
  raporCharts = {};
  const cc = getChartColors();
  const baseOpt = { responsive:true, maintainAspectRatio:false, plugins:{ legend:{ labels:{ color:cc.legendColor, font:{size:11} } } }, scales:{ x:{ grid:{ color:cc.grid }, ticks:{ color:cc.tick }, border:{ display:false } }, y:{ grid:{ color:cc.grid }, ticks:{ color:cc.tick }, border:{ display:false } } } };

  // 1. İstatistik Kartları
  apiFetch('/api/istatistikler').then(data => {
    const cards = document.querySelectorAll('#page-raporlar .stat-value');
    if (cards.length >= 2) {
      cards[0].textContent = '₺' + Number(data.buAyGelir || 0).toLocaleString();
      cards[1].textContent = data.toplamUye || 0;
    }
  });

  // 2. Gelir Trendi
  apiFetch('/api/aylik-gelir').then(data => {
    const c1 = document.getElementById('raporGelirChart');
    if (!c1) return;
    const labels = data.aylar.map(a => `${a.ay}/${a.yil}`);
    const values = data.aylar.map(a => a.toplam);
    raporCharts.gelir = new Chart(c1.getContext('2d'), {
      type:'bar',
      data:{ labels, datasets:[{ label:'Gelir (₺)', data:values, backgroundColor:'rgba(139,92,246,0.6)', borderRadius:8, barThickness:24 }] },
      options:{...baseOpt, plugins:{legend:{display:false}}}
    });
  });

  // 3. Üye Artışı
  apiFetch('/api/aylik-uye').then(data => {
    const c2 = document.getElementById('raporUyeChart');
    if (!c2) return;
    const labels = data.aylar.map(a => `${a.ay}/${a.yil}`);
    const values = data.aylar.map(a => a.toplam);
    const g2 = c2.getContext('2d').createLinearGradient(0,0,0,220);
    g2.addColorStop(0,'rgba(0,212,255,0.3)');
    g2.addColorStop(1,'rgba(0,0,0,0)');
    raporCharts.uye = new Chart(c2.getContext('2d'), {
      type:'line',
      data:{ labels, datasets:[{ label:'Toplam Üye', data:values, borderColor:'rgba(0,212,255,0.8)', backgroundColor:g2, fill:true, tension:0.4, pointRadius:4, pointBackgroundColor:'rgba(0,212,255,0.8)' }] },
      options:{...baseOpt, plugins:{legend:{display:false}}}
    });
  });
}

/**
 * Raporlar sayfasını PDF olarak indirir.
 * Koyu tema desteği ile grafiklerin okunabilir olmasını sağlar.
 */
async function downloadReportPDF() {
  const { jsPDF } = window.jspdf;
  const element = document.getElementById('page-raporlar');
  if (!element) return;

  showToast('PDF Hazırlanıyor... Lütfen bekleyin.', 'info');

  try {
    // PDF Sayfa ayarları
    const canvas = await html2canvas(element, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#0f172a', // Koyu arka plan
      logging: false,
      onclone: (clonedDoc) => {
        // Klonlanan dokümanda PDF'e özel stil ayarlamaları yapabiliriz
        const el = clonedDoc.getElementById('page-raporlar');
        el.style.padding = '20px';
        el.style.background = '#0f172a';
        // Butonları gizle
        const buttons = el.querySelectorAll('.topbar-right');
        buttons.forEach(b => b.style.display = 'none');
      }
    });

    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF('p', 'mm', 'a4');
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = (canvas.height * pdfWidth) / canvas.width;

    // Koyu arka planı PDF'e de uygula (İmajın altında beyazlık kalmasın)
    pdf.setFillColor(15, 23, 42); // #0f172a
    pdf.rect(0, 0, pdfWidth, pdf.internal.pageSize.getHeight(), 'F');

    pdf.addImage(imgData, 'PNG', 0, 0, pdfWidth, pdfHeight);
    pdf.save(`FitZone_Rapor_${new Date().toLocaleDateString()}.pdf`);

    showToast('PDF Başarıyla indirildi! ✓');
  } catch (err) {
    console.error('PDF Hatası:', err);
    showToast('PDF oluşturulurken bir hata oluştu.', 'error');
  }
}

// ═══════════════════════════════════════════
// AYARLAR SAYFASI
// ═══════════════════════════════════════════
function renderAyarlarPage() {
  const user = getSavedUser() || {};
  const profilAd = document.getElementById('profilAd');
  if (profilAd) {
    profilAd.value = user.ad || '';
    document.getElementById('profilSoyad').value = user.soyad || '';
    document.getElementById('profilEmail').value = user.email || '';
    document.getElementById('profilTelefon').value = user.telefon || '';
  }

  const tbody = document.getElementById('ayarlarKullaniciTable');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:20px;color:var(--text-muted);">Yükleniyor...</td></tr>';
  
  if (user.rol === 'admin') {
      apiFetch('/api/kullanicilar')
        .then(users => {
          const rolColors = { 'admin':'#a78bfa', 'antrenor':'#00d4ff', 'uye':'#fbbf24', 'Süper Admin':'#a78bfa' };
          const rolLabels = { 'admin':'Süper Admin', 'uye':'Üye', 'antrenor':'Antrenör' };
          tbody.innerHTML = '';
          users.forEach((u, idx) => {
            const color = avatarColors[idx % avatarColors.length];
            const rc = rolColors[u.rol] || '#94a3b8';
            const rLabel = rolLabels[u.rol] || u.rol;
            const uName = u.ad + ' ' + u.soyad;
            tbody.innerHTML += `<tr>
              <td><div class="member-info"><div class="m-avatar" style="background:${color}">${getInitials(uName)}</div><div class="m-name">${uName}</div></div></td>
              <td><span style="font-size:11px;background:${rc}20;color:${rc};padding:4px 10px;border-radius:20px;font-weight:600;">${rLabel}</span></td>
              <td style="color:var(--text-muted);font-size:12px">${u.email}</td>
              <td><span class="status-dot ${u.durum === 'aktif' ? 'aktif' : 'pasif'}">${u.durum === 'aktif' ? 'Aktif' : 'Pasif'}</span></td></tr>`;
          });
        })
        .catch(e => {
            if(e.message!=='AUTH_ERROR') tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">Kullanıcılar yüklenemedi.</td></tr>';
        });
  }
}

function submitProfilGuncelle() {
  const ad = document.getElementById('profilAd').value.trim();
  const soyad = document.getElementById('profilSoyad').value.trim();
  const telefon = document.getElementById('profilTelefon').value.trim();

  if (!ad || !soyad) {
    showToast('Ad ve soyad zorunludur!');
    return;
  }

  apiFetch('/api/profil-guncelle', {
    method: 'POST',
    body: JSON.stringify({ ad, soyad, telefon })
  })
  .then(data => {
    showToast(data.mesaj);
    if (data.basarili) {
      const saved = getSavedUser() || {};
      saved.ad = ad;
      saved.soyad = soyad;
      saved.telefon = telefon;
      saved.name = ad + ' ' + soyad;
      localStorage.setItem('fitzone_user', JSON.stringify(saved));
      loginAs(saved.rol, saved);
    }
  })
  .catch(e => {
    if(e.message!=='AUTH_ERROR') showToast('Sunucu bağlantısı hatası!');
  });
}

// ═══════════════════════════════════════════
// AUTH — GİRİŞ / ÇIKIŞ / MODAL
// ═══════════════════════════════════════════
let currentRole = null;

// --- Modal helpers ---
function showLoginModal() {
  document.getElementById('loginModal').classList.add('open');
  // Beni Hatırla: kayıtlı credential varsa doldur
  const saved = JSON.parse(localStorage.getItem('fitzone_remember') || 'null');
  if (saved && saved.email) {
    document.getElementById('loginEmail').value    = saved.email;
    document.getElementById('loginPassword').value = saved.sifre || '';
    document.getElementById('rememberMe').checked  = true;
  }
}
function closeLoginModal()   { document.getElementById('loginModal').classList.remove('open'); }
function showRegisterModal() { document.getElementById('registerModal').classList.add('open'); }
function closeRegisterModal(){ document.getElementById('registerModal').classList.remove('open'); }
function showForgotModal()   { document.getElementById('forgotModal').classList.add('open'); }
function closeForgotModal()  { document.getElementById('forgotModal').classList.remove('open'); }
function showResetModal()    { document.getElementById('resetPasswordModal').classList.add('open'); }
function closeResetModal()   { document.getElementById('resetPasswordModal').classList.remove('open'); }

// Beni Hatırla: email inputuna yazınca kayıtlı şifreyi otomatik getir
function onLoginEmailInput() {
  const email = document.getElementById('loginEmail').value.trim().toLowerCase();
  const saved = JSON.parse(localStorage.getItem('fitzone_remember') || 'null');
  if (saved && saved.email === email) {
    document.getElementById('loginPassword').value = saved.sifre || '';
    document.getElementById('rememberMe').checked  = true;
  } else {
    // Eşleşme yoksa şifre alanını temizle (kullanıcı başka mail yazdıysa)
    document.getElementById('loginPassword').value = '';
    document.getElementById('rememberMe').checked  = false;
  }
}

// --- Login handler — email + şifre ile giriş (API destekli) ---
function handleLogin() {
  const email = document.getElementById('loginEmail').value.trim().toLowerCase();
  const sifre = document.getElementById('loginPassword').value;

  if (!email || !sifre) {
    showToast('E-posta ve şifre alanlarını doldurun!');
    return;
  }

  // API'ye giriş isteği gönder
  const loginBtn = document.querySelector('#loginModal .btn-primary');
  if (loginBtn) { loginBtn.disabled = true; loginBtn.textContent = 'Giriş yapılıyor...'; }

  fetch(API_URL + '/api/giris', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, sifre })
  })
  .then(res => res.json())
  .then(data => {
    if (loginBtn) { loginBtn.disabled = false; loginBtn.innerHTML = '<i class="fas fa-right-to-bracket"></i> Giriş Yap'; }
    if (data.basarili) {
      // Token ve kullanıcı bilgisini kaydet
      if (data.token) {
        setToken(data.token);
        localStorage.setItem('fitzone_user', JSON.stringify(data.kullanici));
      }
      if (data.csrfToken) {
        localStorage.setItem('fitzone_csrf', data.csrfToken);
      }
      // Beni Hatırla: checkbox durumuna göre kaydet veya sil
      const rememberChecked = document.getElementById('rememberMe').checked;
      if (rememberChecked) {
        localStorage.setItem('fitzone_remember', JSON.stringify({ email, sifre }));
      } else {
        localStorage.removeItem('fitzone_remember');
      }
      const k = data.kullanici;
      const user = { id: k.id, ad: k.ad, soyad: k.soyad, email: k.email, rol: k.rol, telefon: k.telefon || '', name: k.ad + ' ' + k.soyad };
      closeLoginModal();
      loginAs(user.rol, user);
    } else {
      // Rate limit özel mesajı
      if (data.kod === 'RATE_LIMITED') {
        showToast('🚫 ' + (data.mesaj || 'Çok fazla deneme! Lütfen bekleyin.'));
      } else {
        showToast(data.mesaj || 'Giriş başarısız!');
      }
    }
  })
  .catch(() => {
    if (loginBtn) { loginBtn.disabled = false; loginBtn.innerHTML = '<i class="fas fa-right-to-bracket"></i> Giriş Yap'; }
    showToast('🔌 Sunucu bağlantısı kurulamadı! Lütfen tekrar deneyin.');
  });
}

function handleRegister() {
  const ad     = document.getElementById('regName').value.trim();
  const soyad  = document.getElementById('regSurname').value.trim();
  const email  = document.getElementById('regEmail').value.trim().toLowerCase();
  const telefon= document.getElementById('regPhone').value.trim();
  const cinsiyet=document.getElementById('regGender').value;
  const dogum  = document.getElementById('regBirth').value;
  const sifre  = document.getElementById('regPassword').value;
  const sifre2 = document.getElementById('regPassword2').value;

  if (!ad || !soyad || !email || !sifre) {
    showToast('Tüm alanları doldurun!');
    return;
  }
  if (sifre !== sifre2) {
    showToast('Şifreler eşleşmiyor!');
    return;
  }
  if (sifre.length < 6) {
    showToast('Şifre en az 6 karakter olmalıdır!');
    return;
  }

  // API üzerinden SQL Server'a kaydet
  const regBtn = document.querySelector('#registerModal .btn-primary');
  if (regBtn) { regBtn.disabled = true; regBtn.textContent = 'Kaydediliyor...'; }

  fetch(API_URL + '/api/kayit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ad, soyad, email, telefon, cinsiyet, dogum_tarihi: dogum, sifre, rol: 'kullanici' })
  })
  .then(res => res.json())
  .then(data => {
    if (regBtn) { regBtn.disabled = false; regBtn.innerHTML = '<i class="fas fa-user-plus"></i> Kaydol'; }
    if (data.basarili) {
      closeRegisterModal();
      showToast('✅ Kayıt başarılı! Giriş yapabilirsiniz.');
      setTimeout(() => showLoginModal(), 600);
    } else {
      showToast(data.mesaj || 'Kayıt başarısız!');
    }
  })
  .catch(() => {
    if (regBtn) { regBtn.disabled = false; regBtn.innerHTML = '<i class="fas fa-user-plus"></i> Kaydol'; }
    showToast('🔌 Sunucu bağlantısı kurulamadı!');
  });
}

function handleForgot() {
  const email = document.getElementById('forgotEmail').value.trim().toLowerCase();
  if (!email) { showToast('E-posta girmelisiniz!'); return; }
  
  fetch(API_URL + '/api/sifremi-unuttum', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email })
  })
  .then(res => res.json())
  .then(data => {
    closeForgotModal();
    if(data.token) {
      console.log('%c [TEST] Şifre Sıfırlama Linki: ', 'background: #222; color: #bada55; font-size: 14px;');
      console.log('http://13.53.225.142:8080/?resetToken=' + data.token);
      console.log('%c E-posta sunucunuz yapılandırılmamışsa yukarıdaki linki kullanarak test edebilirsiniz.', 'color: #888;');
    }
    showToast(data.mesaj);
  })
  .catch(() => { showToast('Bağlantı hatası!'); });
}

function handleReset() {
  const token = document.getElementById('resetTokenInput').value;
  const yeni_sifre = document.getElementById('resetPassword').value;
  const sifre2 = document.getElementById('resetPassword2').value;
  
  if (!yeni_sifre || yeni_sifre !== sifre2) { showToast('Şifreler eşleşmiyor veya boş!'); return; }
  if (yeni_sifre.length < 6) { showToast('Şifre en az 6 karakter olmalı!'); return; }
  
  fetch(API_URL + '/api/sifre-sifirla', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token, yeni_sifre })
  })
  .then(res => res.json())
  .then(data => {
    if (data.basarili) {
      closeResetModal();
      showToast('Şifreniz güncellendi, giriş yapabilirsiniz.');
      setTimeout(() => showLoginModal(), 600);
      window.history.replaceState({}, document.title, window.location.pathname);
    } else {
      showToast(data.mesaj);
    }
  })
  .catch(() => { showToast('Bağlantı hatası!'); });
}

// --- Kayıtlı kullanıcılar (localStorage destekli) ---
const defaultUsers = [
  { ad:'Admin',  soyad:'Yönetici',  email:'admin@fitzone.com', sifre:'admin123',  rol:'admin',    name:'Admin Yönetici'  },
  { ad:'Ahmet',  soyad:'Yılmaz',    email:'ahmet@mail.com',    sifre:'ahmet123',  rol:'uye',      name:'Ahmet Yılmaz'    },
  { ad:'Fatma',  soyad:'Kaya',      email:'fatma@mail.com',    sifre:'fatma123',  rol:'uye',      name:'Fatma Kaya'      },
  { ad:'Can',    soyad:'Öztürk',    email:'can@mail.com',      sifre:'can123456', rol:'uye',      name:'Can Öztürk'      },
  { ad:'Selin',  soyad:'Arslan',    email:'selin@mail.com',    sifre:'selin123',  rol:'uye',      name:'Selin Arslan'    },
  { ad:'Emre',   soyad:'Demir',     email:'emre@mail.com',     sifre:'emre12345', rol:'uye',      name:'Emre Demir'      },
  { ad:'Zeynep', soyad:'Şahin',     email:'zeynep@mail.com',   sifre:'zeynep123', rol:'uye',      name:'Zeynep Şahin'    },
  { ad:'Murat',  soyad:'Çelik',     email:'murat@mail.com',    sifre:'murat123',  rol:'uye',      name:'Murat Çelik'     },
  { ad:'Ayşe',   soyad:'Yıldız',    email:'ayse@mail.com',     sifre:'ayse12345', rol:'uye',      name:'Ayşe Yıldız'     },
  { ad:'Kemal',  soyad:'Antrenör',  email:'kemal@fitzone.com', sifre:'kemal123',  rol:'antrenor', name:'Kemal Antrenör'  },
  { ad:'Deniz',  soyad:'Koç',       email:'deniz@fitzone.com', sifre:'deniz123',  rol:'antrenor', name:'Deniz Koç'       },
];
let registeredUsers = JSON.parse(localStorage.getItem('fitzone_users')) || [...defaultUsers];

// Giriş yapan kullanıcı bilgisi
let activeUser = null;

// Rol etiketleri
const rolLabels = { admin: 'Süper Admin', uye: 'Üye', antrenor: 'Antrenör' };

function loginAs(role, user) {
  currentRole = role;
  activeUser = user || null;
  if (user) { roleProfiles[role] = { name: user.name, email: user.email, rol: role }; }

  const displayName = user ? user.name : (role === 'admin' ? 'Admin Yönetici' : 'Kullanıcı');
  const displayRole = rolLabels[role] || role;
  const initials    = displayName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

  // Update sidebar user info
  document.getElementById('sidebarAvatar').textContent   = initials;
  document.getElementById('sidebarUserName').textContent  = displayName;
  document.getElementById('sidebarUserRole').textContent  = displayRole;

  // Filter sidebar items by role
  applySidebarRole(role);

  // Stats grid: sadece admin'de görünür
  const statsGrid = document.querySelector('#page-dashboard .stats-grid');
  if (statsGrid) statsGrid.style.display = role === 'admin' ? '' : 'none';

  const isUye     = role === 'uye';
  const isAntrenor = role === 'antrenor';
  const isAdmin   = role === 'admin';

  // Admin-only büyük bölümler (grafik+sağpanel, üye listesi, seans/ders ekle butonları)
  ['adminContentGrid', 'adminMembersGrid', 'adminAddProgramBtn', 'adminAddClassBtn'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.style.display = isAdmin ? '' : 'none';
  });

  // Uye-only bolum
  const uyeGrid = document.getElementById('uyeDashboardGrid');
  if (uyeGrid) {
      uyeGrid.style.display = isUye ? 'flex' : 'none';
      uyeGrid.style.flexDirection = 'column';
  }

  // Antrenör-only bölüm
  const antrenorGrid = document.getElementById('antrenorDashboardGrid');
  if (antrenorGrid) antrenorGrid.style.display = isAntrenor ? '' : 'none';

  // Bottom Grid 1 kartları
  // Antrenör ve Üye: Abonelik Planları + Hızlı İşlemler gizli
  ['dashPlanlariCard'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.style.display = isAdmin ? '' : 'none';
  });
  // Antrenör ve Üye: Bugünkü Dersler de gizli (kendi takvim bölümleri var)
  const bugunDersler = document.getElementById('dashBugunDerslerCard');
  if (bugunDersler) bugunDersler.style.display = isAdmin ? '' : 'none';
  // Grid1 sütun ayarı
  const bg1 = document.getElementById('dashBottomGrid1');
  if (bg1) bg1.style.gridTemplateColumns = isAdmin ? '' : '1fr';

  // Bottom Grid 2 kartları
  const girisCikis   = document.getElementById('dashGirisCikisCard');
  const antrenorlerC = document.getElementById('dashAntrenorlerCard');
  const ekipmanC     = document.getElementById('dashEkipmanCard');
  if (girisCikis)   girisCikis.style.display   = isAdmin ? '' : 'none';
  if (antrenorlerC) antrenorlerC.style.display  = isAdmin ? '' : 'none';
  // Ekipman: admin'de görünür, üye ve antrenörde gizli (üye için yeni panel var)
  if (ekipmanC) ekipmanC.style.display = isAdmin ? '' : 'none';
  // Grid2 sütun: üyede 2 kart (antrenörler yok, ekipman var → 1 sütun ekipman ile olmaz doğrusu)
  const bg2 = document.getElementById('dashBottomGrid2');
  if (bg2) {
    if (isAdmin)   bg2.style.gridTemplateColumns = '';
    if (isUye)     bg2.style.gridTemplateColumns = '1fr 1fr';
    if (isAntrenor) bg2.style.display = 'none';
  }

  // Sadece admin'e özel butonlar
  const abonelikPlanBtn = document.getElementById('abonelikYeniPlanBtn');
  const odemeBtn        = document.getElementById('odemeAlBtn');
  if (abonelikPlanBtn) abonelikPlanBtn.style.display = isAdmin ? '' : 'none';
  if (odemeBtn)        odemeBtn.style.display        = isAdmin ? '' : 'none';

  // Karşılama mesajını kişiye göre güncelle
  const welcomeEl = document.getElementById('dashboardWelcome');
  if (welcomeEl) welcomeEl.textContent = `Hoş Geldiniz, ${displayName} 👋`;

  // Hide landing, show panel
  document.getElementById('landing-page').style.display = 'none';
  document.getElementById('app-layout').style.display = '';

  // Reset to dashboard
  currentPage = '';
  const dashLink = document.querySelector('[data-page="dashboard"]');
  if (dashLink) navigateTo('dashboard', dashLink);

  setTimeout(() => loadAppData(), 100);
  if (isUye)      { renderUyeWeeklyCalendar(); renderUyeAktiviteChart(); }
  if (isAntrenor) { renderAntrenorWeeklyCalendar(); renderAntrenorKatilimChart(); }
  showToast(`${displayRole} olarak giriş yapıldı! 🎉`);
}


function applySidebarRole(role) {
  // Nav items
  document.querySelectorAll('.nav-item[data-role]').forEach(el => {
    const roles = el.getAttribute('data-role').split(' ');
    el.style.display = roles.includes(role) ? '' : 'none';
  });
  // Section labels
  document.querySelectorAll('.nav-section-label[data-role]').forEach(el => {
    const roles = el.getAttribute('data-role').split(' ');
    el.style.display = roles.includes(role) ? '' : 'none';
  });
  // Section labels without data-role: show for admin only
  document.querySelectorAll('.nav-section-label:not([data-role])').forEach(el => {
    el.style.display = role === 'admin' ? '' : 'none';
  });
  // Role-specific display for cards
  document.querySelectorAll('.glass-card[data-role]').forEach(el => {
    const roles = el.getAttribute('data-role').split(' ');
    el.style.display = roles.includes(role) ? '' : 'none';
  });
}

function logout() {
  clearToken();  // JWT token'ı sil
  currentRole = null;
  activeUser  = null;
  document.getElementById('landing-page').style.display = '';
  document.getElementById('app-layout').style.display = 'none';
  window.scrollTo(0, 0);
}

// ═══════════════════════════════════════════
// ÜYE DASHBOARD — HAFTALIK DERS TAKVİMİ
// ═══════════════════════════════════════════
function renderUyeWeeklyCalendar() {
  const container = document.getElementById('uyeWeeklyCalendar');
  if (!container) return;

  const gunler = ['Pazartesi', 'Salı', 'Çarşamba', 'Perşembe', 'Cuma', 'Cumartesi'];
  const dersRenkleri = {
    'Yoga Flow':           { bg:'rgba(139,92,246,.15)',  color:'#a78bfa', icon:'🧘' },
    'Kickboks':            { bg:'rgba(244,114,182,.15)', color:'#f472b6', icon:'🥊' },
    'Aqua Aerobik':        { bg:'rgba(0,212,255,.12)',   color:'#00d4ff', icon:'🏊' },
    'Fonksiyonel Fitness': { bg:'rgba(251,146,60,.15)',  color:'#fb923c', icon:'🏋️' },
    'Pilates':             { bg:'rgba(74,222,128,.12)',  color:'#4ade80', icon:'🤸' },
  };

  // API'den kullanıcının rezervasyonlarını ve programı al
  apiFetch('/api/dersler').then(data => {
    const currentProfile = roleProfiles[currentRole];
    const benimRezervasyonlar = (data.rezervasyonlar || [])
      .filter(r => r.uye === currentProfile?.name && r.durum === 'aktif');

    // Rezervasyon yapılan ders isimlerini bul
    const rezerveDersler = benimRezervasyonlar.map(r => r.ders);

    // Program verisinden sadece kullanıcının rezerve ettiği dersleri filtrele
    const programMap = {};
    gunler.forEach(g => programMap[g] = []);
    (data.program || []).forEach(p => {
      if (programMap[p.gun] && rezerveDersler.includes(p.ders)) {
        programMap[p.gun].push(p);
      }
    });

    const toplamDers = rezerveDersler.length;

    let html = '';
    if (toplamDers === 0) {
      html = `<div style="text-align:center;padding:30px 10px;">
        <div style="font-size:36px;margin-bottom:12px;">📋</div>
        <div style="font-size:14px;font-weight:700;color:var(--text-primary);margin-bottom:6px;">Henüz Ders Rezervasyonu Yok</div>
        <div style="font-size:12px;color:var(--text-muted);line-height:1.6;">Dersler sayfasından ders seçerek haftalık programınızı oluşturabilirsiniz.</div>
      </div>`;
    } else {
      html += `<div style="display:flex;align-items:center;gap:8px;margin-bottom:12px;padding:0 4px;">
        <div style="font-size:11px;background:rgba(139,92,246,.1);color:#a78bfa;padding:4px 10px;border-radius:16px;font-weight:700;">${toplamDers} Aktif Ders</div>
      </div>`;
      html += `<div style="display:grid;grid-template-columns:repeat(6,1fr);gap:10px;min-width:600px;padding:4px 2px 8px;">`;
      gunler.forEach(gun => {
        const dersler = programMap[gun] || [];
        html += `<div style="display:flex;flex-direction:column;gap:8px;">
          <div style="font-size:11px;font-weight:700;color:var(--text-muted);text-transform:uppercase;letter-spacing:.06em;text-align:center;padding:4px 0;border-bottom:1px solid var(--glass-border);">${gun.slice(0,3)}</div>`;
        if (dersler.length === 0) {
          html += `<div style="text-align:center;color:var(--text-muted);font-size:11px;padding:12px 0;">—</div>`;
        } else {
          dersler.forEach(d => {
            const r = dersRenkleri[d.ders] || { bg:'rgba(100,116,139,.12)', color:'#94a3b8', icon:'📋' };
            html += `<div style="background:${r.bg};border:1px solid ${r.color}30;border-radius:10px;padding:8px 7px;cursor:pointer;transition:transform .15s,box-shadow .15s;" 
                 onmouseover="this.style.transform='translateY(-2px)';this.style.boxShadow='0 6px 20px ${r.color}25'"
                 onmouseout="this.style.transform='';this.style.boxShadow=''">
              <div style="font-size:16px;text-align:center;margin-bottom:4px;">${r.icon}</div>
              <div style="font-size:10px;font-weight:700;color:${r.color};text-align:center;line-height:1.3;">${d.ders}</div>
              <div style="font-size:9px;color:var(--text-muted);text-align:center;margin-top:3px;">${d.saat}</div>
              <div style="font-size:9px;color:var(--text-muted);text-align:center;">${d.salon}</div>
            </div>`;
          });
        }
        html += `</div>`;
      });
      html += `</div>`;
    }
    container.innerHTML = html;
  }).catch(() => {
    // Fallback: boş takvim göster
    container.innerHTML = `<div style="text-align:center;padding:30px 10px;">
      <div style="font-size:36px;margin-bottom:12px;">⚠️</div>
      <div style="font-size:13px;color:var(--text-muted);">Ders programı yüklenemedi.</div>
    </div>`;
  });
}

// ═══════════════════════════════════════════
// ÜYE DASHBOARD — AKTİVİTE GRAFİĞİ
// ═══════════════════════════════════════════
let uyeAktiviteChartInstance = null;
function renderUyeAktiviteChart() {
  const ctx = document.getElementById('uyeAktiviteChart');
  if (!ctx) return;
  if (uyeAktiviteChartInstance) uyeAktiviteChartInstance.destroy();

  const gunler = ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'];
  const gunAdlari = ['Pazartesi', 'Salı', 'Çarşamba', 'Perşembe', 'Cuma', 'Cumartesi', 'Pazar'];

  // Bu haftanın tarihlerini hesapla (Pazartesi'den Pazar'a)
  const bugun = new Date();
  const pazartesi = new Date(bugun);
  const gunFark = bugun.getDay() === 0 ? 6 : bugun.getDay() - 1;
  pazartesi.setDate(bugun.getDate() - gunFark);
  pazartesi.setHours(0, 0, 0, 0);

  const haftaTarihleri = [];
  for (let i = 0; i < 7; i++) {
    const d = new Date(pazartesi);
    d.setDate(pazartesi.getDate() + i);
    haftaTarihleri.push(d.toISOString().split('T')[0]); // YYYY-MM-DD
  }

  // Kullanıcının giriş-çıkış loglarından aktivite hesapla
  // Giriş-çıkış API'si admin/antrenör kısıtlıysa, dersler API'sinden rezervasyonlara bak
  const currentProfile = roleProfiles[currentRole];
  const userName = currentProfile?.name;

  // Önce giriş-çıkış verisini dene
  apiFetch('/api/giris-cikis').then(data => {
    const kayitlar = data.kayitlar || data || [];
    const aktivite = new Array(7).fill(0);

    // Kullanıcının kendi kayıtlarını filtrele ve gün bazlı dakika hesapla
    kayitlar.forEach(k => {
      if (k.uye && k.uye.includes(userName?.split(' ')[0] || '___')) {
        // Giriş ve çıkış saatinden dakika hesapla
        const girisD = parseTimeToMinutes(k.giris);
        const cikisD = k.cikis ? parseTimeToMinutes(k.cikis) : girisD + 60; // Çıkış yoksa 60 dk varsay
        const sure = Math.max(0, cikisD - girisD);

        // Bugünün hangi güne denk geldiğini bul
        const bugunIdx = bugun.getDay() === 0 ? 6 : bugun.getDay() - 1;
        if (k.durum === 'giris' || k.durum === 'cikis') {
          aktivite[bugunIdx] += sure;
        }
      }
    });

    buildAktiviteChart(ctx, gunler, aktivite);
  }).catch(() => {
    // Fallback: Derslerden rezervasyon bazlı aktivite hesapla
    apiFetch('/api/dersler').then(data => {
      const aktivite = new Array(7).fill(0);
      const benimRez = (data.rezervasyonlar || [])
        .filter(r => r.uye === userName && r.durum === 'aktif');

      // Rezervasyonları gün bazlı eşle
      benimRez.forEach(r => {
        // Rezervasyon tarihinden gün bul
        if (r.tarih) {
          const dayIdx = haftaTarihleri.indexOf(r.tarih);
          if (dayIdx >= 0) {
            aktivite[dayIdx] += 60; // Her ders yaklaşık 60 dk
          }
        }
      });

      // Ayrıca program günlerinden de eşle
      const rezerveDersler = benimRez.map(r => r.ders);
      (data.program || []).forEach(p => {
        if (rezerveDersler.includes(p.ders)) {
          const gunIdx = gunAdlari.indexOf(p.gun);
          if (gunIdx >= 0 && aktivite[gunIdx] === 0) {
            // Saat bilgisinden süre hesapla (ör: 08:00-09:00)
            const saatParts = (p.saat || '').split('–');
            if (saatParts.length === 2) {
              const bas = parseTimeToMinutes(saatParts[0].trim());
              const bit = parseTimeToMinutes(saatParts[1].trim());
              aktivite[gunIdx] += Math.max(0, bit - bas);
            } else {
              aktivite[gunIdx] += 60;
            }
          }
        }
      });

      buildAktiviteChart(ctx, gunler, aktivite);
    }).catch(() => {
      // Son fallback: boş grafik
      buildAktiviteChart(ctx, gunler, [0, 0, 0, 0, 0, 0, 0]);
    });
  });
}

// Saat string'ini dakikaya çevir (ör: "08:30" → 510)
function parseTimeToMinutes(timeStr) {
  if (!timeStr) return 0;
  const parts = timeStr.split(':');
  return (parseInt(parts[0]) || 0) * 60 + (parseInt(parts[1]) || 0);
}

// Aktivite grafiğini oluştur
function buildAktiviteChart(ctx, gunler, aktivite) {
  if (uyeAktiviteChartInstance) uyeAktiviteChartInstance.destroy();

  const maxVal = Math.max(...aktivite, 90);
  const grad = ctx.getContext('2d').createLinearGradient(0, 0, 0, 200);
  grad.addColorStop(0, 'rgba(139,92,246,0.85)');
  grad.addColorStop(1, 'rgba(0,212,255,0.6)');

  uyeAktiviteChartInstance = new Chart(ctx.getContext('2d'), {
    type: 'bar',
    data: {
      labels: gunler,
      datasets: [{
        label: 'Aktivite (dk)',
        data: aktivite,
        backgroundColor: aktivite.map(v => v > 0 ? grad : 'rgba(148,163,184,0.1)'),
        borderRadius: 10,
        borderSkipped: false,
        barThickness: 28,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(15,20,40,0.95)',
          borderColor: 'rgba(255,255,255,0.1)', borderWidth: 1,
          titleColor: '#f0f4ff', bodyColor: '#94a3b8',
          padding: 10, cornerRadius: 8,
          callbacks: { label: ctx => `${ctx.parsed.y} dakika` }
        }
      },
      scales: {
        x: { grid: { display: false }, ticks: { color: 'rgba(200,210,255,0.55)', font: { size: 11 } }, border: { display: false } },
        y: { grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: 'rgba(200,210,255,0.5)', font: { size: 11 }, callback: v => v + ' dk' }, border: { display: false }, min: 0, max: maxVal + 10 }
      }
    }
  });
}





// ═══════════════════════════════════════════
// ANTRENÖR DASHBOARD — HAFTALIK PROGRAM TAKVİMİ
// ═══════════════════════════════════════════
function renderAntrenorWeeklyCalendar() {
  const container = document.getElementById('antrenorWeeklyCalendar');
  if (!container) return;

  const currentProfile = roleProfiles[currentRole]; 
  const gunler = ['Pazartesi', 'Salı', 'Çarşamba', 'Perşembe', 'Cuma', 'Cumartesi', 'Pazar'];

  const dersRenkleri = {
    'Yoga Flow':           { bg:'rgba(139,92,246,.15)',  color:'#a78bfa', icon:'🧘' },
    'Kickboks':            { bg:'rgba(244,114,182,.15)', color:'#f472b6', icon:'🥊' },
    'Aqua Aerobik':        { bg:'rgba(0,212,255,.12)',   color:'#00d4ff', icon:'🏊' },
    'Fonksiyonel Fitness': { bg:'rgba(251,146,60,.15)',  color:'#fb923c', icon:'🏋️' },
    'Pilates':             { bg:'rgba(74,222,128,.12)',  color:'#4ade80', icon:'🤸' },
  };

  apiFetch('/api/dersler').then(data => {
    // Backend zaten bu antrenörün derslerini filtreliyor (Step 1'de yaptık)
    const programMap = {};
    gunler.forEach(g => programMap[g] = []);
    (data.program || []).forEach(p => {
      if (programMap[p.gun]) programMap[p.gun].push(p);
    });

    let html = `<div style="display:grid;grid-template-columns:repeat(7,1fr);gap:10px;min-width:700px;padding:4px 2px 8px;">`;
    gunler.forEach(gun => {
      const dersler = programMap[gun] || [];
      html += `
        <div style="display:flex;flex-direction:column;gap:8px;">
          <div style="font-size:11px;font-weight:700;color:var(--text-muted);text-transform:uppercase;letter-spacing:.06em;text-align:center;padding:4px 0;border-bottom:1px solid var(--glass-border);">${gun.slice(0,3)}</div>
      `;
      if (dersler.length === 0) {
        html += `<div style="text-align:center;color:var(--text-muted);font-size:20px;padding:16px 0;">—</div>`;
      } else {
        dersler.forEach(d => {
          const r = dersRenkleri[d.ders] || { bg:'rgba(100,116,139,.12)', color:'#94a3b8', icon:'📋' };
          html += `
            <div style="background:${r.bg};border:1px solid ${r.color}35;border-radius:10px;padding:9px 7px;cursor:default;transition:transform .15s,box-shadow .15s;"
                 onmouseover="this.style.transform='translateY(-2px)';this.style.boxShadow='0 8px 24px ${r.color}30'"
                 onmouseout="this.style.transform='';this.style.boxShadow=''">
              <div style="font-size:18px;text-align:center;margin-bottom:4px;">${r.icon}</div>
              <div style="font-size:10px;font-weight:700;color:${r.color};text-align:center;line-height:1.3;">${d.ders}</div>
              <div style="font-size:9px;color:var(--text-muted);text-align:center;margin-top:3px;">${d.saat}</div>
              <div style="font-size:9px;color:var(--text-muted);text-align:center;">${d.salon}</div>
            </div>`;
        });
      }
      html += `</div>`;
    });
    html += `</div>`;
    container.innerHTML = html;

    // Ders listesini de render et
    renderAntrenorDersListesi(data.dersler);
  }).catch(() => {
    container.innerHTML = '<div style="text-align:center;padding:20px;color:var(--text-muted);">Program yüklenemedi.</div>';
  });
}

function renderAntrenorDersListesi(benimDerslerData) {
  const container = document.getElementById('antrenorDersListesi');
  if (!container) return;
  const dersRenkleri = {
    'Yoga Flow':           { color:'#a78bfa', icon:'🧘' },
    'Kickboks':            { color:'#f472b6', icon:'🥊' },
    'Aqua Aerobik':        { color:'#00d4ff', icon:'🏊' },
    'Fonksiyonel Fitness': { color:'#fb923c', icon:'🏋️' },
    'Pilates':             { color:'#4ade80', icon:'🤸' },
  };
  container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;padding:4px 0;">';
  
  if (!benimDerslerData || benimDerslerData.length === 0) {
      container.innerHTML = '<div style="text-align:center;padding:20px;color:var(--text-muted);font-size:12px;">Henüz aktif dersiniz bulunmuyor.</div>';
      return;
  }

  benimDerslerData.forEach(d => {
    const r = dersRenkleri[d.ders] || { color:'#94a3b8', icon:'📋' };
    container.innerHTML += `
      <div class="plan-card">
        <div class="plan-left">
          <div class="plan-icon" style="background:${r.color}18;color:${r.color};font-size:17px;">${r.icon}</div>
          <div>
            <div class="plan-name">${d.ders}</div>
            <div class="plan-members">${d.sure} dk · ${d.kontenjan} kişi · ${d.kategori}</div>
          </div>
        </div>
        <div style="font-size:11px;background:${r.color}18;color:${r.color};padding:4px 10px;border-radius:20px;font-weight:700;">Aktif</div>
      </div>`;
  });
  container.innerHTML += '</div>';
}

// ═══════════════════════════════════════════
// ANTRENÖR DASHBOARD — KATILIM GRAFİĞİ
// ═══════════════════════════════════════════
let antrenorKatilimChartInstance = null;
function renderAntrenorKatilimChart() {
  const ctx = document.getElementById('antrenorKatilimChart');
  if (!ctx) return;
  if (antrenorKatilimChartInstance) antrenorKatilimChartInstance.destroy();

  apiFetch('/api/dersler').then(data => {
    const labels = data.dersler.map(d => d.ders);
    if (labels.length === 0) return;

    // Gerçek katılım oranlarını rezervasyonlardan hesaplayalım
    const dataValues = data.dersler.map(d => {
        const rezCount = (data.rezervasyonlar || []).filter(r => r.ders === d.ders && r.durum === 'aktif').length;
        return Math.min(100, Math.round((rezCount / (d.kontenjan || 20)) * 100));
    });

    const colors = ['#f472b6','#fb923c','#a78bfa','#00d4ff','#4ade80','#6366f1','#ec4899'];

    antrenorKatilimChartInstance = new Chart(ctx.getContext('2d'), {
        type: 'bar',
        data: {
        labels,
        datasets: [{
            label: 'Katılım %',
            data: dataValues,
            backgroundColor: colors.slice(0, labels.length).map(c => c + 'CC'),
            borderColor:     colors.slice(0, labels.length),
            borderWidth: 1.5,
            borderRadius: 10,
            borderSkipped: false,
            barThickness: 30,
        }]
        },
        options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { display: false },
            tooltip: {
            backgroundColor: 'rgba(15,20,40,0.95)',
            borderColor: 'rgba(255,255,255,0.1)', borderWidth: 1,
            titleColor: '#f0f4ff', bodyColor: '#94a3b8',
            padding: 10, cornerRadius: 8,
            callbacks: { label: c => `Katılım: %${c.parsed.x}` }
            }
        },
        scales: {
            x: {
            grid: { color: 'rgba(255,255,255,0.04)' },
            ticks: { color: 'rgba(200,210,255,0.5)', font: { size: 11 }, callback: v => '%' + v },
            border: { display: false }, min: 0, max: 100
            },
            y: {
            grid: { display: false },
            ticks: { color: 'rgba(200,210,255,0.6)', font: { size: 11 } },
            border: { display: false }
            }
        }
        }
    });
  });
}

// ═══════════════════════════════════════════
// YENİ EKLE BUTONLARI VE MODALLARI (Antrenör, Ekipman, Ders)
// ═══════════════════════════════════════════

// --- ANTRENÖR EKLE ---
function showAddTrainerModal() {
  document.getElementById('addTrainerAd').value = '';
  document.getElementById('addTrainerSoyad').value = '';
  document.getElementById('addTrainerEmail').value = '';
  document.getElementById('addTrainerTelefon').value = '';
  document.getElementById('addTrainerUzmanlik').value = '';
  document.getElementById('addTrainerDeneyim').value = '';
  document.getElementById('addTrainerSertifikalar').value = '';
  document.getElementById('addTrainerSifre').value = '';
  document.getElementById('addTrainerModal').classList.add('open');
}
function closeAddTrainerModal() {
  document.getElementById('addTrainerModal').classList.remove('open');
}
function submitAddTrainer() {
  const ad = document.getElementById('addTrainerAd').value.trim();
  const soyad = document.getElementById('addTrainerSoyad').value.trim();
  const email = document.getElementById('addTrainerEmail').value.trim();
  const telefon = document.getElementById('addTrainerTelefon').value.trim();
  const uzmanlik = document.getElementById('addTrainerUzmanlik').value.trim();
  const deneyim = document.getElementById('addTrainerDeneyim').value.trim();
  const sertifikalar = document.getElementById('addTrainerSertifikalar').value.trim();
  const sifre = document.getElementById('addTrainerSifre').value.trim();

  if (!ad || !soyad || !email) { showToast('Ad, Soyad ve E-posta zorunlu!'); return; }
  if (!sifre || sifre.length < 6) { showToast('Şifre en az 6 karakter olmalıdır!'); return; }

  closeAddTrainerModal();
  apiFetch('/api/antrenor-ekle', {
    method: 'POST',
    body: JSON.stringify({ ad, soyad, email, telefon, uzmanlik, deneyim, sertifikalar, sifre })
  }).then(data => {
    showToast(data.mesaj);
    if(data.basarili) renderAntrenorlerPage();
  }).catch(() => showToast('Sunucu hatası!'));
}

// --- EKİPMAN EKLE ---
function showAddEquipmentModal() {
  document.getElementById('addEquipAd').value = '';
  document.getElementById('addEquipKategori').value = 'Kardio';
  document.getElementById('addEquipAdet').value = '1';
  document.getElementById('addEquipFiyat').value = '';
  document.getElementById('addEquipTarih').value = new Date().toISOString().split('T')[0];
  document.getElementById('addEquipmentModal').classList.add('open');
}
function closeAddEquipmentModal() {
  document.getElementById('addEquipmentModal').classList.remove('open');
}
function submitAddEquipment() {
  const ad = document.getElementById('addEquipAd').value.trim();
  const kategori = document.getElementById('addEquipKategori').value;
  const adet = document.getElementById('addEquipAdet').value || '1';
  const fiyat = document.getElementById('addEquipFiyat').value || '0';
  const satinAlma = document.getElementById('addEquipTarih').value;

  if (!ad) { showToast('Ekipman adı zorunlu!'); return; }

  closeAddEquipmentModal();
  apiFetch('/api/ekipman-ekle', {
    method: 'POST',
    body: JSON.stringify({ ad, kategori, adet, fiyat, satinAlma })
  }).then(data => {
    showToast(data.mesaj);
    if(data.basarili) renderEkipmanPage();
  }).catch(() => showToast('Sunucu hatası!'));
}

// --- DERS EKLE ---
function showAddClassModal() {
  document.getElementById('addClassAd').value = '';
  document.getElementById('addClassAntrenor').innerHTML = '<option value="">Yükleniyor...</option>';
  document.getElementById('addClassKategori').value = 'Kardio';
  document.getElementById('addClassKontenjan').value = '20';
  document.getElementById('addClassSure').value = '60';
  
  apiFetch('/api/antrenorler-detay')
    .then(data => {
      const select = document.getElementById('addClassAntrenor');
      select.innerHTML = '<option value="">Seçiniz</option>';
      data.forEach(t => {
        if(t.durum === 'aktif') {
          select.innerHTML += `<option value="${t.id}">${t.isim}</option>`;
        }
      });
    })
    .catch(() => {
      document.getElementById('addClassAntrenor').innerHTML = '<option value="">Antrenörler yüklenemedi</option>';
    });

  document.getElementById('addClassModal').classList.add('open');
}
function closeAddClassModal() {
  document.getElementById('addClassModal').classList.remove('open');
}
function submitAddClass() {
  const dersAd = document.getElementById('addClassAd').value.trim();
  const antrenorId = document.getElementById('addClassAntrenor').value.trim();
  const kategori = document.getElementById('addClassKategori').value;
  const kontenjan = document.getElementById('addClassKontenjan').value || '20';
  const sure = document.getElementById('addClassSure').value || '60';

  if (!dersAd || !antrenorId) { showToast('Ders adı ve Antrenör ID zorunlu!'); return; }

  closeAddClassModal();
  apiFetch('/api/ders-ekle', {
    method: 'POST',
    body: JSON.stringify({ dersAd, antrenorId, kategori, kontenjan, sure })
  }).then(data => {
    showToast(data.mesaj);
    if(data.basarili) renderDerslerPage();
  }).catch(() => showToast('Sunucu hatası!'));
}

// ═══════════════════════════════════════════
// DÜZENLE VE SİL İŞLEMLERİ
// ═══════════════════════════════════════════

// --- GENEL SİLME MODALI ---
function openDeleteModal(type, id, name) {
  document.getElementById('deleteTargetType').value = type;
  document.getElementById('deleteTargetId').value = id;
  document.getElementById('deleteConfirmItemName').textContent = name;
  document.getElementById('universalDeleteModal').classList.add('open');
}
function closeUniversalDeleteModal() { document.getElementById('universalDeleteModal').classList.remove('open'); }
function submitUniversalDelete() {
  const type = document.getElementById('deleteTargetType').value;
  const id = document.getElementById('deleteTargetId').value;
  let endpoint = '';
  let refreshFn = null;

  if (type === 'ders') { endpoint = '/api/ders-sil'; refreshFn = renderDerslerPage; }
  else if (type === 'antrenor') { endpoint = '/api/antrenor-sil'; refreshFn = renderAntrenorlerPage; }
  else if (type === 'ekipman') { endpoint = '/api/ekipman-sil'; refreshFn = renderEkipmanPage; }

  closeUniversalDeleteModal();
  apiFetch(endpoint, {
    method: 'POST',
    body: JSON.stringify({ id: id })
  }).then(data => {
    showToast(data.mesaj);
    if(data.basarili && refreshFn) refreshFn();
  }).catch(() => showToast('Sunucu hatası!'));
}

// --- DERS DÜZENLE ---
function openEditClassModal(id) {
  const d = derslerCachedData.find(x => x.id === id);
  if(!d) return;

  document.getElementById('editClassId').value = d.id;
  document.getElementById('editClassAd').value = d.ders;
  // Antrenor options doldur
  document.getElementById('editClassAntrenor').innerHTML = '<option value="">Yükleniyor...</option>';
  apiFetch('/api/antrenorler-detay').then(data => {
    const sel = document.getElementById('editClassAntrenor');
    sel.innerHTML = '<option value="">Seçiniz</option>';
    data.forEach(t => {
      // isActive check might omit if trainer is pasif, but for editing existing we might still show them if they match.
      // Easiest is to add them all and select the one matching
      const selected = (t.isim === d.antrenor) ? 'selected' : '';
      sel.innerHTML += `<option value="${t.id}" ${selected}>${t.isim}</option>`;
    });
  });

  document.getElementById('editClassKategori').value = d.kategori || 'Kardio';
  document.getElementById('editClassKontenjan').value = d.kontenjan;
  document.getElementById('editClassSure').value = d.sure;
  document.getElementById('editClassDurum').value = (d.durum || '').toLowerCase() === 'aktif' ? 'aktif' : 'pasif';
  
  document.getElementById('editClassModal').classList.add('open');
}
function closeEditClassModal() { document.getElementById('editClassModal').classList.remove('open'); }
function submitEditClass() {
  const id = document.getElementById('editClassId').value;
  const dersAd = document.getElementById('editClassAd').value.trim();
  const antrenorId = document.getElementById('editClassAntrenor').value;
  const kategori = document.getElementById('editClassKategori').value;
  const kontenjan = document.getElementById('editClassKontenjan').value;
  const sure = document.getElementById('editClassSure').value;
  const durum = document.getElementById('editClassDurum').value;

  if(!dersAd || !antrenorId) { showToast('Eksik alanlar!'); return; }

  closeEditClassModal();
  apiFetch('/api/ders-guncelle', {
    method: 'POST',
    body: JSON.stringify({ id, dersAd, antrenorId, kategori, kontenjan, sure, durum })
  }).then(res => {
    showToast(res.mesaj);
    if(res.basarili) renderDerslerPage();
  }).catch(()=>showToast('Hata!'));
}

// --- EKİPMAN DÜZENLE ---
function openEditEquipmentModal(id) {
  const e = ekipmanCachedData.find(x => x.id === id);
  if(!e) return;
  document.getElementById('editEquipId').value = e.id;
  document.getElementById('editEquipAd').value = e.ad;
  document.getElementById('editEquipKategori').value = e.kategori || 'Kardio';
  document.getElementById('editEquipAdet').value = e.adet;
  document.getElementById('editEquipFiyat').value = e.fiyat;
  document.getElementById('editEquipDurum').value = e.durum || 'calisiyor';
  document.getElementById('editEquipmentModal').classList.add('open');
}
function closeEditEquipmentModal() { document.getElementById('editEquipmentModal').classList.remove('open'); }
function submitEditEquipment() {
  const id = document.getElementById('editEquipId').value;
  const ad = document.getElementById('editEquipAd').value.trim();
  const kategori = document.getElementById('editEquipKategori').value;
  const adet = document.getElementById('editEquipAdet').value;
  const fiyat = document.getElementById('editEquipFiyat').value;
  const durum = document.getElementById('editEquipDurum').value;
  
  if(!ad) { showToast('Ekipman adı gerekli!'); return; }
  
  closeEditEquipmentModal();
  apiFetch('/api/ekipman-guncelle', {
    method:'POST', body: JSON.stringify({ id, ad, kategori, adet, fiyat, durum })
  }).then(res=>{ showToast(res.mesaj); if(res.basarili) renderEkipmanPage(); });
}

// --- ANTRENÖR DÜZENLE ---
function openEditTrainerModal(id) {
  const t = antrenorlerCachedData.find(x => x.id === id);
  if(!t) return;
  document.getElementById('editTrainerId').value = t.id;
  // The API returns 'isim' (Ad Soyad space separated). We try to split roughly
  const parts = t.isim.split(' ');
  document.getElementById('editTrainerSoyad').value = parts.length > 1 ? parts.pop() : '';
  document.getElementById('editTrainerAd').value = parts.join(' ');
  document.getElementById('editTrainerEmail').value = t.email || '';
  document.getElementById('editTrainerTelefon').value = t.telefon || '';
  document.getElementById('editTrainerUzmanlik').value = t.uzmanlik || '';
  document.getElementById('editTrainerDeneyim').value = t.deneyim || 0;
  document.getElementById('editTrainerSertifikalar').value = t.sertifikalar || '';
  document.getElementById('editTrainerDurum').value = (t.durum||'aktif').toLowerCase();

  document.getElementById('editTrainerModal').classList.add('open');
}
function closeEditTrainerModal() { document.getElementById('editTrainerModal').classList.remove('open'); }
function submitEditTrainer() {
  const id = document.getElementById('editTrainerId').value;
  const ad = document.getElementById('editTrainerAd').value.trim();
  const soyad = document.getElementById('editTrainerSoyad').value.trim();
  const email = document.getElementById('editTrainerEmail').value.trim();
  const telefon = document.getElementById('editTrainerTelefon').value.trim();
  const uzmanlik = document.getElementById('editTrainerUzmanlik').value.trim();
  const deneyim = document.getElementById('editTrainerDeneyim').value;
  const sertifikalar = document.getElementById('editTrainerSertifikalar').value.trim();
  const durum = document.getElementById('editTrainerDurum').value;

  if(!ad || !soyad || !email) { showToast('Ad, Soyad, ve Email gerekli!'); return; }

  closeEditTrainerModal();
  apiFetch('/api/antrenor-guncelle', {
    method:'POST', body: JSON.stringify({ id, ad, soyad, email, telefon, uzmanlik, deneyim, sertifikalar, durum })
  }).then(res=>{ showToast(res.mesaj); if(res.basarili) renderAntrenorlerPage(); });
}

function bookClass(dersId, programId) {
    const ders = derslerCachedData.find(d => d.id === dersId);
    if (!ders) return;

    // Plan bazlı ders seçim kontrolü
    if (currentRole === 'uye') {
        if (!uyeAktifPlan) {
            showToast('Aktif aboneliğiniz bulunmuyor! Ders seçebilmek için bir plan satın alın.', true);
            return;
        }
        const limit = PLAN_DERS_LIMIT[uyeAktifPlan] ?? 0;
        if (limit === 0) {
            showToast('Basic planda ders seçim hakkı bulunmamaktadır. Planınızı yükseltin!', true);
            return;
        }
        if (limit !== Infinity && uyeAktifRezCount >= limit) {
            showToast(`${uyeAktifPlan} planında maksimum ${limit} ders seçebilirsiniz. Hakkınız doldu!`, true);
            return;
        }
    }

    if (confirm(`${ders.ders} dersi için rezervasyon yapmak istediğinize emin misiniz?`)) {
        apiFetch('/api/rezervasyon-yap', {
            method: 'POST',
            body: JSON.stringify({ program_id: String(programId) })
        }).then(data => {
            showToast(data.mesaj, !data.basarili);
            if (data.basarili) {
                renderDerslerPage();
                loadDashboardStats();
            }
        }).catch(err => {
            showToast('Rezervasyon sırasında bir hata oluştu.', true);
        });
    }
}

function cancelReservation(rezId) {
    if (confirm('Bu rezervasyonu iptal etmek istediğinizden emin misiniz?')) {
        apiFetch('/api/rezervasyon-iptal', {
            method: 'POST',
            body: JSON.stringify({ rezervasyon_id: rezId })
        }).then(data => {
            showToast(data.mesaj);
            if (data.basarili) {
                renderDerslerPage();
                loadDashboardStats();
            }
        });
    }
}

function openAddProgramModal() {
    const sel = document.getElementById('addProgDers');
    sel.innerHTML = '<option value="">Yükleniyor...</option>';
    apiFetch('/api/dersler').then(data => {
        sel.innerHTML = '<option value="">Ders Seçin</option>';
        data.dersler.forEach(d => {
            sel.innerHTML += `<option value="${d.id}">${d.ders}</option>`;
        });
    });
    document.getElementById('addProgramModal').classList.add('open');
}
function closeAddProgramModal() { document.getElementById('addProgramModal').classList.remove('open'); }
function submitAddProgram() {
    const ders_id = document.getElementById('addProgDers').value;
    const gun = document.getElementById('addProgGun').value;
    const baslangic = document.getElementById('addProgBas').value;
    const bitis = document.getElementById('addProgBit').value;
    const salon = document.getElementById('addProgSalon').value.trim();

    if(!ders_id || !baslangic || !bitis) { showToast('Lütfen tüm alanları doldurun!'); return; }

    closeAddProgramModal();
    apiFetch('/api/program-ekle', {
        method: 'POST',
        body: JSON.stringify({ ders_id, gun, baslangic, bitis, salon })
    }).then(data => {
        showToast(data.mesaj);
        if (data.basarili) {
            renderDerslerPage();
            loadDashboardStats();
        }
    });
}

function deleteProgram(id) {
    if (confirm('Bu program slotunu silmek istediğinizden emin misiniz?')) {
        apiFetch('/api/program-sil', {
            method: 'POST',
            body: JSON.stringify({ program_id: id })
        }).then(data => {
            showToast(data.mesaj);
            if (data.basarili) {
                renderDerslerPage();
                loadDashboardStats();
            }
        });
    }
}

// --- ABONELİK DÜZENLEME (Admin) ---
function openEditAbonelikModal(id, uye, plan, baslangic, bitis) {
    document.getElementById('editAbonelikId').value = id;
    document.getElementById('editAbonelikUye').value = uye;
    document.getElementById('editAbonelikPlan').value = plan;
    document.getElementById('editAbonelikBaslangic').value = baslangic;
    document.getElementById('editAbonelikBitis').value = bitis;
    document.getElementById('editAbonelikModal').classList.add('open');
}
function closeEditAbonelikModal() {
    document.getElementById('editAbonelikModal').classList.remove('open');
}
function submitEditAbonelik() {
    const abonelik_id = document.getElementById('editAbonelikId').value;
    const baslangic = document.getElementById('editAbonelikBaslangic').value;
    const bitis = document.getElementById('editAbonelikBitis').value;

    if (!baslangic || !bitis) { showToast('Başlangıç ve bitiş tarihi zorunlu!'); return; }
    if (bitis <= baslangic) { showToast('Bitiş tarihi başlangıçtan sonra olmalı!'); return; }

    closeEditAbonelikModal();
    apiFetch('/api/abonelik-guncelle', {
        method: 'POST',
        body: JSON.stringify({ abonelik_id: parseInt(abonelik_id), baslangic, bitis })
    }).then(data => {
        showToast(data.mesaj);
        if (data.basarili) renderAboneliklerPage();
    }).catch(() => showToast('Sunucu hatası!'));
}
