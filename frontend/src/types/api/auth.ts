import type { AgencyResponse } from "./agency";

export interface AuthUserResponse {
  id: number;
  fullName: string;
  email: string;
  phoneNumber?: string;
  avatarUrl?: string;
  roles: string[];
  position?: string;
  enabled?: boolean;
  emailVerified?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  tokenType: string;
  accessToken: string;
  expiresInSeconds: number;
  user: AuthUserResponse;
  agency?: AgencyResponse;
}

export interface UserResponse {
  user: AuthUserResponse;
  agency?: AgencyResponse;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface UserRegisterRequest {
  fullName: string;
  email: string;
  password: string;
  phoneNumber: string;
}

export interface AgencyRegisterRequest {
  agencyName: string;
  agencyDescription: string;
  agencyBusinessEmail: string;
  agencyBusinessPhone: string;
  agencyWebsiteUrl: string;
  agencyCity: string;
  agencyAddress: string;
}

export interface AgencyOwnerRegisterRequest {
  owner: UserRegisterRequest;
  agency: AgencyRegisterRequest;
}
