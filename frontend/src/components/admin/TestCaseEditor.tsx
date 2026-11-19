import { useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { getAdminTestCase } from "../../api/admin";
import { createTestCase, updateTestCase } from "../../api/testCases";
import type { TestCase, TestCaseRequest } from "../../types/testcase";
import { MAX_CASE_FILE_BYTES } from "../../utils/adminFiles";
import { AdminError, FileTextField } from "./AdminUi";

function EditorForm({ problemId, testCase, onSaved, onClose }: { problemId: string; testCase?: TestCase; onSaved: () => Promise<void>; onClose: () => void }) {
  const { t } = useTranslation("admin");
  const [input, setInput] = useState(testCase?.input ?? "");
  const [output, setOutput] = useState(testCase?.expectedOutput ?? "");
  const [sample, setSample] = useState(testCase?.isSample ?? false);
  const [order, setOrder] = useState(testCase?.orderIndex.toString() ?? "");
  const [reading, setReading] = useState(0);
  const [invalid, setInvalid] = useState(false);
  const mutation = useMutation({ mutationFn: (request: TestCaseRequest) => testCase ? updateTestCase(problemId, testCase.id, request) : createTestCase(problemId, request), retry: false, onSuccess: async () => { await onSaved(); onClose(); } });
  const pending = mutation.isPending || reading > 0;
  return <form className="admin-form" onSubmit={event => {
    event.preventDefault(); if (pending) return;
    const orderIndex = order === "" ? undefined : Number(order);
    if ((orderIndex !== undefined && (!Number.isInteger(orderIndex) || orderIndex < 0 || orderIndex > 2147483647)) || new TextEncoder().encode(input).length > MAX_CASE_FILE_BYTES || new TextEncoder().encode(output).length > MAX_CASE_FILE_BYTES) { setInvalid(true); return; }
    setInvalid(false); mutation.mutate({ input, expectedOutput: output, isSample: sample, orderIndex });
  }}>
    <div className="admin-content-grid">
      <FileTextField id="case-input" label={t("input")} value={input} onChange={setInput} maxBytes={MAX_CASE_FILE_BYTES} maxCharacters={MAX_CASE_FILE_BYTES} disabled={mutation.isPending} onBusyChange={busy => setReading(value => value + (busy ? 1 : -1))} />
      <FileTextField id="case-output" label={t("output")} value={output} onChange={setOutput} maxBytes={MAX_CASE_FILE_BYTES} maxCharacters={MAX_CASE_FILE_BYTES} disabled={mutation.isPending} onBusyChange={busy => setReading(value => value + (busy ? 1 : -1))} />
    </div>
    <div className="admin-filters"><label>{t("order")}<input type="number" min={0} max={2147483647} step={1} placeholder={t("autoOrder")} value={order} disabled={pending} onChange={event => setOrder(event.target.value)} /></label><label className="admin-check"><input type="checkbox" checked={sample} disabled={pending} onChange={event => setSample(event.target.checked)} />{t("sample")}</label></div>
    {invalid && <p role="alert" className="admin-error">{t("caseInvalid")}</p>}
    {mutation.isError && <AdminError error={mutation.error} />}
    <div className="admin-actions"><button className="button primary" type="submit" disabled={pending}>{t(mutation.isPending ? "saving" : "saveCase")}</button><button className="button" type="button" disabled={pending} onClick={onClose}>{t("cancel")}</button></div>
  </form>;
}

export default function TestCaseEditor({ problemId, caseId, onSaved, onClose }: { problemId: string; caseId?: number; onSaved: () => Promise<void>; onClose: () => void }) {
  const { t } = useTranslation("admin");
  const query = useQuery({ queryKey: ["admin", "test-case", problemId, caseId], queryFn: ({ signal }) => getAdminTestCase(problemId, caseId!, signal), enabled: caseId !== undefined, retry: false, gcTime: 0, refetchOnWindowFocus: false });
  return <section className="admin-panel"><h2>{t(caseId === undefined ? "addCase" : "editCase", { id: caseId })}</h2>
    {caseId === undefined ? <EditorForm problemId={problemId} onSaved={onSaved} onClose={onClose} /> : query.isPending ? <p role="status">{t("loading")}</p> : query.isError ? <><AdminError error={query.error} retry={() => void query.refetch()} /><button type="button" className="button" onClick={onClose}>{t("cancel")}</button></> : <EditorForm key={query.data.id} problemId={problemId} testCase={query.data} onSaved={onSaved} onClose={onClose} />}
  </section>;
}
