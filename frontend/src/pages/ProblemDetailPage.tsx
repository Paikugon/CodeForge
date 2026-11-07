import { useQuery, useMutation } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getProblemById, runCode } from "../api/problems";
import type { Language } from "../components/CodeEditor";
import { useState } from "react";
import CodeEditor from "../components/CodeEditor";
import type { RunCodeRequest } from "../types/run";

export default function ProblemDetailPage() {
  const { id } = useParams();
  const { t } = useTranslation();
  const [language, setLanguage] = useState<Language>("cpp");
  const [sourceCode, setSourceCode] = useState("");
  const [input, setInput] = useState("");

  const { data: problem, isPending, isError } = useQuery({
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
    return <p role="status">{t("problemDetail.loading")}</p>;
  }

  const samples = problem.testCases
    .filter((testCase) => testCase.isSample === true)
    .sort((a, b) => a.orderIndex - b.orderIndex || a.id - b.id);

  return (
    <section>
      <Link to="/problems">{t("problems.title")}</Link>

      <h1>{problem.title}</h1>

      <p>
        {t("problemDetail.timeLimit")}: {problem.timeLimitMs} ms
      </p>

      <p>
        {t("problemDetail.memoryLimit")}: {problem.memoryLimitMb} MB
      </p>

      <div style={{ whiteSpace: "pre-wrap" }}>
        {problem.description}
      </div>

      {samples.length > 0 && (
        <section>
          <h2>{t("problemDetail.examples")}</h2>

          {samples.map((sample, index) => (
            <article className="sample-case" key={sample.id}>
              <h3>
                {t("problemDetail.example", { number: index + 1 })}
              </h3>

              <h4>{t("problemDetail.input")}</h4>
              <pre><code>{sample.input}</code></pre>

              <h4>{t("problemDetail.output")}</h4>
              <pre><code>{sample.expectedOutput}</code></pre>
            </article>
          ))}
        </section>
      )}

      <section>
        <h2>{t("problemDetail.codeEditor.code")}</h2>

        <label>
          {t("problemDetail.codeEditor.language")}{" "}
          <select
            value={language}
            onChange={(event) =>
              setLanguage(event.target.value as Language)
            }
          >
            <option value="cpp">C++ 20</option>
            <option value="java">Java 21</option>
            <option value="python">Python 3</option>
          </select>
        </label>

        <CodeEditor
          language={language}
          value={sourceCode}
          onChange={setSourceCode}
        />

        <label>
          {t("problemDetail.codeEditor.customInput")}

          <textarea
            rows={4}
            value={input}
            onChange={(event) => setInput(event.target.value)}
          />
        </label>

        <button
          disabled={runMutation.isPending || !sourceCode.trim()}
          onClick={() =>
            runMutation.mutate({
              language,
              sourceCode,
              input,
            })
          }
        >
          {t(
            runMutation.isPending
              ? "problemDetail.codeEditor.running"
              : "problemDetail.codeEditor.run",
          )}
        </button>

        {runMutation.isError && (
          <p role="alert">{t("problemDetail.codeEditor.runError")}</p>
        )}

        {runMutation.isSuccess && (
          <section aria-live="polite">
            <h3>{t("problemDetail.codeEditor.runResult")}</h3>

            <p>
              {t("problemDetail.codeEditor.resultCode", {
                code: runMutation.data.resultCode,
              })}
            </p>

            <pre>
              {runMutation.data.compileOutput ||
                runMutation.data.stdout ||
                t("problemDetail.codeEditor.noOutput")}
            </pre>
          </section>
        )}
      </section>
    </section>
  );
} 