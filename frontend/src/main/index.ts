import { app, BrowserWindow } from 'electron'
import { createMainWindow } from './window'
import { createTray, isSessionInProgress } from './tray'
import { registerIpcHandlers } from './ipc'

let mainWindow: BrowserWindow | null = null
let isQuitting = false

function getMainWindow(): BrowserWindow | null {
  return mainWindow
}

app.whenReady().then(() => {
  mainWindow = createMainWindow()
  registerIpcHandlers(getMainWindow)
  createTray(getMainWindow)

  // technical-spec.md §8: blur/focus distraction detection needs the BrowserWindow to exist and
  // receive real OS focus events — relayed to the renderer, which owns the 3-second-threshold
  // logic and the actual API call (design-spec.md §5).
  mainWindow.on('blur', () => mainWindow?.webContents.send('distraction:blur'))
  mainWindow.on('focus', () => mainWindow?.webContents.send('distraction:focus'))

  // design-spec.md §3: closing the window during an active/paused session hides it instead of
  // quitting — the window instance must persist for blur/focus events to keep firing, so this is
  // a hide, never a close-and-recreate-on-restore. Outside of a session, closing quits normally.
  mainWindow.on('close', (event) => {
    if (isSessionInProgress() && !isQuitting) {
      event.preventDefault()
      mainWindow?.hide()
    }
  })

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      mainWindow = createMainWindow()
    }
  })
})

app.on('before-quit', () => {
  isQuitting = true
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})
