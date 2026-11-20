import { useEffect, useId, useRef, useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { Eye, EyeOff, X } from "lucide-react";
import { resetUserPassword, updateUser } from "../../api/users";
import type {
  AdminUpdateUserRequest,
  UserListItem,
  UserRole,
} from "../../types/user";
import { passwordError } from "../../utils/account";
import { useAuth } from "../../context/AuthContext";
import { AdminError } from "./AdminUi";

export type UserDialogMode = "edit" | "password";

export default function AdminUserDialog({
  user,
  mode,
  onSaved,
  onClose,
}: {
  user: UserListItem;
  mode: UserDialogMode;
  onSaved: () => Promise<void>;
  onClose: () => void;
}) {
  const { t } = useTranslation("adminUsers");
  const { user: currentUser } = useAuth();
  const self = currentUser?.id === user.id;
  const [role, setRole] = useState<UserRole>(user.role ?? "USER");
  const [enabled, setEnabled] = useState(user.enabled);
  const [password, setPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<{
    password?: string;
    confirmation?: string;
    form?: string;
  }>({});
  const dialog = useRef<HTMLDialogElement>(null);
  const heading = useRef<HTMLHeadingElement>(null);
  const prefix = useId();
  const changed = role !== user.role || enabled !== user.enabled;
  const mutation = useMutation({
    mutationFn: async () => {
      if (self) throw new Error("selfAction");
      if (mode === "password")
        return resetUserPassword(user.id, { newPassword: password });
      const request: AdminUpdateUserRequest = {};
      if (role !== user.role) request.role = role;
      if (enabled !== user.enabled) request.enabled = enabled;
      return updateUser(user.id, request);
    },
    retry: false,
    onSuccess: async () => {
      setPassword("");
      setConfirmation("");
      await onSaved();
      onClose();
    },
  });
  const pending = mutation.isPending;
  useEffect(() => {
    const element = dialog.current;
    const previousFocus =
      document.activeElement instanceof HTMLElement
        ? document.activeElement
        : null;
    const overflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    element?.showModal();
    heading.current?.focus();
    return () => {
      element?.close();
      document.body.style.overflow = overflow;
      if (previousFocus?.isConnected) previousFocus.focus();
    };
  }, []);

  return (
    <dialog
      ref={dialog}
      className="live-admin admin-user-dialog"
      aria-modal="true"
      aria-labelledby={`${prefix}-title`}
      aria-busy={pending}
      onCancel={(event) => {
        event.preventDefault();
        if (!pending) onClose();
      }}
    >
      <div className="admin-user-dialog-heading">
        <h2 ref={heading} tabIndex={-1} id={`${prefix}-title`}>
          {t(mode === "password" ? "resetTitle" : "editTitle", {
            username: user.username,
          })}
        </h2>
        <button
          type="button"
          className="button"
          aria-label={t("admin:cancel")}
          disabled={pending}
          onClick={onClose}
        >
          <X size={18} aria-hidden="true" />
        </button>
      </div>
      <form
        className="admin-form"
        noValidate
        onSubmit={(event) => {
          event.preventDefault();
          if (pending || self || (mode === "edit" && !changed)) return;
          if (mode === "password") {
            const next = {
              password: passwordError(password),
              confirmation:
                !confirmation || password !== confirmation
                  ? "validation.confirmPassword"
                  : undefined,
            };
            setErrors(next);
            if (next.password || next.confirmation) {
              document
                .getElementById(
                  `${prefix}-${next.password ? "password" : "confirmation"}`,
                )
                ?.focus();
              return;
            }
          } else if (!["USER", "ADMIN"].includes(role)) {
            setErrors({ form: "unsaved" });
            return;
          }
          setErrors({});
          mutation.mutate();
        }}
      >
        <p className="admin-note">
          {t(mode === "password" ? "resetNote" : "editNote")}
        </p>
        {self && (
          <p role="alert" className="admin-error">
            {t("selfAction")}
          </p>
        )}
        {mode === "edit" ? (
          <div className="admin-form-grid">
            <label>
              {t("role")}
              <select
                value={role}
                disabled={pending || self}
                onChange={(event) => setRole(event.target.value as UserRole)}
              >
                {["USER", "ADMIN"].map((value) => (
                  <option value={value} key={value}>
                    {t(`roles.${value}`)}
                  </option>
                ))}
              </select>
            </label>
            <label>
              {t("status")}
              <select
                value={String(enabled)}
                disabled={pending || self}
                onChange={(event) => setEnabled(event.target.value === "true")}
              >
                <option value="true">{t("enabled")}</option>
                <option value="false">{t("disabled")}</option>
              </select>
            </label>
          </div>
        ) : (
          <>
            <div className="admin-field">
              <label htmlFor={`${prefix}-password`}>{t("password")}</label>
              <input
                id={`${prefix}-password`}
                type={showPassword ? "text" : "password"}
                autoComplete="new-password"
                maxLength={72}
                value={password}
                disabled={pending || self}
                aria-invalid={Boolean(errors.password)}
                aria-describedby={`${prefix}-password-hint${errors.password ? ` ${prefix}-password-error` : ""}`}
                onChange={(event) => setPassword(event.target.value)}
              />
              <p id={`${prefix}-password-hint`} className="admin-note">
                {t("passwordHint")}
              </p>
              {errors.password && (
                <p
                  id={`${prefix}-password-error`}
                  role="alert"
                  className="admin-user-field-error"
                >
                  {t(errors.password, { ns: "account" })}
                </p>
              )}
            </div>
            <div className="admin-field">
              <label htmlFor={`${prefix}-confirmation`}>
                {t("confirmation")}
              </label>
              <input
                id={`${prefix}-confirmation`}
                type={showPassword ? "text" : "password"}
                autoComplete="new-password"
                maxLength={72}
                value={confirmation}
                disabled={pending || self}
                aria-invalid={Boolean(errors.confirmation)}
                aria-describedby={
                  errors.confirmation
                    ? `${prefix}-confirmation-error`
                    : undefined
                }
                onChange={(event) => setConfirmation(event.target.value)}
              />
              {errors.confirmation && (
                <p
                  id={`${prefix}-confirmation-error`}
                  role="alert"
                  className="admin-user-field-error"
                >
                  {t(errors.confirmation, { ns: "account" })}
                </p>
              )}
            </div>
            <button
              type="button"
              className="button admin-user-password-toggle"
              disabled={pending}
              onClick={() => setShowPassword((value) => !value)}
            >
              {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              {t(showPassword ? "hidePassword" : "showPassword")}
            </button>
          </>
        )}
        {errors.form && (
          <p role="alert" className="admin-error">
            {t(errors.form)}
          </p>
        )}
        {mutation.isError && <AdminError error={mutation.error} />}
        <div className="admin-actions admin-user-dialog-actions">
          <button
            type="button"
            className="button"
            disabled={pending}
            onClick={onClose}
          >
            {t("admin:cancel")}
          </button>
          <button
            type="submit"
            className="button primary"
            disabled={pending || self || (mode === "edit" && !changed)}
          >
            {pending
              ? t("admin:saving")
              : t(mode === "password" ? "reset" : "save")}
          </button>
        </div>
      </form>
    </dialog>
  );
}
