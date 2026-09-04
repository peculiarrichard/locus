import { baseApi } from './baseApi'

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  mfaChallengeToken: string | null
}

export interface ProfileResponse {
  id: string
  email: string
  displayName: string | null
  timezone: string | null
  emailVerified: boolean
  mfaEnabled: boolean
}

export interface DeviceResponse {
  id: string
  deviceLabel: string | null
  createdAt: string
  expiresAt: string
}

export const authApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    register: builder.mutation<void, { email: string; password: string }>({
      query: (body) => ({ url: '/auth/register', method: 'POST', body })
    }),
    resendVerification: builder.mutation<void, { email: string }>({
      query: (body) => ({ url: '/auth/verify-email/resend', method: 'POST', body })
    }),
    requestPasswordReset: builder.mutation<void, { email: string }>({
      query: (body) => ({ url: '/auth/password-reset/request', method: 'POST', body })
    }),
    login: builder.mutation<LoginResponse, { email: string; password: string }>({
      query: (body) => ({ url: '/auth/login', method: 'POST', body })
    }),
    verifyMfaChallenge: builder.mutation<LoginResponse, { mfaChallengeToken: string; code: string }>({
      query: (body) => ({ url: '/auth/mfa/challenge', method: 'POST', body })
    }),
    refresh: builder.mutation<{ accessToken: string; refreshToken: string }, { refreshToken: string }>({
      query: (body) => ({ url: '/auth/refresh', method: 'POST', body })
    }),
    getProfile: builder.query<ProfileResponse, void>({
      query: () => '/users/me',
      providesTags: ['Profile']
    }),
    updateProfile: builder.mutation<void, { displayName?: string; timezone?: string }>({
      query: (body) => ({ url: '/users/me', method: 'PATCH', body }),
      invalidatesTags: ['Profile']
    }),
    logout: builder.mutation<void, { refreshToken: string }>({
      query: (body) => ({ url: '/auth/logout', method: 'POST', body })
    }),
    enrollMfa: builder.mutation<{ otpAuthUri: string }, void>({
      query: () => ({ url: '/auth/mfa/enroll', method: 'POST' })
    }),
    confirmMfa: builder.mutation<{ recoveryCodes: string[] }, { code: string }>({
      query: (body) => ({ url: '/auth/mfa/confirm', method: 'POST', body }),
      invalidatesTags: ['Profile']
    }),
    disableMfa: builder.mutation<void, { password: string }>({
      query: (body) => ({ url: '/auth/mfa/disable', method: 'POST', body }),
      invalidatesTags: ['Profile']
    }),
    listDevices: builder.query<DeviceResponse[], void>({
      query: () => '/users/me/devices',
      providesTags: ['Devices']
    }),
    revokeDevice: builder.mutation<void, string>({
      query: (deviceId) => ({ url: `/users/me/devices/${deviceId}`, method: 'DELETE' }),
      invalidatesTags: ['Devices']
    }),
    deleteAccount: builder.mutation<void, { password: string }>({
      query: (body) => ({ url: '/users/me', method: 'DELETE', body })
    })
  })
})

export const {
  useRegisterMutation,
  useResendVerificationMutation,
  useRequestPasswordResetMutation,
  useLoginMutation,
  useVerifyMfaChallengeMutation,
  useRefreshMutation,
  useGetProfileQuery,
  useUpdateProfileMutation,
  useLogoutMutation,
  useEnrollMfaMutation,
  useConfirmMfaMutation,
  useDisableMfaMutation,
  useListDevicesQuery,
  useRevokeDeviceMutation,
  useDeleteAccountMutation
} = authApi
