import axios from "axios";
import type { AxiosRequestConfig } from "axios";
import type { ApiResponse } from "../types/api/api";

const API_URL = (import.meta.env.VITE_API_URL ?? "http://localhost:8080/api").replace(/\/+$/, "");

const PUBLIC_ENDPOINTS = ["/auth/login", "/auth/register", "/auth/refresh"];

const api = axios.create({
  baseURL: API_URL,
  timeout: 10_000,
  withCredentials: true,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  const isPublic = PUBLIC_ENDPOINTS.some((path) => config.url?.startsWith(path));

  if (token && !isPublic) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isPublic = PUBLIC_ENDPOINTS.some((path) => error.config?.url?.startsWith(path));

    if (error.response?.status === 401 && !isPublic) {
      localStorage.removeItem("accessToken");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  },
);

export const apiClient = {
  get: <T>(url: string, params?: object, config?: AxiosRequestConfig) =>
    api.get<ApiResponse<T>>(url, { ...config, params }).then((res) => res.data),

  post: <T>(url: string, body?: object, config?: AxiosRequestConfig) =>
    api.post<ApiResponse<T>>(url, body, config).then((res) => res.data),

  put: <T>(url: string, body?: object, config?: AxiosRequestConfig) =>
    api.put<ApiResponse<T>>(url, body, config).then((res) => res.data),

  patch: <T>(url: string, body?: object, config?: AxiosRequestConfig) =>
    api.patch<ApiResponse<T>>(url, body, config).then((res) => res.data),

  delete: <T = void>(url: string, config?: AxiosRequestConfig) =>
    api.delete<ApiResponse<T>>(url, config).then((res) => res.data),
};

export default api;
