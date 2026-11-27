import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import CodeMirror from "@uiw/react-codemirror";
import { cpp } from "@codemirror/lang-cpp";
import { java } from "@codemirror/lang-java";
import { python } from "@codemirror/lang-python";
import {
  AlertCircle,
  ArrowLeft,
  Check,
  CheckCircle2,
  ClipboardCheck,
  Clock3,
  Code2,
  Copy,
  FileCode2,
  LoaderCircle,
  LockKeyhole,
  MemoryStick,
  RefreshCw,
  XCircle,
} from "lucide-react";
import { getSubmission } from "../api/submissions";
import { getProblemById } from "../api/problems";
import { ApiError } from "../api/http";
import { useTheme } from "../context/ThemeContext";
import type { Submission } from "../types/submission";
import { SkeletonBlock } from "../components/ui/Skeleton";

const languageNames: Record<string, string> = {
  cpp: "C++ 20",
  java: "Java 21",
  python: "Python 3",
};
const languageExtensions = { cpp: cpp(), java: java(), python: python() };

function isActive(status?: Submission["status"]) {
  return status === "PENDING" || status === "JUDGING";
}

function getTone(submission: Submission) {
  if (isActive(submission.status)) return "pending";
  if (submission.status === "FAILED") return "danger";
  if (submission.verdict === "AC") return "success";
  if (["WA", "TLE", "MLE"].includes(submission.verdict ?? "")) return "warning";
  if (submission.verdict) return "danger";
  return "neutral";
}

function CopyCodeButton({ source }: { source: string }) {
  const { t } = useTranslation();
  const [state, setState] = useState<"idle" | "copied" | "error">("idle");

  useEffect(() => {
    if (state !== "copied") return;
    const timer = window.setTimeout(() => setState("idle"), 2000);
    return () => window.clearTimeout(timer);
  }, [state]);

  return (
    <div className="live-copy-control">
      <span role="status" className="live-copy-status">
        {state === "error"
          ? t("submissionDetail.copyFailed")
          : state === "copied"
            ? t("submissionDetail.copied")
            : ""}
      </span>
      <button
        type="button"
        className="icon-button"
        aria-label={t(
          state === "copied"
            ? "submissionDetail.copied"
            : "submissionDetail.copy",
        )}
        title={t("submissionDetail.copy")}
        onClick={async () => {
          try {
            await navigator.clipboard.writeText(source);
            setState("copied");
          } catch {
            setState("error");
          }
        }}
      >
        {state === "copied" ? <Check size={17} /> : <Copy size={17} />}
      </button>
    </div>
  );
}

function SubmissionDetailSkeleton({ label }: { label: string }) {
  return (
    <section
      className="live-submission-detail submission-detail-skeleton"
      role="status"
      aria-label={label}
    >
      <div className="submission-skeleton-heading">
        <SkeletonBlock className="skeleton--heading" />
        <SkeletonBlock className="skeleton--line skeleton--line-short" />
      </div>
      <div className="submission-skeleton-banner">
        <SkeletonBlock className="skeleton--pill" />
        <div>
          <SkeletonBlock className="skeleton--line" />
          <SkeletonBlock className="skeleton--line skeleton--line-short" />
        </div>
      </div>
      <div className="submission-skeleton-metrics">
        {Array.from({ length: 4 }, (_, index) => (
          <div key={index}>
            <SkeletonBlock className="skeleton--line skeleton--line-short" />
            <SkeletonBlock className="submission-skeleton-metric" />
          </div>
        ))}
      </div>
      <div className="submission-skeleton-grid">
        <div className="submission-skeleton-main">
          <div className="submission-skeleton-card">
            <SkeletonBlock className="skeleton--line skeleton--line-short" />
            <SkeletonBlock className="submission-skeleton-code" />
          </div>
          <div className="submission-skeleton-card">
            <SkeletonBlock className="skeleton--line skeleton--line-short" />
            <SkeletonBlock className="skeleton--line" />
            <SkeletonBlock className="skeleton--line" />
          </div>
        </div>
        <div className="submission-skeleton-card">
          <SkeletonBlock className="skeleton--line skeleton--line-short" />
          <SkeletonBlock className="skeleton--line" />
          <SkeletonBlock className="skeleton--line" />
          <SkeletonBlock className="skeleton--line skeleton--line-short" />
        </div>
      </div>
    </section>
  );
}

export default function SubmissionDetailPage() {
  const { id } = useParams();
  const { t, i18n } = useTranslation();
  const { theme } = useTheme();
  const validId = Boolean(id && /^[1-9]\d*$/.test(id));

  const submissionQuery = useQuery({
    queryKey: ["submission", id],
    queryFn: ({ signal }) => {
      if (!id) throw new Error("Missing submission ID");
      return getSubmission(id, signal);
    },
    enabled: validId,
    retry: false,
    refetchOnWindowFocus: false,
    refetchInterval: (query) =>
      !query.state.error && isActive(query.state.data?.status) ? 1500 : false,
  });

  const submission = submissionQuery.data;
  const problemId = submission?.problemId;
  const responseStatus =
    submissionQuery.error instanceof ApiError
      ? submissionQuery.error.status
      : undefined;
  const accessDenied =
    responseStatus === 401 || responseStatus === 403 || responseStatus === 404;

  const problemQuery = useQuery({
    queryKey: ["problem", problemId == null ? undefined : String(problemId)],
    queryFn: ({ signal }) => {
      if (problemId == null) throw new Error("Missing problem ID");
      return getProblemById(String(problemId), signal);
    },
    enabled: problemId != null && !accessDenied,
    retry: false,
    staleTime: 60_000,
    refetchOnWindowFocus: false,
  });

  if (!validId || accessDenied || (!submissionQuery.isPending && !submission)) {
    const status = responseStatus;
    const errorKey = !validId
      ? "invalidId"
      : status === 401
        ? "unauthorized"
        : status === 403
          ? "forbidden"
          : status === 404
            ? "notFound"
            : "loadError";

    return (
      <section className="live-submission-detail live-detail-state">
        <AlertCircle size={32} aria-hidden="true" />
        <h1 role="alert">{t(`submissionDetail.${errorKey}`)}</h1>
        <div className="live-detail-actions">
          {validId && (
            <button
              className="button"
              type="button"
              disabled={submissionQuery.isFetching}
              onClick={() => void submissionQuery.refetch()}
            >
              <RefreshCw size={16} aria-hidden="true" />
              {t("submissionDetail.retry")}
            </button>
          )}
          {status === 401 && (
            <Link className="button primary" to="/login">
              {t("submissionDetail.login")}
            </Link>
          )}
          <Link className="button" to="/problems">
            {t("submissionDetail.problems")}
          </Link>
        </div>
      </section>
    );
  }

  if (!submission) {
    return <SubmissionDetailSkeleton label={t("submissionDetail.loading")} />;
  }

  const active = isActive(submission.status);
  const tone = getTone(submission);
  const resultKey =
    submission.status === "COMPLETED" && submission.verdict
      ? submission.verdict
      : submission.status;
  const resultLabel = t(
    submission.status === "COMPLETED" && submission.verdict
      ? `submissionDetail.verdict.${submission.verdict}`
      : `submissionDetail.status.${submission.status}`,
  );
  const summary =
    resultKey === "AC"
      ? submission.totalTestCases != null && submission.totalTestCases > 0
        ? t("submissionDetail.acceptedSummary", {
            count: submission.totalTestCases,
          })
        : t("submissionDetail.acceptedFallback")
      : t(`submissionDetail.summary.${resultKey}`);
  const ResultIcon = active
    ? LoaderCircle
    : tone === "success"
      ? CheckCircle2
      : tone === "danger"
        ? XCircle
        : AlertCircle;
  const languageKey = submission.language.toLowerCase();
  const language = languageNames[languageKey] ?? submission.language;
  const extension =
    languageExtensions[languageKey as keyof typeof languageExtensions];
  const locale = i18n.resolvedLanguage?.startsWith("vi") ? "vi-VN" : "en-GB";
  const problemTitle =
    problemQuery.data?.title ??
    t("submissionDetail.problemFallback", { id: submission.problemId });
  const hasSource = submission.sourceCode != null;
  const failedCase = submission.failedTestCase;
  const testCases =
    submission.passedTestCases == null || submission.totalTestCases == null
      ? "-"
      : `${submission.passedTestCases} / ${submission.totalTestCases}`;
  const formatDate = (value: string) => {
    const date = new Date(value);
    return Number.isNaN(date.getTime())
      ? "-"
      : new Intl.DateTimeFormat(locale, {
          day: "2-digit",
          month: "short",
          year: "numeric",
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
          timeZone: "Asia/Ho_Chi_Minh",
        }).format(date);
  };

  return (
    <section className="live-submission-detail" data-tone={tone}>
      <h1>{t("submissionDetail.eyebrow", { id: submission.id })}</h1>
      <Link to={`/problems/${submission.problemId}`} className="back-link">
        <ArrowLeft size={16} aria-hidden="true" />
        {t("submissionDetail.backToProblem")}
      </Link>

      <div className="verdict-banner" role="status" aria-live="polite">
        <ResultIcon
          size={32}
          className={active ? "spin" : undefined}
          aria-hidden="true"
        />
        <div>
          <h2>{resultLabel}</h2>
          <p>{summary}</p>
        </div>
      </div>

      {submissionQuery.isError && (
        <p className="live-update-error" role="alert">
          {t("submissionDetail.updateError")}
        </p>
      )}

      <div className="submission-metrics">
        <div>
          <Clock3 size={21} aria-hidden="true" />
          <span>
            {t("submissionDetail.executionTime")}
            <strong>
              {submission.executionTimeMs == null
                ? "-"
                : `${submission.executionTimeMs} ms`}
            </strong>
          </span>
        </div>
        <div>
          <MemoryStick size={21} aria-hidden="true" />
          <span>
            {t("submissionDetail.memory")}
            <strong>
              {submission.memoryUsedKb == null
                ? "-"
                : `${new Intl.NumberFormat(locale, {
                    minimumFractionDigits: 1,
                    maximumFractionDigits: 1,
                  }).format(submission.memoryUsedKb / 1024)} MB`}
            </strong>
          </span>
        </div>
        <div>
          <ClipboardCheck size={21} aria-hidden="true" />
          <span>
            {t("submissionDetail.testCases")}
            <strong>{testCases}</strong>
          </span>
        </div>
        <div>
          <Code2 size={21} aria-hidden="true" />
          <span>
            {t("submissionDetail.language")}
            <strong>{language}</strong>
          </span>
        </div>
      </div>

      <div className="submission-detail-grid">
        <div className="live-submission-main">
          <section className="source-card">
            <div className="card-title">
              <h2>
                <FileCode2 size={18} aria-hidden="true" />
                {t("submissionDetail.sourceCode")}
              </h2>
              <span className="live-source-privacy">
                <LockKeyhole size={13} aria-hidden="true" />
                {t("submissionDetail.privateSource")}
              </span>
              {hasSource && (
                <CopyCodeButton
                  key={submission.id}
                  source={submission.sourceCode ?? ""}
                />
              )}
            </div>
            {hasSource ? (
              <div className="live-submission-source">
                <CodeMirror
                  value={submission.sourceCode}
                  theme={theme}
                  extensions={extension ? [extension] : []}
                  editable={false}
                  readOnly
                  maxHeight="650px"
                  aria-label={t("submissionDetail.sourceCode")}
                  basicSetup={{
                    foldGutter: false,
                    highlightActiveLine: false,
                    highlightActiveLineGutter: false,
                    tabSize: 4,
                  }}
                />
              </div>
            ) : (
              <div className="source-locked">
                <span>
                  <LockKeyhole size={28} aria-hidden="true" />
                </span>
                <h3>{t("submissionDetail.sourceUnavailable")}</h3>
                <p>{t("submissionDetail.sourceUnavailableDescription")}</p>
              </div>
            )}
          </section>

          {submission.compileOutput && (
            <section className="source-card live-diagnostic">
              <div className="card-title">
                <h2>{t("submissionDetail.compileOutput")}</h2>
              </div>
              <pre>
                <code>{submission.compileOutput}</code>
              </pre>
            </section>
          )}
          {submission.errorMessage && (
            <section className="source-card live-diagnostic">
              <div className="card-title">
                <h2>{t("submissionDetail.errorMessage")}</h2>
              </div>
              <pre>{submission.errorMessage}</pre>
            </section>
          )}
          {failedCase && (
            <section className="source-card live-diagnostic">
              <div className="card-title">
                <h2>
                  {t("submissionDetail.failedTestCase", {
                    id: failedCase.testCase,
                  })}
                </h2>
              </div>
              <div className="live-failed-cases">
                {(
                  [
                    ["input", failedCase.input],
                    ["expectedOutput", failedCase.expectedOutput],
                    ["actualOutput", failedCase.actualOutput],
                  ] as const
                ).map(([label, value]) => (
                  <div key={label}>
                    <h3>{t(`submissionDetail.${label}`)}</h3>
                    <pre>{value || t("submissionDetail.emptyOutput")}</pre>
                  </div>
                ))}
              </div>
            </section>
          )}
          {!failedCase && submission.failedOutput && (
            <section className="source-card live-diagnostic">
              <div className="card-title">
                <h2>{t("submissionDetail.actualOutput")}</h2>
              </div>
              <pre>{submission.failedOutput}</pre>
            </section>
          )}
        </div>

        <aside className="detail-meta" aria-labelledby="submission-meta-title">
          <h2 id="submission-meta-title">{t("submissionDetail.details")}</h2>
          <dl>
            <dt>{t("submissionDetail.submittedBy")}</dt>
            <dd className="live-submitter">
              <span className="avatar small blue" aria-hidden="true">
                {submission.username.slice(0, 2).toUpperCase()}
              </span>
              {submission.userId > 0 ? (
                <Link to={`/users/${submission.userId}`}>
                  {submission.username}
                </Link>
              ) : (
                <span>{submission.username ?? "—"}</span>
              )}
            </dd>
            <dt>{t("submissionDetail.problem")}</dt>
            <dd>
              <Link to={`/problems/${submission.problemId}`}>
                {problemTitle}
              </Link>
            </dd>
            <dt>{t("submissionDetail.submittedAt")}</dt>
            <dd>
              <time dateTime={submission.createdAt}>
                {formatDate(submission.createdAt)}
              </time>
              <small>{t("submissionDetail.timezone")}</small>
            </dd>
            <dt>{t("submissionDetail.updatedAt")}</dt>
            <dd>
              <time dateTime={submission.updatedAt}>
                {formatDate(submission.updatedAt)}
              </time>
            </dd>
          </dl>
        </aside>
      </div>
    </section>
  );
}
