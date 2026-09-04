import { baseApi } from './baseApi'

export interface MilestoneResponse {
  id: string
  milestoneName: string
  description: string | null
  dueDate: string
  completedAt: string | null
}

export interface GoalResponse {
  id: string
  goalType: 'EXAM' | 'CERTIFICATION' | 'CAPSTONE'
  title: string
  targetDate: string
  status: 'ACTIVE' | 'COMPLETED' | 'ABANDONED' | 'EXPIRED'
  milestones: MilestoneResponse[]
}

export const goalsApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    listGoals: builder.query<GoalResponse[], void>({
      query: () => '/goals',
      providesTags: ['Goal']
    }),
    getGoal: builder.query<GoalResponse, string>({
      query: (id) => `/goals/${id}`,
      providesTags: ['Goal']
    }),
    createGoal: builder.mutation<GoalResponse, { goalType: GoalResponse['goalType']; title: string; targetDate: string }>({
      query: (body) => ({ url: '/goals', method: 'POST', body }),
      invalidatesTags: ['Goal']
    }),
    updateGoal: builder.mutation<GoalResponse, { id: string; title?: string; targetDate?: string }>({
      query: ({ id, ...body }) => ({ url: `/goals/${id}`, method: 'PATCH', body }),
      invalidatesTags: ['Goal']
    }),
    completeMilestone: builder.mutation<MilestoneResponse, { goalId: string; milestoneId: string }>({
      query: ({ goalId, milestoneId }) => ({ url: `/goals/${goalId}/milestones/${milestoneId}/complete`, method: 'POST' }),
      invalidatesTags: ['Goal']
    }),
    abandonGoal: builder.mutation<GoalResponse, string>({
      query: (id) => ({ url: `/goals/${id}/abandon`, method: 'POST' }),
      invalidatesTags: ['Goal']
    })
  })
})

export const {
  useListGoalsQuery,
  useGetGoalQuery,
  useCreateGoalMutation,
  useUpdateGoalMutation,
  useCompleteMilestoneMutation,
  useAbandonGoalMutation
} = goalsApi
