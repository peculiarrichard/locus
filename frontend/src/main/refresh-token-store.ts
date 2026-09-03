import { app, safeStorage } from 'electron'
import { existsSync, readFileSync, unlinkSync, writeFileSync } from 'fs'
import { join } from 'path'

// The refresh token is the one piece of long-lived auth state that survives an app restart — it
// lives encrypted on disk via OS-backed safeStorage (Keychain/DPAPI/libsecret), never in
// localStorage or a Redux-persisted slice, per technical-spec.md §8/design-spec.md §1. The access
// token is never persisted here or anywhere else — it's memory-only Redux state, lost on restart
// by design, and re-obtained via a silent refresh on launch using this stored refresh token.
const filePath = (): string => join(app.getPath('userData'), 'refresh-token.enc')

export function readRefreshToken(): string | null {
  const path = filePath()
  if (!existsSync(path) || !safeStorage.isEncryptionAvailable()) {
    return null
  }
  try {
    return safeStorage.decryptString(readFileSync(path))
  } catch {
    return null
  }
}

export function writeRefreshToken(token: string): void {
  if (!safeStorage.isEncryptionAvailable()) {
    return
  }
  writeFileSync(filePath(), safeStorage.encryptString(token))
}

export function clearRefreshToken(): void {
  const path = filePath()
  if (existsSync(path)) {
    unlinkSync(path)
  }
}
