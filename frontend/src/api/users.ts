import type { Page } from "../types/page";
import type {
  AdminResetPasswordRequest,
  AdminUpdateUserRequest,
  ChangePasswordRequest,
  UserListItem,
  UserProfile,
  UserRole,
} from "../types/user";
import { apiRequest, createQuery } from "./http";

export async function getMyProfile(
  signal?: AbortSignal,
): Promise<UserProfile> {
  return apiRequest<UserProfile>("/api/users/profile/me", { signal });
}

export async function getUserProfile(
  userId: number | string,
  signal?: AbortSignal,
): Promise<UserProfile> {
  return apiRequest<UserProfile>(
    `/api/users/profile/${encodeURIComponent(userId)}`,
    { signal },
  );
}

export type GetUsersParams = {
  role?: UserRole;
  enabled?: boolean;
  page?: number;
  size?: number;
  sort?: string;
};

export async function getUsers(
  params: GetUsersParams = {},
  signal?: AbortSignal,
): Promise<Page<UserListItem>> {
  const { page = 0, size = 20, sort = "createdAt,desc", role, enabled } = params;

  return apiRequest<Page<UserListItem>>(
    `/api/users${createQuery({ page, size, sort, role, enabled })}`,
    { signal },
  );
}

export async function updateUser(
  userId: number | string,
  request: AdminUpdateUserRequest,
): Promise<void> {
  await apiRequest<void>(`/api/users/${encodeURIComponent(userId)}`, {
    method: "PATCH",
    body: JSON.stringify(request),
  });
}

export async function resetUserPassword(
  userId: number | string,
  request: AdminResetPasswordRequest,
): Promise<void> {
  await apiRequest<void>(
    `/api/users/${encodeURIComponent(userId)}/password`,
    {
      method: "PUT",
      body: JSON.stringify(request),
    },
  );
}

export async function changeMyPassword(
  request: ChangePasswordRequest,
): Promise<void> {
  await apiRequest<void>("/api/users/me/password", {
    method: "PUT",
    body: JSON.stringify(request),
  });
}
