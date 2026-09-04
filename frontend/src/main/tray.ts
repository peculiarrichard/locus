import { app, BrowserWindow, Menu, nativeImage, Tray } from 'electron'
import { join } from 'path'
import type { SessionStatus } from './ipc'

let tray: Tray | null = null
let currentStatus: SessionStatus = null
let currentLabel: string | null = null

// design-spec.md §3: tray residency is scoped to an active/paused session, not a general
// always-running mode — this is the single source of truth the main process checks before
// deciding whether closing the window should hide it or quit the app.
export function isSessionInProgress(): boolean {
  return currentStatus !== null
}

export function setSessionStatus(status: SessionStatus, label: string | null, getMainWindow: () => BrowserWindow | null): void {
  currentStatus = status
  currentLabel = label
  updateTray(getMainWindow)
}

export function createTray(getMainWindow: () => BrowserWindow | null): void {
  const icon = nativeImage.createFromPath(join(__dirname, '../../resources/tray-icon.png'))
  tray = new Tray(icon.isEmpty() ? nativeImage.createEmpty() : icon)
  tray.setToolTip('Locus')
  tray.on('click', () => {
    const win = getMainWindow()
    if (win) {
      win.show()
      win.focus()
    }
  })
  updateTray(getMainWindow)
}

function updateTray(getMainWindow: () => BrowserWindow | null): void {
  if (!tray) {
    return
  }
  const win = getMainWindow()
  const send = (channel: string): void => win?.webContents.send(channel)

  if (currentStatus === null) {
    tray.setToolTip('Locus')
    tray.setContextMenu(
      Menu.buildFromTemplate([
        { label: 'Open Locus', click: () => { win?.show(); win?.focus() } },
        { type: 'separator' },
        { label: 'Quit', click: () => app.quit() }
      ])
    )
    return
  }

  const statusLabel = currentLabel ?? (currentStatus === 'paused' ? 'Paused' : 'Active session')
  tray.setToolTip(`Locus — ${statusLabel}`)
  tray.setContextMenu(
    Menu.buildFromTemplate([
      { label: statusLabel, enabled: false },
      { type: 'separator' },
      currentStatus === 'active'
        ? { label: 'Pause', click: () => send('session:tray-action:pause') }
        : { label: 'Resume', click: () => send('session:tray-action:resume') },
      { label: 'End session', click: () => send('session:tray-action:end') },
      { type: 'separator' },
      { label: 'Open Locus', click: () => { win?.show(); win?.focus() } },
      { label: 'Quit', click: () => app.quit() }
    ])
  )
}
