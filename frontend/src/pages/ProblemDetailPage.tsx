import { useQuery, useMutation } from "@tanstack/react-query";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getProblemById, runCode } from "../api/problems";
import type { Language } from "../components/CodeEditor";
import { useState } from "react";
import CodeEditor from "../components/CodeEditor";
import type { RunCodeRequest } from "../types/run";
import ProblemStatement from "../components/ProblemStatement";
import {
  ChevronDown,
  FileCode2,
  LoaderCircle,
  Play,
  RotateCcw,
  Terminal,
} from "lucide-react";
import RunResult from "../components/RunResult";
import { createSubmission } from "../api/submissions";
import { ApiError } from "../api/http";
import type { SubmissionRequest } from "../types/submission";
import { SkeletonBlock } from "../components/ui/Skeleton";

export default function ProblemDetailPage() {
  const { id } = useParams();
  const { t } = useTranslation();
  const [language, setLanguage] = useState<Language>("cpp");
  const [sourceCode, setSourceCode] = useState("");
  const [input, setInput] = useState("");
  const navigate = useNavigate();

  const submitMutation = useMutation({
    mutationFn: (request: SubmissionRequest) => createSubmission(request),

    retry: false,

    onSuccess: (submission) => {
      navigate(`/submissions/${submission.id}`);
    },
  });

  const fileName = {
    cpp: "solution.cpp",
    java: "Main.java",
    python: "solution.py",
  }[language];

  const {
    data: problem,
    isPending,
    isError,
  } = useQuery({
    queryKey: ["problem", id],
    queryFn: ({ signal }) => {
      if (!id) {
        throw new Error("Missing problem ID");
      }

      return getProblemById(id, signal);
    },
    enabled: Boolean(id),
  });

  const runMutation = useMutation({
    mutationFn: (request: RunCodeRequest) => {
      if (!id) {
        throw new Error("Missing problem ID");
      }
      return runCode(id, request);
    },
  });

  if (!id || isError) {
    return <p role="alert">{t("problemDetail.error")}</p>;
  }

  if (isPending) {
    return <ProblemDetailSkeleton label={t("problemDetail.loading")} />;
  }

  return (
    <section className="live-problem-detail">
      <Link to="/problems" className="back-link">
        ← Back to problems
      </Link>

      <div className="live-problem-workspace">
        <ProblemStatement problem={problem} />

        <section className="editor-panel live-editor-panel">
          <div className="editor-header">
            <span>
              <FileCode2 size={16} aria-hidden="true" />
              Solution
            </span>

            <div>
              <label className="live-editor-language">
                <select
                  aria-label="Programming language"
                  value={language}
                  onChange={(event) =>
                    setLanguage(event.target.value as Language)
                  }
                >
                  <option value="cpp">C++ 20</option>
                  <option value="java">Java 21</option>
                  <option value="python">Python 3</option>
                </select>

                <ChevronDown size={15} aria-hidden="true" />
              </label>

              <button
                type="button"
                className="icon-button"
                aria-label="Clear code"
                title="Clear code"
                disabled={runMutation.isPending || !sourceCode}
                onClick={() => {
                  if (window.confirm("Clear the current code?")) {
                    setSourceCode("");
                    runMutation.reset();
                  }
                }}
              >
                <RotateCcw size={16} aria-hidden="true" />
              </button>
            </div>
          </div>

          <div className="editor-file">
            <span className="file-dot" />
            {fileName}
            <span>Editing</span>
          </div>

          <CodeEditor
            language={language}
            value={sourceCode}
            onChange={setSourceCode}
          />

          <div className="editor-status">
            <span>UTF-8 · Tab size: 4</span>
            <span>{sourceCode.length} characters</span>
          </div>

          <div className="run-panel">
            <div className="run-panel-title">
              <span>
                <Terminal size={15} aria-hidden="true" />
                Test console
              </span>

              <span className="small-copy">
                {runMutation.isPending ? "Running…" : "Custom input"}
              </span>
            </div>

            <div className="run-content">
              <label>
                Input
                <textarea
                  aria-label="Custom input"
                  rows={4}
                  spellCheck={false}
                  placeholder="Enter program input…"
                  value={input}
                  onChange={(event) => setInput(event.target.value)}
                />
              </label>

              <div className="live-console-output">
                {runMutation.isPending ? (
                  <p role="status">Running your code…</p>
                ) : runMutation.isError ? (
                  <p className="live-run-error" role="alert">
                    {runMutation.error.message || "Could not run the program."}
                  </p>
                ) : runMutation.isSuccess ? (
                  <RunResult result={runMutation.data} />
                ) : (
                  <p className="console-placeholder">
                    Run your code to see its output.
                  </p>
                )}
              </div>
            </div>
          </div>

          <div className="editor-actions">
            <button
              type="button"
              className="button"
              disabled={runMutation.isPending || !sourceCode.trim()}
              onClick={() =>
                runMutation.mutate({
                  language,
                  sourceCode,
                  input,
                })
              }
            >
              {runMutation.isPending ? (
                <LoaderCircle size={16} className="spin" aria-hidden="true" />
              ) : (
                <Play size={16} aria-hidden="true" />
              )}

              {runMutation.isPending
                ? t("problemDetail.codeEditor.running")
                : t("problemDetail.codeEditor.run")}
            </button>

            <button
              type="button"
              className="button primary"
              disabled={submitMutation.isPending || !sourceCode.trim()}
              onClick={() =>
                submitMutation.mutate({
                  problemId: problem.id,
                  language,
                  sourceCode,
                })
              }
            >
              {submitMutation.isPending
                ? t("problemDetail.codeEditor.submitting")
                : t("problemDetail.codeEditor.submit")}
            </button>

            {submitMutation.isError && (
              <p role="alert">
                {submitMutation.error instanceof ApiError &&
                submitMutation.error.status === 401
                  ? t("auth.loginRequired")
                  : t("problemDetail.submitError")}
              </p>
            )}
          </div>
        </section>
      </div>
    </section>
  );
}

function ProblemDetailSkeleton({ label }: { label: string }) {
  return (
    <section
      className="problem-detail-skeleton"
      role="status"
      aria-label={label}
    >
      <div className="problem-detail-skeleton-panel">
        <SkeletonBlock className="skeleton--heading" />
        <SkeletonBlock className="skeleton--line skeleton--line-short" />
        <SkeletonBlock className="skeleton--line" />
        <SkeletonBlock className="skeleton--line" />
        <SkeletonBlock className="skeleton--line skeleton--line-short" />
        <div className="problem-detail-skeleton-samples">
          <SkeletonBlock className="skeleton--line skeleton--line-short" />
          <SkeletonBlock className="problem-detail-skeleton-sample" />
        </div>
      </div>
      <div className="problem-detail-skeleton-panel problem-detail-skeleton-editor">
        <div className="problem-detail-skeleton-editor-heading">
          <SkeletonBlock className="skeleton--line skeleton--line-short" />
          <SkeletonBlock className="skeleton--control" />
        </div>
        <SkeletonBlock className="problem-detail-skeleton-code" />
      </div>
    </section>
  );
}
