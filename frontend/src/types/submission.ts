export type SubmissionStatus =
  | "PENDING"
  | "JUDGING"
  | "COMPLETED"
  | "FAILED";

export type SubmissionVerdict =
  | "AC"
  | "WA"
  | "TLE"
  | "MLE"
  | "RE"
  | "CE"
  | "SE";

export type SubmissionLanguage = "CPP" | "JAVA" | "PYTHON" | string;

export type SubmissionRequest = {
  problemId: number;
  language: string;
  sourceCode: string;
};

export type FailedTestCase = {
  testCase: string;
  input: string;
  expectedOutput: string;
  actualOutput: string;
};

export type Submission = {
  id: number;
  userId: number;
  username: string;
  problemId: number;
  language: string;
  sourceCode?: string;
  status: SubmissionStatus;
  verdict: SubmissionVerdict | null;
  executionTimeMs: number | null;
  memoryUsedKb: number | null;
  passedTestCases: number | null;
  totalTestCases: number | null;
  compileOutput: string | null;
  errorMessage: string | null;
  failedOutput: string | null;
  failedTestCase: FailedTestCase | null;
  createdAt: string;
  updatedAt: string;
};

export type SubmissionListItem = Omit<
  Submission,
  | "sourceCode"
  | "compileOutput"
  | "errorMessage"
  | "failedOutput"
  | "failedTestCase"
>;
