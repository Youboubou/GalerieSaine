# 📷 GalerieSaine

Une application Android de galerie photo **100% privée**, **sans publicité**, **sans collecte de données**.

![Android](https://img.shields.io/badge/Android-8.0%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1-blue)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

## ✨ Fonctionnalités

- 📷 Visualisation de toutes tes photos
- 🖼️ Plein écran avec zoom (pincement), pan, swipe horizontal entre photos
- 📁 Albums personnels (virtuels) avec photos dans plusieurs albums possibles
- 🗂️ Navigation dans les dossiers du téléphone (DCIM, Screenshots, WhatsApp, etc.)
- ⭐ Favoris
- 📤 Partage vers n'importe quelle application (WhatsApp, Telegram, Gmail, etc.)
- ✅ Sélection multiple pour partage groupé
- 🔄 Support paysage/portrait
- 🎯 Peut être définie comme **galerie par défaut**

## 🛡️ Garanties de confidentialité

- ✅ **Aucune permission Internet** déclarée → l'app est **techniquement incapable** d'envoyer des données à un serveur
- ✅ **Aucune bibliothèque de tracking** (pas de Firebase, Analytics, Crashlytics)
- ✅ **Aucun SDK publicitaire**
- ✅ **Permissions minimales** : uniquement l'accès aux photos
- ✅ **Partage via le système Android natif** — l'app délègue à Android
- ✅ **Sauvegardes cloud désactivées** pour les données de l'app
- ✅ **Code source 100% ouvert** — vérifie toi-même

### 🔎 Où vérifier

- [`app/src/main/AndroidManifest.xml`](app/src/main/AndroidManifest.xml) → pas de `INTERNET`
- [`app/build.gradle.kts`](app/build.gradle.kts) → aucune dépendance de tracking
- [`app/src/main/res/xml/data_extraction_rules.xml`](app/src/main/res/xml/data_extraction_rules.xml) → backup désactivé

## 📥 Installation

### Option 1 — Télécharger l'APK depuis les Releases

1. Va dans l'onglet [**Releases**](../../releases)
2. Télécharge le fichier `app-release.apk` de la dernière version
3. Ouvre-le sur ton téléphone Android et suis les instructions

> ⚠️ Android affichera un avertissement car l'app ne vient pas du Play Store. C'est normal.
> Autorise l'installation depuis cette source quand demandé.

### Option 2 — Compiler depuis le code source

Si tu préfères vérifier le code et compiler toi-même :

```bash
git clone https://github.com/Youboubou/GalerieSaine.git
cd GalerieSaine
```

Puis :
- Ouvre le dossier dans [Android Studio](https://developer.android.com/studio)
- Attends la synchronisation Gradle (5-10 min la première fois)
- Clique sur le bouton ▶️ **Run**

## 🛠️ Stack technique

- **Langage** : Kotlin
- **UI** : Jetpack Compose + Material 3
- **Architecture** : ViewModel + StateFlow
- **Stockage local** : DataStore Preferences
- **Accès aux photos** : MediaStore (API Android officielle)
- **Chargement d'images** : Coil
- **Cible** : Android 8.0+ (API 26)

## 🤝 Contribuer

Les issues et pull requests sont les bienvenues !

## 📄 Licence

MIT — voir [LICENSE](LICENSE)
