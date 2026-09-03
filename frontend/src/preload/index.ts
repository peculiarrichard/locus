import { contextBridge, ipcRenderer } from 'electron'

// The entire renderer-facing surface, per technical-spec.md §8/design-spec.md §5 — no raw
// ipcRenderer exposure, only these specific, purpose-built calls.
const api = {
  auth: {
    getRefreshToken: (): Promise<string | null> => ipcRenderer.invoke('auth:get-refresh-token'),
    setRefreshToken: (token: string): Promise<void> => ipcRenderer.invoke('auth:set-refresh-token', token),
    clearRefreshToken: (): Promise<void> => ipcRenderer.invoke('auth:clear-refresh-token')
  },
  session: {
    setStatus: (status: 'active' | 'paused' | null, label: string | null): void =>
      ipcRenderer.send('session:set-status', status, label),
    onTrayPause: (callback: () => void): (() => void) => subscribe('session:tray-action:pause', callback),
    onTrayResume: (callback: () => void): (() => void) => subscribe('session:tray-action:resume', callback),
    onTrayEnd: (callback: () => void): (() => void) => subscribe('session:tray-action:end', callback)
  },
  distraction: {
    onBlur: (callback: () => void): (() => void) => subscribe('distraction:blur', callback),
    onFocus: (callback: () => void): (() => void) => subscribe('distraction:focus', callback)
  }
}

function subscribe(channel: string, callback: () => void): () => void {
  const listener = (): void => callback()
  ipcRenderer.on(channel, listener)
  return () => ipcRenderer.removeListener(channel, listener)
}

contextBridge.exposeInMainWorld('locus', api)

export type LocusApi = typeof api
