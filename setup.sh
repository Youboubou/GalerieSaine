#!/bin/bash
# Script à lancer UNE SEULE FOIS au premier démarrage du projet
# pour télécharger le fichier gradle-wrapper.jar officiel depuis Gradle.org

set -e
cd "$(dirname "$0")"

WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
GRADLE_VERSION="8.10.2"

if [ -f "$WRAPPER_JAR" ] && [ -s "$WRAPPER_JAR" ]; then
    echo "✅ gradle-wrapper.jar existe déjà, rien à faire."
    exit 0
fi

echo "📥 Téléchargement de gradle-wrapper.jar officiel..."

# Télécharge Gradle et extrait le wrapper JAR
TMP=$(mktemp -d)
curl -L "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$TMP/gradle.zip"
unzip -q "$TMP/gradle.zip" -d "$TMP"
cp "$TMP/gradle-${GRADLE_VERSION}/lib/plugins/gradle-wrapper-main-${GRADLE_VERSION}.jar" "$WRAPPER_JAR" 2>/dev/null \
  || find "$TMP" -name "gradle-wrapper*.jar" -exec cp {} "$WRAPPER_JAR" \;

rm -rf "$TMP"

if [ -f "$WRAPPER_JAR" ] && [ -s "$WRAPPER_JAR" ]; then
    echo "✅ gradle-wrapper.jar installé avec succès !"
    echo "Tu peux maintenant ouvrir le projet dans Android Studio."
else
    echo "❌ Échec du téléchargement."
    echo "Alternative : ouvre simplement le projet dans Android Studio,"
    echo "il régénérera automatiquement le wrapper lors du premier Gradle Sync."
    exit 1
fi
