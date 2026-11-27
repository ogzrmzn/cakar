/* Çakar & Siren Simülatörü — Service Worker (çevrimdışı çalışma)
 *
 * Strateji:
 *  - Sayfanın kendisi (HTML) ÖNCE İNTERNETTEN alınır → her güncelleme hemen görünür.
 *    İnternet yoksa önbellekteki son kopya açılır (uygulama çevrimdışı da çalışır).
 *  - İkonlar ve diğer dosyalar önbellekten hızlıca verilir, arka planda yenilenir.
 */
const CACHE = 'cakar-sim-v6';
const ASSETS = ['./', './index.html', './manifest.webmanifest', './icon.svg', './icon-192.png', './icon-512.png'];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(ASSETS)).then(() => self.skipWaiting()));
});

// Eski sürüm önbelleklerini sil ve açık sayfaların kontrolünü hemen al
self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', e => {
  const req = e.request;
  if (req.method !== 'GET' || new URL(req.url).origin !== location.origin) return;
  const isPage = req.mode === 'navigate' || req.destination === 'document' || req.url.endsWith('.html');

  if (isPage){
    // Önce internet; başarısızsa önbellek
    e.respondWith(
      fetch(req, { cache: 'no-store' })
        .then(res => { if (res.ok){ const copy = res.clone(); caches.open(CACHE).then(c => c.put('./index.html', copy)); } return res; })
        .catch(() => caches.match(req, { ignoreSearch: true }).then(hit => hit || caches.match('./index.html')))
    );
    return;
  }
  // Diğer dosyalar: önbellekten hızlı ver, arka planda güncelle
  e.respondWith(
    caches.match(req, { ignoreSearch: true }).then(hit => {
      const net = fetch(req).then(res => { if (res.ok){ const copy = res.clone(); caches.open(CACHE).then(c => c.put(req, copy)); } return res; });
      return hit || net;
    })
  );
});
