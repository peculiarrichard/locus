import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

type Theme = 'dark' | 'light' | 'system'

interface UiState {
  theme: Theme
  sidebarCollapsed: boolean
}

// Only the theme preference is persisted (a per-viewer convenience, not app data) — a tiny direct
// localStorage read/write, not a full redux-persist setup, since it's the one slice value that
// genuinely needs to survive a restart.
const STORAGE_KEY = 'locus.theme'
function loadTheme(): Theme {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    return stored === 'dark' || stored === 'light' || stored === 'system' ? stored : 'dark'
  } catch {
    return 'dark'
  }
}

const initialState: UiState = { theme: loadTheme(), sidebarCollapsed: false }

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    themeChanged(state, action: PayloadAction<Theme>) {
      state.theme = action.payload
      try {
        localStorage.setItem(STORAGE_KEY, action.payload)
      } catch {
        // best-effort only
      }
    },
    sidebarToggled(state) {
      state.sidebarCollapsed = !state.sidebarCollapsed
    }
  }
})

export const { themeChanged, sidebarToggled } = uiSlice.actions
export default uiSlice.reducer
