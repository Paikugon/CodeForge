import CodeMirror from "@uiw/react-codemirror";
import { cpp } from "@codemirror/lang-cpp";
import { java } from "@codemirror/lang-java";
import { python } from "@codemirror/lang-python";

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
  return (
    <CodeMirror
      value={value}
      onChange={onChange}
      extensions={[extensions[language]]}
      height="350px"
      basicSetup={{ tabSize: 4 }}
    />
  );
}