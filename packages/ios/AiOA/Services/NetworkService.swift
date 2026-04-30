import Foundation
import Combine

protocol NetworkServiceProtocol {
    func fetchAnnouncements() -> AnyPublisher<[Announcement], APIError>
    func fetchCurrentUser() -> AnyPublisher<User, APIError>
}

final class NetworkService: NetworkServiceProtocol {
    static let shared = NetworkService()

    private let apiClient = APIClient.shared

    private init() {}

    func fetchAnnouncements() -> AnyPublisher<[Announcement], APIError> {
        // For demo purposes, return mock data
        return Just(Announcement.mockList)
            .setFailureType(to: APIError.self)
            .eraseToAnyPublisher()

        // Real implementation:
        // return apiClient.request(.announcements())
        //     .map { (result: APIResult<[AnnouncementResponse]>) in ... }
    }

    func fetchCurrentUser() -> AnyPublisher<User, APIError> {
        // For demo purposes, return mock data
        return Just(User.mock)
            .setFailureType(to: APIError.self)
            .eraseToAnyPublisher()

        // Real implementation:
        // return apiClient.request(.currentUser())
        //     .map { (result: APIResult<User>) in ... }
    }
}

// MARK: - Announcement Response (for real API)

struct AnnouncementResponse: Codable {
    let id: String
    let title: String
    let content: String
    let createdAt: Date
}

// MARK: - Network Monitor

final class NetworkMonitor: ObservableObject {
    static let shared = NetworkMonitor()

    @Published var isConnected = true
    @Published var connectionType: ConnectionType = .unknown

    enum ConnectionType {
        case wifi
        case cellular
        case unknown
    }

    private init() {
        // In production, use NWPathMonitor to track network status
        checkConnection()
    }

    func checkConnection() {
        // Simplified check - in production use NWPathMonitor
        isConnected = true
        connectionType = .wifi
    }
}
