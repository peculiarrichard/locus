import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { QueryState } from './QueryState'

describe('QueryState', () => {
  it('renders a loading skeleton while isLoading is true, regardless of other props', () => {
    render(
      <QueryState isLoading data={['x']} emptyState={<p>empty</p>}>
        {() => <p>data</p>}
      </QueryState>
    )
    expect(screen.queryByText('data')).not.toBeInTheDocument()
    expect(screen.queryByText('empty')).not.toBeInTheDocument()
  })

  it("renders the error envelope's message and correlation id when present", () => {
    render(
      <QueryState
        isLoading={false}
        error={{ status: 500, data: null, envelope: { errorCode: 'E', message: 'It broke', correlationId: 'corr-1', timestamp: 't' } }}
        data={undefined}
        emptyState={<p>empty</p>}
      >
        {() => <p>data</p>}
      </QueryState>
    )
    expect(screen.getByText('It broke')).toBeInTheDocument()
    expect(screen.getByText('Reference: corr-1')).toBeInTheDocument()
  })

  it('falls back to a generic message when the error has no envelope', () => {
    render(
      <QueryState isLoading={false} error={{ status: 'FETCH_ERROR', error: 'network down' }} data={undefined} emptyState={<p>empty</p>}>
        {() => <p>data</p>}
      </QueryState>
    )
    expect(screen.getByText('Something went wrong.')).toBeInTheDocument()
  })

  it('renders the empty state when data is undefined', () => {
    render(
      <QueryState isLoading={false} data={undefined} emptyState={<p>empty</p>}>
        {() => <p>data</p>}
      </QueryState>
    )
    expect(screen.getByText('empty')).toBeInTheDocument()
  })

  it('renders the empty state when isEmpty(data) returns true, even though data is defined', () => {
    render(
      <QueryState isLoading={false} data={[]} isEmpty={(d: unknown[]) => d.length === 0} emptyState={<p>empty</p>}>
        {() => <p>data</p>}
      </QueryState>
    )
    expect(screen.getByText('empty')).toBeInTheDocument()
  })

  it('renders children(data) when data is present and not empty', () => {
    render(
      <QueryState isLoading={false} data={['a']} isEmpty={(d: unknown[]) => d.length === 0} emptyState={<p>empty</p>}>
        {(data) => <p>got {data.length} item(s)</p>}
      </QueryState>
    )
    expect(screen.getByText('got 1 item(s)')).toBeInTheDocument()
  })
})
