import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ApiError } from "../api/http";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {
  const { t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { signIn } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const loginMutation = useMutation({
    mutationFn: signIn,
    retry: false,

    onSuccess: () => {
      // Xóa dữ liệu cache có thể thuộc tài khoản trước.
      queryClient.clear();

      const from: unknown = location.state?.from;
      const fromPath = typeof from === "string" ? from.split(/[?#]/)[0] : "";

      const destination =
        typeof from === "string" &&
        (fromPath === "/problems" || fromPath.startsWith("/problems/") ||
          fromPath === "/submissions" || fromPath.startsWith("/submissions/"))
          ? from
          : "/problems";

      navigate(destination, { replace: true });
    },
  });

  const errorKey =
    loginMutation.error instanceof ApiError &&
    loginMutation.error.status === 401
      ? "auth.invalidCredentials"
      : "auth.loginError";

  return (
    <section className="live-login">
      <h1>{t("auth.login")}</h1>
      <p>{t("auth.loginDescription")}</p>

      <form
        onSubmit={(event) => {
          event.preventDefault();

          if (
            loginMutation.isPending ||
            !email.trim() ||
            !password
          ) {
            return;
          }

          loginMutation.mutate({
            email: email.trim(),
            password,
          });
        }}
      >
        <label htmlFor="login-email">
          {t("auth.email")}
        </label>

        <input
          id="login-email"
          name="email"
          type="email"
          autoComplete="username"
          required
          disabled={loginMutation.isPending}
          value={email}
          onChange={(event) => setEmail(event.target.value)}
        />

        <label htmlFor="login-password">
          {t("auth.password")}
        </label>

        <input
          id="login-password"
          name="password"
          type="password"
          autoComplete="current-password"
          required
          disabled={loginMutation.isPending}
          value={password}
          onChange={(event) => setPassword(event.target.value)}
        />

        {loginMutation.isError && (
          <p className="live-run-error" role="alert">
            {t(errorKey)}
          </p>
        )}

        <button
          type="submit"
          className="button primary"
          disabled={
            loginMutation.isPending ||
            !email.trim() ||
            !password
          }
        >
          {t(
            loginMutation.isPending
              ? "auth.loggingIn"
              : "auth.login",
          )}
        </button>
      </form>
    </section>
  );
}
