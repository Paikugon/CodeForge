import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../context/AuthContext";
import { PageSkeleton } from "./ui/Skeleton";

export default function PasswordChangeGuard() {
  const {
    hasToken,
    authLoading,
    authError,
    retryAuth,
    passwordChangeRequired,
  } = useAuth();

  const location = useLocation();
  const { t } = useTranslation();

  if (!hasToken) return <Outlet />;

  if (authLoading) {
    return <PageSkeleton label={t("profile:loading")} />;
  }

  if (authError) {
    return (
      <section className="guard" role="alert">
        <p>{t("profile:loadError")}</p>
        <button type="button" className="button" onClick={retryAuth}>
          {t("profile:retry")}
        </button>
      </section>
    );
  }

  const onSecurityPage =
    location.pathname === "/profile" &&
    new URLSearchParams(location.search).get("tab") === "security";

  if (passwordChangeRequired && !onSecurityPage) {
    return <Navigate to="/profile?tab=security" replace />;
  }

  return <Outlet />;
}
