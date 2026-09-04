import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import { QueryState } from '@renderer/components/QueryState'
import { Button } from '@renderer/components/ui/button'
import { Input } from '@renderer/components/ui/input'
import { Label } from '@renderer/components/ui/label'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useGetPreferencesQuery, useUpdatePreferencesMutation } from '@renderer/store/api/notificationsApi'
import { useAppDispatch, useAppSelector } from '@renderer/store/hooks'
import { themeChanged } from '@renderer/store/slices/uiSlice'
import type { ErrorEnvelope } from '@renderer/store/api/baseApi'

export function NotificationsSettingsScreen(): React.JSX.Element {
  const dispatch = useAppDispatch()
  const theme = useAppSelector((state) => state.ui.theme)
  const query = useGetPreferencesQuery()
  const [updatePreferences, { isLoading }] = useUpdatePreferencesMutation()
  const [reminderTime, setReminderTime] = useState('')

  useEffect(() => {
    if (query.data) {
      setReminderTime(query.data.reminderTime ?? '')
    }
  }, [query.data])

  const onSave = async (): Promise<void> => {
    try {
      await updatePreferences({ reminderTime: reminderTime || null }).unwrap()
      toast.success('Preferences saved')
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Could not save preferences')
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <Card className="max-w-md">
        <CardHeader>
          <CardTitle>Appearance</CardTitle>
        </CardHeader>
        <div className="flex gap-2">
          {(['dark', 'light', 'system'] as const).map((option) => (
            <Button key={option} size="sm" variant={theme === option ? 'default' : 'outline'} onClick={() => dispatch(themeChanged(option))}>
              {option}
            </Button>
          ))}
        </div>
      </Card>

      <Card className="max-w-md">
        <CardHeader>
          <CardTitle>Study reminder</CardTitle>
          <CardDescription>A gentle nudge if you haven't studied by this time each day. Leave blank to opt out.</CardDescription>
        </CardHeader>
        <QueryState {...query} emptyState={<p className="text-sm text-text-secondary">Loading…</p>}>
          {() => (
            <div className="flex flex-col gap-4">
              <div className="flex flex-col gap-2">
                <Label htmlFor="reminderTime">Reminder time</Label>
                <Input id="reminderTime" type="time" value={reminderTime} onChange={(e) => setReminderTime(e.target.value)} />
              </div>
              <Button onClick={() => void onSave()} disabled={isLoading}>
                Save
              </Button>
            </div>
          )}
        </QueryState>
      </Card>
    </div>
  )
}
