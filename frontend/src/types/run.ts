export type RunCodeRequest = {
  language: string;
  sourceCode: string;
  input: string;
};

export type RunCodeResponse = {
  resultCode: number | null;
  stdout: string | null;
  exitCode: number | null;
  executionTimeMs: number | null;
  memoryUsedKb: number | null;
  compileOutput: string | null;
};