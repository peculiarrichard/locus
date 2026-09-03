import { baseApi } from './baseApi'

export interface PreferencesResponse {
  email: string
  timezone: string
  reminderTime: string | null
}

export const notificationsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getPreferences: builder.query<PreferencesResponse, void>({
      query: () => '/notifications/preferences',
      providesTags: ['NotificationPreferences']
    }),
    updatePreferences: builder.mutation<PreferencesResponse, { reminderTime: string | null }>({
      query: (body) => ({ url: '/notifications/preferences', method: 'PATCH', body }),
      invalidatesTags: ['NotificationPreferences']
    })
  })
})

export const { useGetPreferencesQuery, useUpdatePreferencesMutation } = notificationsApi
