import { NavLink, useNavigate } from 'react-router-dom'
import { BarChart3, Home, ListChecks, LogOut, Settings, Timer, Users } from 'lucide-react'
import { cn } from '@renderer/lib/utils'
import { useLogout } from '@renderer/hooks/useLogout'

const items = [
  { to: '/', label: 'Dashboard', icon: Home },
  { to: '/session/start', label: 'Sessions', icon: Timer },
  { to: '/goals', label: 'Goals', icon: ListChecks },
  { to: '/analytics/best-hours', label: 'Analytics', icon: BarChart3 },
  { to: '/accountability', label: 'Accountability', icon: Users },
  { to: '/settings/notifications', label: 'Settings', icon: Settings }
]

export function Sidebar(): React.JSX.Element {
  const navigate = useNavigate()
  const logout = useLogout()

  const onLogout = async (): Promise<void> => {
    await logout()
    navigate('/login')
  }

  return (
    <nav className="flex w-56 flex-col gap-1 border-r border-border bg-surface p-4">
      <div className="mb-4 px-2 text-lg font-semibold text-text-primary">Locus</div>
      {items.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) =>
            cn(
              'flex items-center gap-3 rounded px-3 py-2 text-sm text-text-secondary transition-colors duration-DEFAULT hover:bg-surface-raised hover:text-text-primary',
              isActive && 'bg-accent-subtle text-accent'
            )
          }
        >
          <Icon size={18} strokeWidth={1.75} />
          {label}
        </NavLink>
      ))}
      <button
        type="button"
        onClick={() => void onLogout()}
        className="mt-auto flex items-center gap-3 rounded px-3 py-2 text-sm text-text-secondary transition-colors duration-DEFAULT hover:bg-surface-raised hover:text-text-primary"
      >
        <LogOut size={18} strokeWidth={1.75} />
        Log out
      </button>
    </nav>
  )
}
