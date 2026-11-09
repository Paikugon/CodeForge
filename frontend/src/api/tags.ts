import type { Tag } from "../types/tag";
import { apiRequest } from "./http";

export async function getTags(
  signal?: AbortSignal,
): Promise<Tag[]> {
  return apiRequest<Tag[]>("/api/tags", { signal });
}