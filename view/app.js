// ═══════════════════════════════════════════
// FitZone Pro — app.js
// Sadece Frontend Demo Verileri
// Backend bağlantısı yapılacak
// ═══════════════════════════════════════════

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
const API_URL = 'http://localhost:8080';
let apiMembers = []; // SQL Server'dan gelen üye listesi

// ═══════════════════════════════════════════
// TOKEN YÖNETİMİ — Auth helpers
// ═══════════════════════════════════════════
function getToken()    { return localStorage.getItem('fitzone_token'); }
function setToken(t)   { localStorage.setItem('fitzone_token', t); }
function clearToken()  {
  localStorage.removeItem('fitzone_token');
  localStorage.removeItem('fitzone_user');
}
function getSavedUser() {
  try { return JSON.parse(localStorage.getItem('fitzone_user')); } catch { return null; }
}

/**
 * apiFetch — Authorization header'lı fetch wrapper
 * 401/403 alınırsa otomatik çıkış yapılır.
 */
function apiFetch(path, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': 'Bearer ' + token } : {}),
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
    const data = await fetch(API_URL + '/api/dogrula', {
      headers: { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' }
    }).then(r => r.json());
    if (data.basarili) {
      const k = data.kullanici;
      const saved = getSavedUser() || {};
      const user = {
        id:    k.id,
        ad:    k.ad    || saved.ad    || '',
        soyad: k.soyad || saved.soyad || '',
        email: k.email || saved.email || '',
        rol:   k.rol   || saved.rol   || 'uye',
        telefon: k.telefon || saved.telefon || '',
        name:  (k.ad||saved.ad||'') + ' ' + (k.soyad||saved.soyad||'')
      };
      loginAs(user.rol, user);
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
      apiMembers = data.map(u => ({
        id: u.id, ad: u.ad, soyad: u.soyad,
        name: u.ad + ' ' + u.soyad,
        email: u.email, telefon: u.telefon || '',
        cinsiyet: u.cinsiyet || '', rol: u.rol,
        durum: u.durum, kayitTarihi: u.kayitTarihi || '',
        uyelikNo: u.uyelikNo || '', dogumTarihi: u.dogumTarihi || '',
        abonelikPlan: u.abonelikPlan || '', abonelikBitis: u.abonelikBitis || '',
        abonelikDurum: u.abonelikDurum || ''
      }));
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
  document.getElementById('editCinsiyet').value = m.cinsiyet || '';
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
const chartData = {
  gelir: {
    labels: ['Eyl','Eki','Kas','Ara','Oca','Şub','Mar'],
    data: [32000, 37000, 41000, 38000, 43000, 45000, 47200],
    color1: 'rgba(139,92,246,0.8)', color2: 'rgba(0,212,255,0.2)',
    label: 'Aylık Gelir (₺)'
  },
  uye: {
    labels: ['Eyl','Eki','Kas','Ara','Oca','Şub','Mar'],
    data: [180, 195, 210, 205, 228, 238, 248],
    color1: 'rgba(0,212,255,0.8)', color2: 'rgba(139,92,246,0.2)',
    label: 'Toplam Üye'
  },
  devamsiz: {
    labels: ['Eyl','Eki','Kas','Ara','Oca','Şub','Mar'],
    data: [8, 12, 9, 15, 11, 13, 12],
    color1: 'rgba(244,114,182,0.8)', color2: 'rgba(251,146,60,0.2)',
    label: 'Devamsız Üye'
  }
};

let chart;

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
  const ctx = document.getElementById('mainChart').getContext('2d');
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
      if (el3) el3.textContent = data.aktifUye;
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
    if (data.length === 0) { container.innerHTML += '<div style="text-align:center;color:var(--text-muted);padding:20px;font-size:13px;">Bugün giriş kaydı yok</div>'; }
    data.forEach(l => {
      const isInside = l.durum === 'giris';
      container.innerHTML += `<div class="plan-card"><div class="plan-left"><div class="plan-icon" style="background:${isInside?'rgba(74,222,128,.1)':'rgba(148,163,184,.1)'};color:${isInside?'#4ade80':'#94a3b8'};font-size:16px;"><i class="fas ${turuIcon[l.turu]||'fa-door-open'}"></i></div><div><div class="plan-name">${l.uye}</div><div class="plan-members">Giriş: ${l.giris} · Çıkış: ${l.cikis||'İçeride'} · ${turuLabel[l.turu]||l.turu}</div></div></div><div style="font-size:11px;background:${isInside?'rgba(74,222,128,.1)':'rgba(148,163,184,.1)'};color:${isInside?'#4ade80':'#94a3b8'};padding:4px 10px;border-radius:20px;font-weight:600;">${isInside?'İçeride':'Çıktı'}</div></div>`;
    });
    container.innerHTML += '</div>';
  }).catch(e => { if(e.message!=='AUTH_ERROR') renderAccessLogs(); });
}

function loadEquipmentCardsFromAPI() {
  apiFetch('/api/ekipman').then(data => {
    const container = document.getElementById('equipmentCards');
    if (!container) return;
    const durumStyle = { calisiyor:{ text:'Çalışıyor', color:'#4ade80', bg:'rgba(74,222,128,.1)' }, bakimda:{ text:'Bakımda', color:'#fbbf24', bg:'rgba(251,191,36,.1)' }, arizali:{ text:'Arızalı', color:'#f87171', bg:'rgba(239,68,68,.1)' } };
    const categoryIcons = { 'Kardio':'🏃', 'Güç':'💪', 'Esneklik':'🧘' };
    container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
    data.ekipman.forEach(e => {
      const d = durumStyle[e.durum] || durumStyle.calisiyor;
      const icon = categoryIcons[e.kategori] || '🔧';
      const bakim = data.bakim.find(b => b.ekipman === e.ad);
      container.innerHTML += `<div class="plan-card"><div class="plan-left"><div class="plan-icon" style="background:${d.bg};color:${d.color};font-size:17px;">${icon}</div><div><div class="plan-name">${e.ad}</div><div class="plan-members">${e.kategori} · ${e.adet} adet${bakim ? ' · Son bakım: '+bakim.tarih : ''}</div></div></div><div style="font-size:11px;background:${d.bg};color:${d.color};padding:4px 10px;border-radius:20px;font-weight:600;">${d.text}</div></div>`;
    });
    container.innerHTML += '</div>';
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
      renderUyelerPageTable(apiData);
    });
    return;
  }
  uyelerCachedData = data;
  renderUyelerPageTable(data);
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
  // API'den planları çek
  fetch(API_URL + '/api/planlar').then(r => r.json()).then(plans => {
    const iconMap = { 'Platinum':'💎', 'Gold':'⭐', 'Silver':'🥈', 'Basic':'🔰' };
    const colorMap = { 'Platinum':{ color:'#a78bfa', bg:'rgba(139,92,246,.15)' }, 'Gold':{ color:'#fbbf24', bg:'rgba(251,191,36,.15)' }, 'Silver':{ color:'#94a3b8', bg:'rgba(148,163,184,.15)' }, 'Basic':{ color:'#67e8f9', bg:'rgba(34,211,238,.1)' } };
    planContainer.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;">';
    plans.forEach(p => {
      const c = colorMap[p.ad] || { color:'#94a3b8', bg:'rgba(148,163,184,.15)' };
      const icon = iconMap[p.ad] || '📋';
      const oz = Array.isArray(p.ozellikler) ? p.ozellikler.join(', ') : '';
      planContainer.innerHTML += `<div class="plan-card"><div class="plan-left"><div class="plan-icon" style="background:${c.bg};color:${c.color};font-size:18px;">${icon}</div><div><div class="plan-name">${p.ad}</div><div class="plan-members">${p.aktifUye} aktif üye · ${p.sureAy} ay · ${oz}</div></div></div><div class="plan-price" style="color:${c.color}">₺${Math.round(p.fiyat)}/ay</div></div>`;
  });
    planContainer.innerHTML += '</div>';

  // Plan chart
  const ctx = document.getElementById('planChart');
  if (ctx) {
    if (planChartInstance) planChartInstance.destroy();
    planChartInstance = new Chart(ctx.getContext('2d'), {
      type:'doughnut',
      data:{ labels:['Platinum','Gold','Silver','Basic'], datasets:[{ data:[3,2,2,1], backgroundColor:['#a78bfa','#fbbf24','#94a3b8','#67e8f9'], borderWidth:0 }] },
      options:{ responsive:true, maintainAspectRatio:false, plugins:{ legend:{ position:'bottom', labels:{ color:'rgba(200,210,255,0.7)', padding:16, font:{size:12} } } } }
    });
  }

  // Abonelik tablosu API'den
  fetch(API_URL + '/api/abonelikler').then(r => r.json()).then(abonelikler => {
    const tbody = document.getElementById('aboneliklerTableBody');
    if (!tbody) return;
    const durumLabel = { aktif:'Aktif', pasif:'Pasif', iptal:'İptal', suresi_doldu:'Süresi Doldu' };
    tbody.innerHTML = '';
    const currentProfile = roleProfiles[currentRole];
    const filteredAbonelikler = currentRole === 'uye'
      ? abonelikler.filter(a => a.uye === currentProfile.name)
      : abonelikler;
    filteredAbonelikler.forEach((a, idx) => {
      const color = avatarColors[idx % avatarColors.length];
      const pKey = a.plan.toLowerCase();
      const p = planColors[pKey] || planColors.basic;
      tbody.innerHTML += `<tr>
        <td><div class="member-info"><div class="m-avatar" style="background:${color}">${getInitials(a.uye)}</div><div class="m-name">${a.uye}</div></div></td>
        <td><span class="plan-badge ${p.class}">${p.icon} ${a.plan}</span></td>
        <td style="color:var(--text-muted);font-size:12px">${a.baslangic}</td>
        <td style="color:var(--text-muted);font-size:12px">${a.bitis}</td>
        <td style="text-align:center"><i class="fas ${a.otomatik?'fa-check-circle':'fa-times-circle'}" style="color:${a.otomatik?'#4ade80':'#f87171'}"></i></td>
        <td><span class="status-dot ${a.durum}">${durumLabel[a.durum]}</span></td>
        <td><div style="display:flex;gap:6px"><div class="icon-btn" style="width:30px;height:30px;border-radius:8px;font-size:11px;"><i class="fas fa-pen"></i></div><div class="icon-btn" style="width:30px;height:30px;border-radius:8px;font-size:11px;"><i class="fas fa-clock-rotate-left" style="color:#fbbf24"></i></div></div></td></tr>`;
    });
  }).catch(() => {});
  }).catch(() => {});
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

function renderOdemelerPage() {
  fetch(API_URL + '/api/odemeler').then(r => r.json()).then(apiData => {
    odemelerCachedData = apiData;
    const currentProfile = roleProfiles[currentRole];
    const veri = currentRole === 'uye'
      ? apiData.filter(o => o.uye === currentProfile.name)
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
    const currentProfile = roleProfiles[currentRole];
    const veri = currentRole === 'uye' ? odemelerData.filter(o => o.uye === currentProfile.name) : odemelerData;
    renderOdemelerTable(veri);
  });
}
let odemelerCachedData = [];
function filterOdemeler(st) {
  const currentProfile = roleProfiles[currentRole];
  const source = odemelerCachedData.length > 0 ? odemelerCachedData : odemelerData;
  let veri = currentRole === 'uye'
    ? source.filter(o => o.uye === currentProfile.name)
    : source;
  renderOdemelerTable(st === 'hepsi' ? veri : veri.filter(o => o.durum === st));
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

function renderDerslerPage() {
  fetch(API_URL + '/api/dersler').then(r => r.json()).then(data => {
    const iconMap = { 'Esneklik':'🧘', 'Kardio':'🥊', 'Güç':'🏋️', 'Yüzme':'🏊', 'Pilates':'🤸' };
    const dTb = document.getElementById('derslerTableBody');
    if (dTb) {
      dTb.innerHTML = '';
      data.dersler.forEach(d => {
        const icon = iconMap[d.kategori] || '📋';
        dTb.innerHTML += `<tr><td><div style="display:flex;align-items:center;gap:10px"><span style="font-size:18px">${icon}</span><span class="m-name">${d.ders}</span></div></td><td style="color:var(--text-muted);font-size:12px">${d.antrenor}</td><td style="color:var(--text-muted);font-size:12px">${d.kategori}</td><td style="color:var(--text-muted);font-size:12px">${d.kontenjan} kişi</td><td style="color:var(--text-muted);font-size:12px">${d.sure} dk</td><td><span class="status-dot ${d.durum}">${d.durum === 'aktif' ? 'Aktif' : d.durum}</span></td></tr>`;
      });
    }
    const pTb = document.getElementById('programTableBody');
    if (pTb) {
      pTb.innerHTML = '';
      data.program.forEach(p => {
        pTb.innerHTML += `<tr><td style="font-weight:600;font-size:13px">${p.gun}</td><td style="color:var(--text-muted);font-size:12px">${p.ders}</td><td style="color:var(--text-muted);font-size:12px">${p.saat}</td><td style="color:var(--text-muted);font-size:12px">${p.salon}</td><td><span class="status-dot aktif">Aktif</span></td></tr>`;
      });
    }
    const rTb = document.getElementById('rezervasyonTableBody');
    const rDurum = { aktif:'Aktif', iptal:'İptal', tamamlandi:'Tamamlandı' };
    const isUye = currentRole === 'uye';
    const currentProfile = roleProfiles[currentRole];
    const uyeSutunu = document.getElementById('rezervasyonUyeTh');
    if (uyeSutunu) uyeSutunu.style.display = isUye ? 'none' : '';
    let filteredRez = isUye
      ? data.rezervasyonlar.filter(r => r.uye === currentProfile.name)
      : data.rezervasyonlar;
    if (rTb) {
      rTb.innerHTML = '';
      filteredRez.forEach((r, idx) => {
        const color = avatarColors[idx % avatarColors.length];
        const uyeCell = isUye ? '' : `<td><div class="member-info"><div class="m-avatar" style="background:${color};width:30px;height:30px;font-size:11px;border-radius:8px">${getInitials(r.uye)}</div><div class="m-name">${r.uye}</div></div></td>`;
        rTb.innerHTML += `<tr>${uyeCell}<td style="color:var(--text-muted);font-size:12px">${r.ders}</td><td style="color:var(--text-muted);font-size:12px">${r.tarih}</td><td style="color:var(--text-muted);font-size:12px">${r.saat}</td><td><span class="status-dot ${r.durum}">${rDurum[r.durum] || r.durum}</span></td></tr>`;
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
}

// ═══════════════════════════════════════════
// ANTRENÖRLER SAYFASI
// ═══════════════════════════════════════════
function renderAntrenorlerPage() {
  const container = document.getElementById('antrenorProfileCards');
  if (!container) return;
  fetch(API_URL + '/api/antrenorler-detay').then(r => r.json()).then(trainers => {
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
            <div class="trainer-stat"><div class="trainer-stat-value">—</div><div class="trainer-stat-label">Öğrenci</div></div>
          </div>
          <div style="margin-bottom:10px;"><div style="font-size:11px;color:var(--text-muted);margin-bottom:6px;font-weight:600;text-transform:uppercase;letter-spacing:.08em;">Sertifikalar</div><div class="trainer-certs">${certs || '—'}</div></div>
          <div class="trainer-bio">${t.biyografi || ''}</div>
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
}

let girisCikisCachedData = [];
function renderGirisCikisPage() {
  fetch(API_URL + '/api/giris-cikis').then(r => r.json()).then(data => {
    girisCikisCachedData = data.map(l => ({ uye:l.uye, giris:l.giris, cikis:l.cikis, turu:l.turu, durum:l.durum }));
    renderGirisCikisTable(girisCikisCachedData);
  }).catch(() => { girisCikisCachedData = girisCikisData; renderGirisCikisTable(girisCikisData); });
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
      <td><span style="font-size:11px;background:${d.color}20;color:${d.color};padding:4px 10px;border-radius:20px;font-weight:600;">${d.text}</span></td></tr>`;
  });
}

let ekipmanCachedData = [];
function renderEkipmanPage() {
  fetch(API_URL + '/api/ekipman').then(r => r.json()).then(data => {
    ekipmanCachedData = data.ekipman.map(e => ({ ad:e.ad, kategori:e.kategori, adet:e.adet, satinAlma:e.satinAlma, fiyat:e.fiyat, durum:e.durum }));
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

  const c1 = document.getElementById('raporGelirChart');
  if (c1) { raporCharts.gelir = new Chart(c1.getContext('2d'), { type:'bar', data:{ labels:['Eyl','Eki','Kas','Ara','Oca','Şub','Mar'], datasets:[{ label:'Gelir (₺)', data:[32000,37000,41000,38000,43000,45000,47200], backgroundColor:'rgba(139,92,246,0.6)', borderRadius:8, barThickness:24 }] }, options:{...baseOpt, plugins:{legend:{display:false}}} }); }

  const c2 = document.getElementById('raporUyeChart');
  if (c2) { const g2=c2.getContext('2d').createLinearGradient(0,0,0,220); g2.addColorStop(0,'rgba(0,212,255,0.3)'); g2.addColorStop(1,'rgba(0,0,0,0)'); raporCharts.uye = new Chart(c2.getContext('2d'), { type:'line', data:{ labels:['Eyl','Eki','Kas','Ara','Oca','Şub','Mar'], datasets:[{ label:'Toplam Üye', data:[180,195,210,205,228,238,248], borderColor:'rgba(0,212,255,0.8)', backgroundColor:g2, fill:true, tension:0.4, pointRadius:4, pointBackgroundColor:'rgba(0,212,255,0.8)' }] }, options:{...baseOpt, plugins:{legend:{display:false}}} }); }

  const c3 = document.getElementById('raporOdemeChart');
  if (c3) { raporCharts.odeme = new Chart(c3.getContext('2d'), { type:'doughnut', data:{ labels:['Kredi Kartı','Nakit','Havale','Online'], datasets:[{ data:[5,2,1,1], backgroundColor:['#a78bfa','#4ade80','#fbbf24','#00d4ff'], borderWidth:0 }] }, options:{ responsive:true, maintainAspectRatio:false, plugins:{ legend:{ position:'bottom', labels:{ color:'rgba(200,210,255,0.7)', padding:14, font:{size:11} } } } } }); }

  const c4 = document.getElementById('raporDersChart');
  if (c4) { raporCharts.ders = new Chart(c4.getContext('2d'), { type:'bar', data:{ labels:['Yoga','Kickboks','Aqua','Fonk.Fitness','Pilates'], datasets:[{ label:'Katılım %', data:[80,90,53,75,67], backgroundColor:['#a78bfa','#f472b6','#00d4ff','#fb923c','#4ade80'], borderRadius:8, barThickness:20 }] }, options:{...baseOpt, plugins:{legend:{display:false}}, indexAxis:'y'} }); }
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
function showLoginModal()    { document.getElementById('loginModal').classList.add('open'); }
function closeLoginModal()   { document.getElementById('loginModal').classList.remove('open'); }
function showRegisterModal() { document.getElementById('registerModal').classList.add('open'); }
function closeRegisterModal(){ document.getElementById('registerModal').classList.remove('open'); }
function showForgotModal()   { document.getElementById('forgotModal').classList.add('open'); }
function closeForgotModal()  { document.getElementById('forgotModal').classList.remove('open'); }
function showResetModal()    { document.getElementById('resetPasswordModal').classList.add('open'); }
function closeResetModal()   { document.getElementById('resetPasswordModal').classList.remove('open'); }

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
    body: JSON.stringify({ ad, soyad, email, telefon, cinsiyet, dogum_tarihi: dogum, sifre, rol: 'uye' })
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
      console.log('-----------------------------');
      console.log('SİMÜLEYE EDİLEN E-POSTA (' + email + ')');
      console.log('Şifre sıfırlama linkiniz: http://localhost:8080/?resetToken=' + data.token);
      console.log('-----------------------------');
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

  // Admin-only büyük bölümler (grafik+sağpanel, üye listesi)
  ['adminContentGrid', 'adminMembersGrid'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.style.display = isAdmin ? '' : 'none';
  });

  // Üye-only bölüm
  const uyeGrid = document.getElementById('uyeDashboardGrid');
  if (uyeGrid) uyeGrid.style.display = isUye ? '' : 'none';

  // Antrenör-only bölüm
  const antrenorGrid = document.getElementById('antrenorDashboardGrid');
  if (antrenorGrid) antrenorGrid.style.display = isAntrenor ? '' : 'none';

  // Bottom Grid 1 kartları
  // Antrenör ve Üye: Abonelik Planları + Hızlı İşlemler gizli
  ['dashPlanlariCard', 'dashHizliIslemlerCard'].forEach(id => {
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
  // Ekipman: admin ve üye'de görünür, antrenörde gizli
  if (ekipmanC) ekipmanC.style.display = isAntrenor ? 'none' : '';
  // Grid2 sütun: üyede 2 kart (antrenörler yok, ekipman var → 1 sütun ekipman ile olmaz doğrusu)
  const bg2 = document.getElementById('dashBottomGrid2');
  if (bg2) {
    if (isAdmin)   bg2.style.gridTemplateColumns = '';
    if (isUye)     bg2.style.gridTemplateColumns = '1fr 1fr';
    if (isAntrenor) bg2.style.display = 'none';
  }

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

  loadAppData();
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
  // Haftalık program verisi (programData'dan)
  const programMap = {};
  gunler.forEach(g => programMap[g] = []);
  programData.forEach(p => { if (programMap[p.gun]) programMap[p.gun].push(p); });

  // Üyenin rezervasyonları
  const currentProfile = roleProfiles[currentRole];
  const uyeRezervasyonlar = rezervasyonData.filter(r => r.uye === currentProfile?.name).map(r => r.ders + r.tarih);

  let html = `
    <div style="display:grid;grid-template-columns:repeat(6,1fr);gap:10px;min-width:600px;padding:4px 2px 8px;">
  `;
  gunler.forEach(gun => {
    const dersler = programMap[gun] || [];
    html += `
      <div style="display:flex;flex-direction:column;gap:8px;">
        <div style="font-size:11px;font-weight:700;color:var(--text-muted);text-transform:uppercase;letter-spacing:.06em;text-align:center;padding:4px 0;border-bottom:1px solid var(--glass-border);">${gun.slice(0,3)}</div>
    `;
    if (dersler.length === 0) {
      html += `<div style="text-align:center;color:var(--text-muted);font-size:11px;padding:12px 0;">—</div>`;
    } else {
      dersler.forEach(d => {
        const r = dersRenkleri[d.ders] || { bg:'rgba(100,116,139,.12)', color:'#94a3b8', icon:'📋' };
        html += `
          <div style="background:${r.bg};border:1px solid ${r.color}30;border-radius:10px;padding:8px 7px;cursor:pointer;transition:transform .15s,box-shadow .15s;" 
               onmouseover="this.style.transform='translateY(-2px)';this.style.boxShadow='0 6px 20px ${r.color}25'"
               onmouseout="this.style.transform='';this.style.boxShadow=''">
            <div style="font-size:16px;text-align:center;margin-bottom:4px;">${r.icon}</div>
            <div style="font-size:10px;font-weight:700;color:${r.color};text-align:center;line-height:1.3;">${d.ders}</div>
            <div style="font-size:9px;color:var(--text-muted);text-align:center;margin-top:3px;">${d.saat}</div>
            <div style="font-size:9px;color:var(--text-muted);text-align:center;">${d.salon}</div>
          </div>
        `;
      });
    }
    html += `</div>`;
  });
  html += `</div>`;
  container.innerHTML = html;
}

// ═══════════════════════════════════════════
// ÜYE DASHBOARD — AKTİVİTE GRAFİĞİ
// ═══════════════════════════════════════════
let uyeAktiviteChartInstance = null;
function renderUyeAktiviteChart() {
  const ctx = document.getElementById('uyeAktiviteChart');
  if (!ctx) return;
  if (uyeAktiviteChartInstance) uyeAktiviteChartInstance.destroy();

  // Üyenin bu haftaki aktivite verisi (demo)
  const gunler = ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'];
  const aktivite = [60, 0, 45, 60, 50, 0, 30]; // dakika cinsinden

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
        y: { grid: { color: 'rgba(255,255,255,0.04)' }, ticks: { color: 'rgba(200,210,255,0.5)', font: { size: 11 }, callback: v => v + ' dk' }, border: { display: false }, min: 0, max: 90 }
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

  const currentProfile = roleProfiles[currentRole]; // Kemal Antrenör
  const gunler = ['Pazartesi', 'Salı', 'Çarşamba', 'Perşembe', 'Cuma', 'Cumartesi'];

  const dersRenkleri = {
    'Yoga Flow':           { bg:'rgba(139,92,246,.15)',  color:'#a78bfa', icon:'🧘' },
    'Kickboks':            { bg:'rgba(244,114,182,.15)', color:'#f472b6', icon:'🥊' },
    'Aqua Aerobik':        { bg:'rgba(0,212,255,.12)',   color:'#00d4ff', icon:'🏊' },
    'Fonksiyonel Fitness': { bg:'rgba(251,146,60,.15)',  color:'#fb923c', icon:'🏋️' },
    'Pilates':             { bg:'rgba(74,222,128,.12)',  color:'#4ade80', icon:'🤸' },
  };

  // Sadece bu antrenörün derslerini filtrele
  const benimDersler = derslerDemoData
    .filter(d => d.antrenor === currentProfile.name)
    .map(d => d.ders);

  // Programa göre bu antrenörün derslerini günlere yaz
  const programMap = {};
  gunler.forEach(g => programMap[g] = []);
  programData.forEach(p => {
    if (programMap[p.gun] && benimDersler.includes(p.ders)) programMap[p.gun].push(p);
  });

  let html = `<div style="display:grid;grid-template-columns:repeat(6,1fr);gap:10px;min-width:580px;padding:4px 2px 8px;">`;
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

  // Ders listesini render et
  renderAntrenorDersListesi(benimDersler);
}

function renderAntrenorDersListesi(benimDersler) {
  const container = document.getElementById('antrenorDersListesi');
  if (!container) return;
  const dersRenkleri = {
    'Yoga Flow':           { color:'#a78bfa', icon:'🧘', katilim:80 },
    'Kickboks':            { color:'#f472b6', icon:'🥊', katilim:90 },
    'Aqua Aerobik':        { color:'#00d4ff', icon:'🏊', katilim:53 },
    'Fonksiyonel Fitness': { color:'#fb923c', icon:'🏋️', katilim:75 },
    'Pilates':             { color:'#4ade80', icon:'🤸', katilim:67 },
  };
  container.innerHTML = '<div style="display:flex;flex-direction:column;gap:10px;padding:4px 0;">';
  derslerDemoData.filter(d => benimDersler.includes(d.ders)).forEach(d => {
    const r = dersRenkleri[d.ders] || { color:'#94a3b8', icon:'📋', katilim:0 };
    container.innerHTML += `
      <div class="plan-card">
        <div class="plan-left">
          <div class="plan-icon" style="background:${r.color}18;color:${r.color};font-size:17px;">${r.icon}</div>
          <div>
            <div class="plan-name">${d.ders}</div>
            <div class="plan-members">${d.sure} dk · ${d.kontenjan} kişi · ${d.kategori}</div>
          </div>
        </div>
        <div style="font-size:11px;background:${r.color}18;color:${r.color};padding:4px 10px;border-radius:20px;font-weight:700;">%${r.katilim}</div>
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

  const currentProfile = roleProfiles[currentRole];
  const benimDersler = derslerDemoData
    .filter(d => d.antrenor === currentProfile.name);

  const labels = benimDersler.map(d => d.ders);
  const katilimMap = {
    'Kickboks':90,'Fonksiyonel Fitness':75,'Yoga Flow':80,'Aqua Aerobik':53,'Pilates':67
  };
  const data   = benimDersler.map(d => katilimMap[d.ders] || 70);
  const colors = ['#f472b6','#fb923c','#a78bfa','#00d4ff','#4ade80'];

  antrenorKatilimChartInstance = new Chart(ctx.getContext('2d'), {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: 'Katılım %',
        data,
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
}
