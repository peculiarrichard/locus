import { useNavigate } from 'react-router-dom'
import { Button } from '@renderer/components/ui/button'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'

// design-spec.md §4: a thin guided layer over the real screens (register, goal creation, session
// start), not a separate standalone tutorial that could drift out of sync with them.
export function WelcomeScreen(): React.JSX.Element {
  const navigate = useNavigate()
  return (
    <div className="flex h-screen items-center justify-center bg-background">
      <Card className="w-96 text-center">
        <CardHeader>
          <CardTitle>Welcome to Locus</CardTitle>
          <CardDescription>A calm place to focus, track your progress, and stay accountable.</CardDescription>
        </CardHeader>
        <Button onClick={() => navigate('/register')}>Get started</Button>
      </Card>
    </div>
  )
}
