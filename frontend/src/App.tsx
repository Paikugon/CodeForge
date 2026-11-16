import { Navigate, Route, Routes } from "react-router-dom";
import AppLayout from "./components/AppLayout";
import ProblemList from "./components/ProblemList";
import ProblemDetailPage from "./pages/ProblemDetailPage";
import SubmissionDetailPage from "./pages/SubmissionDetailPage";
import LoginPage from "./pages/LoginPage";

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/problems" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/problems" element={<ProblemList />} />
        <Route path="/problems/:id" element={<ProblemDetailPage />} />
        <Route path="/submissions/:id" element={<SubmissionDetailPage />} />
      </Route>
    </Routes>
  );
}