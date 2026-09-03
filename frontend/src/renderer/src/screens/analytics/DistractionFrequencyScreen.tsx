import { QueryState } from '@renderer/components/QueryState'
import { Card } from '@renderer/components/ui/card'
import { useGetDistractionFrequencyQuery } from '@renderer/store/api/analyticsApi'

function formatHour(hour: number): string {
  const period = hour < 12 ? 'AM' : 'PM'
  const display = hour % 12 === 0 ? 12 : hour % 12
  return `${display}${period}`
}

export function DistractionFrequencyScreen(): React.JSX.Element {
  const query = useGetDistractionFrequencyQuery()
  const maxRate = Math.max(1, ...(query.data?.map((h) => h.distractionsPerFocusHour) ?? [1]))

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-semibold text-text-primary">Distraction frequency</h1>
      <p className="text-sm text-text-secondary">Distractions logged per hour of focus time — higher means that hour is more distraction-prone, not just busier.</p>
      <QueryState
        {...query}
        isEmpty={(hours) => hours.length === 0}
        emptyState={<p className="text-sm text-text-secondary">No distractions logged yet.</p>}
      >
        {(hours) => (
          <Card className="flex flex-col gap-2">
            {[...hours]
              .sort((a, b) => b.distractionsPerFocusHour - a.distractionsPerFocusHour)
              .map((hour) => (
                <div key={hour.hourOfDay} className="flex items-center gap-3">
                  <span className="w-12 text-xs text-text-secondary">{formatHour(hour.hourOfDay)}</span>
                  <div className="h-3 flex-1 rounded bg-surface-raised">
                    <div
                      className="h-3 rounded bg-danger"
                      style={{ width: `${(hour.distractionsPerFocusHour / maxRate) * 100}%` }}
                    />
                  </div>
                  <span className="w-10 text-right text-xs text-text-muted">{hour.distractionCount}</span>
                </div>
              ))}
          </Card>
        )}
      </QueryState>
    </div>
  )
}
