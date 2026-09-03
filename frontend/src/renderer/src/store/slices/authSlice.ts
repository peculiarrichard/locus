import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

interface AuthState {
  userId: string | null
  tokenExpiresAt: number | null
  status: 'idle' | 'authenticated' | 'unauthenticated'
}

const initialState: AuthState = { userId: null, tokenExpiresAt: null, status: 'idle' }

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setAuthenticated(state, action: PayloadAction<{ userId: string; tokenExpiresAt: number }>) {
      state.userId = action.payload.userId
      state.tokenExpiresAt = action.payload.tokenExpiresAt
      state.status = 'authenticated'
    },
    setUnauthenticated(state) {
      state.userId = null
      state.tokenExpiresAt = null
      state.status = 'unauthenticated'
    }
  }
})

export const { setAuthenticated, setUnauthenticated } = authSlice.actions
export default authSlice.reducer
