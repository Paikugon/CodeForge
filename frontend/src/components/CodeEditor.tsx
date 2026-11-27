import { useTranslation } from "react-i18next";
import CodeMirror from "@uiw/react-codemirror";
import { useTheme } from "../context/ThemeContext";
import { useLanguageExtension } from "./useLanguageExtension";

export type Language = "cpp" | "java" | "python";

type CodeEditorProps = {
  language: Language;
  value: string;
  onChange: (value: string) => void;
};

export default function CodeEditor({
  language,
  value,
  onChange,
}: CodeEditorProps) {
  const { theme } = useTheme();
  const { t } = useTranslation();
  const { extension, failed } = useLanguageExtension(language);

  return (
    <>
      <CodeMirror
        value={value}
        onChange={onChange}
        extensions={extension ? [extension] : []}
        theme={theme}
        height="350px"
        basicSetup={{ tabSize: 4 }}
      />
      {failed && <p role="alert">{t("app.languageSupportError")}</p>}
    </>
  );
}
