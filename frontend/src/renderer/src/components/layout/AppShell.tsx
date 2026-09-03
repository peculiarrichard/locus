import { useEffect } from 'react'
import { Outlet } from 'react-router-dom'
import { Toaster, toast } from 'sonner'
import { Sidebar } from './Sidebar'
import { SessionStatusBar } from './SessionStatusBar'
import { useSessionTraySync } from '@renderer/hooks/useSessionTraySync'
import { useDistractionLogging } from '@renderer/hooks/useDistractionLogging'
import { useAppDispatch, useAppSelector } from '@renderer/store/hooks'
import { reconciliationNoticeDismissed } from '@renderer/store/slices/sessionSlice'

export function AppShell(): React.JSX.Element {
  useSessionTraySync()
  const { queueDepth } = useDistractionLogging()
  const dispatch = useAppDispatch()
  const reconciliationNotice = useAppSelector((state) => state.session.reconciliationNotice)

  useEffect(() => {
    if (reconciliationNotice) {
      toast.info(reconciliationNotice)
      dispatch(reconciliationNoticeDismissed())
    }
  }, [reconciliationNotice, dispatch])

  return (
    <div className="flex h-screen">
      <Sidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        <SessionStatusBar />
        {queueDepth > 0 && (
          <div className="border-b border-border bg-surface px-6 py-1 text-xs text-text-muted">
            Some activity data is queued to sync
          </div>
        )}
        <main className="flex-1 overflow-y-auto p-8">
          <Outlet />
        </main>
      </div>
      <Toaster theme="dark" position="bottom-right" />
    </div>
  )
}
