import { Navigate, Route, Routes } from "react-router-dom";
import AppLayout from "./components/AppLayout";
import ProblemList from "./components/ProblemList";
import ProblemDetailPage from "./pages/ProblemDetailPage";

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/problems" replace />} />
        <Route path="/problems" element={<ProblemList />} />
        <Route path="/problems/:id" element={<ProblemDetailPage />} />
      </Route>
    </Routes>
  );
}