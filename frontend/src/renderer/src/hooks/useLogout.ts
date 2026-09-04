import { useCallback } from 'react'
import { setAccessToken } from '@renderer/lib/tokenStore'
import { useAppDispatch } from '@renderer/store/hooks'
import { setUnauthenticated } from '@renderer/store/slices/authSlice'
import { baseApi } from '@renderer/store/api/baseApi'
import { useLogoutMutation } from '@renderer/store/api/authApi'

// Shared by the sidebar's logout action and account deletion: revokes the current refresh token
// server-side (best-effort — a network failure shouldn't block clearing local state), clears both
// token stores, resets every cached RTK Query result, and flips the auth slice back to
// unauthenticated so ProtectedRoute redirects to /login on its own.
export function useLogout(): () => Promise<void> {
  const dispatch = useAppDispatch()
  const [logout] = useLogoutMutation()
  return useCallback(async () => {
    const refreshToken = await window.locus.auth.getRefreshToken()
    if (refreshToken) {
      await logout({ refreshToken }).unwrap().catch(() => undefined)
    }
    setAccessToken(null)
    await window.locus.auth.clearRefreshToken()
    dispatch(baseApi.util.resetApiState())
    dispatch(setUnauthenticated())
  }, [dispatch, logout])
}
