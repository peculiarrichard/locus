import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@renderer/components/ui/button'
import { Input } from '@renderer/components/ui/input'
import { Label } from '@renderer/components/ui/label'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useLoginMutation } from '@renderer/store/api/authApi'
import { useCompleteLogin } from '@renderer/hooks/useCompleteLogin'
import type { ErrorEnvelope } from '@renderer/store/api/baseApi'

export function LoginScreen(): React.JSX.Element {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [login, { isLoading }] = useLoginMutation()
  const completeLogin = useCompleteLogin()

  const onSubmit = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault()
    try {
      const result = await login({ email, password }).unwrap()
      if (result.mfaChallengeToken) {
        navigate('/mfa-challenge', { state: { mfaChallengeToken: result.mfaChallengeToken } })
        return
      }
      await completeLogin(result)
      navigate('/')
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      if (envelope?.errorCode === 'EMAIL_NOT_VERIFIED') {
        navigate('/onboarding/verify-pending', { state: { email } })
        return
      }
      toast.error(envelope?.message ?? 'Login failed')
    }
  }

  return (
    <div className="flex h-screen items-center justify-center bg-background">
      <Card className="w-96">
        <CardHeader>
          <CardTitle>Welcome back</CardTitle>
          <CardDescription>Log in to continue.</CardDescription>
        </CardHeader>
        <form onSubmit={onSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <Label htmlFor="email">Email</Label>
            <Input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className="flex flex-col gap-2">
            <Label htmlFor="password">Password</Label>
            <Input id="password" type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
          </div>
          <Button type="submit" disabled={isLoading}>
            Log in
          </Button>
          <div className="flex justify-between text-sm">
            <Button type="button" variant="ghost" size="sm" onClick={() => navigate('/register')}>
              Create an account
            </Button>
            <Button type="button" variant="ghost" size="sm" onClick={() => navigate('/password-reset')}>
              Forgot password?
            </Button>
          </div>
        </form>
      </Card>
    </div>
  )
}
