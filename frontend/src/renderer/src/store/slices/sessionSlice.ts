import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

export interface ActiveSessionState {
  id: string
  sessionType: 'POMODORO' | 'DEEP_WORK' | 'EXAM_COUNTDOWN'
  status: 'ACTIVE' | 'PAUSED'
  startedAt: string
  accumulatedPauseSeconds: number
  goalId: string | null
  plannedDurationSeconds: number | null
}

interface SessionState {
  active: ActiveSessionState | null
  reconciliationNotice: string | null
}

const initialState: SessionState = { active: null, reconciliationNotice: null }

const sessionSlice = createSlice({
  name: 'session',
  initialState,
  reducers: {
    sessionStarted(state, action: PayloadAction<ActiveSessionState>) {
      state.active = action.payload
    },
    sessionStatusChanged(state, action: PayloadAction<ActiveSessionState['status']>) {
      if (state.active) {
        state.active.status = action.payload
      }
    },
    sessionEnded(state) {
      state.active = null
    },
    reconciliationNoticeShown(state, action: PayloadAction<string>) {
      state.reconciliationNotice = action.payload
    },
    reconciliationNoticeDismissed(state) {
      state.reconciliationNotice = null
    }
  }
})

export const { sessionStarted, sessionStatusChanged, sessionEnded, reconciliationNoticeShown, reconciliationNoticeDismissed } =
  sessionSlice.actions
export default sessionSlice.reducer
