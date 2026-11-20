export const MAX_CASE_FILE_BYTES = 5 * 1024 * 1024;
export const MAX_IMPORT_BYTES = 50 * 1024 * 1024;
export async function readAdminText(
  file: File,
  maxBytes = MAX_CASE_FILE_BYTES,
): Promise<string> {
  if (file.size > maxBytes) throw new Error("fileTooLarge");
  try {
    const text = new TextDecoder("utf-8", { fatal: true }).decode(
      await file.arrayBuffer(),
    );
    if (text.includes("\0")) throw new Error("fileEncoding");
    return text;
  } catch {
    throw new Error("fileEncoding");
  }
}
export function validateImportFiles(files: File[]): string | undefined {
  if (!files.length || files.length > 200) return "fileCount";
  const zip = files.length === 1 && /\.zip$/i.test(files[0].name);
  if (zip) return files[0].size > MAX_IMPORT_BYTES ? "fileTooLarge" : undefined;
  if (files.some((file) => !/\.(in|out)$/i.test(file.name)))
    return "fileFormat";
  if (
    files.some((file) => file.size > MAX_CASE_FILE_BYTES) ||
    files.reduce((sum, file) => sum + file.size, 0) > MAX_IMPORT_BYTES
  )
    return "fileTooLarge";
}
export function validAdminId(value?: string): value is string {
  return Boolean(
    value &&
    /^\d+$/.test(value) &&
    Number.isSafeInteger(Number(value)) &&
    Number(value) > 0,
  );
}
