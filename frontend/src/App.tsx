import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import AppLayout from "./components/AppLayout";

const ProblemList = lazy(() => import("./components/ProblemList"));
const ProblemDetailPage = lazy(() => import("./pages/ProblemDetailPage"));
const SubmissionDetailPage = lazy(() => import("./pages/SubmissionDetailPage"));
const LoginPage = lazy(() => import("./pages/LoginPage"));
const SubmissionHistoryPage = lazy(() => import("./pages/SubmissionHistoryPage"));
const RegisterPage = lazy(() => import("./pages/RegisterPage"));
const ProfilePage = lazy(() => import("./pages/ProfilePage"));

export default function App() {
  return (
    <Suspense fallback={<p role="status">Loading…</p>}>
      <Routes>
        <Route element={<AppLayout />}>
          <Route index element={<Navigate to="/problems" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/profile" element={<ProfilePage own />} />
          <Route path="/users/:id" element={<ProfilePage />} />
          <Route path="/problems" element={<ProblemList />} />
          <Route path="/problems/:id" element={<ProblemDetailPage />} />
          <Route path="/submissions/:id" element={<SubmissionDetailPage />} />
          <Route path="/submissions" element={<SubmissionHistoryPage />} />
        </Route>
      </Routes>
    </Suspense>
  );
}
