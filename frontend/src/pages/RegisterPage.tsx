import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { register } from "../api/auth";
import { useAuth } from "../context/AuthContext";
import AccountInput from "../components/AccountInput";
import {
  accountErrorKey,
  hasAccountErrors,
  safeAccountReturnTo,
  validateRegistration,
  type AccountErrors,
} from "../utils/account";
import "../styles/pages/auth.css";
import "../styles/pages/account.css";

export default function RegisterPage() {
  const { t } = useTranslation("account");
  const { hasToken } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [errors, setErrors] = useState<AccountErrors>({});
  const destination = safeAccountReturnTo(location.state?.from);
  const mutation = useMutation({
    mutationFn: register,
    retry: false,
    onSuccess: (account) =>
      navigate("/login", {
        replace: true,
        state: { registered: true, email: account.email, from: destination },
      }),
  });

  if (hasToken) return <Navigate to="/profile" replace />;

  return (
    <section
      className="live-login live-account-form"
      aria-labelledby="register-title"
    >
      <h1 id="register-title">{t("register")}</h1>
      <p>{t("registerDescription")}</p>
      <form
        onSubmit={(event) => {
          event.preventDefault();
          if (mutation.isPending) return;
          const request = {
            username: username.trim(),
            email: email.trim().toLowerCase(),
            password,
          };
          const nextErrors = validateRegistration(request, confirmation);
          setErrors(nextErrors);
          if (!hasAccountErrors(nextErrors)) mutation.mutate(request);
        }}
      >
        <AccountInput
          id="register-username"
          name="username"
          label={t("username")}
          value={username}
          autoComplete="username"
          required
          minLength={3}
          maxLength={50}
          disabled={mutation.isPending}
          error={errors.username ? t(errors.username) : undefined}
          onChange={(event) => {
            setUsername(event.target.value);
            setErrors({});
            mutation.reset();
          }}
        />
        <AccountInput
          id="register-email"
          name="email"
          type="email"
          label={t("email")}
          value={email}
          autoComplete="email"
          required
          maxLength={255}
          disabled={mutation.isPending}
          error={errors.email ? t(errors.email) : undefined}
          onChange={(event) => {
            setEmail(event.target.value);
            setErrors({});
            mutation.reset();
          }}
        />
        <AccountInput
          id="register-password"
          name="password"
          type="password"
          label={t("password")}
          value={password}
          autoComplete="new-password"
          required
          minLength={8}
          maxLength={72}
          disabled={mutation.isPending}
          error={errors.password ? t(errors.password) : undefined}
          onChange={(event) => {
            setPassword(event.target.value);
            setErrors({});
            mutation.reset();
          }}
        />
        <p className="account-hint">{t("passwordHint")}</p>
        <AccountInput
          id="register-confirmation"
          name="confirmPassword"
          type="password"
          label={t("confirmPassword")}
          value={confirmation}
          autoComplete="new-password"
          required
          disabled={mutation.isPending}
          error={errors.confirmPassword ? t(errors.confirmPassword) : undefined}
          onChange={(event) => {
            setConfirmation(event.target.value);
            setErrors({});
            mutation.reset();
          }}
        />
        {mutation.isError && (
          <p className="account-alert" role="alert">
            {t(accountErrorKey(mutation.error, "errors.register"))}
          </p>
        )}
        <button
          type="submit"
          className="button primary"
          disabled={mutation.isPending}
        >
          {t(mutation.isPending ? "registering" : "register")}
        </button>
      </form>
      <p className="account-switch">
        {t("haveAccount")}{" "}
        <Link to="/login" state={{ from: destination }}>
          {t("login")}
        </Link>
      </p>
    </section>
  );
}
