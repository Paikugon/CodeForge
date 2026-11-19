import { useEffect, useId, useRef, useState } from "react";
import { X } from "lucide-react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { getAdminTestCase } from "../../api/admin";
import { createTestCase, updateTestCase } from "../../api/testCases";
import type { TestCase, TestCaseRequest } from "../../types/testcase";
import { MAX_CASE_FILE_BYTES } from "../../utils/adminFiles";
import { AdminError, FileTextField } from "./AdminUi";

function EditorForm({
  problemId,
  testCase,
  onSaved,
  onClose,
  onBusyChange,
}: {
  problemId: string;
  testCase?: TestCase;
  onSaved: () => Promise<void>;
  onClose: () => void;
  onBusyChange: (busy: boolean) => void;
}) {
  const { t } = useTranslation("admin");
  const [input, setInput] = useState(testCase?.input ?? "");
  const [output, setOutput] = useState(testCase?.expectedOutput ?? "");
  const [sample, setSample] = useState(testCase?.isSample ?? false);
  const [order, setOrder] = useState(testCase?.orderIndex.toString() ?? "");
  const [reading, setReading] = useState(0);
  const [invalid, setInvalid] = useState(false);
  const mutation = useMutation({
    mutationFn: (request: TestCaseRequest) =>
      testCase
        ? updateTestCase(problemId, testCase.id, request)
        : createTestCase(problemId, request),
    retry: false,
    onSuccess: async () => {
      await onSaved();
      onClose();
    },
  });
  const pending = mutation.isPending || reading > 0;
  useEffect(() => {
    onBusyChange(pending);
  }, [pending, onBusyChange]);
  return (
    <form
      className="admin-form"
      aria-busy={pending}
      onSubmit={(event) => {
        event.preventDefault();
        if (pending) return;
        const orderIndex = order === "" ? undefined : Number(order);
        if (
          (orderIndex !== undefined &&
            (!Number.isInteger(orderIndex) ||
              orderIndex < 0 ||
              orderIndex > 2147483647)) ||
          new TextEncoder().encode(input).length > MAX_CASE_FILE_BYTES ||
          new TextEncoder().encode(output).length > MAX_CASE_FILE_BYTES
        ) {
          setInvalid(true);
          return;
        }
        setInvalid(false);
        mutation.mutate({
          input,
          expectedOutput: output,
          isSample: sample,
          orderIndex,
        });
      }}
    >
      <div className="admin-content-grid">
        <FileTextField
          id="case-input"
          rows={10}
          label={t("input")}
          value={input}
          onChange={setInput}
          maxBytes={MAX_CASE_FILE_BYTES}
          maxCharacters={MAX_CASE_FILE_BYTES}
          disabled={mutation.isPending}
          onBusyChange={(busy) =>
            setReading((value) => value + (busy ? 1 : -1))
          }
        />
        <FileTextField
          id="case-output"
          rows={10}
          label={t("output")}
          value={output}
          onChange={setOutput}
          maxBytes={MAX_CASE_FILE_BYTES}
          maxCharacters={MAX_CASE_FILE_BYTES}
          disabled={mutation.isPending}
          onBusyChange={(busy) =>
            setReading((value) => value + (busy ? 1 : -1))
          }
        />
      </div>
      <div className="admin-filters">
        <label>
          {t("order")}
          <input
            type="number"
            min={0}
            max={2147483647}
            step={1}
            placeholder={t("autoOrder")}
            value={order}
            disabled={pending}
            onChange={(event) => setOrder(event.target.value)}
          />
        </label>
        <label className="admin-check">
          <input
            type="checkbox"
            checked={sample}
            disabled={pending}
            onChange={(event) => setSample(event.target.checked)}
          />
          {t("sample")}
        </label>
      </div>
      {invalid && (
        <p role="alert" className="admin-error">
          {t("caseInvalid")}
        </p>
      )}
      {mutation.isError && <AdminError error={mutation.error} />}
      <div className="admin-actions">
        <button className="button primary" type="submit" disabled={pending}>
          {t(mutation.isPending ? "saving" : "saveCase")}
        </button>
        <button
          className="button"
          type="button"
          disabled={pending}
          onClick={onClose}
        >
          {t("cancel")}
        </button>
      </div>
    </form>
  );
}

export default function TestCaseEditor({
  problemId,
  caseId,
  onSaved,
  onClose,
}: {
  problemId: string;
  caseId?: number;
  onSaved: () => Promise<void>;
  onClose: () => void;
}) {
  const { t } = useTranslation("admin");
  const query = useQuery({
    queryKey: ["admin", "test-case", problemId, caseId],
    queryFn: ({ signal }) => getAdminTestCase(problemId, caseId!, signal),
    enabled: caseId !== undefined,
    retry: false,
    gcTime: 0,
    refetchOnWindowFocus: false,
  });
  const dialogRef = useRef<HTMLDialogElement>(null);
  const headingRef = useRef<HTMLHeadingElement>(null);
  const titleId = useId();
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    const dialog = dialogRef.current;
    const previousFocus =
      document.activeElement instanceof HTMLElement
        ? document.activeElement
        : null;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    dialog?.showModal();
    headingRef.current?.focus();
    return () => {
      dialog?.close();
      document.body.style.overflow = previousOverflow;
      if (previousFocus?.isConnected) previousFocus.focus();
    };
  }, []);
  return (
    <dialog
      ref={dialogRef}
      className="live-admin admin-case-dialog"
      aria-labelledby={titleId}
      aria-modal="true"
      aria-busy={busy}
      onCancel={(event) => {
        event.preventDefault();
        if (!busy) onClose();
      }}
    >
      <div className="admin-dialog-heading">
        <h2 ref={headingRef} id={titleId} tabIndex={-1}>
          {t(caseId === undefined ? "addCase" : "editCase", { id: caseId })}
        </h2>
        <button
          type="button"
          className="button admin-dialog-close"
          aria-label={t("cancel")}
          disabled={busy}
          onClick={onClose}
        >
          <X size={20} aria-hidden="true" />
        </button>
      </div>
      {caseId === undefined ? (
        <EditorForm
          problemId={problemId}
          onSaved={onSaved}
          onClose={onClose}
          onBusyChange={setBusy}
        />
      ) : query.isPending ? (
        <div className="admin-dialog-state">
          <p role="status">{t("loading")}</p>
        </div>
      ) : query.isError ? (
        <div className="admin-dialog-state">
          <AdminError error={query.error} retry={() => void query.refetch()} />
          <button type="button" className="button" onClick={onClose}>
            {t("cancel")}
          </button>
        </div>
      ) : (
        <EditorForm
          key={query.data.id}
          problemId={problemId}
          testCase={query.data}
          onSaved={onSaved}
          onClose={onClose}
          onBusyChange={setBusy}
        />
      )}
    </dialog>
  );
}
