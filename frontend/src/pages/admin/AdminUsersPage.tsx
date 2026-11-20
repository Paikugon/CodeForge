import { useEffect, useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { RefreshCw } from "lucide-react";
import { getUsers } from "../../api/users";
import { useAuth } from "../../context/AuthContext";
import type { UserListItem, UserRole } from "../../types/user";
import Table from "../../components/ui/Table";
import { AdminError, AdminPagination } from "../../components/admin/AdminUi";
import AdminUserDialog, {
  type UserDialogMode,
} from "../../components/admin/AdminUserDialog";

export default function AdminUsersPage() {
  const { t, i18n } = useTranslation("adminUsers");
  const { user: currentUser } = useAuth();
  const client = useQueryClient();
  const [search, setSearch] = useSearchParams();
  const rawPage = Number(search.get("page") ?? 0);
  const page =
    Number.isSafeInteger(rawPage) && rawPage >= 0 && rawPage <= 1_000_000
      ? rawPage
      : 0;
  const rawRole = search.get("role");
  const role =
    rawRole === "ADMIN" || rawRole === "PROBLEM_MANAGER" || rawRole === "USER"
      ? (rawRole as UserRole)
      : undefined;
  const enabled =
    search.get("enabled") === "true"
      ? true
      : search.get("enabled") === "false"
        ? false
        : undefined;
  const [dialog, setDialog] = useState<{
    user: UserListItem;
    mode: UserDialogMode;
  } | null>(null);
  const [notice, setNotice] = useState("");
  useEffect(() => {
    if (!notice) {
      return;
    }

    const timeout = window.setTimeout(() => setNotice(""), 5_000);
    return () => window.clearTimeout(timeout);
  }, [notice]);
  const query = useQuery({
    queryKey: ["admin", "users", { page, role, enabled }],
    queryFn: ({ signal }) =>
      getUsers({ page, size: 20, sort: "id,desc", role, enabled }, signal),
    retry: false,
    staleTime: 30_000,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const dateLabel = (value: string) => {
    const date = new Date(value);
    return Number.isNaN(date.getTime())
      ? "—"
      : date.toLocaleDateString(i18n.language === "vi" ? "vi-VN" : "en-GB", {
          day: "2-digit",
          month: "short",
          year: "numeric",
        });
  };
  return (
    <section className="live-admin" aria-labelledby="admin-users-title">
      <div className="admin-heading">
        <div>
          <h1 id="admin-users-title">{t("title")}</h1>
          <p className="admin-note">{t("subtitle")}</p>
        </div>
        <button
          type="button"
          className="button"
          disabled={query.isFetching || Boolean(dialog)}
          onClick={() => {
            setNotice("");
            void query.refetch();
          }}
        >
          <RefreshCw size={15} />
          {t("admin:refresh")}
        </button>
      </div>
      <form
        key={`${role}:${enabled}`}
        className="admin-filters"
        onSubmit={(event) => {
          event.preventDefault();
          if (dialog) return;
          const values = new FormData(event.currentTarget);
          const next = new URLSearchParams();
          for (const key of ["role", "enabled"]) {
            const value = String(values.get(key) ?? "");
            if (value) next.set(key, value);
          }
          setNotice("");
          setSearch(next);
        }}
      >
        <label>
          {t("role")}
          <select
            name="role"
            defaultValue={role ?? ""}
            disabled={Boolean(dialog)}
          >
            <option value="">{t("admin:all")}</option>
            <option value="USER">{t("roles.USER")}</option>
            <option value="PROBLEM_MANAGER">
              {t("roles.PROBLEM_MANAGER")}
            </option>
            <option value="ADMIN">{t("roles.ADMIN")}</option>
          </select>
        </label>
        <label>
          {t("status")}
          <select
            name="enabled"
            defaultValue={enabled === undefined ? "" : String(enabled)}
            disabled={Boolean(dialog)}
          >
            <option value="">{t("admin:all")}</option>
            <option value="true">{t("enabled")}</option>
            <option value="false">{t("disabled")}</option>
          </select>
        </label>
        <button
          type="submit"
          className="button primary"
          disabled={Boolean(dialog)}
        >
          {t("admin:apply")}
        </button>
      </form>
      <p className="admin-note">
        {t("selfNote")}{" "}
        <Link className="admin-title-link" to="/profile?tab=security">
          {t("security")}
        </Link>
      </p>
      {notice && (
        <div className="toast" role="status" aria-live="polite">
          {notice}
        </div>
      )}
      {query.isError && (
        <AdminError error={query.error} retry={() => void query.refetch()} />
      )}
      {query.isPending ? (
        <p role="status">{t("admin:loading")}</p>
      ) : (
        query.data && (
          <Table
            label={t("title")}
            className="admin-users-table"
            footer={
              <AdminPagination
                data={query.data}
                page={page}
                disabled={query.isFetching || Boolean(dialog)}
                onChange={(nextPage) => {
                  const next = new URLSearchParams(search);
                  next.set("page", String(nextPage));
                  setSearch(next);
                }}
              />
            }
          >
            <thead>
              <tr>
                <th scope="col">{t("username")}</th>
                <th scope="col">{t("email")}</th>
                <th scope="col">{t("role")}</th>
                <th scope="col">{t("status")}</th>
                <th scope="col">{t("joined")}</th>
                <th scope="col">{t("admin:actions")}</th>
              </tr>
            </thead>
            <tbody>
              {query.data.content.length ? (
                query.data.content.map((user) => {
                  const self = user.id === currentUser?.id;
                  return (
                    <tr key={user.id}>
                      <td>
                        <Link
                          className="admin-title-link"
                          to={`/users/${user.id}`}
                        >
                          {user.username}
                        </Link>
                        <small>
                          #{user.id}
                          {self ? ` · ${t("you")}` : ""}
                        </small>
                      </td>
                      <td className="admin-user-email">{user.email ?? "—"}</td>
                      <td>
                        <span
                          className={`badge ${user.role === "ADMIN" ? "active" : ""}`}
                        >
                          {user.role
                            ? t(`roles.${user.role}`)
                            : t("unknownRole")}
                        </span>
                      </td>
                      <td>
                        <span
                          className={`badge ${user.enabled ? "active" : "disabled"}`}
                        >
                          {t(user.enabled ? "enabled" : "disabled")}
                        </span>
                      </td>
                      <td>
                        <time dateTime={user.createdAt} title={user.createdAt}>
                          {dateLabel(user.createdAt)}
                        </time>
                      </td>
                      <td>
                        <div className="admin-actions">
                          <button
                            type="button"
                            className="button"
                            disabled={self || !user.role || Boolean(dialog)}
                            title={self ? t("selfAction") : undefined}
                            aria-label={t("editTitle", {
                              username: user.username,
                            })}
                            onClick={() => setDialog({ user, mode: "edit" })}
                          >
                            {t("edit")}
                          </button>
                          <button
                            type="button"
                            className="button"
                            disabled={self || Boolean(dialog)}
                            title={self ? t("selfAction") : undefined}
                            aria-label={t("resetTitle", {
                              username: user.username,
                            })}
                            onClick={() =>
                              setDialog({ user, mode: "password" })
                            }
                          >
                            {t("reset")}
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              ) : (
                <tr>
                  <td colSpan={6} className="admin-empty">
                    {t("empty")}
                  </td>
                </tr>
              )}
            </tbody>
          </Table>
        )
      )}
      {dialog && (
        <AdminUserDialog
          key={`${dialog.mode}:${dialog.user.id}`}
          user={dialog.user}
          mode={dialog.mode}
          onClose={() => setDialog(null)}
          onSaved={async () => {
            setNotice(
              t(dialog.mode === "password" ? "passwordReset" : "updated", {
                username: dialog.user.username,
              }),
            );
            await client.invalidateQueries({ queryKey: ["admin", "users"] });
          }}
        />
      )}
    </section>
  );
}
