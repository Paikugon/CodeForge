import { Link } from "react-router-dom";
import type { Problem } from "../types/problem";
import type { ReactNode } from "react";
import Table from "./ui/Table";
import TableRow from "./ui/TableRow";

export default function ProblemTable({
  problems,
  footer,
}: {
  problems: Problem[];
  footer?: ReactNode;
}) {

  return (
    <Table
      label="Problems"
      className="problem-table"
      footer={footer}
    >
      <thead>
        <tr>
          <th scope="col">ID</th>
          <th scope="col">Problem</th>
          <th scope="col">Difficulty</th>
          <th scope="col">Acceptance</th>
        </tr>
      </thead>

      <tbody>
        {problems.length === 0 ? (
          <tr>
            <td colSpan={4} className="problem-empty-cell">
              <strong>No matching problems</strong>
              <p>Try another difficulty or remove a tag.</p>
            </td>
          </tr>
        ) : (
          problems.map((problem) => (
            <TableRow
              key={problem.id}
              className="table-link-row"
            >
              <td className="mono">
                {String(problem.id).padStart(3, "0")}
              </td>

              <td>
                <Link
                  to={`/problems/${problem.id}`}
                  className="problem-title row-link"
                >
                  {problem.title}
                </Link>

                {problem.tags.length > 0 && (
                  <div className="live-problem-tags">
                    {problem.tags.map((tag) => (
                      <span className="live-problem-tag" key={tag.id}>
                        {tag.name}
                      </span>
                    ))}
                  </div>
                )}
              </td>

              <td>
                <span
                  className={`badge ${problem.difficulty.toLowerCase()}`}
                >
                  {problem.difficulty === "EASY"
                    ? "Easy"
                    : problem.difficulty === "MEDIUM"
                      ? "Medium"
                      : "Hard"}
                </span>
              </td>

              <td
                className="problem-acceptance"
                title={`${problem.acceptedSubmissionCount} accepted / ${problem.judgedSubmissionCount} judged`}
              >
                {problem.acceptanceRate === null
                  ? "—"
                  : `${problem.acceptanceRate.toFixed(2)}%`}
              </td>
            </TableRow>
          ))
        )}
      </tbody>
    </Table>
  );
}