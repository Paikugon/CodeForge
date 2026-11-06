import i18n from "i18next";
import { initReactI18next } from "react-i18next";

i18n.use(initReactI18next).init({
  resources: {
    vi: {
      translation: {
        app: {
          language: "Ngôn ngữ",
          title: "CodeForge",
          tagline: "Mỗi bài toán, một bước tiến.",
        },
      },
    },
    en: {
      translation: {
        app: {
          language: "Language",
          title: "CodeForge",
          tagline: "One problem, one step forward.",
        },
      },
    },
  },
  lng: "vi",
  fallbackLng: "en",
  interpolation: {
    escapeValue: false,
  },
});

export default i18n;