import { baseApi } from './baseApi'

export interface SummaryResponse {
  currentStreakDays: number
  longestStreakDays: number
  sessionsCompletedThisWeek: number
  sessionsAbandonedThisWeek: number
  totalFocusSecondsThisWeek: number
  completionRateThisWeek: number
}

export interface BestHourResponse {
  hourOfDay: number
  totalFocusSeconds: number
}

export interface DistractionFrequencyResponse {
  hourOfDay: number
  distractionCount: number
  totalFocusSecondsInBucket: number
  distractionsPerFocusHour: number
}

export interface HistoryDayResponse {
  date: string
  sessionsCompleted: number
  sessionsAbandoned: number
  totalFocusSeconds: number
  distractionCount: number
}

export const analyticsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getSummary: builder.query<SummaryResponse, void>({
      query: () => '/analytics/summary',
      providesTags: ['Analytics']
    }),
    getBestHours: builder.query<BestHourResponse[], void>({
      query: () => '/analytics/best-hours',
      providesTags: ['Analytics']
    }),
    getDistractionFrequency: builder.query<DistractionFrequencyResponse[], void>({
      query: () => '/analytics/distraction-frequency',
      providesTags: ['Analytics']
    }),
    getHistory: builder.query<HistoryDayResponse[], number | void>({
      query: (range) => `/analytics/history${range ? `?range=${range}` : ''}`,
      providesTags: ['Analytics']
    })
  })
})

export const { useGetSummaryQuery, useGetBestHoursQuery, useGetDistractionFrequencyQuery, useGetHistoryQuery } = analyticsApi
