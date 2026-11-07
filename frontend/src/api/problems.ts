import type { ProblemPage, ProblemDetail } from "../types/problem";
import type { RunCodeRequest, RunCodeResponse } from "../types/run";
import { apiRequest, createQuery } from "./http";

export type ProblemRequest = {
  title: string;
  slug: string;
  description: string;
  difficulty?: "EASY" | "MEDIUM" | "HARD";
  timeLimitMs?: number;
  memoryLimitMb?: number;
  spjEnabled: boolean;
  spjSource?: string | null;
  enabled?: boolean | null;
};

type GetProblemsParams = {
  page?: number;
  size?: number;
  sort?: string;
  enabled?: boolean;
};

export async function getProblems(
  params: GetProblemsParams = {},
  signal?: AbortSignal,
): Promise<ProblemPage> {
  const { page = 0, size = 20, sort = "createdAt,desc", enabled } = params;

  return apiRequest<ProblemPage>(
    `/api/problems${createQuery({ page, size, sort, enabled })}`,
    { signal },
  );
}

export async function getProblemById(
  id: string,
  signal?: AbortSignal,
): Promise<ProblemDetail> {
  return apiRequest<ProblemDetail>(
    `/api/problems/${encodeURIComponent(id)}`,
    { signal },
  );
}

export async function runCode(
  id: string,
  request: RunCodeRequest,
): Promise<RunCodeResponse> {
  return apiRequest<RunCodeResponse>(
    `/api/problems/${encodeURIComponent(id)}/run`,
    {
      method: "POST",
      body: JSON.stringify(request),
    },
  );
}

export async function createProblem(
  request: ProblemRequest,
): Promise<ProblemDetail> {
  return apiRequest<ProblemDetail>("/api/problems", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export async function updateProblem(
  id: number | string,
  request: ProblemRequest,
): Promise<ProblemDetail> {
  return apiRequest<ProblemDetail>(
    `/api/problems/${encodeURIComponent(id)}`,
    {
      method: "PUT",
      body: JSON.stringify(request),
    },
  );
}

export async function deleteProblem(id: number | string): Promise<void> {
  await apiRequest<void>(
    `/api/problems/${encodeURIComponent(id)}`,
    { method: "DELETE" },
  );
}

export async function getSolvedProblems(
  params: GetProblemsParams = {},
  signal?: AbortSignal,
): Promise<ProblemPage> {
  return getUserProblems("/api/problems/me/solved", params, signal);
}

export async function getUnsolvedProblems(
  params: GetProblemsParams = {},
  signal?: AbortSignal,
): Promise<ProblemPage> {
  return getUserProblems("/api/problems/me/unsolved", params, signal);
}

export async function getUserSolvedProblems(
  userId: number | string,
  params: GetProblemsParams = {},
  signal?: AbortSignal,
): Promise<ProblemPage> {
  return getUserProblems(
    `/api/problems/${encodeURIComponent(userId)}/solved`,
    params,
    signal,
  );
}

export async function getUserUnsolvedProblems(
  userId: number | string,
  params: GetProblemsParams = {},
  signal?: AbortSignal,
): Promise<ProblemPage> {
  return getUserProblems(
    `/api/problems/${encodeURIComponent(userId)}/unsolved`,
    params,
    signal,
  );
}

async function getUserProblems(
  path: string,
  params: GetProblemsParams,
  signal?: AbortSignal,
): Promise<ProblemPage> {
  const { page = 0, size = 20, sort = "createdAt,desc" } = params;

  return apiRequest<ProblemPage>(
    `${path}${createQuery({ page, size, sort })}`,
    { signal },
  );
}