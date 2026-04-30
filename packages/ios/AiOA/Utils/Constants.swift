import Foundation
import UIKit

enum Constants {
    // MARK: - API

    enum API {
        static let baseURL = "https://api.example.com/v1"
        static let timeout: TimeInterval = 30
        static let maxRetries = 3
    }

    // MARK: - Keychain

    enum Keychain {
        static let authToken = "auth_token"
        static let refreshToken = "refresh_token"
        static let userId = "user_id"
    }

    // MARK: - UserDefaults

    enum UserDefaultsKeys {
        static let isFirstLaunch = "isFirstLaunch"
        static let lastSyncDate = "lastSyncDate"
        static let userProfile = "userProfile"
        static let appSettings = "appSettings"
    }

    // MARK: - UI

    enum UI {
        static let animationDuration: TimeInterval = 0.3
        static let cornerRadius: CGFloat = 12
        static let shadowRadius: CGFloat = 4
        static let borderWidth: CGFloat = 1
    }

    // MARK: - App Info

    enum App {
        static let name = "AI-OA"
        static let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
        static let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "1"
        static let bundleId = Bundle.main.bundleIdentifier ?? "com.company.ai-oa"
    }

    // MARK: - Feature Flags

    enum FeatureFlags {
        static let enableAIAssistant = true
        static let enableFaceID = false
        static let enableDarkMode = true
    }
}
