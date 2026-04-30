import SwiftUI

struct LoginView: View {
    @EnvironmentObject var authManager: AuthManager
    @State private var username = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: Theme.Spacing.large) {
                    // Logo & Title
                    VStack(spacing: Theme.Spacing.medium) {
                        Image(systemName: "building.2.crop.circle.fill")
                            .font(.system(size: 80))
                            .foregroundColor(Theme.Colors.primary)

                        Text("AI-OA")
                            .font(Theme.Typography.largeTitle)
                            .foregroundColor(Theme.Colors.textPrimary)

                        Text("智能办公平台")
                            .font(Theme.Typography.subheadline)
                            .foregroundColor(Theme.Colors.textSecondary)
                    }
                    .padding(.top, 60)

                    // Login Form
                    VStack(spacing: Theme.Spacing.medium) {
                        TextField("用户名 / 邮箱", text: $username)
                            .textFieldStyle(LoginTextFieldStyle())
                            .textContentType(.username)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)

                        SecureField("密码", text: $password)
                            .textFieldStyle(LoginTextFieldStyle())
                            .textContentType(.password)

                        if let error = errorMessage {
                            Text(error)
                                .font(Theme.Typography.caption)
                                .foregroundColor(.red)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        Button(action: login) {
                            HStack {
                                if isLoading {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                        .scaleEffect(0.8)
                                }
                                Text(isLoading ? "登录中..." : "登录")
                            }
                            .frame(maxWidth: .infinity)
                            .padding(Theme.Spacing.medium)
                            .background(isFormValid ? Theme.Colors.primary : Theme.Colors.primary.opacity(0.5))
                            .foregroundColor(.white)
                            .cornerRadius(Theme.CornerRadius.small)
                        }
                        .disabled(!isFormValid || isLoading)
                    }
                    .padding(.horizontal, Theme.Spacing.large)

                    // Footer
                    VStack(spacing: Theme.Spacing.small) {
                        Text("登录即表示同意")
                            .font(Theme.Typography.caption)
                            .foregroundColor(Theme.Colors.textSecondary)

                        HStack(spacing: 4) {
                            Button("《用户协议》") {}
                                .font(Theme.Typography.caption)
                            Text("和")
                                .font(Theme.Typography.caption)
                                .foregroundColor(Theme.Colors.textSecondary)
                            Button("《隐私政策》") {}
                                .font(Theme.Typography.caption)
                        }
                        .foregroundColor(Theme.Colors.primary)
                    }
                    .padding(.top, Theme.Spacing.large)

                    Spacer()
                }
            }
            .navigationBarHidden(true)
        }
    }

    private var isFormValid: Bool {
        !username.isEmpty && !password.isEmpty
    }

    private func login() {
        errorMessage = nil
        isLoading = true

        Task {
            do {
                try await authManager.login(username: username, password: password)
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                    isLoading = false
                }
            }
        }
    }
}

struct LoginTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(Theme.Spacing.medium)
            .background(Theme.Colors.cardBackground)
            .cornerRadius(Theme.CornerRadius.small)
            .overlay(
                RoundedRectangle(cornerRadius: Theme.CornerRadius.small)
                    .stroke(Theme.Colors.border, lineWidth: 1)
            )
    }
}

#Preview {
    LoginView()
        .environmentObject(AuthManager.shared)
}
