import { configureStore } from '@reduxjs/toolkit'
import { baseApi } from './api/baseApi'
import authReducer from './slices/authSlice'
import sessionReducer from './slices/sessionSlice'
import uiReducer from './slices/uiSlice'
import './api/authApi'
import './api/sessionApi'
import './api/goalsApi'
import './api/analyticsApi'
import './api/accountabilityApi'
import './api/notificationsApi'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    session: sessionReducer,
    ui: uiReducer,
    [baseApi.reducerPath]: baseApi.reducer
  },
  middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(baseApi.middleware)
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
