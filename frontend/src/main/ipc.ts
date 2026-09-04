import { BrowserWindow, ipcMain } from 'electron'
import { clearRefreshToken, readRefreshToken, writeRefreshToken } from './refresh-token-store'
import { setSessionStatus } from './tray'

export type SessionStatus = 'active' | 'paused' | null

export function registerIpcHandlers(getMainWindow: () => BrowserWindow | null): void {
  ipcMain.handle('auth:get-refresh-token', () => readRefreshToken())
  ipcMain.handle('auth:set-refresh-token', (_event, token: string) => writeRefreshToken(token))
  ipcMain.handle('auth:clear-refresh-token', () => clearRefreshToken())

  // The renderer owns the `session` Redux slice (design-spec.md §1) and pushes status changes
  // here so the main process can decide close-vs-hide behavior and mirror the tray icon —
  // main never independently tracks session state, only reflects what the renderer already knows.
  ipcMain.on('session:set-status', (_event, status: SessionStatus, label: string | null) => {
    setSessionStatus(status, label, getMainWindow)
  })
}
