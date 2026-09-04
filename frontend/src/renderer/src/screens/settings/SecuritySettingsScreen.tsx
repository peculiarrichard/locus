import { useEffect, useState } from 'react'
import QRCode from 'qrcode'
import { toast } from 'sonner'
import { QueryState } from '@renderer/components/QueryState'
import { ReauthDialog } from '@renderer/components/ReauthDialog'
import { Button } from '@renderer/components/ui/button'
import { Input } from '@renderer/components/ui/input'
import { Label } from '@renderer/components/ui/label'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import {
  useConfirmMfaMutation,
  useDisableMfaMutation,
  useEnrollMfaMutation,
  useGetProfileQuery,
  useListDevicesQuery,
  useRevokeDeviceMutation
} from '@renderer/store/api/authApi'
import type { ErrorEnvelope } from '@renderer/store/api/baseApi'

type MfaStep = 'idle' | 'scan' | 'recovery-codes'

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

function MfaSection(): React.JSX.Element {
  const query = useGetProfileQuery()
  const [enroll, { isLoading: isEnrolling }] = useEnrollMfaMutation()
  const [confirm, { isLoading: isConfirming }] = useConfirmMfaMutation()
  const [disable] = useDisableMfaMutation()
  const [step, setStep] = useState<MfaStep>('idle')
  const [otpAuthUri, setOtpAuthUri] = useState('')
  const [qrDataUrl, setQrDataUrl] = useState('')
  const [code, setCode] = useState('')
  const [recoveryCodes, setRecoveryCodes] = useState<string[]>([])
  const [disableDialogOpen, setDisableDialogOpen] = useState(false)

  useEffect(() => {
    if (!otpAuthUri) {
      setQrDataUrl('')
      return
    }
    let cancelled = false
    void QRCode.toDataURL(otpAuthUri).then((url) => {
      if (!cancelled) {
        setQrDataUrl(url)
      }
    })
    return () => {
      cancelled = true
    }
  }, [otpAuthUri])

  const onStartEnroll = async (): Promise<void> => {
    try {
      const result = await enroll().unwrap()
      setOtpAuthUri(result.otpAuthUri)
      setStep('scan')
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Could not start enrollment')
    }
  }

  const onConfirmCode = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault()
    try {
      const result = await confirm({ code }).unwrap()
      setRecoveryCodes(result.recoveryCodes)
      setStep('recovery-codes')
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Invalid code')
    }
  }

  const onFinishEnrollment = (): void => {
    setStep('idle')
    setOtpAuthUri('')
    setCode('')
    setRecoveryCodes([])
    toast.success('Two-factor authentication enabled')
  }

  const onConfirmDisable = async (password: string): Promise<void> => {
    try {
      await disable({ password }).unwrap()
      setDisableDialogOpen(false)
      toast.success('Two-factor authentication disabled')
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Could not disable — check your password')
    }
  }

  return (
    <Card className="max-w-md">
      <CardHeader>
        <CardTitle>Two-factor authentication</CardTitle>
        <CardDescription>Require an authenticator app code in addition to your password when logging in.</CardDescription>
      </CardHeader>
      <QueryState {...query} emptyState={<p className="text-sm text-text-secondary">Loading…</p>}>
        {(profile) => {
          if (step === 'scan') {
            return (
              <div className="flex flex-col gap-4">
                {qrDataUrl && <img src={qrDataUrl} alt="Scan with your authenticator app" className="h-48 w-48 self-center rounded bg-white p-2" />}
                <p className="text-xs text-text-muted">
                  Scan this with your authenticator app, then enter the 6-digit code it shows.
                </p>
                <form onSubmit={(e) => void onConfirmCode(e)} className="flex flex-col gap-4">
                  <div className="flex flex-col gap-2">
                    <Label htmlFor="mfa-code">Code</Label>
                    <Input id="mfa-code" inputMode="numeric" autoFocus required value={code} onChange={(e) => setCode(e.target.value)} />
                  </div>
                  <Button type="submit" disabled={isConfirming}>
                    Confirm
                  </Button>
                  <Button type="button" variant="ghost" size="sm" onClick={() => setStep('idle')}>
                    Cancel
                  </Button>
                </form>
              </div>
            )
          }

          if (step === 'recovery-codes') {
            return (
              <div className="flex flex-col gap-4">
                <p className="text-sm text-warning">
                  Save these recovery codes now — each can be used once if you lose access to your authenticator app. They
                  won't be shown again.
                </p>
                <div className="grid grid-cols-2 gap-2 rounded border border-border bg-surface-raised p-4 font-mono text-sm text-text-primary">
                  {recoveryCodes.map((rc) => (
                    <span key={rc}>{rc}</span>
                  ))}
                </div>
                <Button onClick={onFinishEnrollment}>I've saved these codes</Button>
              </div>
            )
          }

          return profile.mfaEnabled ? (
            <div className="flex items-center justify-between">
              <span className="text-sm text-success">Enabled</span>
              <Button variant="destructive" size="sm" onClick={() => setDisableDialogOpen(true)}>
                Disable
              </Button>
              <ReauthDialog
                open={disableDialogOpen}
                onOpenChange={setDisableDialogOpen}
                title="Disable two-factor authentication"
                description="Confirm your password to disable two-factor authentication for your account."
                confirmLabel="Disable"
                destructive
                onConfirm={onConfirmDisable}
              />
            </div>
          ) : (
            <div className="flex items-center justify-between">
              <span className="text-sm text-text-secondary">Not enabled</span>
              <Button size="sm" onClick={() => void onStartEnroll()} disabled={isEnrolling}>
                Enable
              </Button>
            </div>
          )
        }}
      </QueryState>
    </Card>
  )
}

function DevicesSection(): React.JSX.Element {
  const query = useListDevicesQuery()
  const [revokeDevice] = useRevokeDeviceMutation()

  const onRevoke = async (deviceId: string): Promise<void> => {
    try {
      await revokeDevice(deviceId).unwrap()
      toast.success('Device signed out')
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Could not sign out that device')
    }
  }

  return (
    <Card className="max-w-md">
      <CardHeader>
        <CardTitle>Devices</CardTitle>
        <CardDescription>Everywhere you're currently signed in. Revoking a device signs it out immediately.</CardDescription>
      </CardHeader>
      <QueryState
        {...query}
        isEmpty={(devices) => devices.length === 0}
        emptyState={<p className="text-sm text-text-secondary">No active devices.</p>}
      >
        {(devices) => (
          <div className="flex flex-col gap-3">
            {devices.map((device) => (
              <div key={device.id} className="flex items-center justify-between border-b border-border pb-3 last:border-0 last:pb-0">
                <div className="flex flex-col">
                  <span className="text-sm text-text-primary">{device.deviceLabel ?? 'Unknown device'}</span>
                  <span className="text-xs text-text-muted">Signed in {formatDate(device.createdAt)}</span>
                </div>
                <Button variant="outline" size="sm" onClick={() => void onRevoke(device.id)}>
                  Sign out
                </Button>
              </div>
            ))}
          </div>
        )}
      </QueryState>
    </Card>
  )
}

export function SecuritySettingsScreen(): React.JSX.Element {
  return (
    <div className="flex flex-col gap-6">
      <MfaSection />
      <DevicesSection />
    </div>
  )
}
