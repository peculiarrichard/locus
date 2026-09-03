import { useNavigate } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { QueryState } from '@renderer/components/QueryState'
import { Button } from '@renderer/components/ui/button'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useListGoalsQuery } from '@renderer/store/api/goalsApi'

const STATUS_BADGE: Record<string, string> = {
  ACTIVE: 'text-accent',
  COMPLETED: 'text-success',
  ABANDONED: 'text-text-muted',
  EXPIRED: 'text-warning'
}

export function GoalsListScreen(): React.JSX.Element {
  const navigate = useNavigate()
  const query = useListGoalsQuery()

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-text-primary">Goals</h1>
        <Button onClick={() => navigate('/goals/new')}>
          <Plus size={16} /> New goal
        </Button>
      </div>
      <QueryState
        {...query}
        isEmpty={(goals) => goals.length === 0}
        emptyState={
          <Card className="text-center">
            <CardHeader>
              <CardTitle>No goals yet</CardTitle>
              <CardDescription>Create a goal to get a milestone plan built for you.</CardDescription>
            </CardHeader>
            <Button onClick={() => navigate('/goals/new')}>Create your first goal</Button>
          </Card>
        }
      >
        {(goals) => (
          <div className="flex flex-col gap-3">
            {goals.map((goal) => (
              <Card key={goal.id} className="cursor-pointer hover:border-accent" onClick={() => navigate(`/goals/${goal.id}`)}>
                <CardHeader className="mb-0 flex-row items-center justify-between">
                  <div>
                    <CardTitle>{goal.title}</CardTitle>
                    <CardDescription>
                      {goal.goalType} · due {new Date(goal.targetDate).toLocaleDateString()}
                    </CardDescription>
                  </div>
                  <span className={`text-xs font-medium ${STATUS_BADGE[goal.status]}`}>{goal.status}</span>
                </CardHeader>
              </Card>
            ))}
          </div>
        )}
      </QueryState>
    </div>
  )
}
