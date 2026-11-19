import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./i18n";
import App from "./App";
import "./styles/globals.css";
import "./styles/components/tags.css";
import "./styles/pages/profile.css";
import "./styles/pages/problems.css";
import "./styles/components/table.css";
import "./styles/pages/problem-detail.css";
import "./styles/pages/auth.css";
import "./styles/pages/submissions.css";
import "./styles/app-layout.css";
import "./styles/components/button.css";
import "./styles/theme.css";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";
import { ThemeProvider } from "./context/ThemeContext";
import { AuthProvider } from "./context/AuthContext";

const queryClient = new QueryClient();

const rootElement = document.getElementById("root");

if (!rootElement) {
  throw new Error("Không tìm thấy phần tử root");
}

createRoot(rootElement).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ThemeProvider>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </ThemeProvider>
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>,
);
