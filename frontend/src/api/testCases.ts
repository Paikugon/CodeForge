import type { Page } from "../types/page";
import type { TestCase, TestCaseRequest } from "../types/testcase";
import { apiRequest, createQuery } from "./http";

export type GetTestCasesParams = {
  page?: number;
  size?: number;
  sort?: string;
};

export async function getTestCases(
  problemId: number | string,
  params: GetTestCasesParams = {},
  signal?: AbortSignal,
): Promise<Page<TestCase>> {
  const { page = 0, size = 100, sort = "orderIndex,asc" } = params;

  return apiRequest<Page<TestCase>>(
    `/api/problems/${encodeURIComponent(problemId)}/test-cases${createQuery({
      page,
      size,
      sort,
    })}`,
    { signal },
  );
}

export async function createTestCase(
  problemId: number | string,
  request: TestCaseRequest,
): Promise<TestCase> {
  return apiRequest<TestCase>(
    `/api/problems/${encodeURIComponent(problemId)}/test-cases`,
    {
      method: "POST",
      body: JSON.stringify(request),
    },
  );
}

export async function updateTestCase(
  problemId: number | string,
  testCaseId: number | string,
  request: TestCaseRequest,
): Promise<TestCase> {
  return apiRequest<TestCase>(
    `/api/problems/${encodeURIComponent(problemId)}/test-cases/${encodeURIComponent(testCaseId)}`,
    {
      method: "PUT",
      body: JSON.stringify(request),
    },
  );
}

export async function deleteTestCase(
  problemId: number | string,
  testCaseId: number | string,
): Promise<void> {
  await apiRequest<void>(
    `/api/problems/${encodeURIComponent(problemId)}/test-cases/${encodeURIComponent(testCaseId)}`,
    { method: "DELETE" },
  );
}
