# JavaScript köprüsü (CakarApp) metotları küçültme sırasında silinmesin
-keepclassmembers class com.cakarsiren.simulator.MainActivity$Bridge {
    @android.webkit.JavascriptInterface <methods>;
}
