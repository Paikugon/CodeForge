type SkeletonBlockProps = {
  className?: string;
};

export function SkeletonBlock({ className = "" }: SkeletonBlockProps) {
  return <span aria-hidden="true" className={`skeleton ${className}`} />;
}

export function SkeletonTable({
  columns,
  rows = 6,
  label,
}: {
  columns: number;
  rows?: number;
  label?: string;
}) {
  return (
    <div
      className="skeleton-table"
      role={label ? "status" : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : true}
    >
      {Array.from({ length: rows }, (_, row) => (
        <div
          className="skeleton-table-row"
          key={row}
          style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}
        >
          {Array.from({ length: columns }, (_, column) => (
            <SkeletonBlock className="skeleton--cell" key={column} />
          ))}
        </div>
      ))}
    </div>
  );
}

export function PageSkeleton({ label }: { label: string }) {
  return (
    <div className="page-skeleton" role="status" aria-label={label}>
      <SkeletonBlock className="skeleton--heading" />
      <SkeletonBlock className="skeleton--line skeleton--line-short" />
      <div className="page-skeleton-panel">
        <SkeletonBlock className="skeleton--line" />
        <SkeletonBlock className="skeleton--line" />
        <SkeletonBlock className="skeleton--line skeleton--line-short" />
      </div>
      <SkeletonTable columns={4} rows={5} />
    </div>
  );
}
