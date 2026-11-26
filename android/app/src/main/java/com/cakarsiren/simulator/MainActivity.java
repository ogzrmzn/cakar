package com.cakarsiren.simulator;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Insets;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.window.OnBackInvokedDispatcher;

import androidx.webkit.WebViewAssetLoader;

/**
 * Çakar & Siren Simülatörü — Android kabuğu.
 *
 * Uygulamanın tamamı (HTML/CSS/JS) paketin içindeki assets/www klasöründedir.
 * WebViewAssetLoader bu dosyaları güvenli bir https adresinden sunar; böylece
 * hosting ya da internet bağlantısı gerekmez ve localStorage, Web Audio vb. sorunsuz çalışır.
 *
 * Yerel olarak yapılanlar:
 *  - Sürükleyici (immersive) tam ekran: durum ve gezinme çubukları gizlenir.
 *  - Ekranı açık tutma: sayfa "CakarApp.keepScreenOn(true/false)" ile ister.
 *  - Geri tuşu: sayfa geçmişinde geri gider (çakar ekranından ana ekrana döner).
 *  - Ses tuşları medya sesini kontrol eder.
 *  - Ekran yönü: varsayılan olarak telefon döndürme kilidi açık olmasa da döner; sayfa yatay/dikey kilitleyebilir.
 */
public class MainActivity extends Activity {

    private static final String HOST = "appassets.androidplatform.net";
    private static final String START_URL = "https://" + HOST + "/assets/www/index.html";

    private WebView web;
    private WebViewAssetLoader assetLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ses tuşları siren sesini (medya akışını) ayarlasın
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Çentikli ekranlarda çentiğin olduğu kenarı da kullan (içerik aşağıda içe kaydırılır)
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        root.addView(web, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        // Kamera çentiği / delik ekran kontrollerin üstüne binmesin: o kenar kadar boşluk bırak
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int l = 0, t = 0, r = 0, b = 0;
            if (Build.VERSION.SDK_INT >= 30) {
                Insets c = insets.getInsets(WindowInsets.Type.displayCutout());
                l = c.left; t = c.top; r = c.right; b = c.bottom;
            } else if (Build.VERSION.SDK_INT >= 28) {
                DisplayCutout dc = insets.getDisplayCutout();
                if (dc != null) {
                    l = dc.getSafeInsetLeft(); t = dc.getSafeInsetTop();
                    r = dc.getSafeInsetRight(); b = dc.getSafeInsetBottom();
                }
            }
            v.setPadding(l, t, r, b);
            return insets;
        });

        // assets/ klasörünü https://appassets.androidplatform.net/assets/ altında sun
        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);                    // ayarlar (localStorage) hatırlansın
        s.setMediaPlaybackRequiresUserGesture(false);    // Web Audio sesleri sorunsuz başlasın
        s.setAllowFileAccess(false);                     // dosya sistemine erişim yok (güvenlik)
        s.setAllowContentAccess(false);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (HOST.equals(uri.getHost())) return false;     // kendi sayfalarımız: WebView'de aç
                try {                                             // dış bağlantılar: tarayıcıda aç
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException ignored) { }
                return true;
            }
        });

        // Sayfadan çağrılabilen yerel köprü: window.CakarApp
        web.addJavascriptInterface(new Bridge(), "CakarApp");

        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        if (web.getUrl() == null) web.loadUrl(START_URL);

        // Android 13+ geri hareketi (Android 16'da eski onBackPressed artık çağrılmıyor)
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::handleBack);
        }

        hideSystemBars();
    }

    /** Geri: sayfa geçmişi varsa (çakar ekranı) ana ekrana dön, yoksa uygulamayı kapat. */
    private void handleBack() {
        if (web != null && web.canGoBack()) web.goBack();
        else finish();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {   // Android 12 ve öncesi
        handleBack();
    }

    /** Durum ve gezinme çubuklarını gizle; kenardan kaydırınca geçici olarak görünsünler. */
    @SuppressWarnings("deprecation")
    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    // Uygulama arka plana geçince sayfa da duraklar (ışık ve ses durur), dönünce devam eder
    @Override
    protected void onPause() {
        super.onPause();
        if (web != null) { web.onPause(); web.pauseTimers(); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) { web.resumeTimers(); web.onResume(); }
        hideSystemBars();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (web != null) web.saveState(outState);
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.removeJavascriptInterface("CakarApp");
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }

    /** JavaScript köprüsü. Yalnızca paketin içindeki yerel sayfa yüklendiği için güvenlidir. */
    class Bridge {
        /** Ekran yönü: "auto" (telefon kilitli olsa da döner), "landscape" (yatay), "portrait" (dikey). */
        @JavascriptInterface
        public void setOrientation(String mode) {
            runOnUiThread(() -> {
                if ("landscape".equals(mode)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                else if ("portrait".equals(mode)) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            });
        }

        @JavascriptInterface
        public void keepScreenOn(boolean on) {
            runOnUiThread(() -> {
                if (on) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            });
        }
    }
}
