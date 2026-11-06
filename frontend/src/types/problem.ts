import type { TestCase } from "./testcase";

export type Problem = {
  id: number;
  title: string;
  difficulty: string;
};

export type ProblemPage = {
  content: Problem[];
  number: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
};

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