import { useTranslation } from "react-i18next";
import { useQuery } from "@tanstack/react-query";
import { getProblems } from "../api/problems";
import { useState } from "react";
import TagPicker from "./TagPicker";
import { getTags } from "../api/tags";
import { ChevronDown } from "lucide-react";
import ProblemTable from "./ProblemTable";
import TablePagination from "./ui/TablePagination";

type DifficultyFilter = "" | "EASY" | "MEDIUM" | "HARD";

export default function ProblemList() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [size] = useState(20);

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

  const { data, isPending, isError, isFetching } = useQuery({
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
    <section className="live-problem-page">
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
        <label className="select-field difficulty-filter">
          <select
            aria-label="Problem difficulty"
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

          <ChevronDown size={16} aria-hidden="true" />
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

        <button type="submit" className="button primary find-problems">
          Find
        </button>
      </form>

      {/* Problem table */}
      <ProblemTable
        problems={problems}
        footer={
          <TablePagination
            page={page}
            pageSize={size}
            totalPages={data.totalPages}
            totalElements={data.totalElements}
            onPageChange={setPage}
            disabled={isFetching}
          />
        }
      />
    </section>
  );
}