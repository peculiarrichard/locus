import { describe, expect, it } from 'vitest'
import reducer, {
  reconciliationNoticeDismissed,
  reconciliationNoticeShown,
  sessionEnded,
  sessionStarted,
  sessionStatusChanged
} from './sessionSlice'
import type { ActiveSessionState } from './sessionSlice'

const session: ActiveSessionState = {
  id: 's1',
  sessionType: 'DEEP_WORK',
  status: 'ACTIVE',
  startedAt: '2026-01-01T00:00:00Z',
  accumulatedPauseSeconds: 0,
  goalId: null,
  plannedDurationSeconds: 1800
}

describe('sessionSlice', () => {
  const initialState = { active: null, reconciliationNotice: null }

  it('starts with no active session and no notice', () => {
    expect(reducer(undefined, { type: '@@INIT' })).toEqual(initialState)
  })

  it('sessionStarted sets the active session verbatim', () => {
    expect(reducer(initialState, sessionStarted(session)).active).toEqual(session)
  })

  it('sessionStatusChanged updates only the status field of an existing session', () => {
    const started = reducer(initialState, sessionStarted(session))
    const paused = reducer(started, sessionStatusChanged('PAUSED'))
    expect(paused.active).toEqual({ ...session, status: 'PAUSED' })
  })

  it('sessionStatusChanged is a no-op when there is no active session', () => {
    expect(reducer(initialState, sessionStatusChanged('PAUSED')).active).toBeNull()
  })

  it('sessionEnded clears the active session', () => {
    const started = reducer(initialState, sessionStarted(session))
    expect(reducer(started, sessionEnded()).active).toBeNull()
  })

  it('reconciliation notice can be shown and dismissed independently of the active session', () => {
    const shown = reducer(initialState, reconciliationNoticeShown('Previous session ended unexpectedly'))
    expect(shown.reconciliationNotice).toBe('Previous session ended unexpectedly')
    expect(reducer(shown, reconciliationNoticeDismissed()).reconciliationNotice).toBeNull()
  })
})
