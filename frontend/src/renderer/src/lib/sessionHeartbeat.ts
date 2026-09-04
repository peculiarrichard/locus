// frd.md's orphaned-session reconciliation needs a last-known local timestamp to survive a
// crash/force-quit, which in-memory Redux state can't do — a tiny localStorage heartbeat
// specifically for this, updated periodically while a session is active/paused and cleared the
// moment it ends normally. Not the session's real state (that's the `session` Redux slice, never
// persisted) — just enough to detect and reconcile an orphan on next launch.
const KEY = 'locus.session-heartbeat'

interface Heartbeat {
  sessionId: string
  lastKnownAt: string
}

export function writeHeartbeat(sessionId: string): void {
  try {
    localStorage.setItem(KEY, JSON.stringify({ sessionId, lastKnownAt: new Date().toISOString() } satisfies Heartbeat))
  } catch {
    // best-effort only
  }
}

export function clearHeartbeat(): void {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // best-effort only
  }
}

export function readHeartbeat(): Heartbeat | null {
  try {
    const raw = localStorage.getItem(KEY)
    return raw ? (JSON.parse(raw) as Heartbeat) : null
  } catch {
    return null
  }
}
