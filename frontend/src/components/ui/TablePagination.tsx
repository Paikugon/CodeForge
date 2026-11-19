import { ChevronLeft, ChevronRight } from "lucide-react";

type TablePaginationProps = {
  page: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
  onPageChange: (page: number) => void;
  disabled?: boolean;
  labels?: {
    label: string;
    results: string;
    page: string;
    previous: string;
    next: string;
  };
};

export default function TablePagination({
  page,
  pageSize,
  totalPages,
  totalElements,
  onPageChange,
  disabled = false,
  labels,
}: TablePaginationProps) {
  const from = totalElements === 0 ? 0 : page * pageSize + 1;
  const to = Math.min((page + 1) * pageSize, totalElements);
  const hasResults = totalElements > 0 && totalPages > 0;

  return (
    <nav
      className="pagination table-pagination"
      aria-label={labels?.label ?? "Table pagination"}
    >
      <span aria-live="polite">
        {labels?.results ??
          (totalElements === 0
            ? "0 results"
            : `${from}–${to} of ${totalElements} results`)}
      </span>

      {hasResults && (
        <div className="table-pagination-controls">
          <button
            type="button"
            aria-label={labels?.previous ?? "Previous page"}
            disabled={disabled || page <= 0}
            onClick={() => onPageChange(page - 1)}
          >
            <ChevronLeft size={16} aria-hidden="true" />
          </button>

          <span>{labels?.page ?? `Page ${page + 1} of ${totalPages}`}</span>

          <button
            type="button"
            aria-label={labels?.next ?? "Next page"}
            disabled={disabled || page + 1 >= totalPages}
            onClick={() => onPageChange(page + 1)}
          >
            <ChevronRight size={16} aria-hidden="true" />
          </button>
        </div>
      )}
    </nav>
  );
}
