import { useEffect, useId, useRef, useState } from "react";
import { ArrowLeft } from "lucide-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Link,
  useNavigate,
  useParams,
  useSearchParams,
} from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  createProblem,
  updateProblem,
  type ProblemRequest,
} from "../../api/problems";
import { getAdminProblem } from "../../api/admin";
import { getTags } from "../../api/tags";
import { AdminError, FileTextField } from "../../components/admin/AdminUi";
import { PageSkeleton, SkeletonBlock } from "../../components/ui/Skeleton";
import type { ProblemDetail } from "../../types/problem";
import { validAdminId } from "../../utils/adminFiles";

import AdminTestCasesPage from "./AdminTestCasesPage";

function ProblemForm({ problem }: { problem?: ProblemDetail }) {
  const { t } = useTranslation("admin");
  const client = useQueryClient();
  const navigate = useNavigate();
  const tabs = ["general", "statement", "judging", "test-cases"] as const;
  type EditorTab = (typeof tabs)[number];
  const [search, setSearch] = useSearchParams();
  const requestedTab = search.get("tab");
  const activeTab: EditorTab =
    requestedTab === "statement" ||
    requestedTab === "judging" ||
    (requestedTab === "test-cases" && problem)
      ? requestedTab
      : "general";
  const setActiveTab = (tab: EditorTab) =>
    setSearch((previous) => {
      const next = new URLSearchParams(previous);
      if (tab === "general") next.delete("tab");
      else next.set("tab", tab);
      return next;
    });
  const [casesVisited, setCasesVisited] = useState(activeTab === "test-cases");
  useEffect(() => {
    if (activeTab === "test-cases") setCasesVisited(true);
  }, [activeTab]);
  const tabPrefix = useId();
  const tabRefs = useRef<(HTMLButtonElement | null)[]>([]);
  const enabledTabs = tabs.filter((tab) => tab !== "test-cases" || problem);
  const [title, setTitle] = useState(problem?.title ?? "");
  const [slug, setSlug] = useState(problem?.slug ?? "");
  const [description, setDescription] = useState(problem?.description ?? "");
  const [difficulty, setDifficulty] = useState(problem?.difficulty ?? "EASY");
  const [time, setTime] = useState(String(problem?.timeLimitMs ?? 1000));
  const [memory, setMemory] = useState(String(problem?.memoryLimitMb ?? 256));
  const [enabled, setEnabled] = useState(problem?.enabled ?? false);
  const [spj, setSpj] = useState(problem?.spjEnabled ?? false);
  const [source, setSource] = useState(problem?.spjSource ?? "");
  const [tagIds, setTagIds] = useState<number[]>(
    problem?.tags.map((tag) => tag.id) ?? [],
  );
  const [invalid, setInvalid] = useState("");
  const [reading, setReading] = useState(0);
  const [saved, setSaved] = useState(false);
  useEffect(() => {
    if (!saved) {
      return;
    }

    const timeout = window.setTimeout(() => setSaved(false), 5_000);
    return () => window.clearTimeout(timeout);
  }, [saved]);
  const tags = useQuery({
    queryKey: ["tags"],
    queryFn: ({ signal }) => getTags(signal),
    retry: false,
    staleTime: 300_000,
    refetchOnWindowFocus: false,
  });
  const mutation = useMutation({
    mutationFn: (request: ProblemRequest) =>
      problem ? updateProblem(problem.id, request) : createProblem(request),
    retry: false,
    onSuccess: async (result) => {
      client.setQueryData(["admin", "problem", String(result.id)], result);
      await Promise.all([
        client.invalidateQueries({ queryKey: ["admin", "problems"] }),
        client.invalidateQueries({ queryKey: ["problems"] }),
        client.invalidateQueries({ queryKey: ["problem"] }),
      ]);
      setSaved(true);
      if (!problem)
        navigate(`/admin/problems/${result.id}?tab=test-cases`, {
          replace: true,
        });
    },
  });
  const pending = mutation.isPending || reading > 0;
  return (
    <div className="admin-editor">
      <div
        className="admin-form-tabs"
        role="tablist"
        aria-label={t("editorTabs")}
      >
        {tabs.map((tab, index) => (
          <button
            key={tab}
            ref={(element) => {
              tabRefs.current[index] = element;
            }}
            type="button"
            role="tab"
            id={`${tabPrefix}-${tab}-tab`}
            aria-controls={`${tabPrefix}-${tab}-panel`}
            aria-selected={activeTab === tab}
            tabIndex={activeTab === tab ? 0 : -1}
            disabled={tab === "test-cases" && !problem}
            title={
              tab === "test-cases" && !problem
                ? t("saveBeforeTestCases")
                : undefined
            }
            onClick={() => setActiveTab(tab)}
            onKeyDown={(event) => {
              const current = enabledTabs.indexOf(tab);
              let next: number;
              if (event.key === "ArrowRight")
                next = (current + 1) % enabledTabs.length;
              else if (event.key === "ArrowLeft")
                next = (current + enabledTabs.length - 1) % enabledTabs.length;
              else if (event.key === "Home") next = 0;
              else if (event.key === "End") next = enabledTabs.length - 1;
              else return;
              event.preventDefault();
              setActiveTab(enabledTabs[next]);
              tabRefs.current[tabs.indexOf(enabledTabs[next])]?.focus();
            }}
          >
            {t(`tabs.${tab}`)}
          </button>
        ))}
      </div>
      <form
        className="admin-form"
        hidden={activeTab === "test-cases"}
        noValidate
        onSubmit={(event) => {
          event.preventDefault();
          if (pending) return;
          const timeLimitMs = Number(time),
            memoryLimitMb = Number(memory);
          const reject = (tab: typeof activeTab, field: string) => {
            setActiveTab(tab);
            setInvalid("formInvalid");
            setSaved(false);
            requestAnimationFrame(() =>
              document.getElementById(field)?.focus(),
            );
          };
          if (!title.trim() || title.trim().length > 255) {
            reject("general", "problem-title");
            return;
          }
          if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(slug) || slug.length > 255) {
            reject("general", "problem-slug");
            return;
          }
          if (tagIds.length > 20) {
            reject("general", `${tabPrefix}-general-tab`);
            return;
          }
          if (!description.trim() || description.length > 100_000) {
            reject("statement", "problem-description");
            return;
          }
          if (
            !Number.isInteger(timeLimitMs) ||
            timeLimitMs < 1 ||
            timeLimitMs > 30_000
          ) {
            reject("judging", "problem-time");
            return;
          }
          if (
            !Number.isInteger(memoryLimitMb) ||
            memoryLimitMb < 1 ||
            memoryLimitMb > 1024
          ) {
            reject("judging", "problem-memory");
            return;
          }
          if ((spj && !source.trim()) || source.length > 65_536) {
            reject("judging", "problem-spj");
            return;
          }
          setInvalid("");
          setSaved(false);
          mutation.mutate({
            title: title.trim(),
            slug,
            description,
            difficulty,
            timeLimitMs,
            memoryLimitMb,
            spjEnabled: spj,
            spjSource: spj ? source : null,
            enabled,
            tagIds,
          });
        }}
      >
        {!problem && <p className="admin-note">{t("draftNote")}</p>}
        <div
          className="admin-form-panel"
          role="tabpanel"
          id={`${tabPrefix}-general-panel`}
          aria-labelledby={`${tabPrefix}-general-tab`}
          hidden={activeTab !== "general"}
        >
          <div className="admin-form-grid">
            <label>
              {t("problemTitle")}
              <input
                id="problem-title"
                required
                maxLength={255}
                value={title}
                disabled={pending}
                onChange={(event) => setTitle(event.target.value)}
                onBlur={() => {
                  if (!problem && !slug)
                    setSlug(
                      title
                        .normalize("NFD")
                        .replace(/[\u0300-\u036f]/g, "")
                        .replace(/[đĐ]/g, "d")
                        .toLowerCase()
                        .replace(/[^a-z0-9]+/g, "-")
                        .replace(/^-|-$/g, ""),
                    );
                }}
              />
            </label>
            <label>
              {t("slug")}
              <input
                id="problem-slug"
                required
                maxLength={255}
                pattern="[a-z0-9]+(-[a-z0-9]+)*"
                value={slug}
                disabled={pending}
                onChange={(event) => setSlug(event.target.value)}
              />
            </label>
            <label>
              {t("difficulty")}
              <select
                value={difficulty}
                disabled={pending}
                onChange={(event) =>
                  setDifficulty(
                    event.target.value as ProblemDetail["difficulty"],
                  )
                }
              >
                {["EASY", "MEDIUM", "HARD"].map((value) => (
                  <option value={value} key={value}>
                    {t(value.toLowerCase())}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <fieldset disabled={pending}>
            <legend>{t("tags")}</legend>
            {tags.isPending ? (
              <span role="status" aria-label={t("loadingTags")}>
                <SkeletonBlock className="skeleton--control" />
              </span>
            ) : tags.isError ? (
              <AdminError
                error={tags.error}
                retry={() => void tags.refetch()}
              />
            ) : tags.data.length ? (
              <div className="admin-tag-options">
                {tags.data.map((tag) => (
                  <label className="admin-check" key={tag.id}>
                    <input
                      type="checkbox"
                      checked={tagIds.includes(tag.id)}
                      disabled={!tagIds.includes(tag.id) && tagIds.length >= 20}
                      onChange={(event) =>
                        setTagIds((values) =>
                          event.target.checked
                            ? [...values, tag.id]
                            : values.filter((id) => id !== tag.id),
                        )
                      }
                    />
                    {tag.name}
                  </label>
                ))}
              </div>
            ) : (
              <p>{t("noTags")}</p>
            )}
          </fieldset>
          <label className="admin-check">
            <input
              type="checkbox"
              checked={enabled}
              disabled={pending}
              onChange={(event) => setEnabled(event.target.checked)}
            />
            {t("enabled")}
          </label>
        </div>
        <div
          className="admin-form-panel"
          role="tabpanel"
          id={`${tabPrefix}-statement-panel`}
          aria-labelledby={`${tabPrefix}-statement-tab`}
          hidden={activeTab !== "statement"}
        >
          <FileTextField
            id="problem-description"
            rows={18}
            label={t("description")}
            value={description}
            onChange={setDescription}
            maxBytes={400_000}
            maxCharacters={100_000}
            disabled={mutation.isPending}
            onBusyChange={(busy) =>
              setReading((value) => value + (busy ? 1 : -1))
            }
          />
        </div>
        <div
          className="admin-form-panel"
          role="tabpanel"
          id={`${tabPrefix}-judging-panel`}
          aria-labelledby={`${tabPrefix}-judging-tab`}
          hidden={activeTab !== "judging"}
        >
          <div className="admin-form-grid">
            <label>
              {t("time")}
              <input
                id="problem-time"
                required
                type="number"
                min={1}
                max={30000}
                step={1}
                value={time}
                disabled={pending}
                onChange={(event) => setTime(event.target.value)}
              />
            </label>
            <label>
              {t("memory")}
              <input
                id="problem-memory"
                required
                type="number"
                min={1}
                max={1024}
                step={1}
                value={memory}
                disabled={pending}
                onChange={(event) => setMemory(event.target.value)}
              />
            </label>
          </div>
          <label className="admin-check">
            <input
              type="checkbox"
              checked={spj}
              disabled={pending}
              onChange={(event) => setSpj(event.target.checked)}
            />
            {t("spj")}
          </label>
          {spj && (
            <FileTextField
              id="problem-spj"
              rows={16}
              label={t("spjSource")}
              value={source}
              onChange={setSource}
              maxBytes={262_144}
              maxCharacters={65_536}
              disabled={mutation.isPending}
              onBusyChange={(busy) =>
                setReading((value) => value + (busy ? 1 : -1))
              }
            />
          )}
        </div>
        {invalid && (
          <p role="alert" className="admin-error">
            {t(invalid)}
          </p>
        )}
        {mutation.isError && <AdminError error={mutation.error} />}
        {saved && (
          <div className="toast" role="status" aria-live="polite">
            {t("saved")}
          </div>
        )}
        <div className="admin-actions admin-form-footer">
          <button
            className="button primary"
            type="submit"
            disabled={pending || tags.isPending || tags.isError}
          >
            {t(mutation.isPending ? "saving" : "save")}
          </button>
        </div>
      </form>
      <div
        className="admin-form-panel"
        role="tabpanel"
        id={`${tabPrefix}-test-cases-panel`}
        aria-labelledby={`${tabPrefix}-test-cases-tab`}
        hidden={activeTab !== "test-cases"}
      >
        {!problem ? (
          <p className="admin-note">{t("saveBeforeTestCases")}</p>
        ) : (
          (casesVisited || activeTab === "test-cases") && (
            <AdminTestCasesPage embedded active={activeTab === "test-cases"} />
          )
        )}
      </div>
    </div>
  );
}

export default function AdminProblemFormPage({
  create = false,
}: {
  create?: boolean;
}) {
  const { t } = useTranslation("admin");
  const { id } = useParams();
  const valid = create || validAdminId(id);
  const query = useQuery({
    queryKey: ["admin", "problem", id],
    queryFn: ({ signal }) => getAdminProblem(id!, signal),
    enabled: !create && valid,
    retry: false,
    staleTime: 30_000,
    refetchOnWindowFocus: false,
  });
  return (
    <section className="live-admin">
      <div className="admin-heading">
        <div className="admin-heading-start">
          <Link to="/admin/problems" className="button">
            <ArrowLeft size={16} aria-hidden="true" />
            {t("back")}
          </Link>
          <h1>{t(create ? "newProblem" : "editProblem")}</h1>
        </div>
      </div>
      {!valid ? (
        <p role="alert">{t("formInvalid")}</p>
      ) : create ? (
        <ProblemForm />
      ) : query.isPending ? (
        <PageSkeleton page="admin-form" bodyOnly label={t("loading")} />
      ) : query.isError ? (
        <AdminError error={query.error} retry={() => void query.refetch()} />
      ) : (
        <ProblemForm key={query.data.id} problem={query.data} />
      )}
    </section>
  );
}
