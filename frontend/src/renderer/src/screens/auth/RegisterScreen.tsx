import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { Button } from '@renderer/components/ui/button'
import { Input } from '@renderer/components/ui/input'
import { Label } from '@renderer/components/ui/label'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useRegisterMutation } from '@renderer/store/api/authApi'
import type { ErrorEnvelope } from '@renderer/store/api/baseApi'

export function RegisterScreen(): React.JSX.Element {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [register, { isLoading }] = useRegisterMutation()

  const onSubmit = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault()
    try {
      await register({ email, password }).unwrap()
      navigate('/onboarding/verify-pending', { state: { email } })
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Registration failed')
    }
  }

  return (
    <div className="flex h-screen items-center justify-center bg-background">
      <Card className="w-96">
        <CardHeader>
          <CardTitle>Create your account</CardTitle>
          <CardDescription>Start focusing with Locus.</CardDescription>
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
            Register
          </Button>
          <Button type="button" variant="ghost" onClick={() => navigate('/login')}>
            Already have an account? Log in
          </Button>
        </form>
      </Card>
    </div>
  )
}
