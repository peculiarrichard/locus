import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@renderer/components/ui/button'
import { Input } from '@renderer/components/ui/input'
import { Label } from '@renderer/components/ui/label'
import { Card, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useCreateGoalMutation, type GoalResponse } from '@renderer/store/api/goalsApi'
import type { ErrorEnvelope } from '@renderer/store/api/baseApi'

const GOAL_TYPES: GoalResponse['goalType'][] = ['EXAM', 'CERTIFICATION', 'CAPSTONE']

export function CreateGoalScreen(): React.JSX.Element {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [goalType, setGoalType] = useState<GoalResponse['goalType']>('EXAM')
  const [targetDate, setTargetDate] = useState('')
  const [createGoal, { isLoading }] = useCreateGoalMutation()

  const onSubmit = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault()
    try {
      const goal = await createGoal({ title, goalType, targetDate }).unwrap()
      toast.success('Goal created')
      navigate(`/goals/${goal.id}`)
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Could not create goal')
    }
  }

  return (
    <Card className="max-w-md">
      <CardHeader>
        <CardTitle>New goal</CardTitle>
      </CardHeader>
      <form onSubmit={onSubmit} className="flex flex-col gap-4">
        <div className="flex flex-col gap-2">
          <Label htmlFor="title">Title</Label>
          <Input id="title" required maxLength={200} value={title} onChange={(e) => setTitle(e.target.value)} />
        </div>
        <div className="flex flex-col gap-2">
          <Label htmlFor="goalType">Type</Label>
          <select
            id="goalType"
            className="h-10 rounded border border-border bg-surface px-3 text-sm text-text-primary"
            value={goalType}
            onChange={(e) => setGoalType(e.target.value as GoalResponse['goalType'])}
          >
            {GOAL_TYPES.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
        </div>
        <div className="flex flex-col gap-2">
          <Label htmlFor="targetDate">Target date</Label>
          <Input
            id="targetDate"
            type="date"
            required
            min={new Date(Date.now() + 86400000).toISOString().slice(0, 10)}
            value={targetDate}
            onChange={(e) => setTargetDate(e.target.value)}
          />
        </div>
        <div className="flex gap-2">
          <Button type="submit" disabled={isLoading}>
            Create goal
          </Button>
          <Button type="button" variant="ghost" onClick={() => navigate('/goals')}>
            Cancel
          </Button>
        </div>
      </form>
    </Card>
  )
}
