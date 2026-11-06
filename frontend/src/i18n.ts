import i18n from "i18next";
import { initReactI18next } from "react-i18next";

const savedLanguage = localStorage.getItem("language");
const initialLanguage = savedLanguage === "en" ? "en" : "vi";

i18n.use(initReactI18next).init({
  resources: {
    vi: {
      translation: {
        app: {
          language: "Ngôn ngữ",
          title: "CodeForge",
          tagline: "Mỗi bài toán, một bước tiến.",
        },
        problems: {
          title: "Danh sách bài tập",
          id: "Mã bài",
          name: "Tên bài",
          difficulty: "Độ khó",
          tags: "Thẻ",
          loading: "Đang tải...",
          error: "Đã xảy ra lỗi khi tải danh sách bài tập.",
          empty: "Không có bài tập nào được tìm thấy.",
          previous: "Trước",
          next: "Tiếp",
          pageInfo: "Trang {{current}}/{{total}}",
          // funny, I know
          difficultyness: { // If you're reading this and wondering why there's a "ness", it's to distinguish it from "difficulty" key
            easy: "Dễ",
            medium: "Trung bình",
            hard: "Khó",
          },
        },
        problemDetail: {
          title: "Chi tiết bài tập",
          loading: "Đang tải bài tập...",
          error: "Không thể tải bài tập.",
          timeLimit: "Giới hạn thời gian",
          memoryLimit: "Giới hạn bộ nhớ",
          examples: "Ví dụ",
          example: "Ví dụ {{number}}",
          input: "Dữ liệu vào",
          output: "Kết quả mong đợi",
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
        problems: {
          title: "Problem List",
          id: "Problem ID",
          name: "Problem Name",
          difficulty: "Difficulty",
          tags: "Tags",
          loading: "Loading...",
          error: "An error occurred while loading the problem list.",
          empty: "No problems found.",
          previous: "Previous",
          next: "Next",
          pageInfo: "Page {{current}}/{{total}}",
          difficultyness: {
            easy: "Easy",
            medium: "Medium",
            hard: "Hard",
          },
        },
        problemDetail: {
          title: "Problem Detail",
          loading: "Loading problem...",
          error: "Unable to load the problem.",
          timeLimit: "Time Limit",
          memoryLimit: "Memory Limit",
          examples: "Examples",
          example: "Example {{number}}",
          input: "Input",
          output: "Output",
        },
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