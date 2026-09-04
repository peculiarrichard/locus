import { useEffect, useRef, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { Pause, Play, Square } from 'lucide-react'
import { Button } from '@renderer/components/ui/button'
import { useAppDispatch, useAppSelector } from '@renderer/store/hooks'
import { sessionEnded, sessionStatusChanged } from '@renderer/store/slices/sessionSlice'
import { useEndSessionMutation, usePauseSessionMutation, useResumeSessionMutation } from '@renderer/store/api/sessionApi'
import { clearHeartbeat, writeHeartbeat } from '@renderer/lib/sessionHeartbeat'

// design-spec.md §4: end is always explicit, never auto-triggered by a timer reaching zero, per
// frd.md — even a planned-duration session just keeps counting past its target.
export function ActiveSessionScreen(): React.JSX.Element | null {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const active = useAppSelector((state) => state.session.active)
  const [now, setNow] = useState(() => Date.now())
  const [pauseSession] = usePauseSessionMutation()
  const [resumeSession] = useResumeSessionMutation()
  const [endSession, { isLoading: isEnding }] = useEndSessionMutation()
  const endingRef = useRef(false)

  useEffect(() => {
    if (!active) {
      return
    }
    writeHeartbeat(active.id)
    if (active.status === 'PAUSED') {
      return
    }
    const interval = setInterval(() => {
      setNow(Date.now())
      writeHeartbeat(active.id)
    }, 1000)
    return () => clearInterval(interval)
  }, [active])

  if (!active) {
    // Also reached mid-flow from onEnd below: dispatch(sessionEnded()) notifies react-redux
    // subscribers synchronously, which can re-render this still-mounted component with
    // active === null before React has committed the pending navigate() to /session/summary —
    // rendering null here (instead of redirecting again) lets that navigation win instead of
    // this fallback's own <Navigate> clobbering it with /session/start.
    return endingRef.current ? null : <Navigate to="/session/start" replace />
  }

  const elapsedMs = now - new Date(active.startedAt).getTime()
  const activeSeconds = Math.max(0, Math.floor(elapsedMs / 1000) - active.accumulatedPauseSeconds)
  const minutes = Math.floor(activeSeconds / 60)
  const seconds = activeSeconds % 60

  const onEnd = async (): Promise<void> => {
    endingRef.current = true
    await endSession(active.id).unwrap()
    clearHeartbeat()
    navigate(`/session/summary/${active.id}`)
    dispatch(sessionEnded())
  }

  return (
    <div className="flex h-full flex-col items-center justify-center gap-8">
      <p className="text-sm uppercase tracking-wide text-text-secondary">{active.sessionType.replace('_', ' ')}</p>
      <p className="font-mono text-6xl text-text-primary">
        {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
      </p>
      {active.status === 'PAUSED' && <p className="text-sm text-warning">Paused</p>}
      <div className="flex gap-3">
        {active.status === 'ACTIVE' ? (
          <Button
            size="lg"
            variant="outline"
            onClick={() => void pauseSession(active.id).then(() => dispatch(sessionStatusChanged('PAUSED')))}
          >
            <Pause size={18} /> Pause
          </Button>
        ) : (
          <Button
            size="lg"
            variant="outline"
            onClick={() => void resumeSession(active.id).then(() => dispatch(sessionStatusChanged('ACTIVE')))}
          >
            <Play size={18} /> Resume
          </Button>
        )}
        <Button size="lg" variant="destructive" disabled={isEnding} onClick={() => void onEnd()}>
          <Square size={18} /> End session
        </Button>
      </div>
    </div>
  )
}
