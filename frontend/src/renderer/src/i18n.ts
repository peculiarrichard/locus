import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

// design-spec.md §6/rules.md R13.1: strings routed through i18next from the start, English-only
// content for v1 — the architectural cost is low now and high to retrofit later.
void i18n.use(initReactI18next).init({
  resources: {
    en: {
      translation: {
        appName: 'Locus'
      }
    }
  },
  lng: 'en',
  fallbackLng: 'en',
  interpolation: { escapeValue: false }
})

export default i18n
