import { createApi, fetchBaseQuery, type BaseQueryFn, type FetchArgs, type FetchBaseQueryError } from '@reduxjs/toolkit/query/react'
import { getAccessToken, setAccessToken } from '@renderer/lib/tokenStore'
import { setUnauthenticated } from '@renderer/store/slices/authSlice'

// Gateway origin — configurable so a packaged build can point at something other than localhost
// without a rebuild (design-spec.md §1). Vite exposes only VITE_-prefixed env vars to the renderer.
const GATEWAY_ORIGIN = import.meta.env.VITE_GATEWAY_ORIGIN ?? 'http://localhost:8080'

interface ErrorEnvelope {
  errorCode: string
  message: string
  correlationId: string
  timestamp: string
}

const rawBaseQuery = fetchBaseQuery({
  baseUrl: `${GATEWAY_ORIGIN}/api/v1`,
  prepareHeaders: (headers) => {
    const token = getAccessToken()
    if (token) {
      headers.set('Authorization', `Bearer ${token}`)
    }
    return headers
  }
})

// One silent refresh attempt on a Gateway-signaled expired token (frd.md's distinct error codes),
// surfaced to the rest of the app as an ordinary loading state — design-spec.md §5.
export const baseApiQuery: BaseQueryFn<string | FetchArgs, unknown, FetchBaseQueryError & { envelope?: ErrorEnvelope }> = async (
  args,
  api,
  extraOptions
) => {
  let result = await rawBaseQuery(args, api, extraOptions)
  const envelope = result.error?.data as ErrorEnvelope | undefined

  if (result.error?.status === 401 && envelope?.errorCode === 'TOKEN_EXPIRED') {
    const refreshToken = await window.locus.auth.getRefreshToken()
    if (refreshToken) {
      const refreshResult = await rawBaseQuery({ url: '/auth/refresh', method: 'POST', body: { refreshToken } }, api, extraOptions)
      const refreshed = refreshResult.data as { accessToken: string; refreshToken: string } | undefined
      if (refreshed) {
        setAccessToken(refreshed.accessToken)
        await window.locus.auth.setRefreshToken(refreshed.refreshToken)
        result = await rawBaseQuery(args, api, extraOptions)
      } else {
        setAccessToken(null)
        await window.locus.auth.clearRefreshToken()
        api.dispatch(setUnauthenticated())
      }
    } else {
      api.dispatch(setUnauthenticated())
    }
  }

  if (result.error) {
    return { error: { ...result.error, envelope }, meta: result.meta }
  }
  return { data: result.data, meta: result.meta }
}

export const baseApi = createApi({
  reducerPath: 'api',
  baseQuery: baseApiQuery,
  tagTypes: ['Profile', 'Devices', 'Session', 'Goal', 'Analytics', 'AccountabilityGroup', 'NotificationPreferences'],
  endpoints: () => ({})
})

export type { ErrorEnvelope }
