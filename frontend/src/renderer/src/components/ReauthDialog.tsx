import { useState } from 'react'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@renderer/components/ui/dialog'
import { Button } from '@renderer/components/ui/button'
import { Input } from '@renderer/components/ui/input'
import { Label } from '@renderer/components/ui/label'

interface ReauthDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  description: string
  confirmLabel: string
  destructive?: boolean
  onConfirm: (password: string) => Promise<void>
}

// Shared by MFA-disable and account-deletion — any action design-spec.md §4 calls out as requiring
// re-authentication rather than just an active session, per frd.md's ReauthRequest{password}.
export function ReauthDialog({
  open,
  onOpenChange,
  title,
  description,
  confirmLabel,
  destructive,
  onConfirm
}: ReauthDialogProps): React.JSX.Element {
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const onSubmit = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault()
    setIsSubmitting(true)
    try {
      await onConfirm(password)
      setPassword('')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        if (!isSubmitting) {
          setPassword('')
          onOpenChange(next)
        }
      }}
    >
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <Label htmlFor="reauth-password">Confirm your password</Label>
            <Input
              id="reauth-password"
              type="password"
              autoFocus
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <Button type="submit" variant={destructive ? 'destructive' : 'default'} disabled={isSubmitting}>
            {confirmLabel}
          </Button>
        </form>
      </DialogContent>
    </Dialog>
  )
}
