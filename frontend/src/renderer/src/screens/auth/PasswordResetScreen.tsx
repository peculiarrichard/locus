import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@renderer/components/ui/button'
import { Input } from '@renderer/components/ui/input'
import { Label } from '@renderer/components/ui/label'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useRequestPasswordResetMutation } from '@renderer/store/api/authApi'

export function PasswordResetScreen(): React.JSX.Element {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [requestReset, { isLoading }] = useRequestPasswordResetMutation()

  const onSubmit = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault()
    try {
      await requestReset({ email }).unwrap()
      toast.success('If that account exists, a reset email is on its way')
    } catch {
      toast.error('Something went wrong')
    }
  }

  return (
    <div className="flex h-screen items-center justify-center bg-background">
      <Card className="w-96">
        <CardHeader>
          <CardTitle>Reset your password</CardTitle>
          <CardDescription>We'll send a reset link to your email.</CardDescription>
        </CardHeader>
        <form onSubmit={onSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <Label htmlFor="email">Email</Label>
            <Input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <Button type="submit" disabled={isLoading}>
            Send reset link
          </Button>
          <Button type="button" variant="ghost" onClick={() => navigate('/login')}>
            Back to login
          </Button>
        </form>
      </Card>
    </div>
  )
}
