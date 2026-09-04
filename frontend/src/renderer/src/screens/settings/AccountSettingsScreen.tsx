import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { ReauthDialog } from '@renderer/components/ReauthDialog'
import { Button } from '@renderer/components/ui/button'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useDeleteAccountMutation } from '@renderer/store/api/authApi'
import { useLogout } from '@renderer/hooks/useLogout'
import type { ErrorEnvelope } from '@renderer/store/api/baseApi'

// technical-spec.md §9: deleting the account cascades across every consuming service. Re-auth is
// required (ReauthDialog), and the consequence is stated explicitly rather than a bare confirm.
export function AccountSettingsScreen(): React.JSX.Element {
  const navigate = useNavigate()
  const [deleteAccount] = useDeleteAccountMutation()
  const logout = useLogout()
  const [dialogOpen, setDialogOpen] = useState(false)

  const onConfirmDelete = async (password: string): Promise<void> => {
    try {
      await deleteAccount({ password }).unwrap()
      setDialogOpen(false)
      await logout()
      navigate('/login')
      toast.success('Your account has been deleted')
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Could not delete account — check your password')
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <Card className="max-w-md border-danger/40">
        <CardHeader>
          <CardTitle>Delete account</CardTitle>
          <CardDescription>
            Permanently deletes your account and all associated data — goals, sessions, distraction history, analytics,
            and accountability memberships. This cannot be undone.
          </CardDescription>
        </CardHeader>
        <Button variant="destructive" onClick={() => setDialogOpen(true)}>
          Delete my account
        </Button>
        <ReauthDialog
          open={dialogOpen}
          onOpenChange={setDialogOpen}
          title="Delete your account"
          description="This permanently deletes your account and all associated data. Confirm your password to continue."
          confirmLabel="Permanently delete"
          destructive
          onConfirm={onConfirmDelete}
        />
      </Card>
    </div>
  )
}
