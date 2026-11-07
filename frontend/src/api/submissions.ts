import type { Page } from "../types/page";
import type {
  Submission,
  SubmissionListItem,
  SubmissionRequest,
  SubmissionVerdict,
} from "../types/submission";
import { apiRequest, createQuery } from "./http";

export type GetSubmissionsParams = {
  problemId?: number;
  verdict?: SubmissionVerdict;
  page?: number;
  size?: number;
  sort?: string;
};

export async function createSubmission(
  request: SubmissionRequest,
): Promise<Submission> {
  return apiRequest<Submission>("/api/submissions", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export async function getSubmission(
  id: number | string,
  signal?: AbortSignal,
): Promise<Submission> {
  return apiRequest<Submission>(
    `/api/submissions/${encodeURIComponent(id)}`,
    { signal },
  );
}

export async function getSubmissions(
  params: GetSubmissionsParams = {},
  signal?: AbortSignal,
): Promise<Page<SubmissionListItem>> {
  const { page = 0, size = 20, sort = "createdAt,desc", problemId } = params;

  return apiRequest<Page<SubmissionListItem>>(
    `/api/submissions${createQuery({ page, size, sort, problemId })}`,
    { signal },
  );
}

export async function getMySubmissions(
  params: GetSubmissionsParams = {},
  signal?: AbortSignal,
): Promise<Page<SubmissionListItem>> {
  const {
    page = 0,
    size = 20,
    sort = "createdAt,desc",
    problemId,
    verdict,
  } = params;

  return apiRequest<Page<SubmissionListItem>>(
    `/api/submissions/me${createQuery({
      page,
      size,
      sort,
      problemId,
      verdict,
    })}`,
    { signal },
  );
}
