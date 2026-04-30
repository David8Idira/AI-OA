#!/bin/bash

# AI-OA Android Build Script
# Usage: ./scripts/build.sh [debug|release] [clean]

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

BUILD_TYPE="${1:-debug}"
CLEAN="${2:-}"

echo "=============================================="
echo "  AI-OA Android Build Script"
echo "  Build Type: $BUILD_TYPE"
echo "=============================================="

# Check Java version
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed"
    exit 1
fi

echo "Java version:"
java -version 2>&1 | head -1

# Check Android SDK
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    echo "Warning: ANDROID_HOME or ANDROID_SDK_ROOT is not set"
    echo "SDK location will be auto-detected"
fi

# Clean if requested
if [ "$CLEAN" = "clean" ]; then
    echo ""
    echo "Cleaning build..."
    ./gradlew clean --no-daemon
fi

# Create local.properties if not exists
if [ ! -f "local.properties" ]; then
    echo "sdk.dir=$ANDROID_HOME" > local.properties
fi

# Build
echo ""
echo "Building $BUILD_TYPE APK..."
echo ""

if [ "$BUILD_TYPE" = "release" ]; then
    ./gradlew assembleRelease --no-daemon
    APK_PATH="$PROJECT_DIR/app/build/outputs/apk/release"
else
    ./gradlew assembleDebug --no-daemon
    APK_PATH="$PROJECT_DIR/app/build/outputs/apk/debug"
fi

# Find APK
APK_FILE=$(find "$APK_PATH" -name "*.apk" 2>/dev/null | head -1)

if [ -f "$APK_FILE" ]; then
    APK_SIZE=$(du -h "$APK_FILE" | cut -f1)
    echo ""
    echo "=============================================="
    echo "  Build Successful!"
    echo "  APK: $APK_FILE"
    echo "  Size: $APK_SIZE"
    echo "=============================================="
else
    echo ""
    echo "Build completed but APK not found in $APK_PATH"
    echo "Check build output for details"
    exit 1
fi