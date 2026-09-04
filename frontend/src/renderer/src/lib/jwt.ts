// Client-side JWT payload decode for UI purposes only (expiry display, subject) — never treated
// as a trust boundary; every backend service independently re-validates the signature itself
// (technical-spec.md §1's zero-trust model).
export interface JwtPayload {
  sub: string
  mfa: boolean
  exp: number
  iat: number
  jti: string
  roles: string[]
}

export function decodeJwt(token: string): JwtPayload {
  const payload = token.split('.')[1]
  const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
  return JSON.parse(json) as JwtPayload
}
