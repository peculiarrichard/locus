import { act, renderHook, waitFor } from '@testing-library/react'
import { Provider } from 'react-redux'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useCompleteLogin } from './useCompleteLogin'
import { createTestStore, type TestStore } from '../../test/testStore'
import { getAccessToken, setAccessToken } from '@renderer/lib/tokenStore'

function fakeAccessToken(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'RS256' }))
  const body = btoa(JSON.stringify(payload))
  return `${header}.${body}.sig`
}

let store: TestStore

describe('useCompleteLogin', () => {
  beforeEach(() => {
    store = createTestStore()
    setAccessToken(null)
    window.locus.auth.setRefreshToken = vi.fn().mockResolvedValue(undefined)
  })

  it('stashes the access token, persists the refresh token via IPC, and marks the auth slice authenticated from the token claims', async () => {
    const accessToken = fakeAccessToken({ sub: 'user-42', exp: 1999999999, mfa: false, iat: 1, jti: 'x', roles: ['user'] })
    const { result } = renderHook(() => useCompleteLogin(), {
      wrapper: ({ children }) => <Provider store={store}>{children}</Provider>
    })

    await act(() => result.current({ accessToken, refreshToken: 'refresh-abc', mfaChallengeToken: null }))

    await waitFor(() => {
      expect(getAccessToken()).toBe(accessToken)
      expect(window.locus.auth.setRefreshToken).toHaveBeenCalledWith('refresh-abc')
      expect(store.getState().auth).toEqual({ userId: 'user-42', tokenExpiresAt: 1999999999000, status: 'authenticated' })
    })
  })
})
