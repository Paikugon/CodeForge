import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Plus, RefreshCw } from "lucide-react";
import { deleteProblem, getProblems } from "../../api/problems";
import { setProblemVisibility } from "../../api/admin";
import Table from "../../components/ui/Table";
import {
  AdminError,
  AdminPagination,
  ConfirmDialog,
  FloatingSelection,
} from "../../components/admin/AdminUi";
import type { Problem } from "../../types/problem";

export default function AdminProblemsPage() {
  const { t } = useTranslation("admin");
  const client = useQueryClient();
  const [search, setSearch] = useSearchParams();
  const rawPage = Number(search.get("page") ?? 0);
  const page =
    Number.isSafeInteger(rawPage) && rawPage >= 0 && rawPage <= 1_000_000
      ? rawPage
      : 0;
  const difficultyValue = search.get("difficulty");
  const difficulty = ["EASY", "MEDIUM", "HARD"].includes(difficultyValue ?? "")
    ? (difficultyValue as Problem["difficulty"])
    : undefined;
  const enabled =
    search.get("enabled") === "true"
      ? true
      : search.get("enabled") === "false"
        ? false
        : undefined;
  const [selected, setSelected] = useState<number[]>([]);
  const [deleting, setDeleting] = useState<Problem | null>(null);
  const [saved, setSaved] = useState(false);
  const query = useQuery({
    queryKey: ["admin", "problems", { page, difficulty, enabled }],
    queryFn: ({ signal }) =>
      getProblems({ page, size: 20, difficulty, enabled }, signal),
    retry: false,
    staleTime: 30_000,
    refetchOnWindowFocus: false,
  });
  useEffect(() => {
    setSelected([]);
  }, [page, difficulty, enabled]);
  useEffect(() => {
    if (!saved) {
      return;
    }

    const timeout = window.setTimeout(() => setSaved(false), 5_000);
    return () => window.clearTimeout(timeout);
  }, [saved]);
  const refresh = async () => {
    setSelected([]);
    await Promise.all([
      client.invalidateQueries({ queryKey: ["admin"] }),
      client.invalidateQueries({ queryKey: ["problems"] }),
      client.invalidateQueries({ queryKey: ["problem"] }),
      client.invalidateQueries({ queryKey: ["profile"] }),
    ]);
  };
  const visibility = useMutation({
    mutationFn: ({ ids, enabled }: { ids: number[]; enabled: boolean }) =>
      setProblemVisibility(ids, enabled),
    retry: false,
    onSuccess: async () => {
      setSaved(true);
      await refresh();
    },
  });
  const remove = useMutation({
    mutationFn: deleteProblem,
    retry: false,
    onSuccess: async () => {
      setDeleting(null);
      setSaved(true);
      await refresh();
    },
  });
  const pending = visibility.isPending || remove.isPending;
  const rows = query.data?.content ?? [];

  return (
    <section
      className="live-admin admin-selectable"
      aria-labelledby="admin-title"
    >
      <div className="admin-heading">
        <h1 id="admin-title">{t("title")}</h1>
        <div className="admin-actions">
          <button
            className="button"
            type="button"
            disabled={pending || query.isFetching}
            onClick={() => void query.refetch()}
          >
            <RefreshCw size={15} />
            {t("refresh")}
          </button>
          <Link to="/admin/problems/new" className="button primary">
            <Plus size={16} />
            {t("newProblem")}
          </Link>
        </div>
      </div>
      <form
        key={`${difficulty}:${enabled}`}
        className="admin-filters"
        onSubmit={(event) => {
          event.preventDefault();
          const values = new FormData(event.currentTarget);
          const next = new URLSearchParams();
          for (const key of ["difficulty", "enabled"]) {
            const value = String(values.get(key) ?? "");
            if (value) next.set(key, value);
          }
          setSearch(next);
        }}
      >
        <label>
          {t("difficulty")}
          <select name="difficulty" defaultValue={difficulty ?? ""}>
            <option value="">{t("all")}</option>
            {["EASY", "MEDIUM", "HARD"].map((value) => (
              <option key={value} value={value}>
                {t(value.toLowerCase())}
              </option>
            ))}
          </select>
        </label>
        <label>
          {t("visibility")}
          <select
            name="enabled"
            defaultValue={enabled === undefined ? "" : String(enabled)}
          >
            <option value="">{t("all")}</option>
            <option value="true">{t("enabled")}</option>
            <option value="false">{t("disabled")}</option>
          </select>
        </label>
        <button className="button primary" type="submit" disabled={pending}>
          {t("apply")}
        </button>
      </form>
      {saved && (
        <div className="toast" role="status" aria-live="polite">
          {t("saved")}
        </div>
      )}
      {query.isError && (
        <AdminError error={query.error} retry={() => void query.refetch()} />
      )}
      {visibility.isError && <AdminError error={visibility.error} />}
      {remove.isError && !deleting && <AdminError error={remove.error} />}
      <FloatingSelection
        count={selected.length}
        disabled={pending}
        onClear={() => setSelected([])}
      >
        <button
          type="button"
          className="button"
          disabled={pending}
          onClick={() => visibility.mutate({ ids: selected, enabled: true })}
        >
          {t("show")}
        </button>
        <button
          type="button"
          className="button"
          disabled={pending}
          onClick={() => visibility.mutate({ ids: selected, enabled: false })}
        >
          {t("hide")}
        </button>
      </FloatingSelection>
      {query.isPending ? (
        <p role="status">{t("loading")}</p>
      ) : (
        query.data && (
          <Table
            label={t("title")}
            className="admin-problem-table"
            footer={
              <AdminPagination
                data={query.data}
                page={page}
                disabled={pending || query.isFetching}
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
                <th>
                  <input
                    type="checkbox"
                    aria-label={t("selectAll")}
                    disabled={!rows.length || pending || query.isFetching}
                    checked={
                      rows.length > 0 &&
                      rows.every((row) => selected.includes(row.id))
                    }
                    onChange={(event) =>
                      setSelected(
                        event.target.checked ? rows.map((row) => row.id) : [],
                      )
                    }
                  />
                </th>
                <th scope="col">{t("id")}</th>
                <th scope="col">{t("name")}</th>
                <th scope="col">{t("createdBy")}</th>
                <th scope="col">{t("difficulty")}</th>
                <th scope="col">{t("visibility")}</th>
                <th scope="col">{t("actions")}</th>
              </tr>
            </thead>
            <tbody>
              {!rows.length ? (
                <tr>
                  <td colSpan={7} className="admin-empty">
                    {t("empty")}
                  </td>
                </tr>
              ) : (
                rows.map((problem) => (
                  <tr key={problem.id}>
                    <td>
                      <input
                        type="checkbox"
                        aria-label={t("selectRow", { id: problem.id })}
                        disabled={pending || query.isFetching}
                        checked={selected.includes(problem.id)}
                        onChange={(event) =>
                          setSelected((values) =>
                            event.target.checked
                              ? [...values, problem.id]
                              : values.filter((id) => id !== problem.id),
                          )
                        }
                      />
                    </td>
                    <td>#{problem.id}</td>
                    <td>
                      <Link
                        className="admin-title-link"
                        to={`/admin/problems/${problem.id}`}
                      >
                        {problem.title}
                      </Link>
                      <small>{problem.slug}</small>
                    </td>
                    <td>
                      {problem.createdById === null ? (
                        "-"
                      ) : (
                        <Link to={`/users/${problem.createdById}`}>
                          {problem.createdBy}
                        </Link>
                      )}
                    </td>
                    <td>
                      <span
                        className={`badge ${problem.difficulty.toLowerCase()}`}
                      >
                        {t(problem.difficulty.toLowerCase())}
                      </span>
                    </td>
                    <td>
                      <span
                        className={`badge ${problem.enabled ? "active" : "disabled"}`}
                      >
                        {t(problem.enabled ? "enabled" : "disabled")}
                      </span>
                    </td>
                    <td>
                      <div className="admin-actions">
                        <Link
                          to={`/admin/problems/${problem.id}`}
                          className="button"
                        >
                          {t("edit")}
                        </Link>
                        <Link
                          to={`/admin/problems/${problem.id}/test-cases`}
                          className="button"
                        >
                          {t("testCases")}
                        </Link>
                        <button
                          type="button"
                          className="button danger"
                          disabled={pending}
                          onClick={() => {
                            remove.reset();
                            setDeleting(problem);
                          }}
                        >
                          {t("remove")}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </Table>
        )
      )}
      {deleting && (
        <ConfirmDialog
          title={t("removeProblem", { id: deleting.id })}
          pending={remove.isPending}
          onConfirm={() => remove.mutate(deleting.id)}
          onCancel={() => setDeleting(null)}
        >
          <p>
            <strong>{deleting.title}</strong>
          </p>
          <p>{t("removeProblemNote")}</p>
          {remove.isError && <AdminError error={remove.error} />}
        </ConfirmDialog>
      )}
    </section>
  );
}
