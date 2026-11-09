import CodeMirror from "@uiw/react-codemirror";
import { cpp } from "@codemirror/lang-cpp";
import { java } from "@codemirror/lang-java";
import { python } from "@codemirror/lang-python";
import { useTheme } from "../context/ThemeContext";

export type Language = "cpp" | "java" | "python";

type CodeEditorProps = {
  language: Language;
  value: string;
  onChange: (value: string) => void;
};

const extensions = {
  cpp: cpp(),
  java: java(),
  python: python(),
};

export default function CodeEditor({
  language,
  value,
  onChange,
}: CodeEditorProps) {

  const { theme } = useTheme();

  return (
    <CodeMirror
      value={value}
      onChange={onChange}
      extensions={[extensions[language]]}
      theme={theme}
      height="350px"
      basicSetup={{ tabSize: 4 }}
    />
  );
}