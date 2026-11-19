import { apiRequest, createQuery } from "./http";
import type { Page } from "../types/page";
import type { ProblemDetail } from "../types/problem";
import type { TestCase } from "../types/testcase";

export type TestCaseSummary = { id: number; isSample: boolean; orderIndex: number; inputCharacters: number; outputCharacters: number; updatedAt: string };
export type TestCaseStatus = { synced: boolean; totalTestCases: number };
export type ImportCase = { name: string; inputBytes: number; outputBytes: number; inputPreview: string; outputPreview: string };
export type ImportEntry = { name: string; isSample: boolean; orderIndex?: number };

export function getAdminProblem(id: string, signal?: AbortSignal) {
  return apiRequest<ProblemDetail>(`/api/problems/${encodeURIComponent(id)}/authoring`, { signal });
}
export function setProblemVisibility(ids: number[], enabled: boolean) {
  return apiRequest<void>("/api/problems/batch-visibility", { method: "PATCH", body: JSON.stringify({ ids, enabled }) });
}
const base = (id: string) => `/api/problems/${encodeURIComponent(id)}/test-cases`;
export function getTestCaseSummaries(id: string, page = 0, signal?: AbortSignal) {
  return apiRequest<Page<TestCaseSummary>>(`${base(id)}/summaries${createQuery({ page, size: 20 })}`, { signal });
}
export function getAdminTestCase(id: string, testId: number, signal?: AbortSignal) {
  return apiRequest<TestCase>(`${base(id)}/${testId}`, { signal });
}
export function getTestCaseStatus(id: string, signal?: AbortSignal) {
  return apiRequest<TestCaseStatus>(`${base(id)}/status`, { signal });
}
export function syncTestCases(id: string) { return apiRequest<void>(`${base(id)}/sync`, { method: "POST" }); }
export function updateTestCasesSample(id: string, ids: number[], isSample: boolean) {
  return apiRequest<void>(`${base(id)}/batch`, { method: "PATCH", body: JSON.stringify({ ids, isSample }) });
}
export function deleteTestCasesBatch(id: string, ids: number[]) {
  return apiRequest<void>(`${base(id)}/batch-delete`, { method: "POST", body: JSON.stringify({ ids }) });
}
function importBody(files: File[], entries?: ImportEntry[]) {
  const body = new FormData();
  files.forEach(file => body.append("files", file));
  if (entries) body.append("manifest", new Blob([JSON.stringify({ testCases: entries })], { type: "application/json" }));
  return body;
}
export function previewTestCaseImport(id: string, files: File[]) {
  return apiRequest<{ testCases: ImportCase[] }>(`${base(id)}/import/preview`, { method: "POST", body: importBody(files) });
}
export function importTestCases(id: string, files: File[], entries: ImportEntry[]) {
  return apiRequest<{ createdCount: number }>(`${base(id)}/import`, { method: "POST", body: importBody(files, entries) });
}
