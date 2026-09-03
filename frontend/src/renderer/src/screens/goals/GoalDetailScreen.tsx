import { useParams } from 'react-router-dom'
import { CheckCircle2, Circle } from 'lucide-react'
import { QueryState } from '@renderer/components/QueryState'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { Button } from '@renderer/components/ui/button'
import { useCompleteMilestoneMutation, useGetGoalQuery } from '@renderer/store/api/goalsApi'

export function GoalDetailScreen(): React.JSX.Element {
  const { goalId } = useParams<{ goalId: string }>()
  const query = useGetGoalQuery(goalId ?? '', { skip: !goalId })
  const [completeMilestone] = useCompleteMilestoneMutation()

  return (
    <QueryState {...query} emptyState={<p className="text-text-secondary">Goal not found.</p>}>
      {(goal) => (
        <div className="flex flex-col gap-6">
          <div>
            <h1 className="text-xl font-semibold text-text-primary">{goal.title}</h1>
            <p className="text-sm text-text-secondary">
              {goal.goalType} · due {new Date(goal.targetDate).toLocaleDateString()} · {goal.status}
            </p>
          </div>
          <Card>
            <CardHeader>
              <CardTitle>Milestones</CardTitle>
              <CardDescription>Copied from the {goal.goalType.toLowerCase()} plan template when this goal was created.</CardDescription>
            </CardHeader>
            <div className="flex flex-col gap-2">
              {goal.milestones.map((milestone) => (
                <div key={milestone.id} className="flex items-center gap-3 rounded border border-border p-3">
                  <Button
                    variant="ghost"
                    size="sm"
                    disabled={!!milestone.completedAt}
                    onClick={() => goalId && void completeMilestone({ goalId, milestoneId: milestone.id })}
                  >
                    {milestone.completedAt ? (
                      <CheckCircle2 size={18} className="text-success" />
                    ) : (
                      <Circle size={18} className="text-text-muted" />
                    )}
                  </Button>
                  <div className="flex-1">
                    <p className="text-sm text-text-primary">{milestone.milestoneName}</p>
                    <p className="text-xs text-text-muted">Due {new Date(milestone.dueDate).toLocaleDateString()}</p>
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </div>
      )}
    </QueryState>
  )
}
