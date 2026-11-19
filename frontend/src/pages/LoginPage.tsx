import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ApiError } from "../api/http";
import { useAuth } from "../context/AuthContext";
import { safeAccountReturnTo } from "../utils/account";
import "../styles/pages/auth.css";
import "../styles/pages/account.css";

export default function LoginPage() {
  const { t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();
  const { signIn } = useAuth();

  const [email, setEmail] = useState(() => typeof location.state?.email === "string" ? location.state.email : "");
  const [password, setPassword] = useState("");

  const loginMutation = useMutation({
    mutationFn: signIn,
    retry: false,

    onSuccess: response => {
      navigate(response.mustChangePassword ? "/profile?tab=security" : safeAccountReturnTo(location.state?.from), { replace: true });
    },
  });

  const errorKey =
    loginMutation.error instanceof ApiError &&
    loginMutation.error.status === 401
      ? "auth.invalidCredentials"
      : "auth.loginError";

  return (
    <section className="live-login live-account-form">
      <h1>{t("auth.login")}</h1>
      <p>{t("auth.loginDescription")}</p>
      {location.state?.registered && <p className="account-success" role="status">{t("account:registered")}</p>}
      {location.state?.passwordChanged && <p className="account-success" role="status">{t("account:passwordChanged")}</p>}
      {location.state?.sessionExpired && <p className="account-alert" role="alert">{t("account:errors.sessionExpired")}</p>}

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
      <p className="account-switch">{t("account:noAccount")} <Link to="/register" state={{ from: safeAccountReturnTo(location.state?.from) }}>{t("account:register")}</Link></p>
    </section>
  );
}
