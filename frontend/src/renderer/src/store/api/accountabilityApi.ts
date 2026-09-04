import { baseApi } from './baseApi'

export interface GroupResponse {
  id: string
  groupType: 'PAIR' | 'GROUP'
  createdBy: string
  createdAt: string
  memberCount: number
}

export interface InviteResponse {
  id: string
  code: string
  groupId: string
  expiresAt: string
}

export interface MemberStatusResponse {
  userId: string
  completedToday: boolean
  currentStreakDays: number
}

export interface GroupStatusResponse {
  groupId: string
  members: MemberStatusResponse[]
}

export const accountabilityApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    listGroups: builder.query<GroupResponse[], void>({
      query: () => '/accountability/groups',
      providesTags: ['AccountabilityGroup']
    }),
    createInvite: builder.mutation<InviteResponse, { groupId?: string; groupType?: 'PAIR' | 'GROUP' }>({
      query: (body) => ({ url: '/accountability/invites', method: 'POST', body })
    }),
    acceptInvite: builder.mutation<GroupResponse, string>({
      query: (code) => ({ url: `/accountability/invites/${code}/accept`, method: 'POST' }),
      invalidatesTags: ['AccountabilityGroup']
    }),
    getGroupStatus: builder.query<GroupStatusResponse, string>({
      query: (groupId) => `/accountability/groups/${groupId}/status`,
      providesTags: ['AccountabilityGroup']
    }),
    leaveGroup: builder.mutation<void, string>({
      query: (groupId) => ({ url: `/accountability/groups/${groupId}/leave`, method: 'POST' }),
      invalidatesTags: ['AccountabilityGroup']
    }),
    dissolveGroup: builder.mutation<void, string>({
      query: (groupId) => ({ url: `/accountability/groups/${groupId}`, method: 'DELETE' }),
      invalidatesTags: ['AccountabilityGroup']
    })
  })
})

export const {
  useListGroupsQuery,
  useCreateInviteMutation,
  useAcceptInviteMutation,
  useGetGroupStatusQuery,
  useLeaveGroupMutation,
  useDissolveGroupMutation
} = accountabilityApi
