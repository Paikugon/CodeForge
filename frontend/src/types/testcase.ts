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