import { useEffect } from 'react'
import { useAppDispatch, useAppSelector } from '@renderer/store/hooks'
import { sessionEnded, sessionStatusChanged } from '@renderer/store/slices/sessionSlice'
import { useEndSessionMutation, usePauseSessionMutation, useResumeSessionMutation } from '@renderer/store/api/sessionApi'

// Keeps the main process's tray icon in sync with the `session` slice (design-spec.md §3), and
// wires the tray's own pause/resume/end menu items back into the same mutations the in-app
// controls use — one source of truth for the session lifecycle either way.
export function useSessionTraySync(): void {
  const dispatch = useAppDispatch()
  const active = useAppSelector((state) => state.session.active)
  const [pauseSession] = usePauseSessionMutation()
  const [resumeSession] = useResumeSessionMutation()
  const [endSession] = useEndSessionMutation()

  useEffect(() => {
    const label = active ? (active.status === 'PAUSED' ? 'Paused' : 'Active session') : null
    window.locus.session.setStatus(active ? (active.status === 'PAUSED' ? 'paused' : 'active') : null, label)
  }, [active])

  useEffect(() => {
    const unsubscribePause = window.locus.session.onTrayPause(() => {
      if (active) {
        void pauseSession(active.id).then(() => dispatch(sessionStatusChanged('PAUSED')))
      }
    })
    const unsubscribeResume = window.locus.session.onTrayResume(() => {
      if (active) {
        void resumeSession(active.id).then(() => dispatch(sessionStatusChanged('ACTIVE')))
      }
    })
    const unsubscribeEnd = window.locus.session.onTrayEnd(() => {
      if (active) {
        void endSession(active.id).then(() => dispatch(sessionEnded()))
      }
    })
    return () => {
      unsubscribePause()
      unsubscribeResume()
      unsubscribeEnd()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [active])
}
