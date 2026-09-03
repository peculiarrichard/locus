import type { ReactNode } from 'react'
import type { SerializedError } from '@reduxjs/toolkit'
import type { FetchBaseQueryError } from '@reduxjs/toolkit/query/react'
import type { ErrorEnvelope } from '@renderer/store/api/baseApi'

// design-spec.md §5: every RTK Query-backed screen renders through the same loading/error/data
// (or zero) states rather than each screen inventing its own conditionals.
interface QueryStateProps<T> {
  isLoading: boolean
  error?: (FetchBaseQueryError & { envelope?: ErrorEnvelope }) | SerializedError | undefined
  data?: T | undefined
  isEmpty?: (data: T) => boolean
  emptyState: ReactNode
  children: (data: T) => ReactNode
}

export function QueryState<T>({ isLoading, error, data, isEmpty, emptyState, children }: QueryStateProps<T>): ReactNode {
  if (isLoading) {
    return <div className="animate-pulse space-y-3">
      <div className="h-4 w-1/3 rounded bg-surface-raised" />
      <div className="h-24 rounded bg-surface-raised" />
    </div>
  }

  if (error) {
    const envelope = 'envelope' in error ? error.envelope : undefined
    return (
      <div className="rounded border border-danger/40 bg-danger/10 p-4 text-sm text-danger">
        <p>{envelope?.message ?? 'Something went wrong.'}</p>
        {envelope?.correlationId && <p className="mt-1 text-xs text-text-muted">Reference: {envelope.correlationId}</p>}
      </div>
    )
  }

  if (data === undefined || (isEmpty && isEmpty(data))) {
    return <>{emptyState}</>
  }

  return <>{children(data)}</>
}
