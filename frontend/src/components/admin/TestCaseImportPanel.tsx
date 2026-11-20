import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import {
  importTestCases,
  previewTestCaseImport,
  type ImportCase,
  type ImportEntry,
} from "../../api/admin";
import { validateImportFiles } from "../../utils/adminFiles";
import { AdminError } from "./AdminUi";

export default function TestCaseImportPanel({
  problemId,
  onSaved,
  onClose,
}: {
  problemId: string;
  onSaved: () => Promise<void>;
  onClose: () => void;
}) {
  const { t } = useTranslation("admin");
  const [files, setFiles] = useState<File[]>([]);
  const [cases, setCases] = useState<
    (ImportCase & { isSample: boolean; order: string })[]
  >([]);
  const [invalid, setInvalid] = useState("");
  const [imported, setImported] = useState<number>();
  const preview = useMutation({
    mutationFn: () => previewTestCaseImport(problemId, files),
    retry: false,
    onSuccess: (result) => {
      setCases(
        [...result.testCases]
          .sort((a, b) =>
            a.name.localeCompare(b.name, undefined, { numeric: true }),
          )
          .map((item) => ({ ...item, isSample: false, order: "" })),
      );
    },
  });
  const commit = useMutation({
    mutationFn: (entries: ImportEntry[]) =>
      importTestCases(problemId, files, entries),
    retry: false,
    onSuccess: async (result) => {
      setImported(result.createdCount);
      setCases([]);
      setFiles([]);
      await onSaved();
    },
  });
  const pending = preview.isPending || commit.isPending;

  return (
    <section className="admin-panel" aria-labelledby="import-title">
      <div className="admin-heading">
        <h2 id="import-title">{t("importTitle")}</h2>
      </div>
      <p className="admin-note">{t("importHint")}</p>
      <div className="admin-file-row">
        <input
          type="file"
          aria-label={t("chooseFile")}
          accept=".zip,.in,.out"
          multiple
          disabled={pending}
          onChange={(event) => {
            const next = Array.from(event.target.files ?? []);
            event.target.value = "";
            setFiles(next);
            setCases([]);
            setInvalid(validateImportFiles(next) ?? "");
            setImported(undefined);
            preview.reset();
            commit.reset();
          }}
        />
        {!cases.length && (
          <div className="admin-actions">
            <button
              type="button"
              className="button primary"
              disabled={pending || !files.length || Boolean(invalid)}
              onClick={() => {
                setInvalid(validateImportFiles(files) ?? "");
                if (!validateImportFiles(files)) preview.mutate();
              }}
            >
              {t(preview.isPending ? "previewing" : "preview")}
            </button>
            <button
              type="button"
              className="button"
              disabled={pending}
              onClick={onClose}
            >
              {t("cancel")}
            </button>
          </div>
        )}
      </div>
      {files.length > 0 && (
        <p className="admin-note">
          {files.length} file ·{" "}
          {(
            files.reduce((sum, file) => sum + file.size, 0) /
            1024 /
            1024
          ).toFixed(2)}{" "}
          MiB
        </p>
      )}
      {invalid && (
        <p className="admin-error" role="alert">
          {t(invalid)}
        </p>
      )}
      {preview.isError && <AdminError error={preview.error} />}
      {commit.isError && (
        <>
          <AdminError error={commit.error} />
          <p className="admin-note">{t("unknownResult")}</p>
        </>
      )}
      {imported !== undefined && (
        <p className="admin-success" role="status">
          {t("imported", { count: imported })}
        </p>
      )}
      {cases.length > 0 && (
        <>
          <p className="admin-note">{t("previewNote")}</p>
          <div
            className="table-scroll admin-preview-scroll"
            role="region"
            aria-label={t("importTitle")}
            tabIndex={0}
          >
            <table className="data-table admin-import-table">
              <thead>
                <tr>
                  <th scope="col">{t("filename")}</th>
                  <th scope="col">{t("bytes")}</th>
                  <th scope="col">{t("sample")}</th>
                  <th scope="col">{t("order")}</th>
                </tr>
              </thead>
              <tbody>
                {cases.map((item, index) => (
                  <tr key={item.name}>
                    <td>
                      <strong>{item.name}</strong>
                      <details>
                        <summary>{t("previewContent")}</summary>
                        <div className="admin-preview-pair">
                          <div>
                            <span>{t("input")}</span>
                            <pre>{item.inputPreview}</pre>
                          </div>
                          <div>
                            <span>{t("output")}</span>
                            <pre>{item.outputPreview}</pre>
                          </div>
                        </div>
                      </details>
                    </td>
                    <td>
                      {(item.inputBytes / 1024).toFixed(1)} /{" "}
                      {(item.outputBytes / 1024).toFixed(1)} KiB
                    </td>
                    <td>
                      <input
                        type="checkbox"
                        aria-label={`${t("sample")} ${item.name}`}
                        checked={item.isSample}
                        disabled={pending}
                        onChange={(event) =>
                          setCases((values) =>
                            values.map((entry, i) =>
                              i === index
                                ? { ...entry, isSample: event.target.checked }
                                : entry,
                            ),
                          )
                        }
                      />
                    </td>
                    <td>
                      <input
                        type="number"
                        aria-label={`${t("order")} ${item.name}`}
                        min={0}
                        max={2147483647}
                        step={1}
                        placeholder={t("autoOrder")}
                        value={item.order}
                        disabled={pending}
                        onChange={(event) =>
                          setCases((values) =>
                            values.map((entry, i) =>
                              i === index
                                ? { ...entry, order: event.target.value }
                                : entry,
                            ),
                          )
                        }
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="admin-actions">
            <button
              className="button primary"
              type="button"
              disabled={pending}
              onClick={() => {
                if (
                  cases.some(
                    (item) =>
                      item.order !== "" &&
                      (!Number.isInteger(Number(item.order)) ||
                        Number(item.order) < 0 ||
                        Number(item.order) > 2147483647),
                  )
                ) {
                  setInvalid("caseInvalid");
                  return;
                }
                setInvalid("");
                commit.mutate(
                  cases.map((item) => ({
                    name: item.name,
                    isSample: item.isSample,
                    orderIndex:
                      item.order === "" ? undefined : Number(item.order),
                  })),
                );
              }}
            >
              {t(commit.isPending ? "importing" : "importConfirm", {
                count: cases.length,
              })}
            </button>
            <button
              type="button"
              className="button"
              disabled={pending}
              onClick={() => setCases([])}
            >
              {t("discard")}
            </button>
          </div>
        </>
      )}
    </section>
  );
}
