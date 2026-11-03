import { apiClient } from "../config/api/api";
import type { ApiResponse } from "../types/api/api";
import type { AuthResponse, LoginRequest, UserResponse } from "../types/api/auth";

let refreshPromise: Promise<ApiResponse<AuthResponse>> | null = null;

export const authService = {
  login: (payload: LoginRequest) => apiClient.post<AuthResponse>("/auth/login", payload),

  refresh: () => {
    if (!refreshPromise) {
      refreshPromise = apiClient.post<AuthResponse>("/auth/refresh").finally(() => {
        refreshPromise = null;
      });
    }
    return refreshPromise;
  },

  getCurrentUser: () => apiClient.get<UserResponse>("/auth/me"),

  logout: () => apiClient.post<void>("/auth/logout"),
};
