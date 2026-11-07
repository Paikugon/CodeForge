export type LoginRequest = {
  email: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  mustChangePassword: boolean;
};

export type RegisterRequest = {
  username: string;
  email: string;
  password: string;
};

export type AuthUser = {
  id: number;
  username: string;
  email: string;
  role: string;
};
