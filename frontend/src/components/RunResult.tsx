import type { RunCodeResponse } from "../types/run";

export default function RunResult({
  result,
}: {
  result: RunCodeResponse;
}) {
  const metrics = [
    { label: "Result code", value: result.resultCode ?? "—" },
    { label: "Exit code", value: result.exitCode ?? "—" },
    {
      label: "Time",
      value: result.executionTimeMs === null
        ? "—"
        : `${result.executionTimeMs} ms`,
    },
    {
      label: "Memory",
      value: result.memoryUsedKb === null
        ? "—"
        : `${result.memoryUsedKb} KB`,
    },
  ];

  return (
    <section className="live-run-result" aria-live="polite">
      <h3>Run result</h3>

      <dl className="live-run-metrics">
        {metrics.map(({ label, value }) => (
          <div key={label}>
            <dt>{label}</dt>
            <dd>{value}</dd>
          </div>
        ))}
      </dl>

      {result.compileOutput && (
        <div className="live-run-output">
          <h4>Compiler output</h4>
          <pre>{result.compileOutput}</pre>
        </div>
      )}

      <div className="live-run-output">
        <h4>Output</h4>

        {result.stdout !== null && result.stdout !== "" ? (
          <pre>{result.stdout}</pre>
        ) : (
          <p className="live-no-output">No output.</p>
        )}
      </div>
    </section>
  );
}