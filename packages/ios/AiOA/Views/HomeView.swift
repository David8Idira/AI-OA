import SwiftUI

struct HomeView: View {
    @EnvironmentObject var authManager: AuthManager
    @State private var selectedTab: Tab = .workbench
    @State private var showProfile = false

    enum Tab: String, CaseIterable {
        case workbench = "工作台"
        case tasks = "任务"
        case messages = "消息"
        case profile = "我的"
    }

    var body: some View {
        TabView(selection: $selectedTab) {
            WorkbenchTab()
                .tabItem {
                    Label(Tab.workbench.rawValue, systemImage: "square.grid.2x2")
                }
                .tag(Tab.workbench)

            TasksTab()
                .tabItem {
                    Label(Tab.tasks.rawValue, systemImage: "checklist")
                }
                .tag(Tab.tasks)

            MessagesTab()
                .tabItem {
                    Label(Tab.messages.rawValue, systemImage: "message")
                }
                .tag(Tab.messages)

            ProfileTab()
                .tabItem {
                    Label(Tab.profile.rawValue, systemImage: "person.circle")
                }
                .tag(Tab.profile)
        }
        .tint(Theme.primaryColor)
    }
}

// MARK: - Workbench Tab

struct WorkbenchTab: View {
    @State private var announcements: [Announcement] = []
    @State private var quickActions: [QuickAction] = QuickAction.defaults
    @State private var isLoading = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: Theme.Spacing.medium) {
                    // Header Card
                    headerCard

                    // Quick Actions
                    quickActionsSection

                    // Announcements
                    announcementsSection
                }
                .padding(Theme.Spacing.medium)
            }
            .navigationTitle("工作台")
            .refreshable { await loadData() }
            .task { await loadData() }
        }
    }

    private var headerCard: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.small) {
            Text("欢迎回来")
                .font(Theme.Typography.title)
                .foregroundColor(Theme.Colors.primary)

            Text(authManager.currentUser?.displayName ?? "用户")
                .font(Theme.Typography.headline)

            HStack {
                statItem(value: "3", label: "待处理")
                Divider().frame(height: 30)
                statItem(value: "12", label: "已完成")
                Divider().frame(height: 30)
                statItem(value: "5", label: "本月")
            }
            .padding(.top, Theme.Spacing.small)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(Theme.Spacing.medium)
        .background(Theme.Colors.cardBackground)
        .cornerRadius(Theme.CornerRadius.medium)
        .shadow(color: Theme.Colors.shadow, radius: 4, x: 0, y: 2)
    }

    private func statItem(value: String, label: String) -> some View {
        VStack(spacing: 4) {
            Text(value)
                .font(Theme.Typography.headline)
                .foregroundColor(Theme.Colors.primary)
            Text(label)
                .font(Theme.Typography.caption)
                .foregroundColor(Theme.Colors.textSecondary)
        }
        .frame(maxWidth: .infinity)
    }

    private var quickActionsSection: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.small) {
            Text("快捷操作")
                .font(Theme.Typography.subheadline)
                .foregroundColor(Theme.Colors.textSecondary)

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: Theme.Spacing.small) {
                ForEach(quickActions) { action in
                    QuickActionButton(action: action)
                }
            }
        }
    }

    private var announcementsSection: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.small) {
            HStack {
                Text("公告")
                    .font(Theme.Typography.subheadline)
                    .foregroundColor(Theme.Colors.textSecondary)

                Spacer()

                NavigationLink("查看全部") {
                    AnnouncementsListView()
                }
                .font(Theme.Typography.caption)
            }

            if announcements.isEmpty {
                Text("暂无公告")
                    .font(Theme.Typography.body)
                    .foregroundColor(Theme.Colors.textSecondary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, Theme.Spacing.large)
            } else {
                ForEach(announcements) { item in
                    AnnouncementRow(announcement: item)
                }
            }
        }
    }

    private func loadData() async {
        isLoading = true
        // Simulate API call
        try? await Task.sleep(nanoseconds: 500_000_000)
        announcements = Announcement.mockList
        isLoading = false
    }
}

struct QuickAction: Identifiable {
    let id = UUID()
    let title: String
    let icon: String
    let color: Color
    let action: () -> Void

    static let defaults: [QuickAction] = [
        QuickAction(title: "发起流程", icon: "plus.circle", color: .blue) {},
        QuickAction(title: "审批", icon: "checkmark.circle", color: .green) {},
        QuickAction(title: "签到", icon: "location.circle", color: .orange) {},
        QuickAction(title: "更多", icon: "ellipsis.circle", color: .gray) {}
    ]
}

struct QuickActionButton: View {
    let action: QuickAction

    var body: some View {
        Button(action: action.action) {
            VStack(spacing: Theme.Spacing.small) {
                Image(systemName: action.icon)
                    .font(.title2)
                    .foregroundColor(action.color)
                Text(action.title)
                    .font(Theme.Typography.caption)
                    .foregroundColor(Theme.Colors.textPrimary)
            }
            .frame(maxWidth: .infinity)
            .padding(Theme.Spacing.medium)
            .background(Theme.Colors.cardBackground)
            .cornerRadius(Theme.CornerRadius.small)
        }
        .buttonStyle(.plain)
    }
}

struct Announcement: Identifiable {
    let id: String
    let title: String
    let content: String
    let date: Date

    static let mockList = [
        Announcement(id: "1", title: "系统升级通知", content: "系统将于本周六进行例行升级...", date: Date()),
        Announcement(id: "2", title: "新功能上线", content: "AI智能助手功能已上线...", date: Date().addingTimeInterval(-86400))
    ]
}

struct AnnouncementRow: View {
    let announcement: Announcement

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(announcement.title)
                .font(Theme.Typography.body)
                .foregroundColor(Theme.Colors.textPrimary)
            Text(announcement.content)
                .font(Theme.Typography.caption)
                .foregroundColor(Theme.Colors.textSecondary)
                .lineLimit(2)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(Theme.Spacing.medium)
        .background(Theme.Colors.cardBackground)
        .cornerRadius(Theme.CornerRadius.small)
    }
}

struct AnnouncementsListView: View {
    var body: some View {
        Text("公告列表")
            .navigationTitle("公告")
    }
}

// MARK: - Tasks Tab

struct TasksTab: View {
    var body: some View {
        NavigationStack {
            Text("任务列表")
                .navigationTitle("任务")
        }
    }
}

// MARK: - Messages Tab

struct MessagesTab: View {
    var body: some View {
        NavigationStack {
            Text("消息列表")
                .navigationTitle("消息")
        }
    }
}

// MARK: - Profile Tab

struct ProfileTab: View {
    @EnvironmentObject var authManager: AuthManager

    var body: some View {
        NavigationStack {
            List {
                Section {
                    HStack(spacing: Theme.Spacing.medium) {
                        Image(systemName: "person.circle.fill")
                            .font(.system(size: 50))
                            .foregroundColor(Theme.Colors.primary)

                        VStack(alignment: .leading) {
                            Text(authManager.currentUser?.displayName ?? "未登录")
                                .font(Theme.Typography.headline)
                            Text(authManager.currentUser?.email ?? "")
                                .font(Theme.Typography.caption)
                                .foregroundColor(Theme.Colors.textSecondary)
                        }
                    }
                    .padding(.vertical, Theme.Spacing.small)
                }

                Section("设置") {
                    NavigationLink("账号安全") { Text("账号安全") }
                    NavigationLink("通知设置") { Text("通知设置") }
                    NavigationLink("通用设置") { Text("通用设置") }
                }

                Section {
                    Button("退出登录") {
                        authManager.logout()
                    }
                    .foregroundColor(.red)
                }
            }
            .navigationTitle("我的")
        }
    }
}

// MARK: - Auth Manager

class AuthManager: ObservableObject {
    static let shared = AuthManager()

    @Published var isAuthenticated: Bool = false
    @Published var currentUser: User?

    private let tokenKey = "auth_token"
    private let userKey = "current_user"

    init() {
        loadStoredAuth()
    }

    func login(username: String, password: String) async throws {
        // Simulate login API call
        try await Task.sleep(nanoseconds: 1_000_000_000)

        await MainActor.run {
            self.currentUser = User.mock
            self.isAuthenticated = true
            saveAuth()
        }
    }

    func logout() {
        currentUser = nil
        isAuthenticated = false
        clearAuth()
    }

    private func loadStoredAuth() {
        if let token = KeychainHelper.shared.read(key: tokenKey),
           !token.isEmpty {
            isAuthenticated = true
            currentUser = User.mock // In real app, fetch user from token
        }
    }

    private func saveAuth() {
        KeychainHelper.shared.save(key: tokenKey, value: "mock_token")
        if let userData = try? JSONEncoder().encode(currentUser) {
            UserDefaults.standard.set(userData, forKey: userKey)
        }
    }

    private func clearAuth() {
        KeychainHelper.shared.delete(key: tokenKey)
        UserDefaults.standard.removeObject(forKey: userKey)
    }
}

// MARK: - Keychain Helper

class KeychainHelper {
    static let shared = KeychainHelper()

    func save(key: String, value: String) {
        guard let data = value.data(using: .utf8) else { return }
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String: data
        ]
        SecItemDelete(query as CFDictionary)
        SecItemAdd(query as CFDictionary, nil)
    }

    func read(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        var result: AnyObject?
        SecItemCopyMatching(query as CFDictionary, &result)
        guard let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func delete(key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}

#Preview {
    HomeView()
        .environmentObject(AuthManager.shared)
}
