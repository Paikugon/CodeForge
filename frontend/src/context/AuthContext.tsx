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
import {
  ApiError,
  getAccessToken,
  PASSWORD_CHANGE_REQUIRED_EVENT,
} from "../api/http";
import { getMyProfile } from "../api/users";
import type { LoginRequest, LoginResponse } from "../types/auth";
import type { UserProfile } from "../types/user";

type AuthContextValue = {
  hasToken: boolean;
  user: UserProfile | undefined;
  authLoading: boolean;
  authError: unknown;
  retryAuth: () => void;
  passwordChangeRequired: boolean;
  signIn: (request: LoginRequest) => Promise<LoginResponse>;
  signOut: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [token, setToken] = useState(getAccessToken);
  const [passwordRequired, setPasswordRequired] = useState(false);

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
    setPasswordRequired(false);
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

  useEffect(() => {
    if (!token) return;
    const onPasswordRequired = (event: Event) => {
      const eventToken = (event as CustomEvent<string>).detail;
      if (eventToken !== token || getAccessToken() !== token) return;
      setPasswordRequired(true);
      queryClient.setQueryData<UserProfile>(["auth", "me"], (current) =>
        current ? { ...current, mustChangePassword: true } : current,
      );
      void queryClient.invalidateQueries({
        queryKey: ["auth", "me"],
        exact: true,
      });
    };
    window.addEventListener(PASSWORD_CHANGE_REQUIRED_EVENT, onPasswordRequired);
    return () =>
      window.removeEventListener(
        PASSWORD_CHANGE_REQUIRED_EVENT,
        onPasswordRequired,
      );
  }, [token, queryClient]);

  async function signIn(request: LoginRequest) {
    const response = await login(request);

    queryClient.clear();
    setPasswordRequired(response.mustChangePassword === true);
    setToken(response.accessToken);

    return response;
  }

  return (
    <AuthContext.Provider
      value={{
        hasToken: Boolean(token),
        user: token ? profileQuery.data : undefined,
        authLoading: Boolean(token) && profileQuery.isPending,
        authError: token ? profileQuery.error : null,
        retryAuth: () => {
          void profileQuery.refetch();
        },
        passwordChangeRequired: Boolean(
          token && (passwordRequired || profileQuery.data?.mustChangePassword),
        ),
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
