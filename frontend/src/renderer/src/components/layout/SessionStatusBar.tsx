import { useEffect, useState } from 'react'
import { Pause, Play, Square } from 'lucide-react'
import { Button } from '@renderer/components/ui/button'
import { useAppDispatch, useAppSelector } from '@renderer/store/hooks'
import { sessionEnded, sessionStatusChanged } from '@renderer/store/slices/sessionSlice'
import { useEndSessionMutation, usePauseSessionMutation, useResumeSessionMutation } from '@renderer/store/api/sessionApi'
import { clearHeartbeat } from '@renderer/lib/sessionHeartbeat'

function formatElapsed(startedAt: string, accumulatedPauseSeconds: number, isPaused: boolean, pausedAtTick: number): string {
  const elapsedMs = pausedAtTick - new Date(startedAt).getTime()
  const activeSeconds = Math.max(0, Math.floor(elapsedMs / 1000) - accumulatedPauseSeconds)
  const minutes = Math.floor(activeSeconds / 60)
  const seconds = activeSeconds % 60
  const paused = isPaused ? ' (paused)' : ''
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}${paused}`
}

// design-spec.md §3: a cross-cutting concern, visible on every route once a session is active or
// paused, mirroring the same `session` slice state the tray icon reflects (useSessionTraySync).
export function SessionStatusBar(): React.JSX.Element | null {
  const dispatch = useAppDispatch()
  const active = useAppSelector((state) => state.session.active)
  const [now, setNow] = useState(() => Date.now())
  const [pauseSession] = usePauseSessionMutation()
  const [resumeSession] = useResumeSessionMutation()
  const [endSession] = useEndSessionMutation()

  useEffect(() => {
    if (!active || active.status === 'PAUSED') {
      return
    }
    const interval = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(interval)
  }, [active])

  if (!active) {
    return null
  }

  return (
    <div className="flex items-center justify-between border-b border-border bg-surface-raised px-6 py-2">
      <span className="font-mono text-sm text-text-primary">
        {formatElapsed(active.startedAt, active.accumulatedPauseSeconds, active.status === 'PAUSED', now)}
      </span>
      <div className="flex gap-2">
        {active.status === 'ACTIVE' ? (
          <Button
            size="sm"
            variant="outline"
            onClick={() => void pauseSession(active.id).then(() => dispatch(sessionStatusChanged('PAUSED')))}
          >
            <Pause size={14} /> Pause
          </Button>
        ) : (
          <Button
            size="sm"
            variant="outline"
            onClick={() => void resumeSession(active.id).then(() => dispatch(sessionStatusChanged('ACTIVE')))}
          >
            <Play size={14} /> Resume
          </Button>
        )}
        <Button
          size="sm"
          variant="destructive"
          onClick={() =>
            void endSession(active.id).then(() => {
              clearHeartbeat()
              dispatch(sessionEnded())
            })
          }
        >
          <Square size={14} /> End
        </Button>
      </div>
    </div>
  )
}
