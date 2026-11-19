import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getAdminProblem, getTestCaseStatus, getTestCaseSummaries, deleteTestCasesBatch, updateTestCasesSample, syncTestCases } from "../../api/admin";
import { validAdminId } from "../../utils/adminFiles";
import Table from "../../components/ui/Table";
import { AdminError, AdminPagination, ConfirmDialog, FloatingSelection } from "../../components/admin/AdminUi";
import TestCaseEditor from "../../components/admin/TestCaseEditor";
import TestCaseImportPanel from "../../components/admin/TestCaseImportPanel";

export default function AdminTestCasesPage() {
  const { t } = useTranslation("admin");
  const { id = "" } = useParams();
  const valid = validAdminId(id);
  const client = useQueryClient();
  const [search, setSearch] = useSearchParams();
  const rawPage = Number(search.get("page") ?? 0);
  const page = Number.isSafeInteger(rawPage) && rawPage >= 0 && rawPage <= 1_000_000 ? rawPage : 0;
  const [selected, setSelected] = useState<number[]>([]);
  const [editor, setEditor] = useState<number | null | undefined>();
  const [importing, setImporting] = useState(false);
  const [deleting, setDeleting] = useState<number[] | null>(null);
  const problem = useQuery({ queryKey: ["admin", "problem", id], queryFn: ({ signal }) => getAdminProblem(id, signal), enabled: valid, retry: false, staleTime: 30_000, refetchOnWindowFocus: false });
  const query = useQuery({ queryKey: ["admin", "test-cases", id, page], queryFn: ({ signal }) => getTestCaseSummaries(id, page, signal), enabled: valid && problem.isSuccess, retry: false, refetchOnWindowFocus: false });
  const status = useQuery({ queryKey: ["admin", "test-case-status", id], queryFn: ({ signal }) => getTestCaseStatus(id, signal), enabled: valid && problem.isSuccess, retry: false, refetchOnWindowFocus: false });
  useEffect(() => { setSelected([]); setEditor(undefined); setImporting(false); }, [page, id]);
  const refresh = async () => {
    setSelected([]);
    await Promise.all([
      client.invalidateQueries({ queryKey: ["admin", "test-cases", id] }),
      client.invalidateQueries({ queryKey: ["admin", "test-case-status", id] }),
      client.invalidateQueries({ queryKey: ["admin", "problem", id] }),
      client.invalidateQueries({ queryKey: ["problem"] }),
    ]);
  };
  const sample = useMutation({ mutationFn: (isSample: boolean) => updateTestCasesSample(id, selected, isSample), retry: false, onSuccess: refresh });
  const remove = useMutation({ mutationFn: (ids: number[]) => deleteTestCasesBatch(id, ids), retry: false, onSuccess: async () => { setDeleting(null); await refresh(); } });
  const sync = useMutation({ mutationFn: () => syncTestCases(id), retry: false, onSuccess: refresh, onError: () => { void status.refetch(); } });
  const busy = sample.isPending || remove.isPending || sync.isPending;
  const panelOpen = editor !== undefined || importing;
  const rows = query.data?.content ?? [];

  if (!valid) return <section className="live-admin"><p role="alert">{t("formInvalid")}</p></section>;
  if (problem.isPending) return <p role="status">{t("loading")}</p>;
  if (problem.isError) return <section className="live-admin"><AdminError error={problem.error} retry={() => void problem.refetch()} /></section>;

  return <section className="live-admin admin-selectable"><div className="admin-heading"><h1>{t("caseTitle", { title: problem.data.title })}</h1><Link className="button" to={`/admin/problems/${id}`}>{t("editProblem")}</Link></div>
    <div className="admin-status"><span className={`badge ${status.data?.synced ? "active" : "pending"}`}>{status.data ? t(status.data.synced ? "synced" : "unsynced") : t("loading")}</span>
      <button type="button" className="button" disabled={busy || panelOpen} onClick={() => sync.mutate()}>{t("sync")}</button><button type="button" className="button" disabled={busy || query.isFetching || panelOpen} onClick={() => void refresh()}>{t("refresh")}</button></div>
    <p className="admin-note">{t("syncNote")}</p>
    {status.isError && <AdminError error={status.error} retry={() => void status.refetch()} />}
    {sync.isError && <AdminError error={sync.error} />}
    {sample.isError && <AdminError error={sample.error} />}
    <div className="admin-actions admin-section-actions"><button type="button" className="button primary" disabled={busy || panelOpen} onClick={() => setEditor(null)}>{t("addCase")}</button><button type="button" className="button" disabled={busy || panelOpen} onClick={() => setImporting(true)}>{t("importTitle")}</button><Link to="/admin/problems" className="button">{t("back")}</Link></div>
    {editor !== undefined && <TestCaseEditor key={editor ?? "new"} problemId={id} caseId={editor ?? undefined} onSaved={refresh} onClose={() => setEditor(undefined)} />}
    {importing && <TestCaseImportPanel problemId={id} onSaved={refresh} onClose={() => setImporting(false)} />}
    <FloatingSelection count={selected.length} disabled={busy || panelOpen} onClear={() => setSelected([])}><button type="button" className="button" disabled={busy || panelOpen} onClick={() => sample.mutate(true)}>{t("markSample")}</button><button type="button" className="button" disabled={busy || panelOpen} onClick={() => sample.mutate(false)}>{t("unmarkSample")}</button><button type="button" className="button danger" disabled={busy || panelOpen} onClick={() => { remove.reset(); setDeleting([...selected]); }}>{t("remove")}</button></FloatingSelection>
    {query.isError && <AdminError error={query.error} retry={() => void query.refetch()} />}
    {query.isPending ? <p role="status">{t("loading")}</p> : query.data && <Table label={t("testCases")} className="admin-case-table" footer={<AdminPagination data={query.data} page={page} disabled={busy || panelOpen || query.isFetching} onChange={nextPage => { const next = new URLSearchParams(search); next.set("page", String(nextPage)); setSearch(next); }} />}>
      <thead><tr><th><input type="checkbox" aria-label={t("selectAll")} disabled={busy || panelOpen || !rows.length || query.isFetching} checked={rows.length > 0 && rows.every(row => selected.includes(row.id))} onChange={event => setSelected(event.target.checked ? rows.map(row => row.id) : [])} /></th><th scope="col">{t("id")}</th><th scope="col">{t("order")}</th><th scope="col">{t("sample")}</th><th scope="col">{t("contentSize")}</th><th scope="col">{t("actions")}</th></tr></thead>
      <tbody>{!rows.length ? <tr><td colSpan={6} className="admin-empty">{t("empty")}</td></tr> : rows.map(item => <tr key={item.id}>
        <td><input type="checkbox" aria-label={t("selectRow", { id: item.id })} disabled={busy || panelOpen || query.isFetching} checked={selected.includes(item.id)} onChange={event => setSelected(values => event.target.checked ? [...values, item.id] : values.filter(value => value !== item.id))} /></td><td>#{item.id}</td><td>{item.orderIndex}</td><td>{item.isSample ? <span className="badge active">{t("sample")}</span> : "—"}</td><td>{item.inputCharacters.toLocaleString()} / {item.outputCharacters.toLocaleString()} {t("characterUnit")}</td><td><div className="admin-actions"><button type="button" className="button" disabled={busy || panelOpen} onClick={() => setEditor(item.id)}>{t("edit")}</button><button type="button" className="button danger" disabled={busy || panelOpen} onClick={() => { remove.reset(); setDeleting([item.id]); }}>{t("remove")}</button></div></td>
      </tr>)}</tbody>
    </Table>}
    {deleting && <ConfirmDialog title={t("removeCases", { count: deleting.length })} pending={remove.isPending} onConfirm={() => remove.mutate(deleting)} onCancel={() => setDeleting(null)}><p>{t("removeCasesNote")}</p>{remove.isError && <AdminError error={remove.error} />}</ConfirmDialog>}
  </section>;
}
