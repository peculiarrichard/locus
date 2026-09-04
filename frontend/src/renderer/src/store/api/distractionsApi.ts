import { baseApi } from './baseApi'

export interface DistractionResponse {
  id: string
  sessionId: string
  occurredAt: string
  durationSeconds: number
}

export const distractionsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    logDistraction: builder.mutation<DistractionResponse, { id: string; sessionId: string; occurredAt: string; durationSeconds: number }>({
      query: (body) => ({ url: '/distractions', method: 'POST', body })
    }),
    listDistractions: builder.query<DistractionResponse[], string>({
      query: (sessionId) => `/distractions?session_id=${sessionId}`
    })
  })
})

export const { useLogDistractionMutation, useListDistractionsQuery } = distractionsApi
