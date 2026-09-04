import { Navigate, Outlet } from 'react-router-dom'
import { useAppSelector } from '@renderer/store/hooks'

export function ProtectedRoute(): React.JSX.Element {
  const status = useAppSelector((state) => state.auth.status)
  if (status === 'unauthenticated' || status === 'idle') {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}
