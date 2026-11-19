import type { ComponentPropsWithoutRef } from "react";

type Props = ComponentPropsWithoutRef<"input"> & { label: string; error?: string };

export default function AccountInput({ id, label, error, ...props }: Props) {
  return <div className="account-field">
    <label htmlFor={id}>{label}</label>
    <input {...props} id={id} aria-invalid={Boolean(error)} aria-describedby={error ? `${id}-error` : undefined} />
    {error && <p id={`${id}-error`} className="account-field-error" role="alert">{error}</p>}
  </div>;
}
