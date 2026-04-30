import Foundation

struct User: Codable, Identifiable {
    let id: String
    let username: String
    let email: String
    let displayName: String
    let avatarURL: String?
    let role: UserRole
    let department: String?
    let createdAt: Date?

    enum UserRole: String, Codable {
        case admin
        case member
        case guest
    }
}

extension User {
    static let mock = User(
        id: "user_001",
        username: "johndoe",
        email: "johndoe@company.com",
        displayName: "John Doe",
        avatarURL: nil,
        role: .member,
        department: "Engineering",
        createdAt: Date()
    )
}
