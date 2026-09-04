import { useParams } from 'react-router-dom'
import { QueryState } from '@renderer/components/QueryState'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useGetSessionQuery } from '@renderer/store/api/sessionApi'
import { useListDistractionsQuery } from '@renderer/store/api/distractionsApi'

function formatDuration(seconds: number | null): string {
  if (seconds === null) {
    return '—'
  }
  const minutes = Math.floor(seconds / 60)
  const remainder = seconds % 60
  return `${minutes}m ${remainder}s`
}

export function SessionSummaryScreen(): React.JSX.Element {
  const { sessionId } = useParams<{ sessionId: string }>()
  const sessionQuery = useGetSessionQuery(sessionId ?? '', { skip: !sessionId })
  const distractionsQuery = useListDistractionsQuery(sessionId ?? '', { skip: !sessionId })

  return (
    <QueryState {...sessionQuery} emptyState={<p className="text-text-secondary">Session not found.</p>}>
      {(session) => (
        <div className="flex flex-col gap-6">
          <div>
            <h1 className="text-xl font-semibold text-text-primary">Session complete</h1>
            <p className="text-sm text-text-secondary">
              {session.sessionType.replace('_', ' ')} · {formatDuration(session.durationSeconds)}
            </p>
          </div>
          <Card>
            <CardHeader>
              <CardTitle>Distractions during this session</CardTitle>
              <CardDescription>Blur/focus cycles logged while this session was active.</CardDescription>
            </CardHeader>
            <QueryState
              {...distractionsQuery}
              isEmpty={(items) => items.length === 0}
              emptyState={<p className="text-sm text-text-secondary">No distractions logged — nice focus.</p>}
            >
              {(distractions) => (
                <ul className="flex flex-col gap-1 text-sm text-text-secondary">
                  {distractions.map((d) => (
                    <li key={d.id}>
                      {new Date(d.occurredAt).toLocaleTimeString()} — {d.durationSeconds}s away
                    </li>
                  ))}
                </ul>
              )}
            </QueryState>
          </Card>
        </div>
      )}
    </QueryState>
  )
}
