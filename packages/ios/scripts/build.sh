#!/bin/bash

set -e

echo "=========================================="
echo "  AI-OA iOS Build Script"
echo "=========================================="

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCHEME_NAME="AiOA"
CONFIGURATION="Debug"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

cd "$PROJECT_DIR"

# Check XcodeGen
if ! command -v xcodegen &> /dev/null; then
    log_error "XcodeGen not found. Installing..."
    brew install xcodegen
fi

# Check CocoaPods
if ! command -v pod &> /dev/null; then
    log_error "CocoaPods not found. Installing..."
    brew install cocoapods
fi

# Step 1: Generate Xcode project
log_info "Generating Xcode project with XcodeGen..."
xcodegen generate

# Step 2: Install CocoaPods
log_info "Installing CocoaPods dependencies..."
pod install

# Step 3: Check available simulators
log_info "Available iOS simulators:"
xcrun simctl list devices available | grep -E "iPhone|iPad" | head -10

# Step 4: Build
SIMULATOR=${1:-"iPhone 15"}
log_info "Building for simulator: $SIMULATOR..."

xcodebuild -workspace AiOA.xcworkspace \
    -scheme "$SCHEME_NAME" \
    -configuration "$CONFIGURATION" \
    -destination "platform=iOS Simulator,name=$SIMULATOR" \
    -derivedDataPath ./build \
    build 2>&1 | tail -50

# Check result
if [ ${PIPESTATUS[0]} -eq 0 ]; then
    log_info "Build successful!"
else
    log_error "Build failed!"
    exit 1
fi

echo ""
log_info "To open the project, run:"
echo "  open AiOA.xcworkspace"
