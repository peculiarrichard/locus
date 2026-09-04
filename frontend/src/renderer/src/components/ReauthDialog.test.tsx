import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ReauthDialog } from './ReauthDialog'

describe('ReauthDialog', () => {
  it('renders nothing visible when closed', () => {
    render(
      <ReauthDialog open={false} onOpenChange={() => {}} title="T" description="D" confirmLabel="Go" onConfirm={vi.fn()} />
    )
    expect(screen.queryByText('T')).not.toBeInTheDocument()
  })

  it('renders the title, description, and confirm label when open', () => {
    render(<ReauthDialog open title="Delete your account" description="This is permanent." confirmLabel="Permanently delete" onOpenChange={() => {}} onConfirm={vi.fn()} />)
    expect(screen.getByText('Delete your account')).toBeInTheDocument()
    expect(screen.getByText('This is permanent.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Permanently delete' })).toBeInTheDocument()
  })

  it('submits the typed password to onConfirm', async () => {
    const user = userEvent.setup()
    const onConfirm = vi.fn().mockResolvedValue(undefined)
    render(<ReauthDialog open title="T" description="D" confirmLabel="Go" onOpenChange={() => {}} onConfirm={onConfirm} />)

    await user.type(screen.getByLabelText('Confirm your password'), 'hunter2')
    await user.click(screen.getByRole('button', { name: 'Go' }))

    await waitFor(() => expect(onConfirm).toHaveBeenCalledWith('hunter2'))
  })

  it('disables the confirm button while onConfirm is pending, and re-enables it after', async () => {
    const user = userEvent.setup()
    let resolveConfirm: () => void = () => {}
    const onConfirm = vi.fn().mockReturnValue(new Promise<void>((resolve) => { resolveConfirm = resolve }))
    render(<ReauthDialog open title="T" description="D" confirmLabel="Go" onOpenChange={() => {}} onConfirm={onConfirm} />)

    await user.type(screen.getByLabelText('Confirm your password'), 'x')
    await user.click(screen.getByRole('button', { name: 'Go' }))

    await waitFor(() => expect(screen.getByRole('button', { name: 'Go' })).toBeDisabled())
    resolveConfirm()
    await waitFor(() => expect(screen.getByRole('button', { name: 'Go' })).not.toBeDisabled())
  })

  it('does not call onConfirm just from closing the dialog', () => {
    const onConfirm = vi.fn()
    const onOpenChange = vi.fn()
    render(<ReauthDialog open title="T" description="D" confirmLabel="Go" onOpenChange={onOpenChange} onConfirm={onConfirm} />)
    expect(onConfirm).not.toHaveBeenCalled()
  })
})
