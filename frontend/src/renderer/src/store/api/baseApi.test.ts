import { configureStore } from '@reduxjs/toolkit'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { baseApi } from './baseApi'
import authReducer from '@renderer/store/slices/authSlice'
import { setAccessToken, getAccessToken } from '@renderer/lib/tokenStore'

// A throwaway endpoint injected just for this test file, so the silent-refresh-on-401 logic in
// baseApiQuery can be exercised directly without pulling in a real domain API slice.
const testApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    probe: builder.query<{ ok: boolean }, void>({ query: () => '/probe' })
  })
})

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

function buildStore() {
  return configureStore({
    reducer: { auth: authReducer, [baseApi.reducerPath]: baseApi.reducer },
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(baseApi.middleware)
  })
}

describe('baseApiQuery', () => {
  beforeEach(() => {
    setAccessToken(null)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('returns data as-is on a plain successful response', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({ ok: true }))
    vi.stubGlobal('fetch', fetchMock)
    const store = buildStore()

    const result = await store.dispatch(testApi.endpoints.probe.initiate())
    expect(result.data).toEqual({ ok: true })
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('attaches the parsed error envelope to a plain (non-401) error response', async () => {
    const envelope = { errorCode: 'SOMETHING_BROKE', message: 'it broke', correlationId: 'c1', timestamp: 't' }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(envelope, 500)))
    const store = buildStore()

    const result = await store.dispatch(testApi.endpoints.probe.initiate())
    expect(result.error).toMatchObject({ status: 500, envelope })
  })

  it('on a TOKEN_EXPIRED 401 with a stored refresh token, silently refreshes and retries the original request once', async () => {
    setAccessToken('old-token')
    window.locus.auth.getRefreshToken = vi.fn().mockResolvedValue('refresh-1')
    window.locus.auth.setRefreshToken = vi.fn().mockResolvedValue(undefined)

    const fetchMock = vi
      .fn()
      // 1: the original /probe request, rejected as expired
      .mockResolvedValueOnce(jsonResponse({ errorCode: 'TOKEN_EXPIRED', message: 'x', correlationId: 'c', timestamp: 't' }, 401))
      // 2: /auth/refresh succeeds with a new token pair
      .mockResolvedValueOnce(jsonResponse({ accessToken: 'new-token', refreshToken: 'refresh-2' }))
      // 3: the retried /probe request succeeds
      .mockResolvedValueOnce(jsonResponse({ ok: true }))
    vi.stubGlobal('fetch', fetchMock)
    const store = buildStore()

    const result = await store.dispatch(testApi.endpoints.probe.initiate())

    expect(result.data).toEqual({ ok: true })
    expect(fetchMock).toHaveBeenCalledTimes(3)
    expect(getAccessToken()).toBe('new-token')
    expect(window.locus.auth.setRefreshToken).toHaveBeenCalledWith('refresh-2')
  })

  it('on a TOKEN_EXPIRED 401 whose refresh itself fails, clears tokens and marks the session unauthenticated', async () => {
    setAccessToken('old-token')
    window.locus.auth.getRefreshToken = vi.fn().mockResolvedValue('refresh-1')
    window.locus.auth.clearRefreshToken = vi.fn().mockResolvedValue(undefined)

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse({ errorCode: 'TOKEN_EXPIRED', message: 'x', correlationId: 'c', timestamp: 't' }, 401))
      .mockResolvedValueOnce(jsonResponse({ errorCode: 'INVALID_TOKEN', message: 'refresh dead', correlationId: 'c', timestamp: 't' }, 400))
    vi.stubGlobal('fetch', fetchMock)
    const store = buildStore()

    await store.dispatch(testApi.endpoints.probe.initiate())

    expect(getAccessToken()).toBeNull()
    expect(window.locus.auth.clearRefreshToken).toHaveBeenCalledTimes(1)
    expect(store.getState().auth.status).toBe('unauthenticated')
  })

  it('on a TOKEN_EXPIRED 401 with no stored refresh token at all, marks unauthenticated without attempting a refresh call', async () => {
    setAccessToken('old-token')
    window.locus.auth.getRefreshToken = vi.fn().mockResolvedValue(null)

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse({ errorCode: 'TOKEN_EXPIRED', message: 'x', correlationId: 'c', timestamp: 't' }, 401))
    vi.stubGlobal('fetch', fetchMock)
    const store = buildStore()

    await store.dispatch(testApi.endpoints.probe.initiate())

    expect(fetchMock).toHaveBeenCalledTimes(1) // no refresh, no retry
    expect(store.getState().auth.status).toBe('unauthenticated')
  })

  it('does not attempt a silent refresh for a 401 that is not TOKEN_EXPIRED', async () => {
    setAccessToken('old-token')
    window.locus.auth.getRefreshToken = vi.fn()

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse({ errorCode: 'MISSING_TOKEN', message: 'x', correlationId: 'c', timestamp: 't' }, 401))
    vi.stubGlobal('fetch', fetchMock)
    const store = buildStore()

    const result = await store.dispatch(testApi.endpoints.probe.initiate())

    expect(fetchMock).toHaveBeenCalledTimes(1)
    expect(window.locus.auth.getRefreshToken).not.toHaveBeenCalled()
    expect(result.error).toMatchObject({ status: 401 })
  })
})
