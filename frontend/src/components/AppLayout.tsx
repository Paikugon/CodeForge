import { useEffect, useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import {
  ChevronRight,
  Code2,
  Menu,
  Moon,
  Sun,
} from "lucide-react";
import LanguageSwitcher from "./LanguageSwitcher";

export default function AppLayout() {
  const { pathname } = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  const [theme, setTheme] = useState<"light" | "dark">(() => {
    try {
      return localStorage.getItem("codeforge-theme") === "dark"
        ? "dark"
        : "light";
    } catch {
      return "dark";
    }
  });

  useEffect(() => {
    document.documentElement.dataset.theme = theme;

    try {
      localStorage.setItem("codeforge-theme", theme);
    } catch {
      // Theme vẫn hoạt động khi browser không cho lưu.
    }
  }, [theme]);

  useEffect(() => {
    setMenuOpen(false);
  }, [pathname]);

  useEffect(() => {
    if (!menuOpen) return;

    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") setMenuOpen(false);
    };

    window.addEventListener("keydown", closeOnEscape);
    return () => window.removeEventListener("keydown", closeOnEscape);
  }, [menuOpen]);

  return (
    <div className="app-shell">
      <aside
        id="main-navigation"
        className={`sidebar ${menuOpen ? "open" : ""}`}
      >
        <Link
          to="/problems"
          className="brand"
          onClick={() => setMenuOpen(false)}
        >
          <span className="brand-icon">
            <Code2 size={24} />
          </span>
          <span>
            CodeForge<span className="brand-dot">.</span>
          </span>
        </Link>

        <div className="workspace-label">YOUR WORKSPACE</div>

        <nav aria-label="Main navigation">
          <NavLink
            to="/problems"
            className={({ isActive }) =>
              `nav-item ${isActive ? "active" : ""}`
            }
            onClick={() => setMenuOpen(false)}
          >
            <Code2 size={19} />
            Practice
          </NavLink>
        </nav>

        <div className="sidebar-bottom">
          <span className="sidebar-caption">HUTECH CAPSTONE · 2026</span>
        </div>
      </aside>

      {menuOpen && (
        <button
          type="button"
          className="mobile-backdrop"
          aria-label="Close navigation"
          onClick={() => setMenuOpen(false)}
        />
      )}

      <div className="main-shell">
        <header className="topbar">
          <div className="breadcrumb">
            <button
              type="button"
              className="icon-button mobile-menu"
              aria-label="Open navigation"
              aria-controls="main-navigation"
              aria-expanded={menuOpen}
              onClick={() => setMenuOpen((open) => !open)}
            >
              <Menu size={20} />
            </button>

            <span className="breadcrumb-root">Workspace</span>
            <ChevronRight size={14} />
            <strong>
              {pathname.startsWith("/problems/") ? "Problem" : "Practice"}
            </strong>
          </div>

          <div className="header-actions">
            <button
              type="button"
              className="icon-button theme-toggle"
              aria-label={`Switch to ${theme === "light" ? "dark" : "light"} mode`}
              onClick={() =>
                setTheme((current) => current === "light" ? "dark" : "light")
              }
            >
              {theme === "light" ? <Moon size={18} /> : <Sun size={18} />}
            </button>

            <div className="live-language">
              <LanguageSwitcher />
            </div>
          </div>
        </header>

        <main className="main-content">
          <Outlet />
        </main>

        <footer className="footer">
          <span>CodeForge / Practice. Solve. Grow.</span>
          <span>Forged through practice.</span>
        </footer>
      </div>
    </div>
  );
}