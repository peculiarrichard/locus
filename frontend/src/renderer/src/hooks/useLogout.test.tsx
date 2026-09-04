import { act, renderHook, waitFor } from '@testing-library/react'
import { Provider } from 'react-redux'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useLogout } from './useLogout'
import { createTestStore, type TestStore } from '../../test/testStore'
import { setAuthenticated } from '@renderer/store/slices/authSlice'
import { getAccessToken, setAccessToken } from '@renderer/lib/tokenStore'

function jsonResponse(body: unknown, status = 204): Response {
  return new Response(status === 204 ? null : JSON.stringify(body), { status })
}

let store: TestStore

function renderWithStore() {
  return renderHook(() => useLogout(), { wrapper: ({ children }) => <Provider store={store}>{children}</Provider> })
}

describe('useLogout', () => {
  beforeEach(() => {
    store = createTestStore()
    store.dispatch(setAuthenticated({ userId: 'u1', tokenExpiresAt: 9999999999000 }))
    setAccessToken('some-token')
    window.locus.auth.clearRefreshToken = vi.fn().mockResolvedValue(undefined)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('revokes the refresh token server-side, clears both token stores, and flips auth to unauthenticated', async () => {
    window.locus.auth.getRefreshToken = vi.fn().mockResolvedValue('refresh-1')
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(null))
    vi.stubGlobal('fetch', fetchMock)
    const { result } = renderWithStore()

    await act(() => result.current())

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledTimes(1)
      expect(getAccessToken()).toBeNull()
      expect(window.locus.auth.clearRefreshToken).toHaveBeenCalledTimes(1)
      expect(store.getState().auth.status).toBe('unauthenticated')
    })
  })

  it('skips the server-side revoke call entirely when there is no stored refresh token', async () => {
    window.locus.auth.getRefreshToken = vi.fn().mockResolvedValue(null)
    const fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)
    const { result } = renderWithStore()

    await act(() => result.current())

    expect(fetchMock).not.toHaveBeenCalled()
    expect(getAccessToken()).toBeNull()
    expect(store.getState().auth.status).toBe('unauthenticated')
  })

  it('still clears local state and marks unauthenticated even if the server-side revoke call fails', async () => {
    window.locus.auth.getRefreshToken = vi.fn().mockResolvedValue('refresh-1')
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(jsonResponse({ errorCode: 'INTERNAL_ERROR', message: 'x', correlationId: 'c', timestamp: 't' }, 500))
    )
    const { result } = renderWithStore()

    await act(() => result.current())
    expect(getAccessToken()).toBeNull()
    expect(store.getState().auth.status).toBe('unauthenticated')
  })
})
