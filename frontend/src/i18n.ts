import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import { appResources } from "./locales/app";
import { authResources } from "./locales/auth";
import { problemDetailResources } from "./locales/problemDetail";
import { problemsResources } from "./locales/problems";
import { submissionDetailResources } from "./locales/submissionDetail";
import { submissionHistoryResources } from "./locales/submissionHistory";
import { accountResources } from "./locales/account";
import { profileResources } from "./locales/profile";

const savedLanguage = localStorage.getItem("language");
const initialLanguage = savedLanguage === "en" ? "en" : "vi";

i18n.use(initReactI18next).init({
  resources: {
    vi: {
      account: accountResources.vi,
      profile: profileResources.vi,
      submissionHistory: submissionHistoryResources.vi,
      translation: {
        app: appResources.vi,
        auth: authResources.vi,
        problems: problemsResources.vi,
        problemDetail: problemDetailResources.vi,
        submissionDetail: submissionDetailResources.vi,
      },
    },
    en: {
      account: accountResources.en,
      profile: profileResources.en,
      submissionHistory: submissionHistoryResources.en,
      translation: {
        app: appResources.en,
        auth: authResources.en,
        problems: problemsResources.en,
        problemDetail: problemDetailResources.en,
        submissionDetail: submissionDetailResources.en,
      },
    },
  },
  lng: initialLanguage,
  fallbackLng: "en",
  interpolation: {
    escapeValue: false,
  },
});

i18n.on("languageChanged", (lng) => {
  localStorage.setItem("language", lng);
  document.documentElement.lang = lng;
});

export default i18n;
