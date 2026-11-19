import { ApiError } from "../api/http";
import type { RegisterRequest } from "../types/auth";

export type AccountField = "username" | "email" | "password" | "confirmPassword" | "currentPassword" | "newPassword";
export type AccountErrors = Partial<Record<AccountField, string>>;

export function passwordError(password: string): string | undefined {
  if (!password.trim() || password.length < 8 || password.length > 72) return "validation.passwordLength";
  if (new TextEncoder().encode(password).length > 72) return "validation.passwordBytes";
}

export function validateRegistration(request: RegisterRequest, confirmation: string): AccountErrors {
  const errors: AccountErrors = {};
  if (request.username.trim().length < 3 || request.username.trim().length > 50) errors.username = "validation.usernameLength";
  if (request.email.length > 255 || !/^[^\s@]+@[^\s@]+$/.test(request.email)) errors.email = "validation.email";
  errors.password = passwordError(request.password);
  if (!confirmation || request.password !== confirmation) errors.confirmPassword = "validation.confirmPassword";
  return errors;
}

export function validatePasswordChange(current: string, next: string, confirmation: string): AccountErrors {
  const errors: AccountErrors = { newPassword: passwordError(next) };
  if (!current) errors.currentPassword = "validation.required";
  if (current === next) errors.newPassword = "errors.passwordSame";
  if (!confirmation || next !== confirmation) errors.confirmPassword = "validation.confirmPassword";
  return errors;
}

export function hasAccountErrors(errors: AccountErrors): boolean {
  return Object.values(errors).some(Boolean);
}

export function accountErrorKey(error: unknown, fallback: string): string {
  if (!(error instanceof ApiError)) return fallback;
  const code = error.payload?.code ?? error.payload?.message ?? "";
  const messages: Record<string, string> = {
    CODE_USERNAME_ALREADY_EXISTS: "errors.usernameTaken",
    CODE_EMAIL_ALREADY_EXISTS: "errors.emailTaken",
    CODE_USERNAME_OR_EMAIL_ALREADY_EXISTS: "errors.accountTaken",
    CODE_PASSWORD_BCRYPT_LIMIT: "validation.passwordBytes",
    CODE_PASSWORD_LENGTH_INVALID: "validation.passwordLength",
    CODE_CURRENT_PASSWORD_INCORRECT: "errors.currentPassword",
    CODE_NEW_PASSWORD_MUST_DIFFER: "errors.passwordSame",
    CODE_ACCOUNT_BANNED: "errors.disabled",
    CODE_ACCOUNT_DISABLED: "errors.disabled",
    CODE_REQUEST_VALIDATION_FAILED: "errors.invalidFields",
  };
  return messages[code] ?? (error.status === 401 ? "errors.sessionExpired" : fallback);
}

export function safeAccountReturnTo(value: unknown, fallback = "/profile"): string {
  if (typeof value !== "string") return fallback;
  const path = value.split(/[?#]/)[0];
  return ["/problems", "/submissions", "/profile", "/users", "/admin"].some(
    root => path === root || path.startsWith(`${root}/`),
  ) ? value : fallback;
}
