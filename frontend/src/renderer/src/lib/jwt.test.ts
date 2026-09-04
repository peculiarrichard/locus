import { describe, expect, it } from 'vitest'
import { decodeJwt } from './jwt'

function fakeJwt(payload: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'RS256' }))
  const body = btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_')
  return `${header}.${body}.signature`
}

describe('decodeJwt', () => {
  it('decodes the payload segment without verifying the signature', () => {
    const token = fakeJwt({ sub: 'user-1', mfa: false, exp: 123, iat: 100, jti: 'abc', roles: ['user'] })
    expect(decodeJwt(token)).toEqual({ sub: 'user-1', mfa: false, exp: 123, iat: 100, jti: 'abc', roles: ['user'] })
  })

  it('handles base64url-encoded payloads with - and _ characters', () => {
    // a payload whose base64 encoding naturally contains + and / gets converted to - and _
    const token = fakeJwt({ sub: '???>>>', exp: 1, iat: 1, jti: 'x', roles: [], mfa: true })
    expect(decodeJwt(token).sub).toBe('???>>>')
  })
})
