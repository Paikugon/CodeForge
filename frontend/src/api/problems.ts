import type { ProblemPage, ProblemDetail } from "../types/problem";
import type { RunCodeRequest, RunCodeResponse } from "../types/run";

type GetProblemsParams = {
  page?: number;
  size?: number;
  sort?: string;
};

export async function getProblems(
  params: GetProblemsParams = {},
  signal?: AbortSignal,
): Promise<ProblemPage> {
  const {
    page = 0,
    size = 20,
    sort = "createdAt,desc",
  } = params;

  const query = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort,
  });

  const response = await fetch(`/api/problems?${query}`, {
    signal,
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  return response.json();
}

export async function getProblemById(
  id: string,
  signal?: AbortSignal,
): Promise<ProblemDetail> {
  const response = await fetch(
    `/api/problems/${encodeURIComponent(id)}`,
    { signal },
  );

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  return response.json();
}

export async function runCode(
  id: string,
  request: RunCodeRequest,
): Promise<RunCodeResponse> {
  const response = await fetch(
    `/api/problems/${encodeURIComponent(id)}/run`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    },
  );

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  return response.json();
}