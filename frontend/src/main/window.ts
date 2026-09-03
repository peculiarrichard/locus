import { join } from 'path'
import { BrowserWindow, session, shell } from 'electron'
import { GATEWAY_ORIGIN, IS_DEV } from './config'

// technical-spec.md §8: contextIsolation/sandbox on, nodeIntegration off, no raw ipcRenderer
// exposure (the preload script is the only bridge, see src/preload/index.ts).
export function createMainWindow(): BrowserWindow {
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
    minWidth: 960,
    minHeight: 640,
    show: false,
    autoHideMenuBar: true,
    webPreferences: {
      preload: join(__dirname, '../preload/index.cjs'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
      devTools: IS_DEV
    }
  })

  win.once('ready-to-show', () => win.show())

  // Locked to the Gateway origin plus whatever the renderer itself is served from (dev server or
  // the packaged app:// origin) — no unsafe-inline/unsafe-eval in production, per
  // design-spec.md §1/technical-spec.md §8. Dev needs 'unsafe-inline' script-src and a ws:
  // connect-src entry for Vite's React Fast Refresh preamble and HMR socket only.
  const scriptSrc = IS_DEV ? `'self' 'unsafe-inline'` : `'self'`
  const connectSrc = IS_DEV ? `'self' ${GATEWAY_ORIGIN} ws://localhost:5173` : `'self' ${GATEWAY_ORIGIN}`
  session.defaultSession.webRequest.onHeadersReceived((details, callback) => {
    callback({
      responseHeaders: {
        ...details.responseHeaders,
        'Content-Security-Policy': [
          `default-src 'self'; connect-src ${connectSrc}; img-src 'self' data:; style-src 'self' 'unsafe-inline'; script-src ${scriptSrc}; object-src 'none'; base-uri 'self';`
        ]
      }
    })
  })

  // No external navigation and no window.open-spawned windows inside the app — every external
  // link (e.g. a verification email's own page) opens in the OS default browser instead,
  // per technical-spec.md §1/§8.
  win.webContents.on('will-navigate', (event, url) => {
    if (!isAppOrigin(win, url)) {
      event.preventDefault()
      void shell.openExternal(url)
    }
  })
  win.webContents.setWindowOpenHandler(({ url }) => {
    void shell.openExternal(url)
    return { action: 'deny' }
  })

  if (IS_DEV && process.env['ELECTRON_RENDERER_URL']) {
    void win.loadURL(process.env['ELECTRON_RENDERER_URL'])
  } else {
    void win.loadFile(join(__dirname, '../renderer/index.html'))
  }

  return win
}

function isAppOrigin(win: BrowserWindow, url: string): boolean {
  try {
    const target = new URL(url)
    const current = new URL(win.webContents.getURL())
    return target.origin === current.origin
  } catch {
    return false
  }
}
