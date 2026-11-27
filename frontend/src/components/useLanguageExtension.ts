import { useEffect, useState } from "react";
import type { ComponentProps } from "react";
import CodeMirror from "@uiw/react-codemirror";

type LanguageExtension = NonNullable<
  ComponentProps<typeof CodeMirror>["extensions"]
>[number];

async function loadLanguageExtension(
  language: string,
): Promise<LanguageExtension | undefined> {
  switch (language) {
    case "cpp":
      return (await import("@codemirror/lang-cpp")).cpp();
    case "java":
      return (await import("@codemirror/lang-java")).java();
    case "python":
      return (await import("@codemirror/lang-python")).python();
    default:
      return undefined;
  }
}

export function useLanguageExtension(language: string) {
  const [extension, setExtension] = useState<LanguageExtension>();
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let active = true;
    setExtension(undefined);
    setFailed(false);

    void loadLanguageExtension(language).then(
      (loadedExtension) => {
        if (active) setExtension(loadedExtension);
      },
      () => {
        if (active) setFailed(true);
      },
    );

    return () => {
      active = false;
    };
  }, [language]);

  return { extension, failed };
}
