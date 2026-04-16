# 🚀 Guide de démarrage rapide

Ce document t'accompagne **pas à pas** pour lancer ton application pour la première fois.

## Étape 1 : Installer Android Studio

1. Va sur https://developer.android.com/studio
2. Télécharge Android Studio (version "Ladybug" ou plus récente)
3. Lance l'installateur et accepte les options par défaut

⏱️ Temps : ~15 minutes (selon ta connexion)

## Étape 2 : Ouvrir le projet

1. Lance Android Studio
2. Sur l'écran d'accueil, clique sur **"Open"** (pas "New Project" !)
3. Navigue jusqu'au dossier `GalerieSaine` (celui qui contient le fichier `settings.gradle.kts`)
4. Clique sur **"OK"**

Android Studio va afficher un message en bas : **"Gradle sync in progress..."**. Il télécharge Gradle et toutes les dépendances. **⏱️ Compte 5 à 10 minutes la première fois.**

### ⚠️ Si Android Studio signale un wrapper Gradle manquant

Le fichier binaire `gradle-wrapper.jar` n'a pas pu être inclus dans ce projet. **Android Studio le régénère automatiquement dans 99% des cas**, mais si tu vois une erreur du type "Could not find or load main class org.gradle.wrapper.GradleWrapperMain" :

**Option A (la plus simple) :** dans Android Studio, clique sur **File → Sync Project with Gradle Files**. Il téléchargera le wrapper automatiquement.

**Option B :** lance le script fourni :
- Sur Mac/Linux : ouvre un terminal dans le dossier et tape `./setup.sh`
- Sur Windows : double-clique sur `setup.bat`

**Option C :** si tu as Gradle installé sur ton système, tape `gradle wrapper` dans le dossier du projet.

## Étape 3 : Accepter les licences Android SDK

La première fois, Android Studio peut te demander d'installer des composants (SDK, platform-tools). Accepte tout.

## Étape 4 : Créer un émulateur

1. En haut à droite, clique sur **l'icône du téléphone** (Device Manager)
2. Clique sur **"+ Create Virtual Device"**
3. Choisis **Pixel 7** (ou n'importe quel téléphone récent)
4. Choisis **API 34 (Android 14)** — télécharge-le si demandé
5. Clique sur **Finish**

## Étape 5 : Lancer l'application

1. En haut de l'écran, tu vois un bouton vert ▶️ **"Run 'app'"**
2. À côté, sélectionne ton émulateur (Pixel 7)
3. Clique sur ▶️

⏱️ La première compilation prend 2-5 minutes.

L'émulateur démarre, puis l'app s'ouvre automatiquement.

## 📸 Ajouter des photos à l'émulateur pour tester

L'émulateur Android n'a pas de photos par défaut. Pour en ajouter :

**Méthode simple :** glisse-dépose des images depuis ton ordinateur vers l'émulateur. Elles atterrissent dans Downloads.

**Méthode alternative :**
1. Dans l'émulateur, ouvre le navigateur
2. Recherche n'importe quelle image
3. Fais un appui long → "Télécharger l'image"

Relance ton app GalerieSaine, les photos apparaîtront !

## 🐛 Problèmes courants

### "Missing Gradle Wrapper JAR" ou erreur Gradle au premier lancement

Le fichier `gradle-wrapper.jar` est absent (il est trop gros pour être inclus dans le zip initial). Android Studio le téléchargera automatiquement, mais si ça ne marche pas :

1. Ouvre un terminal dans le dossier du projet
2. Lance la commande : `gradle wrapper` (si Gradle est installé système)
3. **OU** : dans Android Studio, va dans **File → Sync Project with Gradle Files**

Si tout échoue, le plus simple :
- Dans Android Studio : **File → New → Import Project** (au lieu de Open)
- Android Studio régénérera automatiquement le wrapper

### "SDK not found"

Va dans **File → Project Structure → SDK Location** et vérifie qu'Android SDK est bien installé.

### L'app plante au démarrage

Regarde l'onglet **Logcat** en bas d'Android Studio pour voir l'erreur. Le plus souvent c'est un problème de permissions : vérifie dans les paramètres Android de l'émulateur que l'app a bien l'accès aux photos.

## 💡 Prochaines étapes pour apprendre

Maintenant que tu as une app qui tourne, voici dans quel ordre explorer le code :

1. **`model/Models.kt`** — classes de données Photo et Album (simple et court)
2. **`ui/screens/PermissionScreen.kt`** — ton premier écran Compose
3. **`ui/screens/MainScreen.kt`** — grille de photos et système d'onglets
4. **`data/PhotoRepository.kt`** — comment Android stocke les photos (MediaStore)
5. **`MainActivity.kt`** — comment la navigation fonctionne

## 🎨 Idées de modifications pour apprendre

- Change la couleur du thème dans `ui/theme/Theme.kt`
- Modifie les textes dans `PermissionScreen.kt`
- Ajoute un 4ème onglet "Récentes" qui filtre les photos de cette semaine
- Change le nombre de colonnes de la grille (3 → 4)

Bon développement ! 🎉
