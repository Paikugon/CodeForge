import type { TestCase } from "./testcase";
import type { Page } from "./page";

export type Problem = {
  id: number;
  title: string;
  slug: string;
  timeLimitMs: number;
  memoryLimitMb: number;
  difficulty: "EASY" | "MEDIUM" | "HARD";
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
};

export type ProblemPage = Page<Problem>;

export type ProblemDetail = {
  id: number;
  title: string;
  slug: string;
  difficulty: "EASY" | "MEDIUM" | "HARD";
  description: string;
  timeLimitMs: number;
  memoryLimitMb: number;
  spjEnabled?: boolean;
  spjSource?: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
  testCases: TestCase[];
};