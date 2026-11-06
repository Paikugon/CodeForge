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
    </section>
  );
}