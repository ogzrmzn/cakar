# Çakar & Siren Simülatörü

Film, video ve eğlence için ışık ve siren efektleri. Tek bir HTML dosyası (PWA) ve aynı dosyayı
içinde taşıyan bir Android uygulaması.

> ⚠️ Yalnızca eğlence, oyun ve film çekimi içindir. Trafikte veya araç camında kullanmak yasa dışıdır.
> ⚡ Yanıp sönen ışıklar içerir; ışığa duyarlı epilepsisi olanlar kullanmamalıdır.

## Klasörler

| Yol | İçerik |
|---|---|
| `index.html`, `sw.js`, `manifest.webmanifest`, ikonlar | Web sürümü (tek dosya + çevrimdışı çalışma) |
| `android/` | Android projesi (WebView kabuğu, `assets/www/index.html`) |
| `dist/cakar-siren-1.2.aab` | Play Console'a yüklenecek paket (yükleme anahtarıyla imzalı) |
| `dist/cakar-siren-1.2-test.apk` | Telefona doğrudan kurup denemek için |
| `play-store/` | Mağaza görselleri (ikon, tanıtım görseli, ekran görüntüleri) |

## Telefonda hızlı test

- **Android uygulaması:** `dist/cakar-siren-1.2-test.apk` dosyasını telefona indirip kurun
  ("bilinmeyen kaynaklara izin ver" gerekir).
- **Tarayıcıda:** Depo ayarlarından *Settings → Pages → Branch: main / (root)* açılırsa
  `https://ogzrmzn.github.io/cakar/` adresinden açılır, ana ekrana eklenebilir.

## Son eklenenler

- **12 LED Çakar teması:** Tek sıra 12 LED (solda 6 kırmızı, sağda 6 mavi), diğer temalarla aynı görünüm.
  Kendi **Orijinal** deseni: kırmızı 1–3 + mavi 7–9 birlikte 3 kez çakar → kırmızı 4–6 + mavi 10–12 birlikte 3 kez çakar.
- Ayarlar paneli yarı saydam; tarayıcı tam ekrandan kendiliğinden çıkarsa bir sonraki dokunuşta geri gelir.

## Sürüm 1.2

- **Ayarlar paneli yeniden tasarlandı:** Işık deseni seçimi panelin içine, Siren sesi'nin üstüne taşındı
  (ikonlu kartlar). Hız büyük rakamla gösterilir; hızlı mod ayrı bir kartta; ses seviyesi siren sesinin altında.
  Seçili kartlar temanın renkleriyle vurgulanır. Canlı ekranın altında yalnızca 3 buton kalır.

## Sürüm 1.1 — neler değişti

- **Yeni tasarım:** Ana ekranda tema kartları; temaya dokununca doğrudan tam ekran ışık.
  Canlı ekranda yalnızca 3 buton (Siren, Korna, Ayarlar).
- **Yeni temalar:** *LED Tepe Lambası* (8 modüllü ışık çubuğu, sıralı çakma) ve
  *Beyaz Ortalı* (mavi-kırmızı, ortada çapraz çakan beyazlar). Toplam 7 tema.
- **Yeni desenler:** *Dörtlü* (4'lü hızlı flaş) ve *Akan* (soldan sağa sıralı).
- **Hız:** Normal modda 1–3 flaş/sn. **Hızlı mod** (onaylı) gerçek tepe lambaları gibi 12 flaş/sn'ye kadar çıkar;
  bu modda tüm ekranı aydınlatan efektler kapatılır.
- **Döndürme:** Uygulama telefonun döndürme kilidinden bağımsız döner; canlı ekrandaki
  döndürme butonu Otomatik → Yatay → Dikey arasında geçer. Yatay ekran için ayrı düzenler.
- **Sesler yeniden yazıldı:** Her ses önceden hesaplanmış, kesintisiz bir döngüdür
  (takılma / kayma olmaz). Wail, Yelp, Phaser, Hi-Lo, Uyarı Bip, Parti.
- **Polis kornası:** Elektronik siren "air horn" tonu: kare dalga, saniyede ~85 kez
  450→1350 Hz tarama → kalın, vızıltılı ses. Korna basılıyken siren kısılır.

## Android derleme

```bash
cd android
./gradlew bundleRelease
```

İmzalı paket için proje klasörüne `upload-keystore.jks` ve `keystore.properties` dosyalarını koyun
(bu dosyalar `.gitignore` ile depodan hariç tutulur, **asla yüklemeyin**).
Yeni sürümde `android/app/build.gradle.kts` içindeki `versionCode`'u artırın.
