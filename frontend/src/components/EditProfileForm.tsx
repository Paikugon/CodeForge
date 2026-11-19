import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { updateMyProfile } from "../api/users";
import { ApiError } from "../api/http";
import { useAuth } from "../context/AuthContext";
import type { UserProfile } from "../types/user";
import AccountInput from "./AccountInput";

export default function EditProfileForm({ profile }: { profile: UserProfile }) {
  const { t } = useTranslation("profile");
  const client = useQueryClient();
  const { signOut } = useAuth();
  const navigate = useNavigate();
  const [displayName, setDisplayName] = useState(profile.displayName ?? "");
  const [bio, setBio] = useState(profile.bio ?? "");
  const [invalid, setInvalid] = useState(false);
  const mutation = useMutation({
    mutationFn: updateMyProfile,
    retry: false,
    onSuccess: (_, request) => {
      const details = { displayName: request.displayName?.trim() || null, bio: request.bio?.trim() || null };
      // Update an existing public cache without copying private profile fields into it.
      for (const key of [["auth", "me"], ["public-profile", profile.id]]) {
        client.setQueryData<UserProfile>(key, current => current ? { ...current, ...details } : current);
      }
      setDisplayName(details.displayName ?? ""); setBio(details.bio ?? "");
      void client.invalidateQueries({ queryKey: ["auth", "me"] });
      void client.invalidateQueries({ queryKey: ["public-profile", profile.id] });
    },
    onError: error => {
      if (error instanceof ApiError && error.status === 401) {
        signOut();
        navigate("/login", { replace: true, state: { sessionExpired: true, from: "/profile?tab=edit" } });
      }
    },
  });
  const dirty = displayName !== (profile.displayName ?? "") || bio !== (profile.bio ?? "");
  const expired = mutation.error instanceof ApiError && mutation.error.status === 401;

  return <section className="profile-editor live-account-form" aria-labelledby="edit-profile-title">
    <h2 id="edit-profile-title">{t("editProfile")}</h2>
    <p>{t("publicDetailsNote")}</p>
    <form onSubmit={event => {
      event.preventDefault();
      if (mutation.isPending || !dirty) return;
      if (displayName.length > 100 || bio.length > 300) { setInvalid(true); return; }
      setInvalid(false);
      mutation.mutate({ displayName: displayName.trim(), bio: bio.trim() });
    }}>
      <AccountInput id="profile-display-name" name="displayName" label={t("displayName")} value={displayName}
        autoComplete="name" maxLength={100} disabled={mutation.isPending}
        error={invalid && displayName.length > 100 ? t("displayNameLength") : undefined}
        onChange={event => { setDisplayName(event.target.value); setInvalid(false); mutation.reset(); }} />
      <p className="account-hint">{t("displayNameHint")}</p>
      <div className="account-field">
        <label htmlFor="profile-bio">{t("bio")}</label>
        <textarea id="profile-bio" name="bio" rows={4} maxLength={300} value={bio}
          disabled={mutation.isPending} aria-describedby="profile-bio-hint"
          onChange={event => { setBio(event.target.value); setInvalid(false); mutation.reset(); }} />
        <p id="profile-bio-hint" className="account-hint">{t("bioLength", { count: bio.length })}</p>
      </div>
      <p className="account-hint">{t("usernameUnchanged", { username: profile.username })}</p>
      {mutation.isError && <p className="account-alert" role="alert">{t(expired ? "saveExpired" : "saveError")}</p>}
      {mutation.isSuccess && <p className="account-success" role="status">{t("saved")}</p>}
      <div className="profile-editor-actions">
        <button type="submit" className="button primary" disabled={mutation.isPending || !dirty}>{t(mutation.isPending ? "saving" : "save")}</button>
        <button type="button" className="button" disabled={mutation.isPending || !dirty} onClick={() => {
          setDisplayName(profile.displayName ?? ""); setBio(profile.bio ?? ""); setInvalid(false); mutation.reset();
        }}>{t("reset")}</button>
      </div>
    </form>
  </section>;
}
