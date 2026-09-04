import { MemoryRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthBootstrap } from '@renderer/components/AuthBootstrap'
import { ProtectedRoute } from '@renderer/components/ProtectedRoute'
import { AppShell } from '@renderer/components/layout/AppShell'
import { useApplyTheme } from '@renderer/hooks/useApplyTheme'

import { WelcomeScreen } from '@renderer/screens/onboarding/WelcomeScreen'
import { RegisterScreen } from '@renderer/screens/auth/RegisterScreen'
import { VerifyPendingScreen } from '@renderer/screens/auth/VerifyPendingScreen'
import { LoginScreen } from '@renderer/screens/auth/LoginScreen'
import { MfaChallengeScreen } from '@renderer/screens/auth/MfaChallengeScreen'
import { PasswordResetScreen } from '@renderer/screens/auth/PasswordResetScreen'
import { DashboardScreen } from '@renderer/screens/DashboardScreen'
import { StartSessionScreen } from '@renderer/screens/session/StartSessionScreen'
import { ActiveSessionScreen } from '@renderer/screens/session/ActiveSessionScreen'
import { SessionSummaryScreen } from '@renderer/screens/session/SessionSummaryScreen'
import { GoalsListScreen } from '@renderer/screens/goals/GoalsListScreen'
import { CreateGoalScreen } from '@renderer/screens/goals/CreateGoalScreen'
import { GoalDetailScreen } from '@renderer/screens/goals/GoalDetailScreen'
import { AnalyticsLayout } from '@renderer/screens/analytics/AnalyticsLayout'
import { BestHoursScreen } from '@renderer/screens/analytics/BestHoursScreen'
import { DistractionFrequencyScreen } from '@renderer/screens/analytics/DistractionFrequencyScreen'
import { HistoryScreen } from '@renderer/screens/analytics/HistoryScreen'
import { AccountabilityScreen } from '@renderer/screens/accountability/AccountabilityScreen'
import { SettingsLayout } from '@renderer/screens/settings/SettingsLayout'
import { NotificationsSettingsScreen } from '@renderer/screens/settings/NotificationsSettingsScreen'
import { SecuritySettingsScreen } from '@renderer/screens/settings/SecuritySettingsScreen'
import { AccountSettingsScreen } from '@renderer/screens/settings/AccountSettingsScreen'

// design-spec.md §1: MemoryRouter, not Hash/BrowserRouter — the renderer loads from a bundled
// origin, not a real HTTP one.
export function App(): React.JSX.Element {
  useApplyTheme()

  return (
    <MemoryRouter>
      <AuthBootstrap>
        <Routes>
          <Route path="/onboarding/welcome" element={<WelcomeScreen />} />
          <Route path="/onboarding/verify-pending" element={<VerifyPendingScreen />} />
          <Route path="/register" element={<RegisterScreen />} />
          <Route path="/login" element={<LoginScreen />} />
          <Route path="/mfa-challenge" element={<MfaChallengeScreen />} />
          <Route path="/password-reset" element={<PasswordResetScreen />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<AppShell />}>
              <Route path="/" element={<DashboardScreen />} />
              <Route path="/session/start" element={<StartSessionScreen />} />
              <Route path="/session/active" element={<ActiveSessionScreen />} />
              <Route path="/session/summary/:sessionId" element={<SessionSummaryScreen />} />
              <Route path="/goals" element={<GoalsListScreen />} />
              <Route path="/goals/new" element={<CreateGoalScreen />} />
              <Route path="/goals/:goalId" element={<GoalDetailScreen />} />
              <Route path="/analytics" element={<AnalyticsLayout />}>
                <Route index element={<Navigate to="/analytics/best-hours" replace />} />
                <Route path="best-hours" element={<BestHoursScreen />} />
                <Route path="distractions" element={<DistractionFrequencyScreen />} />
                <Route path="history" element={<HistoryScreen />} />
              </Route>
              <Route path="/accountability" element={<AccountabilityScreen />} />
              <Route path="/settings" element={<SettingsLayout />}>
                <Route index element={<Navigate to="/settings/notifications" replace />} />
                <Route path="notifications" element={<NotificationsSettingsScreen />} />
                <Route path="security" element={<SecuritySettingsScreen />} />
                <Route path="account" element={<AccountSettingsScreen />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthBootstrap>
    </MemoryRouter>
  )
}
