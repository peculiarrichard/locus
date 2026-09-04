import { useState } from 'react'
import { QueryState } from '@renderer/components/QueryState'
import { Button } from '@renderer/components/ui/button'
import { useGetHistoryQuery } from '@renderer/store/api/analyticsApi'

const RANGES = [7, 30, 90] as const

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
}

function formatDuration(seconds: number): string {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours === 0) {
    return `${minutes}m`
  }
  return `${hours}h ${minutes}m`
}

export function HistoryScreen(): React.JSX.Element {
  const [range, setRange] = useState<(typeof RANGES)[number]>(30)
  const query = useGetHistoryQuery(range)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-text-primary">History</h1>
        <div className="flex gap-2">
          {RANGES.map((r) => (
            <Button key={r} size="sm" variant={range === r ? 'default' : 'outline'} onClick={() => setRange(r)}>
              {r}d
            </Button>
          ))}
        </div>
      </div>
      <QueryState
        {...query}
        isEmpty={(days) => days.every((d) => d.sessionsCompleted === 0 && d.sessionsAbandoned === 0)}
        emptyState={<p className="text-sm text-text-secondary">No sessions in this range yet.</p>}
      >
        {(days) => (
          <div className="overflow-x-auto rounded-lg border border-border">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-left text-xs text-text-muted">
                  <th className="px-4 py-2 font-medium">Date</th>
                  <th className="px-4 py-2 font-medium">Completed</th>
                  <th className="px-4 py-2 font-medium">Abandoned</th>
                  <th className="px-4 py-2 font-medium">Focus time</th>
                  <th className="px-4 py-2 font-medium">Distractions</th>
                </tr>
              </thead>
              <tbody>
                {[...days]
                  .sort((a, b) => b.date.localeCompare(a.date))
                  .map((day) => (
                    <tr key={day.date} className="border-b border-border last:border-0">
                      <td className="px-4 py-2 text-text-primary">{formatDate(day.date)}</td>
                      <td className="px-4 py-2 text-text-secondary">{day.sessionsCompleted}</td>
                      <td className="px-4 py-2 text-text-secondary">{day.sessionsAbandoned}</td>
                      <td className="px-4 py-2 text-text-secondary">{formatDuration(day.totalFocusSeconds)}</td>
                      <td className="px-4 py-2 text-text-secondary">{day.distractionCount}</td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        )}
      </QueryState>
    </div>
  )
}
