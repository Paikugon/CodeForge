import { useTranslation } from "react-i18next";

export default function LanguageSwitcher() {
  const { t, i18n } = useTranslation();

  return (
    <label>
      {t("app.language")}{" "}
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
  );
}