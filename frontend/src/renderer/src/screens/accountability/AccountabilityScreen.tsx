import { useState } from 'react'
import { toast } from 'sonner'
import { QueryState } from '@renderer/components/QueryState'
import { Button } from '@renderer/components/ui/button'
import { Input } from '@renderer/components/ui/input'
import { Card, CardDescription, CardHeader, CardTitle } from '@renderer/components/ui/card'
import { useAcceptInviteMutation, useCreateInviteMutation, useListGroupsQuery } from '@renderer/store/api/accountabilityApi'
import type { ErrorEnvelope } from '@renderer/store/api/baseApi'
import { GroupStatusCard } from './GroupStatusCard'

// design-spec.md §4: invite flow is two-sided — sending shows a pending state, accepting is an
// explicit screen action, matching the mutual-consent backend requirement (frd.md).
export function AccountabilityScreen(): React.JSX.Element {
  const groupsQuery = useListGroupsQuery()
  const [createInvite, { isLoading: isCreating }] = useCreateInviteMutation()
  const [acceptInvite, { isLoading: isAccepting }] = useAcceptInviteMutation()
  const [pendingCode, setPendingCode] = useState<string | null>(null)
  const [acceptCode, setAcceptCode] = useState('')

  const onCreateInvite = async (): Promise<void> => {
    try {
      const invite = await createInvite({}).unwrap()
      setPendingCode(invite.code)
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Could not create invite')
    }
  }

  const onAcceptInvite = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault()
    try {
      await acceptInvite(acceptCode).unwrap()
      toast.success('Joined the group')
      setAcceptCode('')
    } catch (err) {
      const envelope = (err as { data?: ErrorEnvelope }).data
      toast.error(envelope?.message ?? 'Could not accept invite')
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-semibold text-text-primary">Accountability</h1>

      <div className="grid grid-cols-2 gap-4">
        <Card>
          <CardHeader>
            <CardTitle>Invite a partner</CardTitle>
            <CardDescription>Only a binary daily completion signal is ever shared — nothing more.</CardDescription>
          </CardHeader>
          {pendingCode ? (
            <p className="rounded border border-border bg-surface-raised p-3 font-mono text-sm text-text-primary">
              Share this code: {pendingCode}
            </p>
          ) : (
            <Button onClick={() => void onCreateInvite()} disabled={isCreating}>
              Create invite
            </Button>
          )}
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Accept an invite</CardTitle>
          </CardHeader>
          <form onSubmit={onAcceptInvite} className="flex gap-2">
            <Input placeholder="Invite code" value={acceptCode} onChange={(e) => setAcceptCode(e.target.value)} />
            <Button type="submit" disabled={isAccepting || !acceptCode}>
              Accept
            </Button>
          </form>
        </Card>
      </div>

      <QueryState
        {...groupsQuery}
        isEmpty={(groups) => groups.length === 0}
        emptyState={<p className="text-sm text-text-secondary">You're not in any accountability groups yet.</p>}
      >
        {(groups) => (
          <div className="flex flex-col gap-3">
            {groups.map((group) => (
              <GroupStatusCard key={group.id} group={group} />
            ))}
          </div>
        )}
      </QueryState>
    </div>
  )
}
