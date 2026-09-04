import { NavLink, Outlet } from 'react-router-dom'
import { cn } from '@renderer/lib/utils'

const tabs = [
  { to: '/settings/notifications', label: 'Notifications' },
  { to: '/settings/security', label: 'Security' },
  { to: '/settings/account', label: 'Account' }
]

// design-spec.md §3: /settings/notifications, /settings/security, /settings/account are distinct
// routes, not just client-side tab state — a shared layout with a tab nav + <Outlet/> gets both.
export function SettingsLayout(): React.JSX.Element {
  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-semibold text-text-primary">Settings</h1>
      <div className="flex gap-1 border-b border-border">
        {tabs.map((tab) => (
          <NavLink
            key={tab.to}
            to={tab.to}
            className={({ isActive }) =>
              cn(
                'border-b-2 border-transparent px-4 py-2 text-sm text-text-secondary transition-colors duration-DEFAULT hover:text-text-primary',
                isActive && 'border-accent text-text-primary'
              )
            }
          >
            {tab.label}
          </NavLink>
        ))}
      </div>
      <Outlet />
    </div>
  )
}
