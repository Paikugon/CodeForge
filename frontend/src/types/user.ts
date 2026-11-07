export type UserRole = "USER" | "ADMIN";

export type UserProfile = {
  id: number;
  username: string;
  email: string;
  role: string;
  solvedCount: number;
  acceptedSubmissionCount: number;
  submissionCount: number;
  unsolvedCount: number;
  createdAt: string;
  updatedAt: string;
  mustChangePassword: boolean | null;
};

export type UserListItem = {
  id: number;
  username: string;
  email: string;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
};

export type AdminUpdateUserRequest = {
  role?: UserRole;
  enabled?: boolean;
};

export type AdminResetPasswordRequest = {
  newPassword: string;
};

export type ChangePasswordRequest = {
  currentPassword: string;
  newPassword: string;
};
