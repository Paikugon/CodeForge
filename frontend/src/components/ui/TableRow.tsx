import type { ComponentPropsWithoutRef } from "react";

type TableRowProps = Omit<ComponentPropsWithoutRef<"tr">, "onClick"> & {
  onRowClick?: () => void;
};

export default function TableRow({
  onRowClick,
  className = "",
  children,
  ...props
}: TableRowProps) {
  return (
    <tr
      {...props}
      className={`${className} ${onRowClick ? "clickable-row" : ""}`}
      onClick={
        onRowClick
          ? (event) => {
            if (
              event.defaultPrevented ||
              event.button !== 0 ||
              event.ctrlKey ||
              event.metaKey ||
              event.shiftKey ||
              event.altKey
            ) return;

            const target = event.target;

            if (
              target instanceof Element &&
              target.closest(
                'a, button, input, select, textarea, label, [role="button"], [role="checkbox"], [contenteditable], [data-row-click-ignore]'
              )
            ) return;

            if (window.getSelection()?.toString()) return;

            onRowClick();
          }
          : undefined
      }
    >
      {children}
    </tr>
  );
}