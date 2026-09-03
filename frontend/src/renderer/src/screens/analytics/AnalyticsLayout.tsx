import { NavLink, Outlet } from 'react-router-dom'
import { cn } from '@renderer/lib/utils'

const tabs = [
  { to: '/analytics/best-hours', label: 'Best hours' },
  { to: '/analytics/distractions', label: 'Distractions' },
  { to: '/analytics/history', label: 'History' }
]

// design-spec.md §4: best-hours, distraction-frequency, and history each get their own route
// rather than being crammed onto one dense page — this tab nav switches between those routes,
// it doesn't merge the charts together.
export function AnalyticsLayout(): React.JSX.Element {
  return (
    <div className="flex flex-col gap-6">
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
