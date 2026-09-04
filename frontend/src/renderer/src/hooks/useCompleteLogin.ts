import { useCallback } from 'react'
import { setAccessToken } from '@renderer/lib/tokenStore'
import { decodeJwt } from '@renderer/lib/jwt'
import { useAppDispatch } from '@renderer/store/hooks'
import { setAuthenticated } from '@renderer/store/slices/authSlice'
import type { LoginResponse } from '@renderer/store/api/authApi'

// Shared by login and MFA-challenge-verify: stashes the access token in the non-Redux token
// store, persists the refresh token via safeStorage (main process), and marks the auth slice
// authenticated from the token's own claims — no separate profile fetch needed just to unlock
// routing (screens that need profile fields fetch them via RTK Query directly, cached on demand).
export function useCompleteLogin(): (response: LoginResponse) => Promise<void> {
  const dispatch = useAppDispatch()
  return useCallback(
    async (response: LoginResponse) => {
      setAccessToken(response.accessToken)
      await window.locus.auth.setRefreshToken(response.refreshToken)
      const claims = decodeJwt(response.accessToken)
      dispatch(setAuthenticated({ userId: claims.sub, tokenExpiresAt: claims.exp * 1000 }))
    },
    [dispatch]
  )
}
