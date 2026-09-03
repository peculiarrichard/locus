import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@renderer/components/ui/button'
import { Input } from '@renderer/components/ui/input'
import { Label } from '@renderer/components/ui/label'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useStartSessionMutation, type StartSessionRequest } from '@renderer/store/api/sessionApi'
import { useAppDispatch, useAppSelector } from '@renderer/store/hooks'
import { sessionStarted } from '@renderer/store/slices/sessionSlice'
import { useListGoalsQuery } from '@renderer/store/api/goalsApi'
import type { ErrorEnvelope } from '@renderer/store/api/baseApi'

const SESSION_TYPES: StartSessionRequest['sessionType'][] = ['POMODORO', 'DEEP_WORK', 'EXAM_COUNTDOWN']

// design-spec.md §4: unreachable while a session is already active/paused — the backend's
// one-active-session rule is a hard 409, but the UI shouldn't rely on the error path as its only
// guard.
export function StartSessionScreen(): React.JSX.Element {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const activeSession = useAppSelector((state) => state.session.active)
  const [sessionType, setSessionType] = useState<StartSessionRequest['sessionType']>('DEEP_WORK')
  const [plannedDurationMinutes, setPlannedDurationMinutes] = useState(30)
  const [workMinutes, setWorkMinutes] = useState(25)
  const [breakMinutes, setBreakMinutes] = useState(5)
  const [cycleCount, setCycleCount] = useState(4)
  const [goalId, setGoalId] = useState('')
  const [startSession, { isLoading }] = useStartSessionMutation()
  const { data: goals } = useListGoalsQuery()

  if (activeSession) {
    return <Navigate to="/session/active" replace />
  }

  const onStart = async (): Promise<void> => {
    const body: StartSessionRequest =
      sessionType === 'POMODORO'
        ? { sessionType, workMinutes, breakMinutes, cycleCount, goalId: goalId || undefined }
        : sessionType === 'EXAM_COUNTDOWN'
          ? { sessionType, goalId: goalId || undefined }
          : { sessionType, plannedDurationSeconds: plannedDurationMinutes * 60, goalId: goalId || undefined }
    try {
      const session = await startSession(body).unwrap()
      dispatch(sessionStarted({ ...session, status: 'ACTIVE' }))
      navigate('/session/active')
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Could not start session')
    }
  }

  return (
    <Card className="max-w-md">
      <CardHeader>
        <CardTitle>Start a session</CardTitle>
        <CardDescription>Choose a type and go — the timer stays visible everywhere while it runs.</CardDescription>
      </CardHeader>
      <div className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <Label>Type</Label>
          <div className="flex gap-2">
            {SESSION_TYPES.map((type) => (
              <Button key={type} type="button" size="sm" variant={sessionType === type ? 'default' : 'outline'} onClick={() => setSessionType(type)}>
                {type.replace('_', ' ')}
              </Button>
            ))}
          </div>
        </div>

        {sessionType === 'POMODORO' && (
          <div className="grid grid-cols-3 gap-2">
            <div className="flex flex-col gap-2">
              <Label htmlFor="workMinutes">Work (min)</Label>
              <Input id="workMinutes" type="number" min={1} value={workMinutes} onChange={(e) => setWorkMinutes(Number(e.target.value))} />
            </div>
            <div className="flex flex-col gap-2">
              <Label htmlFor="breakMinutes">Break (min)</Label>
              <Input id="breakMinutes" type="number" min={1} value={breakMinutes} onChange={(e) => setBreakMinutes(Number(e.target.value))} />
            </div>
            <div className="flex flex-col gap-2">
              <Label htmlFor="cycleCount">Cycles</Label>
              <Input id="cycleCount" type="number" min={1} value={cycleCount} onChange={(e) => setCycleCount(Number(e.target.value))} />
            </div>
          </div>
        )}

        {sessionType === 'DEEP_WORK' && (
          <div className="flex flex-col gap-2">
            <Label htmlFor="duration">Planned duration (min)</Label>
            <Input
              id="duration"
              type="number"
              min={1}
              value={plannedDurationMinutes}
              onChange={(e) => setPlannedDurationMinutes(Number(e.target.value))}
            />
          </div>
        )}

        {goals && goals.length > 0 && (
          <div className="flex flex-col gap-2">
            <Label htmlFor="goal">Goal (optional)</Label>
            <select
              id="goal"
              className="h-10 rounded border border-border bg-surface px-3 text-sm text-text-primary"
              value={goalId}
              onChange={(e) => setGoalId(e.target.value)}
            >
              <option value="">None</option>
              {goals.map((goal) => (
                <option key={goal.id} value={goal.id}>
                  {goal.title}
                </option>
              ))}
            </select>
          </div>
        )}

        <Button onClick={() => void onStart()} disabled={isLoading}>
          Start session
        </Button>
      </div>
    </Card>
  )
}
