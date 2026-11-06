import { useTranslation } from "react-i18next";

export default function App() {
  const { t, i18n } = useTranslation();

  return (
    <main>
      <label>
        {t("app.language")}

        <select
          value={i18n.resolvedLanguage}
          onChange={(event) =>
            void i18n.changeLanguage(event.target.value)
          }
        >
          <option value="vi">Tiếng Việt</option>
          <option value="en">English</option>
        </select>
      </label>

      <h1>{t("app.title")}</h1>
      <p>{t("app.tagline")}</p>
    </main>
  );
}