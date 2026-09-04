import { beforeEach, describe, expect, it } from 'vitest'
import { getAccessToken, setAccessToken } from './tokenStore'

describe('tokenStore', () => {
  beforeEach(() => {
    setAccessToken(null)
  })

  it('starts with no token', () => {
    expect(getAccessToken()).toBeNull()
  })

  it('stores and returns the token set', () => {
    setAccessToken('abc.def.ghi')
    expect(getAccessToken()).toBe('abc.def.ghi')
  })

  it('clears back to null', () => {
    setAccessToken('abc.def.ghi')
    setAccessToken(null)
    expect(getAccessToken()).toBeNull()
  })
})
