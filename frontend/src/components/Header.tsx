import LanguageSwitcher from "./LanguageSwitcher";

export default function Header() {
  return (
    <header className="site-header">
      <span className="brand">CodeForge</span>
      <LanguageSwitcher />
    </header>
  );
}