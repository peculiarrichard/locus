import { useLocation, useNavigate } from 'react-router-dom'
import { MailCheck } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@renderer/components/ui/button'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useResendVerificationMutation } from '@renderer/store/api/authApi'

// design-spec.md §4: verification happens on a plain external browser page (technical-spec.md
// §1), not an in-app deep link — the app just waits for the user to come back and log in.
export function VerifyPendingScreen(): React.JSX.Element {
  const navigate = useNavigate()
  const location = useLocation() as { state?: { email?: string } }
  const email = location.state?.email
  const [resend, { isLoading }] = useResendVerificationMutation()

  return (
    <div className="flex h-screen items-center justify-center bg-background">
      <Card className="w-96 text-center">
        <CardHeader>
          <MailCheck className="mx-auto mb-2 text-accent" size={32} strokeWidth={1.5} />
          <CardTitle>Check your inbox</CardTitle>
          <CardDescription>
            We sent a verification link{email ? ` to ${email}` : ''}. Follow it, then come back and log in.
          </CardDescription>
        </CardHeader>
        <div className="flex flex-col gap-2">
          <Button
            variant="outline"
            disabled={isLoading || !email}
            onClick={() =>
              email &&
              resend({ email })
                .unwrap()
                .then(() => toast.success('Verification email resent'))
                .catch(() => toast.error('Could not resend — try again shortly'))
            }
          >
            Resend email
          </Button>
          <Button onClick={() => navigate('/login')}>I've verified — log in</Button>
        </div>
      </Card>
    </div>
  )
}
