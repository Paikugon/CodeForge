import { Navigate, Route, Routes } from "react-router-dom";
import Header from "./components/Header";
import ProblemList from "./components/ProblemList";
import ProblemDetailPage from "./pages/ProblemDetailPage";

export default function App() {
  return (
    <>
      <Header />

      <main className="page-content">
        <Routes>
          <Route
            path="/"
            element={<Navigate to="/problems" replace />}
          />

          <Route path="/problems" element={<ProblemList />} />

          <Route
            path="/problems/:id"
            element={<ProblemDetailPage />}
          />
        </Routes>
      </main>
    </>
  );
}