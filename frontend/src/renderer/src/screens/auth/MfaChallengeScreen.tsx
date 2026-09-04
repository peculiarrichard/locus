import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@renderer/components/ui/button'
import { Input } from '@renderer/components/ui/input'
import { Label } from '@renderer/components/ui/label'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useVerifyMfaChallengeMutation } from '@renderer/store/api/authApi'
import { useCompleteLogin } from '@renderer/hooks/useCompleteLogin'
import type { ErrorEnvelope } from '@renderer/store/api/baseApi'

export function MfaChallengeScreen(): React.JSX.Element {
  const navigate = useNavigate()
  const location = useLocation() as { state?: { mfaChallengeToken?: string } }
  const mfaChallengeToken = location.state?.mfaChallengeToken
  const [code, setCode] = useState('')
  const [verify, { isLoading }] = useVerifyMfaChallengeMutation()
  const completeLogin = useCompleteLogin()

  const onSubmit = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault()
    if (!mfaChallengeToken) {
      navigate('/login')
      return
    }
    try {
      const result = await verify({ mfaChallengeToken, code }).unwrap()
      await completeLogin(result)
      navigate('/')
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Invalid code')
    }
  }

  return (
    <div className="flex h-screen items-center justify-center bg-background">
      <Card className="w-96">
        <CardHeader>
          <CardTitle>Enter your code</CardTitle>
          <CardDescription>Open your authenticator app and enter the current code.</CardDescription>
        </CardHeader>
        <form onSubmit={onSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-2">
            <Label htmlFor="code">Code</Label>
            <Input id="code" inputMode="numeric" autoFocus required value={code} onChange={(e) => setCode(e.target.value)} />
          </div>
          <Button type="submit" disabled={isLoading}>
            Verify
          </Button>
        </form>
      </Card>
    </div>
  )
}
