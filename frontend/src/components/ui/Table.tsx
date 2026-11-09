import type {
  ComponentPropsWithoutRef,
  ReactNode,
} from "react";

type TableProps = ComponentPropsWithoutRef<"table"> & {
  label: string;
  footer?: ReactNode;
};

export default function Table({
  label,
  footer,
  children,
  className = "",
  ...props
}: TableProps) {
  return (
    <div className="table-card">
      <div
        className="table-scroll"
        role="region"
        aria-label={label}
        tabIndex={0}
      >
        <table
          {...props}
          aria-label={label}
          className={`data-table ${className}`}
        >
          {children}
        </table>
      </div>

      {footer}
    </div>
  );
}