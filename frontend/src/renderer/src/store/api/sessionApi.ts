import { baseApi } from './baseApi'
import type { ActiveSessionState } from '@renderer/store/slices/sessionSlice'

export interface SessionResponse {
  id: string
  sessionType: ActiveSessionState['sessionType']
  plannedDurationSeconds: number | null
  goalId: string | null
  workMinutes: number | null
  breakMinutes: number | null
  cycleCount: number | null
  startedAt: string
  accumulatedPauseSeconds: number
  completedAt: string | null
  abandonedAt: string | null
  durationSeconds: number | null
  status: 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'ABANDONED'
}

export interface StartSessionRequest {
  sessionType: ActiveSessionState['sessionType']
  plannedDurationSeconds?: number
  goalId?: string
  workMinutes?: number
  breakMinutes?: number
  cycleCount?: number
}

export const sessionApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    startSession: builder.mutation<SessionResponse, StartSessionRequest>({
      query: (body) => ({ url: '/sessions/start', method: 'POST', body }),
      invalidatesTags: ['Session']
    }),
    pauseSession: builder.mutation<SessionResponse, string>({
      query: (id) => ({ url: `/sessions/${id}/pause`, method: 'POST' })
    }),
    resumeSession: builder.mutation<SessionResponse, string>({
      query: (id) => ({ url: `/sessions/${id}/resume`, method: 'POST' })
    }),
    endSession: builder.mutation<SessionResponse, string>({
      query: (id) => ({ url: `/sessions/${id}/end`, method: 'POST' }),
      invalidatesTags: ['Session', 'Analytics', 'Goal']
    }),
    abandonSession: builder.mutation<SessionResponse, { id: string; abandonedAt?: string }>({
      query: ({ id, abandonedAt }) => ({ url: `/sessions/${id}/abandon`, method: 'POST', body: { abandonedAt } }),
      invalidatesTags: ['Session', 'Analytics']
    }),
    listSessions: builder.query<SessionResponse[], void>({
      query: () => '/sessions',
      providesTags: ['Session']
    }),
    getSession: builder.query<SessionResponse, string>({
      query: (id) => `/sessions/${id}`
    })
  })
})

export const {
  useStartSessionMutation,
  usePauseSessionMutation,
  useResumeSessionMutation,
  useEndSessionMutation,
  useAbandonSessionMutation,
  useListSessionsQuery,
  useGetSessionQuery
} = sessionApi
