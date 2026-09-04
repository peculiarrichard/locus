import { useEffect, useState, type ReactNode } from 'react'
import { setAccessToken } from '@renderer/lib/tokenStore'
import { decodeJwt } from '@renderer/lib/jwt'
import { clearHeartbeat, readHeartbeat } from '@renderer/lib/sessionHeartbeat'
import { useAppDispatch } from '@renderer/store/hooks'
import { setAuthenticated, setUnauthenticated } from '@renderer/store/slices/authSlice'
import { reconciliationNoticeShown } from '@renderer/store/slices/sessionSlice'
import { useRefreshMutation } from '@renderer/store/api/authApi'
import { useAbandonSessionMutation } from '@renderer/store/api/sessionApi'

// Runs once before the router renders: attempts a silent session restore from the stored refresh
// token (design-spec.md §5), then — only once authenticated — reconciles any orphaned
// active/paused session left behind by a crash or force-quit (frd.md), using the last local
// heartbeat timestamp rather than a server-side guess.
export function AuthBootstrap({ children }: { children: ReactNode }): React.JSX.Element | null {
  const dispatch = useAppDispatch()
  const [ready, setReady] = useState(false)
  const [refresh] = useRefreshMutation()
  const [abandonSession] = useAbandonSessionMutation()

  useEffect(() => {
    void (async () => {
      const storedRefreshToken = await window.locus.auth.getRefreshToken()
      if (!storedRefreshToken) {
        dispatch(setUnauthenticated())
        setReady(true)
        return
      }
      try {
        const result = await refresh({ refreshToken: storedRefreshToken }).unwrap()
        setAccessToken(result.accessToken)
        await window.locus.auth.setRefreshToken(result.refreshToken)
        const claims = decodeJwt(result.accessToken)
        dispatch(setAuthenticated({ userId: claims.sub, tokenExpiresAt: claims.exp * 1000 }))

        const heartbeat = readHeartbeat()
        if (heartbeat) {
          await abandonSession({ id: heartbeat.sessionId, abandonedAt: heartbeat.lastKnownAt }).unwrap().catch(() => undefined)
          clearHeartbeat()
          dispatch(reconciliationNoticeShown('Previous session ended unexpectedly'))
        }
      } catch {
        await window.locus.auth.clearRefreshToken()
        dispatch(setUnauthenticated())
      }
      setReady(true)
    })()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  if (!ready) {
    return null
  }
  return <>{children}</>
}
