import { Flame } from 'lucide-react'
import { QueryState } from '@renderer/components/QueryState'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { Button } from '@renderer/components/ui/button'
import { useGetSummaryQuery, type SummaryResponse } from '@renderer/store/api/analyticsApi'
import { useNavigate } from 'react-router-dom'

function formatFocusTime(seconds: number): string {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m`
}

// design-spec.md §4: the app's default landing route — current streak, this week's completion
// rate. A quiet, persistent streak indicator per §2's streak-treatment callout, not a badge.
export function DashboardScreen(): React.JSX.Element {
  const navigate = useNavigate()
  const query = useGetSummaryQuery()

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-semibold text-text-primary">Dashboard</h1>
      <QueryState
        {...query}
        emptyState={
          <Card className="text-center">
            <CardHeader>
              <CardTitle>No sessions yet</CardTitle>
              <CardDescription>Start your first focus session to see your stats here.</CardDescription>
            </CardHeader>
            <Button onClick={() => navigate('/session/start')}>Start a session</Button>
          </Card>
        }
        isEmpty={(data: SummaryResponse) => data.sessionsCompletedThisWeek === 0 && data.currentStreakDays === 0}
      >
        {(summary) => (
          <div className="grid grid-cols-2 gap-4">
            <Card>
              <CardHeader>
                <CardDescription>Current streak</CardDescription>
                <CardTitle className="flex items-center gap-2 text-2xl">
                  <Flame size={20} className="text-warning" strokeWidth={1.75} />
                  {summary.currentStreakDays} {summary.currentStreakDays === 1 ? 'day' : 'days'}
                </CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader>
                <CardDescription>This week's completion rate</CardDescription>
                <CardTitle className="text-2xl">{Math.round(summary.completionRateThisWeek * 100)}%</CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader>
                <CardDescription>Sessions completed this week</CardDescription>
                <CardTitle className="text-2xl">{summary.sessionsCompletedThisWeek}</CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader>
                <CardDescription>Focus time this week</CardDescription>
                <CardTitle className="text-2xl">{formatFocusTime(summary.totalFocusSecondsThisWeek)}</CardTitle>
              </CardHeader>
            </Card>
          </div>
        )}
      </QueryState>
    </div>
  )
}
