import { act, renderHook, waitFor } from '@testing-library/react'
import { Provider } from 'react-redux'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useDistractionLogging } from './useDistractionLogging'
import { createTestStore, type TestStore } from '../../test/testStore'
import { sessionStarted } from '@renderer/store/slices/sessionSlice'
import type { ActiveSessionState } from '@renderer/store/slices/sessionSlice'

const session: ActiveSessionState = {
  id: 'session-1',
  sessionType: 'DEEP_WORK',
  status: 'ACTIVE',
  startedAt: '2026-01-01T00:00:00Z',
  accumulatedPauseSeconds: 0,
  goalId: null,
  plannedDurationSeconds: 1800
}

function jsonResponse(body: unknown, status = 201): Response {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

let store: TestStore
let blurCallback: () => void
let focusCallback: () => void

function renderWithStore() {
  return renderHook(() => useDistractionLogging(), {
    wrapper: ({ children }) => <Provider store={store}>{children}</Provider>
  })
}

describe('useDistractionLogging', () => {
  beforeEach(() => {
    store = createTestStore()
    store.dispatch(sessionStarted(session))
    vi.useFakeTimers({ shouldAdvanceTime: true })
    vi.setSystemTime(new Date('2026-01-01T00:10:00Z'))

    window.locus.distraction.onBlur = vi.fn((cb: () => void) => {
      blurCallback = cb
      return () => {}
    })
    window.locus.distraction.onFocus = vi.fn((cb: () => void) => {
      focusCallback = cb
      return () => {}
    })
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllGlobals()
  })

  it('logs a distraction when the window was blurred for at least 3 seconds', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({ id: 'd1', sessionId: session.id, occurredAt: '2026-01-01T00:10:00.000Z', durationSeconds: 5 }))
    vi.stubGlobal('fetch', fetchMock)
    renderWithStore()

    act(() => blurCallback())
    vi.setSystemTime(new Date('2026-01-01T00:10:05Z'))
    act(() => focusCallback())

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1))
    const request = fetchMock.mock.calls[0][0] as Request
    expect(request.url).toContain('/distractions')
    const body = JSON.parse(await request.clone().text())
    expect(body.sessionId).toBe(session.id)
    expect(body.durationSeconds).toBe(5)
    expect(typeof body.id).toBe('string')
  })

  it('does not log a blur shorter than the 3-second minimum threshold', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({}))
    vi.stubGlobal('fetch', fetchMock)
    renderWithStore()

    act(() => blurCallback())
    vi.setSystemTime(new Date('2026-01-01T00:10:02Z')) // only 2 seconds
    act(() => focusCallback())

    // give any accidental async call a chance to fire before asserting it didn't
    await act(async () => {
      await vi.advanceTimersByTimeAsync(100)
    })
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('does not log anything if focus fires without a preceding blur', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({}))
    vi.stubGlobal('fetch', fetchMock)
    renderWithStore()

    act(() => focusCallback())
    await act(async () => {
      await vi.advanceTimersByTimeAsync(100)
    })
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('does not log a blur/focus cycle while no session is active', async () => {
    store = createTestStore() // no sessionStarted dispatch this time
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({}))
    vi.stubGlobal('fetch', fetchMock)
    renderWithStore()

    act(() => blurCallback())
    vi.setSystemTime(new Date('2026-01-01T00:10:10Z'))
    act(() => focusCallback())

    await act(async () => {
      await vi.advanceTimersByTimeAsync(100)
    })
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('queues a failed log and only surfaces queueDepth once it crosses the notice threshold (3)', async () => {
    const fetchMock = vi.fn().mockImplementation(() => Promise.resolve(new Response('', { status: 503 })))
    vi.stubGlobal('fetch', fetchMock)
    const { result } = renderWithStore()

    for (let i = 0; i < 3; i++) {
      act(() => blurCallback())
      vi.setSystemTime(new Date(Date.now() + 5000))
      act(() => focusCallback())
      // eslint-disable-next-line no-await-in-loop
      await act(async () => {
        await vi.advanceTimersByTimeAsync(50)
      })
    }

    expect(result.current.queueDepth).toBe(3)
  })

  it('flushes the queue on the periodic retry once the backend recovers, clearing queueDepth back to 0', async () => {
    const fetchMock = vi.fn().mockImplementation(() => Promise.resolve(new Response('', { status: 503 })))
    vi.stubGlobal('fetch', fetchMock)
    const { result } = renderWithStore()

    for (let i = 0; i < 3; i++) {
      act(() => blurCallback())
      vi.setSystemTime(new Date(Date.now() + 5000))
      act(() => focusCallback())
      // eslint-disable-next-line no-await-in-loop
      await act(async () => {
        await vi.advanceTimersByTimeAsync(50)
      })
    }
    expect(result.current.queueDepth).toBe(3)

    fetchMock.mockImplementation(() =>
      Promise.resolve(jsonResponse({ id: 'd', sessionId: session.id, occurredAt: '2026-01-01T00:10:00.000Z', durationSeconds: 5 }))
    )
    await act(async () => {
      await vi.advanceTimersByTimeAsync(15000)
    })

    expect(result.current.queueDepth).toBe(0)
  })
})
