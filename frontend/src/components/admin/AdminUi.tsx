import { useEffect, useRef, useState, type ReactNode } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { ApiError } from "../../api/http";
import type { Page } from "../../types/page";
import TablePagination from "../ui/TablePagination";
import { readAdminText } from "../../utils/adminFiles";
import "../../styles/pages/admin.css";

export function AdminError({
  error,
  retry,
}: {
  error: unknown;
  retry?: () => void;
}) {
  const { t } = useTranslation("admin");
  const { signOut } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const api = error instanceof ApiError ? error : undefined;
  const code = api?.payload?.code ?? api?.payload?.message ?? "generic";
  return (
    <div className="admin-error" role="alert">
      <span>
        {api?.status === 401
          ? t("loginRequired")
          : api?.status === 403 && code !== "CODE_PASSWORD_CHANGE_REQUIRED"
            ? t("forbidden")
            : t(`errors.${code}`, {
                defaultValue: t("errors.generic"),
                file: api?.payload?.fieldErrors?.file ?? "",
              })}
      </span>
      {api?.status === 401 ? (
        <button
          className="button"
          type="button"
          onClick={() => {
            signOut();
            navigate("/login", {
              state: { from: location.pathname + location.search },
            });
          }}
        >
          {t("login")}
        </button>
      ) : (
        retry && (
          <button type="button" className="button" onClick={retry}>
            {t("retry")}
          </button>
        )
      )}
    </div>
  );
}
export function AdminPagination<T>({
  data,
  page,
  onChange,
  disabled,
}: {
  data: Page<T>;
  page: number;
  onChange: (page: number) => void;
  disabled?: boolean;
}) {
  const { t } = useTranslation("admin");
  if (page > 0 && data.content.length === 0)
    return (
      <div className="admin-padding">
        <button type="button" className="button" onClick={() => onChange(0)}>
          {t("firstPage")}
        </button>
      </div>
    );
  return (
    <TablePagination
      page={page}
      pageSize={data.size}
      totalPages={data.totalPages}
      totalElements={data.totalElements}
      disabled={disabled}
      onPageChange={onChange}
      labels={{
        label: t("pagination.label"),
        previous: t("pagination.previous"),
        next: t("pagination.next"),
        results: t("pagination.results", {
          from: data.totalElements ? page * data.size + 1 : 0,
          to: Math.min((page + 1) * data.size, data.totalElements),
          total: data.totalElements,
        }),
        page: t("pagination.page", {
          current: page + 1,
          total: data.totalPages,
        }),
      }}
    />
  );
}
export function FloatingSelection({
  count,
  children,
  disabled,
  onClear,
}: {
  count: number;
  children: ReactNode;
  disabled?: boolean;
  onClear: () => void;
}) {
  const { t } = useTranslation("admin");
  if (!count) return null;
  return (
    <aside className="admin-selection" aria-label={t("selectionActions")}>
      <div className="admin-selection-heading">
        <strong role="status" aria-live="polite" aria-atomic="true">
          {t("selected", { count })}
        </strong>
        <button
          type="button"
          className="button"
          disabled={disabled}
          onClick={onClear}
        >
          {t("clear")}
        </button>
      </div>
      <div className="admin-actions">{children}</div>
    </aside>
  );
}
export function ConfirmDialog({
  title,
  children,
  pending,
  onConfirm,
  onCancel,
}: {
  title: string;
  children: ReactNode;
  pending: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  const { t } = useTranslation("admin");
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const dialog = ref.current;
    dialog?.showModal();
    return () => dialog?.close();
  }, []);
  return (
    <dialog
      ref={ref}
      className="admin-confirm"
      aria-labelledby="admin-confirm-title"
      onCancel={(event) => {
        event.preventDefault();
        if (!pending) onCancel();
      }}
    >
      <h2 id="admin-confirm-title">{title}</h2>
      <div>{children}</div>
      <div className="admin-actions">
        <button
          type="button"
          className="button"
          autoFocus
          disabled={pending}
          onClick={onCancel}
        >
          {t("cancel")}
        </button>
        <button
          type="button"
          className="button danger"
          disabled={pending}
          onClick={onConfirm}
        >
          {t("confirm")}
        </button>
      </div>
    </dialog>
  );
}
export function FileTextField({
  id,
  label,
  value,
  onChange,
  maxBytes,
  maxCharacters,
  disabled,
  onBusyChange,
  rows = 7,
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  maxBytes: number;
  maxCharacters: number;
  disabled?: boolean;
  onBusyChange: (busy: boolean) => void;
  rows?: number;
}) {
  const { t } = useTranslation("admin");
  const [error, setError] = useState("");
  const [reading, setReading] = useState(false);
  const large = value.length > 50_000;
  return (
    <div className="admin-field">
      <label htmlFor={id}>{label}</label>
      <textarea
        id={id}
        rows={rows}
        value={large ? value.slice(0, 1000) : value}
        readOnly={large}
        maxLength={maxCharacters}
        disabled={disabled || reading}
        onChange={(event) => onChange(event.target.value)}
        aria-describedby={`${id}-hint`}
      />
      <div className="admin-file-row">
        <label className="admin-file-label">
          {t("chooseFile")}
          <input
            type="file"
            accept=".in,.out,.txt,.md,.cpp,.cc,.c"
            disabled={disabled || reading}
            onChange={async (event) => {
              const file = event.target.files?.[0];
              event.target.value = "";
              if (!file) return;
              setError("");
              setReading(true);
              onBusyChange(true);
              try {
                const content = await readAdminText(file, maxBytes);
                if (content.length > maxCharacters)
                  throw new Error("fileTooLarge");
                onChange(content);
              } catch (cause) {
                setError(
                  cause instanceof Error ? cause.message : "fileEncoding",
                );
              } finally {
                setReading(false);
                onBusyChange(false);
              }
            }}
          />
        </label>
        <span>{t("characters", { count: value.length })}</span>
        <button
          type="button"
          className="button"
          disabled={disabled || reading}
          onClick={() => onChange("")}
        >
          {t("clearContent")}
        </button>
      </div>
      <p id={`${id}-hint`} className="admin-note">
        {t("fileHint")}
      </p>
      {error && (
        <p className="admin-error" role="alert">
          {t(error)}
        </p>
      )}
    </div>
  );
}
