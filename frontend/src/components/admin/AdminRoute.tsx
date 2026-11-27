import { useQuery } from "@tanstack/react-query";
import { Link, Navigate, Outlet, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../context/AuthContext";
import { getMyProfile } from "../../api/users";
import { AdminError } from "./AdminUi";
import { PageSkeleton } from "../ui/Skeleton";

export default function AdminRoute() {
  const { t } = useTranslation("admin");
  const { hasToken } = useAuth();
  const location = useLocation();
  const query = useQuery({
    queryKey: ["auth", "me"],
    queryFn: ({ signal }) => getMyProfile(signal),
    enabled: hasToken,
    retry: false,
    staleTime: 60_000,
    refetchOnWindowFocus: false,
  });
  if (!hasToken)
    return (
      <section className="live-admin admin-state">
        <h1>{t("title")}</h1>
        <p>{t("loginRequired")}</p>
        <Link
          className="button primary"
          to="/login"
          state={{ from: location.pathname + location.search }}
        >
          {t("login")}
        </Link>
      </section>
    );
  if (query.isPending) return <PageSkeleton label={t("loading")} />;
  if (query.isError)
    return (
      <section className="live-admin">
        <AdminError error={query.error} retry={() => void query.refetch()} />
      </section>
    );
  if (query.data.mustChangePassword)
    return <Navigate to="/profile?tab=security" replace />;
  if (!["ADMIN", "PROBLEM_MANAGER"].includes(query.data.role ?? ""))
    return (
      <section className="live-admin admin-state">
        <h1>{t("forbidden")}</h1>
        <Link to="/problems" className="button">
          {t("browse")}
        </Link>
      </section>
    );
  return <Outlet />;
}
