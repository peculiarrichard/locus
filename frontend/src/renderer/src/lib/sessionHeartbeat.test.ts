import { beforeEach, describe, expect, it, vi } from 'vitest'
import { clearHeartbeat, readHeartbeat, writeHeartbeat } from './sessionHeartbeat'

describe('sessionHeartbeat', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('returns null when nothing has been written', () => {
    expect(readHeartbeat()).toBeNull()
  })

  it('round-trips a written heartbeat', () => {
    writeHeartbeat('session-1')
    const heartbeat = readHeartbeat()
    expect(heartbeat?.sessionId).toBe('session-1')
    expect(new Date(heartbeat!.lastKnownAt).toString()).not.toBe('Invalid Date')
  })

  it('clears a written heartbeat', () => {
    writeHeartbeat('session-1')
    clearHeartbeat()
    expect(readHeartbeat()).toBeNull()
  })

  it('fails safe (returns null) instead of throwing on corrupt stored JSON', () => {
    localStorage.setItem('locus.session-heartbeat', '{not valid json')
    expect(readHeartbeat()).toBeNull()
  })

  it('fails safe (does not throw) if localStorage.setItem throws', () => {
    const spy = vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('quota exceeded')
    })
    expect(() => writeHeartbeat('session-1')).not.toThrow()
    spy.mockRestore()
  })
})
