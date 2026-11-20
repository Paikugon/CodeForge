import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { changeMyPassword } from "../api/users";
import { ApiError } from "../api/http";
import { useAuth } from "../context/AuthContext";
import {
  accountErrorKey,
  hasAccountErrors,
  validatePasswordChange,
  type AccountErrors,
} from "../utils/account";
import AccountInput from "./AccountInput";

export default function ChangePasswordForm({
  required = false,
}: {
  required?: boolean;
}) {
  const { t } = useTranslation("account");
  const { signOut } = useAuth();
  const navigate = useNavigate();
  const [current, setCurrent] = useState("");
  const [next, setNext] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [errors, setErrors] = useState<AccountErrors>({});
  const mutation = useMutation({
    mutationFn: changeMyPassword,
    retry: false,
    onSuccess: () => {
      setCurrent("");
      setNext("");
      setConfirmation("");
      signOut();
      navigate("/login", {
        replace: true,
        state: { passwordChanged: true, from: "/profile" },
      });
    },
    onError: (error) => {
      if (error instanceof ApiError && error.status === 401) {
        signOut();
        navigate("/login", {
          replace: true,
          state: { sessionExpired: true, from: "/profile?tab=security" },
        });
      }
    },
  });

  return (
    <section
      className="profile-security live-account-form"
      aria-labelledby="password-title"
    >
      <h2 id="password-title">{t("changePassword")}</h2>
      <p>{t("passwordChangeDescription")}</p>
      {required && (
        <p className="account-alert account-alert--required" role="status">
          {t("passwordRequired")}
        </p>
      )}
      <form
        onSubmit={(event) => {
          event.preventDefault();
          if (mutation.isPending) return;
          const nextErrors = validatePasswordChange(
            current,
            next,
            confirmation,
          );
          setErrors(nextErrors);
          if (!hasAccountErrors(nextErrors))
            mutation.mutate({ currentPassword: current, newPassword: next });
        }}
      >
        <AccountInput
          id="change-current"
          name="currentPassword"
          type="password"
          label={t("currentPassword")}
          value={current}
          autoComplete="current-password"
          required
          disabled={mutation.isPending}
          error={errors.currentPassword ? t(errors.currentPassword) : undefined}
          onChange={(event) => {
            setCurrent(event.target.value);
            setErrors({});
            mutation.reset();
          }}
        />
        <AccountInput
          id="change-new"
          name="newPassword"
          type="password"
          label={t("newPassword")}
          value={next}
          autoComplete="new-password"
          required
          minLength={8}
          maxLength={72}
          disabled={mutation.isPending}
          error={errors.newPassword ? t(errors.newPassword) : undefined}
          onChange={(event) => {
            setNext(event.target.value);
            setErrors({});
            mutation.reset();
          }}
        />
        <p className="account-hint">{t("passwordHint")}</p>
        <AccountInput
          id="change-confirmation"
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
            {t(accountErrorKey(mutation.error, "errors.changePassword"))}
          </p>
        )}
        <button
          type="submit"
          className="button primary"
          disabled={mutation.isPending}
        >
          {t(mutation.isPending ? "changingPassword" : "changePassword")}
        </button>
      </form>
    </section>
  );
}
