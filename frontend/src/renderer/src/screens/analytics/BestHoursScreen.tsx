import { QueryState } from '@renderer/components/QueryState'
import { Card } from '@renderer/components/ui/card'
import { useGetBestHoursQuery } from '@renderer/store/api/analyticsApi'

function formatHour(hour: number): string {
  const period = hour < 12 ? 'AM' : 'PM'
  const display = hour % 12 === 0 ? 12 : hour % 12
  return `${display}${period}`
}

export function BestHoursScreen(): React.JSX.Element {
  const query = useGetBestHoursQuery()
  const maxSeconds = Math.max(1, ...(query.data?.map((h) => h.totalFocusSeconds) ?? [1]))

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-semibold text-text-primary">Best study hours</h1>
      <QueryState
        {...query}
        isEmpty={(hours) => hours.length === 0}
        emptyState={<p className="text-sm text-text-secondary">No sessions yet — start your first one.</p>}
      >
        {(hours) => (
          <Card className="flex flex-col gap-2">
            {[...hours]
              .sort((a, b) => b.totalFocusSeconds - a.totalFocusSeconds)
              .map((hour) => (
                <div key={hour.hourOfDay} className="flex items-center gap-3">
                  <span className="w-12 text-xs text-text-secondary">{formatHour(hour.hourOfDay)}</span>
                  <div className="h-3 flex-1 rounded bg-surface-raised">
                    <div
                      className="h-3 rounded bg-accent"
                      style={{ width: `${(hour.totalFocusSeconds / maxSeconds) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
          </Card>
        )}
      </QueryState>
    </div>
  )
}
