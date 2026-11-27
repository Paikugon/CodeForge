type SkeletonBlockProps = {
  className?: string;
};

export type SkeletonPage =
  | "problem-list"
  | "problem-detail"
  | "submission-history"
  | "submission-detail"
  | "profile"
  | "admin-list"
  | "admin-users"
  | "admin-form"
  | "admin-test-cases"
  | "auth";

export function getSkeletonPageForLocation(
  pathname: string,
  search = "",
): SkeletonPage {
  if (pathname.startsWith("/admin")) {
    if (pathname === "/admin/users") return "admin-users";
    if (
      pathname.endsWith("/test-cases") ||
      new URLSearchParams(search).get("tab") === "test-cases"
    ) {
      return "admin-test-cases";
    }
    if (
      pathname === "/admin/problems/new" ||
      /^\/admin\/problems\/\d+$/.test(pathname)
    ) {
      return "admin-form";
    }
    return "admin-list";
  }
  if (pathname.startsWith("/problems/")) return "problem-detail";
  if (pathname.startsWith("/submissions/")) return "submission-detail";
  if (pathname === "/submissions") return "submission-history";
  if (pathname === "/profile" || pathname.startsWith("/users/")) {
    return "profile";
  }
  if (pathname === "/login" || pathname === "/register") return "auth";
  return "problem-list";
}

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

export function PageSkeleton({
  page,
  label,
  bodyOnly = false,
}: {
  page: SkeletonPage;
  label: string;
  bodyOnly?: boolean;
}) {
  return (
    <div
      className={`page-skeleton page-skeleton--${page}${bodyOnly ? " page-skeleton--body-only" : ""}`}
      role="status"
      aria-label={label}
      aria-busy="true"
    >
      {page === "problem-list" && (
        <>
          <SkeletonBlock className="skeleton--heading" />
          <div className="page-skeleton__filters">
            <SkeletonBlock className="skeleton--control" />
            <SkeletonBlock className="skeleton--control skeleton--wide" />
            <SkeletonBlock className="skeleton--control skeleton--button" />
          </div>
          <SkeletonTable columns={5} rows={7} />
        </>
      )}
      {page === "problem-detail" && (
        <>
          <SkeletonBlock className="skeleton--line skeleton--back-link" />
          <div className="page-skeleton__columns page-skeleton__problem">
            <div className="page-skeleton-panel">
              <SkeletonBlock className="skeleton--heading" />
              <SkeletonBlock className="skeleton--line skeleton--line-short" />
              <SkeletonBlock className="skeleton--line" />
              <SkeletonBlock className="skeleton--line" />
              <SkeletonBlock className="skeleton--example" />
            </div>
            <div className="page-skeleton-panel page-skeleton__editor">
              <div className="page-skeleton__editor-heading">
                <SkeletonBlock className="skeleton--line skeleton--line-short" />
                <SkeletonBlock className="skeleton--control" />
              </div>
              <SkeletonBlock className="skeleton--code" />
            </div>
          </div>
        </>
      )}
      {page === "submission-history" && (
        <>
          <SkeletonBlock className="skeleton--heading" />
          <SkeletonBlock className="skeleton--line skeleton--line-short" />
          <div className="page-skeleton__tabs">
            <SkeletonBlock className="skeleton--tab" />
            <SkeletonBlock className="skeleton--tab" />
          </div>
          <div className="page-skeleton__filters">
            <SkeletonBlock className="skeleton--control" />
            <SkeletonBlock className="skeleton--control" />
            <SkeletonBlock className="skeleton--control skeleton--button" />
          </div>
          <SkeletonTable columns={7} rows={7} />
        </>
      )}
      {page === "submission-detail" && (
        <>
          <SkeletonBlock className="skeleton--line skeleton--back-link" />
          <SkeletonBlock className="skeleton--heading" />
          <div className="page-skeleton__result-banner">
            <SkeletonBlock className="skeleton--avatar" />
            <div>
              <SkeletonBlock className="skeleton--line" />
              <SkeletonBlock className="skeleton--line skeleton--line-short" />
            </div>
          </div>
          <div className="page-skeleton__metrics">
            {Array.from({ length: 4 }, (_, index) => (
              <div className="page-skeleton__metric" key={index}>
                <SkeletonBlock className="skeleton--line skeleton--line-short" />
                <SkeletonBlock className="skeleton--metric" />
              </div>
            ))}
          </div>
          <div className="page-skeleton__columns page-skeleton__submission">
            <div className="page-skeleton-panel">
              <SkeletonBlock className="skeleton--line skeleton--line-short" />
              <SkeletonBlock className="skeleton--code" />
            </div>
            <div className="page-skeleton-panel">
              <SkeletonBlock className="skeleton--line skeleton--line-short" />
              <SkeletonBlock className="skeleton--line" />
              <SkeletonBlock className="skeleton--line" />
            </div>
          </div>
        </>
      )}
      {page === "profile" && (
        <>
          <SkeletonBlock className="skeleton--heading" />
          <div className="page-skeleton__columns page-skeleton__profile">
            <div className="page-skeleton-panel">
              <SkeletonBlock className="skeleton--cover" />
              <SkeletonBlock className="skeleton--avatar" />
              <SkeletonBlock className="skeleton--heading" />
              <SkeletonBlock className="skeleton--line skeleton--line-short" />
              <SkeletonBlock className="skeleton--line" />
            </div>
            <div className="page-skeleton__profile-main">
              <div className="page-skeleton__metrics">
                {Array.from({ length: 4 }, (_, index) => (
                  <div className="page-skeleton__metric" key={index}>
                    <SkeletonBlock className="skeleton--line skeleton--line-short" />
                    <SkeletonBlock className="skeleton--metric" />
                  </div>
                ))}
              </div>
              <div className="page-skeleton-panel">
                <SkeletonBlock className="skeleton--heading" />
                <SkeletonBlock className="skeleton--line skeleton--line-short" />
                <SkeletonTable columns={4} rows={5} />
              </div>
            </div>
          </div>
        </>
      )}
      {(page === "admin-list" || page === "admin-users") && (
        <>
          <SkeletonBlock className="skeleton--heading" />
          <div className="page-skeleton__filters">
            <SkeletonBlock className="skeleton--control" />
            <SkeletonBlock className="skeleton--control" />
            <SkeletonBlock className="skeleton--control skeleton--button" />
          </div>
          <SkeletonTable columns={page === "admin-users" ? 6 : 7} rows={7} />
        </>
      )}
      {page === "admin-form" && (
        <>
          {!bodyOnly && (
            <div className="page-skeleton__heading-row">
              <SkeletonBlock className="skeleton--control skeleton--button" />
              <SkeletonBlock className="skeleton--heading" />
            </div>
          )}
          <div className="page-skeleton-panel page-skeleton__form">
            <div className="page-skeleton__form-grid">
              {Array.from({ length: 4 }, (_, index) => (
                <div key={index}>
                  <SkeletonBlock className="skeleton--line skeleton--line-short" />
                  <SkeletonBlock className="skeleton--control skeleton--full" />
                </div>
              ))}
            </div>
            <SkeletonBlock className="skeleton--line skeleton--line-short" />
            <SkeletonBlock className="skeleton--textarea" />
            <SkeletonBlock className="skeleton--button" />
          </div>
        </>
      )}
      {page === "admin-test-cases" && (
        bodyOnly ? (
          <SkeletonTable columns={6} rows={7} />
        ) : (
          <>
            <div className="page-skeleton__heading-row">
              <SkeletonBlock className="skeleton--heading" />
              <SkeletonBlock className="skeleton--control skeleton--button" />
            </div>
            <div className="page-skeleton__filters">
              <SkeletonBlock className="skeleton--button" />
              <SkeletonBlock className="skeleton--button" />
              <SkeletonBlock className="skeleton--button" />
            </div>
            <SkeletonTable columns={6} rows={7} />
          </>
        )
      )}
      {page === "auth" && (
        <div className="page-skeleton__auth-card">
          <SkeletonBlock className="skeleton--heading" />
          <SkeletonBlock className="skeleton--line skeleton--line-short" />
          <SkeletonBlock className="skeleton--line" />
          <SkeletonBlock className="skeleton--control skeleton--full" />
          <SkeletonBlock className="skeleton--line" />
          <SkeletonBlock className="skeleton--control skeleton--full" />
          <SkeletonBlock className="skeleton--button skeleton--full" />
        </div>
      )}
    </div>
  );
}
