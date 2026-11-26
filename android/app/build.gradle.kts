import java.util.Properties

plugins {
    id("com.android.application")
}

// İmzalama bilgileri keystore.properties dosyasından okunur (bu dosyayı kimseyle paylaşmayın!)
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.cakarsiren.simulator"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cakarsiren.simulator"   // Play'e ilk yüklemeden sonra DEĞİŞTİRİLEMEZ
        minSdk = 24                                   // Android 7.0 ve üzeri
        targetSdk = 36                                // Play'in güncel hedef API şartı
        versionCode = 2                               // Her yeni yüklemede 1 artırın
        versionName = "1.1"
    }

    signingConfigs {
        create("upload") {
            if (keystoreProps.isNotEmpty()) {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystoreProps.isNotEmpty()) signingConfig = signingConfigs.getByName("upload")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Yerel dosyaları güvenli bir https adresinden sunmak için (WebViewAssetLoader)
    implementation("androidx.webkit:webkit:1.14.0")
}
