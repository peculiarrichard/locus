import { useEffect, useRef, useState } from 'react'
import { useAppSelector } from '@renderer/store/hooks'
import { useLogDistractionMutation } from '@renderer/store/api/distractionsApi'

const MIN_DURATION_SECONDS = 3
const QUEUE_NOTICE_THRESHOLD = 3

interface QueuedDistraction {
  id: string
  sessionId: string
  occurredAt: string
  durationSeconds: number
}

// technical-spec.md §8 + frd.md Distraction Logging Service: native blur/focus relayed from the
// main process, paired client-side (blur timestamp + focus duration), 3-second minimum threshold,
// client-generated UUID doubling as the idempotency key, with a small offline retry queue —
// deliberately near-invisible per design-spec.md §5, only surfacing once queue depth crosses a
// threshold rather than showing a sync indicator for something that normally resolves in seconds.
export function useDistractionLogging(): { queueDepth: number } {
  const active = useAppSelector((state) => state.session.active)
  const activeRef = useRef(active)
  activeRef.current = active
  const blurredAtRef = useRef<number | null>(null)
  const queueRef = useRef<QueuedDistraction[]>([])
  const [queueDepth, setQueueDepth] = useState(0)
  const [logDistraction] = useLogDistractionMutation()

  useEffect(() => {
    const flush = async (): Promise<void> => {
      const remaining: QueuedDistraction[] = []
      for (const item of queueRef.current) {
        try {
          await logDistraction(item).unwrap()
        } catch {
          remaining.push(item)
        }
      }
      queueRef.current = remaining
      setQueueDepth(remaining.length)
    }

    const unsubscribeBlur = window.locus.distraction.onBlur(() => {
      blurredAtRef.current = Date.now()
    })
    const unsubscribeFocus = window.locus.distraction.onFocus(() => {
      const blurredAt = blurredAtRef.current
      blurredAtRef.current = null
      const session = activeRef.current
      if (blurredAt === null || !session || session.status !== 'ACTIVE') {
        return
      }
      const durationSeconds = Math.floor((Date.now() - blurredAt) / 1000)
      if (durationSeconds < MIN_DURATION_SECONDS) {
        return
      }
      const event: QueuedDistraction = {
        id: crypto.randomUUID(),
        sessionId: session.id,
        occurredAt: new Date(blurredAt).toISOString(),
        durationSeconds
      }
      logDistraction(event)
        .unwrap()
        .catch(() => {
          queueRef.current.push(event)
          setQueueDepth(queueRef.current.length)
        })
    })

    const retryInterval = setInterval(() => void flush(), 15000)
    return () => {
      unsubscribeBlur()
      unsubscribeFocus()
      clearInterval(retryInterval)
    }
  }, [logDistraction])

  return { queueDepth: queueDepth >= QUEUE_NOTICE_THRESHOLD ? queueDepth : 0 }
}
