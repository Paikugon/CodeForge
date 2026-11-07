import { apiRequest, clearAccessToken, setAccessToken } from "./http";
import type {
  AuthUser,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
} from "../types/auth";

export async function register(request: RegisterRequest): Promise<AuthUser> {
  return apiRequest<AuthUser>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(request),
    token: null,
  });
}

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await apiRequest<LoginResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(request),
    token: null,
  });
  setAccessToken(response.accessToken);
  return response;
}

export function logout(): void {
  clearAccessToken();
}
