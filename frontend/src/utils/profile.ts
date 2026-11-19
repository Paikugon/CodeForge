export function profileInitials(
  displayName: string | null | undefined,
  username: string,
): string {
  const name = displayName?.trim() || username;
  const words = name.split(/\s+/);
  const initials =
    words.length > 1
      ? `${Array.from(words[0])[0] ?? ""}${Array.from(words[words.length - 1])[0] ?? ""}`
      : Array.from(name).slice(0, 2).join("");
  return initials.toUpperCase();
}
