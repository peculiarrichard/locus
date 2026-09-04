import { describe, expect, it } from 'vitest'
import reducer, { setAuthenticated, setUnauthenticated } from './authSlice'

describe('authSlice', () => {
  const initialState = { userId: null, tokenExpiresAt: null, status: 'idle' as const }

  it('starts idle', () => {
    expect(reducer(undefined, { type: '@@INIT' })).toEqual(initialState)
  })

  it('setAuthenticated stores userId/tokenExpiresAt and flips to authenticated', () => {
    const state = reducer(initialState, setAuthenticated({ userId: 'u1', tokenExpiresAt: 1000 }))
    expect(state).toEqual({ userId: 'u1', tokenExpiresAt: 1000, status: 'authenticated' })
  })

  it('setUnauthenticated clears everything back out, even from an authenticated state', () => {
    const authenticated = reducer(initialState, setAuthenticated({ userId: 'u1', tokenExpiresAt: 1000 }))
    const state = reducer(authenticated, setUnauthenticated())
    expect(state).toEqual({ userId: null, tokenExpiresAt: null, status: 'unauthenticated' })
  })
})
