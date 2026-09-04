// design-spec.md §1/§5: the raw access token is deliberately kept OUT of Redux state (and so out
// of Redux DevTools too) — a plain module-level variable instead. The `auth` slice holds
// everything else derived from it (profile, roles, mfa flag, expiry).
let accessToken: string | null = null

export function getAccessToken(): string | null {
  return accessToken
}

export function setAccessToken(token: string | null): void {
  accessToken = token
}
