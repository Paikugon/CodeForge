import type { HealthResponse } from "../types/health";
import { apiRequest } from "./http";

export function getHealth(signal?: AbortSignal): Promise<HealthResponse> {
  return apiRequest<HealthResponse>("/api/health", { signal, token: null });
}
