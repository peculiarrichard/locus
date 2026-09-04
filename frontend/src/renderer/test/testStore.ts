import { configureStore } from '@reduxjs/toolkit'
import { baseApi } from '@renderer/store/api/baseApi'
import authReducer from '@renderer/store/slices/authSlice'
import sessionReducer from '@renderer/store/slices/sessionSlice'
import uiReducer from '@renderer/store/slices/uiSlice'
import '@renderer/store/api/authApi'
import '@renderer/store/api/sessionApi'
import '@renderer/store/api/goalsApi'
import '@renderer/store/api/analyticsApi'
import '@renderer/store/api/accountabilityApi'
import '@renderer/store/api/notificationsApi'
import '@renderer/store/api/distractionsApi'

// A fresh store per test, built the same way store/index.ts builds the real one — so RTK Query
// hooks under test go through the real middleware/cache, not a mock of it.
export function createTestStore() {
  return configureStore({
    reducer: {
      auth: authReducer,
      session: sessionReducer,
      ui: uiReducer,
      [baseApi.reducerPath]: baseApi.reducer
    },
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(baseApi.middleware)
  })
}

export type TestStore = ReturnType<typeof createTestStore>
