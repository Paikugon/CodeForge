import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link, useLocation, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  CheckCircle2,
  Clock3,
  FileClock,
  Filter,
  LoaderCircle,
  RefreshCw,
} from "lucide-react";
import { getMySubmissions, getSubmissions } from "../api/submissions";
import { ApiError } from "../api/http";
import { useAuth } from "../context/AuthContext";
import Table from "../components/ui/Table";
import TableRow from "../components/ui/TableRow";
import TablePagination from "../components/ui/TablePagination";
import type {
  SubmissionListItem,
  SubmissionVerdict,
} from "../types/submission";
import "../styles/pages/submission-history.css";

const verdicts: SubmissionVerdict[] = [
  "AC",
  "WA",
  "TLE",
  "MLE",
  "RE",
  "CE",
  "SE",
];
const size = 20;

function parseProblemId(value: string): number | undefined {
  const id = Number(value);
  return /^\d+$/.test(value) && Number.isSafeInteger(id) && id > 0
    ? id
    : undefined;
}

function Filters({
  problemId,
  verdict,
  mine,
  onApply,
}: {
  problemId?: number;
  verdict?: SubmissionVerdict;
  mine: boolean;
  onApply: (problemId?: number, verdict?: SubmissionVerdict) => void;
}) {
  const { t } = useTranslation("submissionHistory");
  const [problem, setProblem] = useState(problemId?.toString() ?? "");
  const [result, setResult] = useState(verdict ?? "");
  const [invalid, setInvalid] = useState(false);

  return (
    <form
      className="history-filters"
      onSubmit={(event) => {
        event.preventDefault();
        const value = problem.trim();
        const id = value ? parseProblemId(value) : undefined;
        if (value && id === undefined) {
          setInvalid(true);
          return;
        }
        setInvalid(false);
        onApply(id, mine && result ? (result as SubmissionVerdict) : undefined);
      }}
    >
      <label>
        <span>{t("problemId")}</span>
        <input
          value={problem}
          inputMode="numeric"
          placeholder={t("problemPlaceholder")}
          aria-invalid={invalid}
          aria-describedby={invalid ? "history-filter-error" : undefined}
          onChange={(event) => {
            setProblem(event.target.value);
            setInvalid(false);
          }}
        />
      </label>
      {mine && (
        <label>
          <span>{t("verdict")}</span>
          <select
            value={result}
            onChange={(event) =>
              setResult(event.target.value as SubmissionVerdict | "")
            }
          >
            <option value="">{t("anyVerdict")}</option>
            {verdicts.map((value) => (
              <option key={value} value={value}>
                {value} · {t(`results.${value}`)}
              </option>
            ))}
          </select>
        </label>
      )}
      <button type="submit" className="button primary">
        <Filter size={15} aria-hidden="true" />
        {t("apply")}
      </button>
      <button
        type="button"
        className="button"
        onClick={() => {
          setProblem("");
          setResult("");
          setInvalid(false);
          onApply();
        }}
      >
        {t("reset")}
      </button>
      {invalid && (
        <p
          id="history-filter-error"
          role="alert"
          className="history-filter-error"
        >
          {t("invalidProblem")}
        </p>
      )}
    </form>
  );
}

function ResultBadge({ submission }: { submission: SubmissionListItem }) {
  const { t } = useTranslation("submissionHistory");
  const active =
    submission.status === "PENDING" || submission.status === "JUDGING";
  const result =
    !active && submission.status !== "FAILED" ? submission.verdict : null;
  const className = (result ?? submission.status).toLowerCase();
  return (
    <span className={`badge history-result ${className}`}>
      {active ? (
        <Clock3 size={13} aria-hidden="true" />
      ) : result === "AC" ? (
        <CheckCircle2 size={13} aria-hidden="true" />
      ) : null}
      {result ? t(`results.${result}`) : t(`status.${submission.status}`)}
    </span>
  );
}

export default function SubmissionHistoryPage() {
  const { t, i18n } = useTranslation("submissionHistory");
  const { hasToken, user } = useAuth();
  const location = useLocation();
  const [search, setSearch] = useSearchParams();
  const mine = search.get("scope") === "mine";
  const rawPage = Number(search.get("page") ?? "0");
  const page =
    Number.isSafeInteger(rawPage) && rawPage >= 0 && rawPage <= 1_000_000
      ? rawPage
      : 0;
  const problemId = parseProblemId(search.get("problemId") ?? "");
  const rawVerdict = search.get("verdict") as SubmissionVerdict | null;
  const verdict =
    mine && rawVerdict && verdicts.includes(rawVerdict)
      ? rawVerdict
      : undefined;
  const needsLogin = mine && !hasToken;
  const query = useQuery({
    queryKey: [
      "submission-history",
      {
        scope: mine ? "mine" : "all",
        userId: mine ? user?.id : undefined,
        page,
        problemId,
        verdict,
      },
    ],
    queryFn: ({ signal }) =>
      (mine ? getMySubmissions : getSubmissions)(
        { page, size, sort: "createdAt,desc", problemId, verdict },
        signal,
      ),
    enabled: !needsLogin,
    staleTime: 15_000,
    retry: false,
    refetchOnWindowFocus: false,
  });

  function applyFilters(id?: number, result?: SubmissionVerdict) {
    const next = new URLSearchParams();
    if (mine) next.set("scope", "mine");
    if (id) next.set("problemId", String(id));
    if (mine && result) next.set("verdict", result);
    setSearch(next);
  }

  function tabUrl(own: boolean) {
    const next = new URLSearchParams();
    if (own) next.set("scope", "mine");
    if (problemId) next.set("problemId", String(problemId));
    return `/submissions${next.size ? `?${next}` : ""}`;
  }

  const data = query.data;
  const authenticationError =
    query.error instanceof ApiError && query.error.status === 401;
  const forbidden =
    query.error instanceof ApiError && query.error.status === 403;
  const outOfRange = Boolean(data && page > 0 && data.content.length === 0);
  const hasActive = data?.content.some(
    (item) => item.status === "PENDING" || item.status === "JUDGING",
  );
  const formatter = new Intl.DateTimeFormat(
    i18n.language.startsWith("vi") ? "vi-VN" : "en-GB",
    {
      timeZone: "Asia/Ho_Chi_Minh",
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    },
  );
  const returnTo = location.pathname + location.search;

  return (
    <section
      className="live-submission-history"
      aria-labelledby="history-title"
    >
      <div className="history-heading">
        <div>
          <h1 id="history-title">{t("title")}</h1>
          <p>{t("description")}</p>
        </div>
        <button
          type="button"
          className="button"
          disabled={query.isFetching || needsLogin}
          onClick={() => void query.refetch()}
        >
          <RefreshCw
            size={15}
            className={query.isFetching ? "spin" : undefined}
            aria-hidden="true"
          />
          {t("refresh")}
        </button>
      </div>

      <nav className="history-tabs" aria-label={t("title")}>
        <Link to={tabUrl(false)} aria-current={!mine ? "page" : undefined}>
          {t("all")}
        </Link>
        <Link to={tabUrl(true)} aria-current={mine ? "page" : undefined}>
          {t("mine")}
        </Link>
      </nav>

      {needsLogin || authenticationError ? (
        <div className="history-state">
          <FileClock size={32} aria-hidden="true" />
          <h2>{t("loginTitle")}</h2>
          <p>{t("loginDescription")}</p>
          <Link
            to="/login"
            state={{ from: returnTo }}
            className="button primary"
          >
            {t("login")}
          </Link>
        </div>
      ) : (
        <>
          <Filters
            key={`${mine}:${problemId}:${verdict}`}
            problemId={problemId}
            verdict={verdict}
            mine={mine}
            onApply={applyFilters}
          />
          {!mine && <p className="history-filter-note">{t("allFilterNote")}</p>}
          {query.isError && (
            <div className="history-error" role="alert">
              <span>
                {t(
                  forbidden ? "forbidden" : data ? "updateError" : "loadError",
                )}
              </span>
              {!forbidden && (
                <button
                  type="button"
                  className="button"
                  disabled={query.isFetching}
                  onClick={() => void query.refetch()}
                >
                  {t("retry")}
                </button>
              )}
            </div>
          )}
          {query.isPending ? (
            <div className="history-state" role="status">
              <LoaderCircle size={24} className="spin" aria-hidden="true" />
              <p>{t("loading")}</p>
            </div>
          ) : (
            data &&
            !forbidden && (
              <>
                <div className="history-list-heading">
                  <strong>{t("count", { count: data.totalElements })}</strong>
                  <span>{t("newest")}</span>
                </div>
                <Table
                  label={t("title")}
                  className="history-table"
                  aria-busy={query.isFetching}
                  footer={
                    outOfRange ? (
                      <div className="history-first-page">
                        <button
                          type="button"
                          className="button"
                          onClick={() => {
                            const next = new URLSearchParams(search);
                            next.delete("page");
                            setSearch(next);
                          }}
                        >
                          {t("firstPage")}
                        </button>
                      </div>
                    ) : (
                      <TablePagination
                        page={page}
                        pageSize={size}
                        totalPages={data.totalPages}
                        totalElements={data.totalElements}
                        disabled={query.isFetching}
                        onPageChange={(nextPage) => {
                          const next = new URLSearchParams(search);
                          next.set("page", String(nextPage));
                          setSearch(next);
                        }}
                        labels={{
                          label: t("pagination.label"),
                          previous: t("pagination.previous"),
                          next: t("pagination.next"),
                          results: t("pagination.results", {
                            from: data.totalElements ? page * size + 1 : 0,
                            to: Math.min((page + 1) * size, data.totalElements),
                            total: data.totalElements,
                          }),
                          page: t("pagination.page", {
                            current: page + 1,
                            total: data.totalPages,
                          }),
                        }}
                      />
                    )
                  }
                >
                  <thead>
                    <tr>
                      {(
                        [
                          "id",
                          "problem",
                          "user",
                          "result",
                          "language",
                          "time",
                          "memory",
                          "date",
                        ] as const
                      ).map((key) => (
                        <th scope="col" key={key}>
                          {t(`columns.${key}`)}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {data.content.length === 0 ? (
                      <tr>
                        <td colSpan={8} className="history-empty-cell">
                          <FileClock size={30} aria-hidden="true" />
                          <h2>
                            {t(
                              outOfRange
                                ? "outOfRange"
                                : problemId || verdict
                                  ? "noMatch"
                                  : "empty",
                            )}
                          </h2>
                          {!outOfRange && (
                            <>
                              <p>
                                {t(
                                  problemId || verdict
                                    ? "noMatchDescription"
                                    : "emptyDescription",
                                )}
                              </p>
                              {!problemId && !verdict && (
                                <Link to="/problems" className="button">
                                  {t("browse")}
                                </Link>
                              )}
                            </>
                          )}
                        </td>
                      </tr>
                    ) : (
                      data.content.map((item) => {
                        const language =
                          (
                            {
                              cpp: "C++",
                              java: "Java",
                              python: "Python",
                            } as Record<string, string>
                          )[item.language.toLowerCase()] ?? item.language;
                        const createdAt = new Date(item.createdAt);
                        return (
                          <TableRow key={item.id} className="table-link-row">
                            <td>
                              <Link
                                to={`/submissions/${item.id}`}
                                className="row-link history-id"
                                aria-label={t("open", { id: item.id })}
                              >
                                #{item.id}
                              </Link>
                            </td>
                            <td>
                              <Link
                                to={`/problems/${item.problemId}`}
                                className="row-action history-problem"
                                aria-label={t("problem", { slug: item.slug })}
                              >
                                {item.displaySlug}
                              </Link>
                            </td>
                            <td>
                              {item.userId > 0 ? (
                                <Link
                                  to={`/users/${item.userId}`}
                                  className="history-user row-action"
                                  title={item.username}
                                >
                                  {item.username}
                                </Link>
                              ) : (
                                <span className="history-user">
                                  {item.username ?? "—"}
                                </span>
                              )}
                            </td>
                            <td>
                              <ResultBadge submission={item} />
                            </td>
                            <td>{language}</td>
                            <td className="history-number">
                              {item.executionTimeMs == null
                                ? "—"
                                : `${item.executionTimeMs} ms`}
                            </td>
                            <td className="history-number">
                              {item.memoryUsedKb == null
                                ? "—"
                                : `${(item.memoryUsedKb / 1024).toFixed(1)} MB`}
                            </td>
                            <td className="history-date">
                              {Number.isNaN(createdAt.getTime()) ? (
                                "—"
                              ) : (
                                <time dateTime={item.createdAt}>
                                  {formatter.format(createdAt)}
                                </time>
                              )}
                            </td>
                          </TableRow>
                        );
                      })
                    )}
                  </tbody>
                </Table>
                {hasActive && (
                  <p className="history-filter-note" role="status">
                    {t("activeNote")}
                  </p>
                )}
                <p className="history-timezone">{t("timezone")}</p>
              </>
            )
          )}
        </>
      )}
    </section>
  );
}
