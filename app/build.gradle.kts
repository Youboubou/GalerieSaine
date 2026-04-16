plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.galeriesaine.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.galeriesaine.app"
        minSdk = 26          // Android 8.0 (couvre ~97% des téléphones)
        targetSdk = 35       // Android 15
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            // Minification désactivée pour une release fiable sans configuration
            // ProGuard spécifique. Activable plus tard après tests approfondis.
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

// ╔════════════════════════════════════════════════════════════════╗
// ║  DÉPENDANCES : UNIQUEMENT DES BIBLIOTHÈQUES OFFICIELLES ANDROID ║
// ║  Aucun tracker, aucune analytics, aucune pub, aucun Firebase    ║
// ╚════════════════════════════════════════════════════════════════╝
dependencies {
    // Bibliothèques AndroidX de base (Google, open source)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose (UI moderne Android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation entre les écrans
    implementation(libs.androidx.navigation.compose)

    // ViewModel pour la gestion d'état
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coil - Chargement d'images depuis le stockage local (open source, pas de réseau utilisé ici)
    implementation(libs.coil.compose)

    // DataStore - Stockage local des préférences (favoris)
    implementation(libs.androidx.datastore.preferences)

    // Accompanist Permissions - Gestion simplifiée des permissions
    implementation(libs.accompanist.permissions)

    // Outils de développement
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
