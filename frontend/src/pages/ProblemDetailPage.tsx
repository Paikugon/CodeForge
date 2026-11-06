import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getProblemById } from "../api/problems";

export default function ProblemDetailPage() {
  const { id } = useParams();
  const { t } = useTranslation();

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
    </section>
  );
}