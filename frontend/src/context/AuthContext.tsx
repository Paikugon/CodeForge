import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { login, logout } from "../api/auth";
import { ApiError, getAccessToken } from "../api/http";
import { getMyProfile } from "../api/users";
import type { LoginRequest, LoginResponse } from "../types/auth";
import type { UserProfile } from "../types/user";

type AuthContextValue = {
  hasToken: boolean;
  user: UserProfile | undefined;
  signIn: (request: LoginRequest) => Promise<LoginResponse>;
  signOut: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [token, setToken] = useState(getAccessToken);

  const profileQuery = useQuery({
    queryKey: ["auth", "me"],
    queryFn: ({ signal }) => getMyProfile(signal),
    enabled: Boolean(token),
    retry: false,
    staleTime: 60_000,
    refetchOnWindowFocus: false,
  });

  const signOut = useCallback(() => {
    logout();
    setToken(null);
    queryClient.clear();
  }, [queryClient]);

  useEffect(() => {
    if (
      token &&
      profileQuery.error instanceof ApiError &&
      profileQuery.error.status === 401
    ) {
      signOut();
    }
  }, [token, profileQuery.error, signOut]);

  async function signIn(request: LoginRequest) {
    const response = await login(request);

    queryClient.clear();
    setToken(response.accessToken);

    return response;
  }

  return (
    <AuthContext.Provider
      value={{
        hasToken: Boolean(token),
        user: token ? profileQuery.data : undefined,
        signIn,
        signOut,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
