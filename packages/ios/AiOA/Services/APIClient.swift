import Foundation
import Alamofire
import Combine

final class APIClient {
    static let shared = APIClient()

    private let session: Session
    private let baseURL: String
    private let decoder: JSONDecoder

    private init() {
        self.baseURL = Constants.API.baseURL

        // Configure session with interceptors
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = Constants.API.timeout
        configuration.timeoutIntervalForResource = Constants.API.timeout

        // Add logging interceptor in debug mode
        let interceptor = AuthInterceptor()

        self.session = Session(configuration: configuration, interceptor: interceptor)

        // Configure decoder
        self.decoder = JSONDecoder()
        self.decoder.keyDecodingStrategy = .convertFromSnakeCase
        self.decoder.dateDecodingStrategy = .iso8601
    }

    // MARK: - Generic Request Methods

    func request<T: Codable>(_ endpoint: Endpoint) -> AnyPublisher<APIResult<T>, Never> {
        let url = baseURL + endpoint.path

        return session.request(
            url,
            method: endpoint.method,
            parameters: endpoint.parameters,
            encoding: endpoint.encoding,
            headers: endpoint.headers
        )
        .validate()
        .publishDecodable(type: APIResponse<T>.self, decoder: decoder)
        .value()
        .map { response in
            if let data = response.data, let result = data as T {
                return .success(result)
            } else if let error = response.data as? T {
                return .success(error)
            }
            return .failure(.unknown)
        }
        .catch { error -> AnyPublisher<APIResult<T>, Never> in
            return .just(.failure(self.mapError(error)))
        }
        .eraseToAnyPublisher()
    }

    func request<T: Codable>(_ endpoint: Endpoint) async -> APIResult<T> {
        let url = baseURL + endpoint.path

        do {
            let response = try await session.request(
                url,
                method: endpoint.method,
                parameters: endpoint.parameters,
                encoding: endpoint.encoding,
                headers: endpoint.headers
            )
            .validate()
            .serializingDecodable(APIResponse<T>.self, decoder: decoder)
            .value

            if let data = response.data {
                return .success(data)
            }
            return .failure(.unknown)
        } catch let error as AFError {
            return .failure(mapError(error))
        } catch {
            return .failure(.networkError(underlying: error.localizedDescription))
        }
    }

    // MARK: - Error Mapping

    private func mapError(_ error: AFError) -> APIError {
        switch error {
        case .invalidURL:
            return .invalidURL
        case .responseValidationFailed(let reason):
            if case .unacceptableStatusCode(let code) = reason {
                if code == 401 {
                    return .unauthorized
                }
                return .serverError(statusCode: code, message: nil)
            }
            return .unknown
        case .responseSerializationFailed:
            return .decodingError(underlying: error.localizedDescription)
        default:
            return .networkError(underlying: error.localizedDescription)
        }
    }

    // MARK: - Convenience Methods

    func get<T: Codable>(_ path: String, parameters: Parameters? = nil) async -> APIResult<T> {
        let endpoint = Endpoint(path: path, method: .get, parameters: parameters)
        return await request(endpoint)
    }

    func post<T: Codable>(_ path: String, parameters: Parameters? = nil) async -> APIResult<T> {
        let endpoint = Endpoint(path: path, method: .post, parameters: parameters)
        return await request(endpoint)
    }

    func put<T: Codable>(_ path: String, parameters: Parameters? = nil) async -> APIResult<T> {
        let endpoint = Endpoint(path: path, method: .put, parameters: parameters)
        return await request(endpoint)
    }

    func delete<T: Codable>(_ path: String, parameters: Parameters? = nil) async -> APIResult<T> {
        let endpoint = Endpoint(path: path, method: .delete, parameters: parameters)
        return await request(endpoint)
    }
}

// MARK: - Endpoint

struct Endpoint {
    let path: String
    let method: HTTPMethod
    var parameters: Parameters?
    var encoding: ParameterEncoding
    var headers: HTTPHeaders?

    init(
        path: String,
        method: HTTPMethod = .get,
        parameters: Parameters? = nil,
        encoding: ParameterEncoding? = nil,
        headers: HTTPHeaders? = nil
    ) {
        self.path = path
        self.method = method
        self.parameters = parameters
        self.encoding = encoding ?? (method == .get ? URLEncoding.default : JSONEncoding.default)
        self.headers = headers
    }
}

// MARK: - Auth Interceptor

final class AuthInterceptor: RequestInterceptor {
    func adapt(_ urlRequest: URLRequest, for session: Session, completion: @escaping (Result<URLRequest, Error>) -> Void) {
        var request = urlRequest

        // Add auth token if available
        if let token = KeychainHelper.shared.read(key: Constants.Keychain.authToken) {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        // Add common headers
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        completion(.success(request))
    }

    func retry(_ request: Request, for session: Session, dueTo error: Error, completion: @escaping (RetryResult) -> Void) {
        guard let response = request.task?.response as? HTTPURLResponse,
              response.statusCode == 401 else {
            completion(.doNotRetry)
            return
        }

        // Handle token refresh here if needed
        completion(.doNotRetry)
    }
}

// MARK: - API Endpoints

extension Endpoint {
    // Auth
    static func login(username: String, password: String) -> Endpoint {
        Endpoint(
            path: "/auth/login",
            method: .post,
            parameters: ["username": username, "password": password]
        )
    }

    static func logout() -> Endpoint {
        Endpoint(path: "/auth/logout", method: .post)
    }

    // User
    static func currentUser() -> Endpoint {
        Endpoint(path: "/user/me")
    }

    static func updateProfile(_ params: Parameters) -> Endpoint {
        Endpoint(path: "/user/profile", method: .put, parameters: params)
    }

    // Announcements
    static func announcements(page: Int = 1, pageSize: Int = 20) -> Endpoint {
        Endpoint(
            path: "/announcements",
            parameters: ["page": page, "pageSize": pageSize]
        )
    }
}
