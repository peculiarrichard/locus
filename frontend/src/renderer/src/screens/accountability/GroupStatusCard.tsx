import { CheckCircle2, Circle, Flame } from 'lucide-react'
import { QueryState } from '@renderer/components/QueryState'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { Button } from '@renderer/components/ui/button'
import { useGetGroupStatusQuery, useLeaveGroupMutation, type GroupResponse } from '@renderer/store/api/accountabilityApi'

// design-spec.md §4: "today's board" showing exactly what the backend exposes — binary
// completed/not, and a per-member streak — never duration, distraction detail, or goal.
export function GroupStatusCard({ group }: { group: GroupResponse }): React.JSX.Element {
  const statusQuery = useGetGroupStatusQuery(group.id)
  const [leaveGroup] = useLeaveGroupMutation()

  return (
    <Card>
      <CardHeader className="mb-2 flex-row items-center justify-between">
        <div>
          <CardTitle>
            {group.groupType === 'PAIR' ? 'Accountability pair' : 'Accountability group'} ({group.memberCount})
          </CardTitle>
          <CardDescription>Created {new Date(group.createdAt).toLocaleDateString()}</CardDescription>
        </div>
        <Button size="sm" variant="ghost" onClick={() => void leaveGroup(group.id)}>
          Leave
        </Button>
      </CardHeader>
      <QueryState {...statusQuery} emptyState={<p className="text-sm text-text-secondary">No status yet.</p>}>
        {(status) => (
          <ul className="flex flex-col gap-2">
            {status.members.map((member) => (
              <li key={member.userId} className="flex items-center gap-3 text-sm">
                {member.completedToday ? (
                  <CheckCircle2 size={16} className="text-success" />
                ) : (
                  <Circle size={16} className="text-text-muted" />
                )}
                <span className="text-text-primary">{member.userId.slice(0, 8)}</span>
                {member.currentStreakDays > 0 && (
                  <span className="flex items-center gap-1 text-xs text-warning">
                    <Flame size={12} /> {member.currentStreakDays}
                  </span>
                )}
              </li>
            ))}
          </ul>
        )}
      </QueryState>
    </Card>
  )
}
