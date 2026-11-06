import { useTranslation } from "react-i18next";
import { useQuery } from "@tanstack/react-query";
import { getProblems } from "../api/problems";
import { useState } from "react";
import { Link } from "react-router-dom";

export default function ProblemList() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);

  const { data, isPending, isError } = useQuery({
  queryKey: ["problems", { page, size }],
  queryFn: ({ signal }) => getProblems({ page, size }, signal),
});

if (isPending) {
  return <p role="status">{t("problems.loading")}</p>;
}

if (isError) {
  return <p role="alert">{t("problems.error")}</p>;
}

const problems = data.content;

if (problems.length === 0) {
  return <p>{t("problems.empty")}</p>;
}

  return (
    <section>
      <h2>{t("problems.title")}</h2>

      <table className="problem-table">
        <thead>
          <tr>
            <th>{t("problems.id")}</th>
            <th>{t("problems.name")}</th>
            <th>{t("problems.difficulty")}</th>
          </tr>
        </thead>

        <tbody>
          {problems.map((problem) => (
            <tr key={problem.id}>
              <td>{problem.id}</td>
              <Link to={`/problems/${problem.id}`}>
                <td>{problem.title}</td>
              </Link>
              <td>{problem.difficulty}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="pagination">
        <button
          onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
          disabled={data.first}
        >
          {t("problems.previous")}
        </button>
        <span>
          {t("problems.pageInfo", { current: page + 1, total: data.totalPages })}
        </span>
        <button
          onClick={() => setPage((prev) => prev + 1)}
          disabled={data.last}
        >
          {t("problems.next")}
        </button>
      </div>
    </section>
  );
}