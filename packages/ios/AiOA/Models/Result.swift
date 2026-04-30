import Foundation

enum APIResult<T: Codable> {
    case success(T)
    case failure(APIError)
}

enum APIError: Error, LocalizedError, Codable {
    case invalidURL
    case networkError(underlying: String)
    case decodingError(underlying: String)
    case serverError(statusCode: Int, message: String?)
    case unauthorized
    case unknown

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid URL"
        case .networkError(let underlying):
            return "Network error: \(underlying)"
        case .decodingError(let underlying):
            return "Decoding error: \(underlying)"
        case .serverError(let statusCode, let message):
            return "Server error (\(statusCode)): \(message ?? "Unknown")"
        case .unauthorized:
            return "Unauthorized. Please login again."
        case .unknown:
            return "Unknown error occurred"
        }
    }
}

struct APIResponse<T: Codable>: Codable {
    let code: Int
    let message: String?
    let data: T?
}

struct EmptyResponse: Codable {}
