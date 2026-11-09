import { useTranslation } from "react-i18next";
import { useQuery } from "@tanstack/react-query";
import { getProblems } from "../api/problems";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import TagPicker from "./TagPicker";
import { getTags } from "../api/tags";

type DifficultyFilter = "" | "EASY" | "MEDIUM" | "HARD";

export default function ProblemList() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const navigate = useNavigate();

  // draft diff: selected diff, difficulty: applied diff
  const [draftDifficulty, setDraftDifficulty] = useState<DifficultyFilter>("");
  const [difficulty, setDifficulty] = useState<DifficultyFilter>("");

  // same w/ diffs, idk
  const [draftTagIds, setDraftTagIds] = useState<number[]>([]);
  const [tagIds, setTagIds] = useState<number[]>([]);

  const tagsQuery = useQuery({
    queryKey: ["tags"],
    queryFn: ({ signal }) => getTags(signal),
    staleTime: 5 * 60 * 1000, // 5 mins
    refetchOnWindowFocus: false,
  });

  const { data, isPending, isError } = useQuery({
    queryKey: ["problems", { page, size, difficulty, tagIds }],
    queryFn: ({ signal }) =>
      getProblems(
        {
          page,
          size,
          difficulty: difficulty || undefined,
          tagIds,
        },
        signal,
      ),
    staleTime: 2 * 60 * 1000,
    refetchOnWindowFocus: false,
  });

  if (isPending) {
    return <p role="status">{t("problems.loading")}</p>;
  }

  if (isError) {
    return <p role="alert">{t("problems.error")}</p>;
  }

  const problems = data.content;

  return (
    <section>
      <h2>{t("problems.title")}</h2>
      <form
        className="problem-filters"
        onSubmit={(event) => {
          event.preventDefault();
          setPage(0);
          setDifficulty(draftDifficulty);
          setTagIds([...new Set(draftTagIds)].sort((a, b) => a - b));
          console.log("Filters applied:", { difficulty: draftDifficulty, tagIds: tagIds });
        }}
      >
        {/* Difficulty filter */}
        <label>
          Difficulty{" "}
          <select
            value={draftDifficulty}
            onChange={(event) =>
              setDraftDifficulty(event.target.value as DifficultyFilter)
            }
          >
            <option value="">All difficulties</option>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>
        </label>

        {/* Tag picker */}
        {tagsQuery.isPending ? (
          <span role="status">Loading tags…</span>
        ) : tagsQuery.isError ? (
          <span role="alert">
            Could not load tags.{" "}
            <button type="button" onClick={() => tagsQuery.refetch()}>
              Retry
            </button>
          </span>
        ) : (
          <TagPicker
            tags={tagsQuery.data}
            selected={draftTagIds}
            onChange={setDraftTagIds}
            label="Problem tags"
            compact
          />
        )}

        <button type="submit">Find</button>
      </form>

      <table className="problem-table">
        <thead>
          <tr>
            <th>{t("problems.id")}</th>
            <th>{t("problems.name")}</th>
            <th>{t("problems.difficulty")}</th>
            <th>{t("problems.tags", { defaultValue: "Tags" })}</th>
            <th>{t("problems.acceptance", { defaultValue: "Acceptance" })}</th>
          </tr>
        </thead>

        <tbody>
          {problems.map((problem) => (
            <tr
              key={problem.id}
              onClick={() => navigate(`/problems/${problem.id}`)}
              style={{ cursor: "pointer" }}
            >
              <td>{problem.id}</td>
              <td>{problem.title}</td>
              <td>{t(`problems.difficultyness.${problem.difficulty.toLowerCase()}`)}</td>
              <td>{problem.tags.map((tag) => tag.name).join(", ") || "—"}</td>
              <td>
                <span
                  title={`${problem.acceptedSubmissionCount} accepted / ${problem.judgedSubmissionCount} judged`}
                >
                  {problem.acceptanceRate === null
                    ? "—"
                    : `${problem.acceptanceRate.toFixed(2)}%`}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {problems.length === 0 && (
        <p role="status">{t("problems.empty")}</p>
      )}

      <div className="pagination">
        <button
          onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
          disabled={data.first}
        >
          {t("problems.previous")}
        </button>
        <span>
          {t("problems.pageInfo", { current: page + 1, total: Math.max(1, data.totalPages) })}
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