import { Clock, MemoryStick } from "lucide-react";
import type { ProblemDetail } from "../types/problem";

export default function ProblemStatement({
  problem,
}: {
  problem: ProblemDetail;
}) {
  const samples = problem.testCases
    .filter((testCase) => testCase.isSample === true)
    .sort((a, b) => a.orderIndex - b.orderIndex || a.id - b.id);

  const difficulty =
    problem.difficulty.charAt(0) +
    problem.difficulty.slice(1).toLowerCase();

  return (
    <article className="live-statement-card">
      <header className="live-statement-header">
        <div className="live-statement-meta">
          <span className="mono">
            #{String(problem.id).padStart(3, "0")}
          </span>

          <span className={`badge ${problem.difficulty.toLowerCase()}`}>
            {difficulty}
          </span>
        </div>

        <h1>{problem.title}</h1>

        <div className="live-problem-limits">
          <span>
            <Clock size={15} aria-hidden="true" />
            Time: {problem.timeLimitMs} ms
          </span>

          <span>
            <MemoryStick size={15} aria-hidden="true" />
            Memory: {problem.memoryLimitMb} MB
          </span>
        </div>

        {problem.tags.length > 0 && (
          <div className="live-problem-tags">
            {problem.tags.map((tag) => (
              <span className="live-problem-tag" key={tag.id}>
                {tag.name}
              </span>
            ))}
          </div>
        )}
      </header>

      <div className="live-statement-body">
        <div className="live-problem-description">
          {problem.description}
        </div>

        {samples.length > 0 && (
          <section className="live-problem-examples">
            <h2>Examples</h2>

            {samples.map((sample, index) => (
              <article className="live-sample" key={sample.id}>
                <h3>Example {index + 1}</h3>

                <div className="live-sample-grid">
                  <div>
                    <h4>Input</h4>
                    <pre><code>{sample.input}</code></pre>
                  </div>

                  <div>
                    <h4>Output</h4>
                    <pre><code>{sample.expectedOutput}</code></pre>
                  </div>
                </div>
              </article>
            ))}
          </section>
        )}
      </div>
    </article>
  );
}