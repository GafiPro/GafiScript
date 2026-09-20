#!/usr/bin/env sh
set -eu

GRADLE_VERSION=9.6.1

if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

CACHE_ROOT="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists"
INSTALL_ROOT="$CACHE_ROOT/gafiscript-gradle-$GRADLE_VERSION"
GRADLE_BIN="$INSTALL_ROOT/gradle/bin/gradle"
ARCHIVE="$INSTALL_ROOT/gradle.zip"
EXTRACTED="$INSTALL_ROOT/extracted"

if [ ! -x "$GRADLE_BIN" ]; then
    mkdir -p "$INSTALL_ROOT"
    rm -rf "$EXTRACTED"
    mkdir -p "$EXTRACTED"

    echo "Gradle $GRADLE_VERSION was not found; downloading it..."
    curl -fsSL --retry 3 \
        "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" \
        -o "$ARCHIVE"

    unzip -q "$ARCHIVE" -d "$EXTRACTED"
    rm -rf "$INSTALL_ROOT/gradle"
    mv "$EXTRACTED/gradle-$GRADLE_VERSION" "$INSTALL_ROOT/gradle"

    rm -rf "$EXTRACTED" "$ARCHIVE"
fi

exec "$GRADLE_BIN/bin/gradle" "$@"
