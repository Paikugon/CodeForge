export type TestCase = {
  id: number;
  problemId: number;
  input: string;
  expectedOutput: string;
  isSample: boolean | null;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
};

export type TestCaseRequest = {
  input: string;
  expectedOutput: string;
  isSample?: boolean | null;
  orderIndex?: number;
};