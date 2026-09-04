import { getLatestVerificationToken } from './mailpit'

const GATEWAY_ORIGIN = process.env.LOCUS_GATEWAY_ORIGIN ?? 'http://localhost:8080'
const API = `${GATEWAY_ORIGIN}/api/v1`

export const TEST_PASSWORD = 'CorrectHorse123!'

export interface TestUser {
  email: string
  password: string
}

// Registers and verifies a fresh account via the real Gateway + Mailpit — the same path every
// human user goes through, used here as setup for e2e specs that then drive the UI as that user.
export async function createVerifiedUser(label: string): Promise<TestUser> {
  const email = `e2e-${label}-${Date.now()}-${Math.floor(Math.random() * 1000)}@example.com`
  const registerRes = await fetch(`${API}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password: TEST_PASSWORD })
  })
  if (registerRes.status !== 202) {
    throw new Error(`register failed: ${registerRes.status} ${await registerRes.text()}`)
  }

  const token = await getLatestVerificationToken(email)
  const verifyRes = await fetch(`${API}/auth/verify-email`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token })
  })
  if (verifyRes.status !== 200) {
    throw new Error(`verify failed: ${verifyRes.status} ${await verifyRes.text()}`)
  }

  return { email, password: TEST_PASSWORD }
}

// Logs a user in via the real API (not the UI) — used when a test needs a second actor's access
// token for setup/assertions without driving a second Electron window for them.
export async function loginViaApi(user: TestUser): Promise<{ accessToken: string; refreshToken: string }> {
  const res = await fetch(`${API}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(user)
  })
  if (res.status !== 200) {
    throw new Error(`login failed: ${res.status} ${await res.text()}`)
  }
  return res.json() as Promise<{ accessToken: string; refreshToken: string }>
}
