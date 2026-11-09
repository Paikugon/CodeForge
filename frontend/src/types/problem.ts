import type { TestCase } from "./testcase";
import type { Page } from "./page";
import type { Tag } from "./tag";

export type Problem = {
  id: number;
  title: string;
  slug: string;
  timeLimitMs: number;
  memoryLimitMb: number;
  difficulty: "EASY" | "MEDIUM" | "HARD";
  tags: Tag[];
  acceptedSubmissionCount: number;
  judgedSubmissionCount: number;
  acceptanceRate: number | null;
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
  tags: Tag[];
  timeLimitMs: number;
  memoryLimitMb: number;
  spjEnabled?: boolean;
  spjSource?: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
  testCases: TestCase[];
};