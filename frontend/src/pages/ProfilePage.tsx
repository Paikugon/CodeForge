import { useQuery, useQueries, useQueryClient } from "@tanstack/react-query";
import { Link, useLocation, useParams, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { CheckCircle2, ChevronRight, Clock3, Code2, FileClock, LoaderCircle, Pencil, RefreshCw, ShieldCheck, UserRound } from "lucide-react";
import { getMyProfile, getUserProfile } from "../api/users";
import { getSolvedProblems, getUnsolvedProblems, getUserSolvedProblems, getUserUnsolvedProblems } from "../api/problems";
import { ApiError } from "../api/http";
import { useAuth } from "../context/AuthContext";
import Table from "../components/ui/Table";
import TableRow from "../components/ui/TableRow";
import TablePagination from "../components/ui/TablePagination";
import ChangePasswordForm from "../components/ChangePasswordForm";
import EditProfileForm from "../components/EditProfileForm";
import { profileInitials } from "../utils/profile";
import type { UserProfile } from "../types/user";
import type { Problem } from "../types/problem";
import "../styles/pages/account.css";
import "../styles/pages/profile-live.css";

const difficulties: Problem["difficulty"][] = ["EASY", "MEDIUM", "HARD"];
const pageSize = 10;

function DifficultySummary({ profile, own }: { profile: UserProfile; own: boolean }) {
  const { t } = useTranslation("profile");
  const queries = useQueries({ queries: difficulties.map(difficulty => ({
    queryKey: ["profile", own ? "private" : "public", profile.id, "difficulty", difficulty],
    queryFn: async ({ signal }: { signal: AbortSignal }) => {
      const params = { size: 1, difficulty };
      const result = own ? await getSolvedProblems(params, signal) : await getUserSolvedProblems(profile.id, params, signal);
      return result.totalElements;
    },
    staleTime: 60_000, refetchOnWindowFocus: false, retry: false,
  })) });
  const ready = queries.every(query => query.isSuccess);
  const total = queries.reduce((sum, query) => sum + (query.data ?? 0), 0);

  return <section className="account-difficulty" aria-labelledby="difficulty-title">
    <h2 id="difficulty-title">{t("difficultyTitle")}</h2>
    <p>{t("difficultyDescription")}</p>
    <dl className="account-difficulty-grid">
      {difficulties.map((difficulty, index) => <div className={`account-difficulty-stat ${difficulty.toLowerCase()}`} key={difficulty}>
        <dt>{t(`difficulty.${difficulty}`)}</dt>
        <dd>{queries[index].isSuccess ? queries[index].data : "—"}</dd>
        {ready && <div className="account-difficulty-track" aria-hidden="true"><span style={{ width: `${total ? (queries[index].data ?? 0) / total * 100 : 0}%` }} /></div>}
      </div>)}
    </dl>
    {queries.some(query => query.isError) && <div className="account-inline-error" role="alert">
      <span>{t("difficultyError")}</span>
      <button type="button" className="button" onClick={() => queries.forEach(query => { if (query.isError) void query.refetch(); })}>{t("retry")}</button>
    </div>}
  </section>;
}

function ProfileProblems({ profile, own }: { profile: UserProfile; own: boolean }) {
  const { t } = useTranslation("profile");
  const [search, setSearch] = useSearchParams();
  const { pathname } = useLocation();
  const attempted = search.get("tab") === "attempted";
  const rawPage = Number(search.get("page") ?? 0);
  const page = Number.isSafeInteger(rawPage) && rawPage >= 0 && rawPage <= 1_000_000 ? rawPage : 0;
  const rawDifficulty = search.get("difficulty") as Problem["difficulty"] | null;
  const difficulty = rawDifficulty && difficulties.includes(rawDifficulty) ? rawDifficulty : undefined;
  const query = useQuery({
    queryKey: ["profile", own ? "private" : "public", profile.id, "problems", { attempted, page, difficulty }],
    queryFn: ({ signal }) => {
      const params = { page, size: pageSize, sort: "createdAt,desc", difficulty };
      if (own) return (attempted ? getUnsolvedProblems : getSolvedProblems)(params, signal);
      return (attempted ? getUserUnsolvedProblems : getUserSolvedProblems)(profile.id, params, signal);
    },
    staleTime: 30_000, retry: false, refetchOnWindowFocus: false,
  });
  const data = query.data;
  const outOfRange = data && page > 0 && data.content.length === 0;
  const tabLink = (inProgress: boolean) => {
    const next = new URLSearchParams(search);
    next.delete("page");
    if (inProgress) next.set("tab", "attempted"); else next.delete("tab");
    return `${pathname}${next.size ? `?${next}` : ""}`;
  };

  return <section className="account-progress" aria-labelledby="progress-title">
    <h2 id="progress-title">{t("progress")}</h2>
    <div className="account-progress-toolbar">
      <nav className="account-progress-tabs" aria-label={t("progress")}>
        <Link to={tabLink(false)} aria-current={!attempted ? "page" : undefined}>{t("solved")} <span>{profile.solvedCount}</span></Link>
        <Link to={tabLink(true)} aria-current={attempted ? "page" : undefined}>{t("attempted")} <span>{profile.unsolvedCount}</span></Link>
      </nav>
      <form key={difficulty ?? "all"} className="account-difficulty-filter" onSubmit={event => {
        event.preventDefault();
        const value = String(new FormData(event.currentTarget).get("difficulty") ?? "");
        const next = new URLSearchParams(search); next.delete("page");
        if (value) next.set("difficulty", value); else next.delete("difficulty");
        setSearch(next);
      }}>
        <select name="difficulty" defaultValue={difficulty ?? ""} aria-label={t("problemDifficulty")}>
          <option value="">{t("allDifficulties")}</option>
          {difficulties.map(value => <option key={value} value={value}>{t(`difficulty.${value}`)}</option>)}
        </select>
        <button type="submit" className="button">{t("apply")}</button>
      </form>
    </div>
    {query.isError && <div className="account-inline-error" role="alert"><span>{t("listError")}</span>
      <button type="button" className="button" disabled={query.isFetching} onClick={() => void query.refetch()}>{t("retry")}</button>
    </div>}
    {query.isPending ? <p role="status" className="account-list-state">{t("loading")}</p> : data && <>
      <Table label={t("tableLabel")} className="account-problem-table" aria-busy={query.isFetching} footer={outOfRange ?
        <div className="account-list-state"><button type="button" className="button" onClick={() => { const next = new URLSearchParams(search); next.delete("page"); setSearch(next); }}>{t("firstPage")}</button></div> :
        <TablePagination page={page} pageSize={pageSize} totalPages={data.totalPages} totalElements={data.totalElements}
          disabled={query.isFetching} onPageChange={nextPage => { const next = new URLSearchParams(search); next.set("page", String(nextPage)); setSearch(next); }}
          labels={{ label: t("pagination.label"), previous: t("pagination.previous"), next: t("pagination.next"),
            results: t("pagination.results", { from: data.totalElements ? page * pageSize + 1 : 0, to: Math.min((page + 1) * pageSize, data.totalElements), total: data.totalElements }),
            page: t("pagination.page", { current: page + 1, total: data.totalPages }),
          }} />
      }>
        <thead><tr><th scope="col">{t("problemId")}</th><th scope="col">{t("problem")}</th><th scope="col">{t("problemDifficulty")}</th><th scope="col">{t("communityAcceptance")}</th></tr></thead>
        <tbody>{data.content.length === 0 ? <tr><td colSpan={4} className="account-empty">
          <Code2 size={28} aria-hidden="true" />
          <h3>{t(outOfRange ? "outOfRange" : attempted ? "attemptedEmpty" : "solvedEmpty")}</h3>
          {!outOfRange && <><p>{t(own ? "emptyDescription" : "publicEmptyDescription")}</p><Link to="/problems" className="button">{t("browse")}</Link></>}
        </td></tr> : data.content.map(problem => <TableRow key={problem.id} className="table-link-row">
          <td className="account-problem-id">#{problem.id}</td>
          <td><Link to={`/problems/${problem.id}`} className="row-link account-problem-title">{problem.title}</Link></td>
          <td><span className={`badge ${problem.difficulty.toLowerCase()}`}>{t(`difficulty.${problem.difficulty}`)}</span></td>
          <td>{problem.acceptanceRate == null ? "—" : `${problem.acceptanceRate.toFixed(1)}%`}</td>
        </TableRow>)}</tbody>
      </Table>
      {data.content.length > 0 && <p className="account-table-note">{t("communityAcceptanceNote")}</p>}
      {!difficulty && data.totalElements !== (attempted ? profile.unsolvedCount : profile.solvedCount) && <p className="account-table-note">{t("catalogNote")}</p>}
    </>}
  </section>;
}

function ProfileContent({ profile, own }: { profile: UserProfile; own: boolean }) {
  const { t, i18n } = useTranslation("profile");
  const [search] = useSearchParams();
  const required = own && Boolean(profile.mustChangePassword);
  const security = own && (search.get("tab") === "security" || required);
  const editing = own && !required && search.get("tab") === "edit";
  const joined = new Date(profile.createdAt);
  const joinedText = Number.isNaN(joined.getTime()) ? "—" : new Intl.DateTimeFormat(i18n.language.startsWith("vi") ? "vi-VN" : "en-GB", {
    timeZone: "Asia/Ho_Chi_Minh", month: "long", year: "numeric",
  }).format(joined);
  const metrics = [
    { key: "solved", value: profile.solvedCount, icon: CheckCircle2 },
    { key: "attempted", value: profile.unsolvedCount, icon: Code2 },
    { key: "accepted", value: profile.acceptedSubmissionCount, icon: ShieldCheck },
    { key: "submissions", value: profile.submissionCount, icon: FileClock },
  ];

  return <div className="account-profile-layout">
    <aside className="account-profile-card" aria-label={profile.username}>
      <div className="account-profile-cover" aria-hidden="true"><Code2 size={64} strokeWidth={2} /></div>
      <div className="account-profile-identity">
        <span className="account-profile-avatar" aria-hidden="true">{profileInitials(profile.displayName, profile.username)}</span>
        <h2>{profile.displayName?.trim() || profile.username}</h2>
        <p className="account-profile-handle">@{profile.username}</p>
        {profile.bio?.trim() && <p className="account-profile-bio">{profile.bio}</p>}
        <p className="account-joined"><Clock3 size={14} aria-hidden="true" />{t("joined", { date: joinedText })}</p>
        {own && <>
          <div className="account-private-info">
            <h3>{t("accountDetails")}</h3>
            <dl><dt>{t("email")}</dt><dd>{profile.email ?? "—"}</dd>
              <dt>{t("role")}</dt><dd>{profile.role ? t(`roleNames.${profile.role}`) : "—"}</dd></dl>
            <p>{t("privateInfo")}</p>
          </div>
          <Link className="button account-card-link" to={`/users/${profile.id}`}>{t("viewPublic")}<ChevronRight size={14} aria-hidden="true" /></Link>
          {!required && <Link className="button account-card-link account-edit-link" to="/profile?tab=edit"><Pencil size={14} aria-hidden="true" />{t("editProfile")}</Link>}
          {!required && <Link className="account-history-link" to="/submissions?scope=mine">{t("history")}</Link>}
        </>}
      </div>
    </aside>
    <div className="account-profile-main">
      <dl className="account-profile-metrics">
        {metrics.map(({ key, value, icon: Icon }) => <div key={key}><dt><Icon size={16} aria-hidden="true" />{t(key)}</dt><dd>{value}</dd></div>)}
      </dl>
      {own && <nav className="account-section-tabs" aria-label={t("mine")}>
        {!required && <>
          <Link to="/profile" aria-current={!security && !editing ? "page" : undefined}>{t("progress")}</Link>
          <Link to="/profile?tab=edit" aria-current={editing ? "page" : undefined}>{t("personalDetails")}</Link>
        </>}
        <Link to="/profile?tab=security" aria-current={security ? "page" : undefined}>{t("security")}</Link>
      </nav>}
      {security ? <ChangePasswordForm required={required} /> : editing ? <EditProfileForm profile={profile} /> : <>
        <DifficultySummary profile={profile} own={own} />
        <ProfileProblems profile={profile} own={own} />
      </>}
    </div>
  </div>;
}

export default function ProfilePage({ own = false }: { own?: boolean }) {
  const { t } = useTranslation("profile");
  const { id } = useParams();
  const { hasToken } = useAuth();
  const location = useLocation();
  const client = useQueryClient();
  const parsedId = id && /^\d+$/.test(id) && Number.isSafeInteger(Number(id)) && Number(id) > 0 ? Number(id) : undefined;
  const valid = own || parsedId !== undefined;
  const query = useQuery({
    queryKey: own ? ["auth", "me"] : ["public-profile", parsedId],
    queryFn: ({ signal }) => own ? getMyProfile(signal) : getUserProfile(parsedId!, signal),
    enabled: valid && (!own || hasToken),
    retry: false, staleTime: 60_000, refetchOnWindowFocus: false,
  });
  const status = query.error instanceof ApiError ? query.error.status : undefined;
  const needsLogin = own && (!hasToken || status === 401);
  const blocked = status === 403 || status === 404;
  const data = query.data;

  return <section className="live-profile" aria-labelledby="profile-title">
    <div className="account-page-heading">
      <h1 id="profile-title">{t(own ? "mine" : "public")}</h1>
      {data && !needsLogin && !blocked && <button type="button" className="button" disabled={query.isFetching} onClick={() => {
        void query.refetch();
        void client.invalidateQueries({ queryKey: ["profile", own ? "private" : "public", data.id] });
      }}><RefreshCw size={15} aria-hidden="true" />{t("refresh")}</button>}
    </div>
    {!valid ? <div className="account-page-state"><h2>{t("invalidId")}</h2><Link to="/problems" className="button">{t("browse")}</Link></div> :
      needsLogin ? <div className="account-page-state"><UserRound size={32} aria-hidden="true" /><h2>{t("loginTitle")}</h2><p>{t("loginDescription")}</p>
        <div className="account-state-actions"><Link to="/login" state={{ from: location.pathname + location.search }} className="button primary">{t("login")}</Link>
          <Link to="/register" state={{ from: "/profile" }} className="button">{t("register")}</Link></div>
      </div> : query.isPending ? <div className="account-page-state" role="status"><LoaderCircle size={24} className="spin" aria-hidden="true" /><p>{t("loading")}</p></div> : <>
        {query.isError && <div className="account-inline-error" role="alert"><span>{t(status === 404 ? "notFound" : status === 403 ? "forbidden" : data ? "updateError" : "loadError")}</span>
          {!blocked && <button type="button" className="button" disabled={query.isFetching} onClick={() => void query.refetch()}>{t("retry")}</button>}
        </div>}
        {data && !blocked && <ProfileContent key={`${own}:${data.id}`} profile={data} own={own} />}
      </>}
  </section>;
}
