import { _electron as electron, type ElectronApplication, type Page } from '@playwright/test'
import { join, dirname } from 'path'
import { fileURLToPath } from 'url'
import { mkdtempSync } from 'fs'
import { tmpdir } from 'os'

const GATEWAY_ORIGIN = process.env.LOCUS_GATEWAY_ORIGIN ?? 'http://localhost:8080'
const __dirname = dirname(fileURLToPath(import.meta.url))

// Launches the real packaged main process (out/main/index.js, out/preload/index.cjs,
// out/renderer/*) — the production build, not the dev server — so these tests exercise what
// actually ships, CSP included. `npm run build` must have run first.
//
// Each launch gets its own --user-data-dir: without this, every launch shares this machine's
// real Electron userData folder, so safeStorage's persisted refresh token from a previous test's
// login survives into the next launch and AuthBootstrap silently signs back in as that earlier
// user instead of landing on the login screen — found live, not a hypothetical.
export async function launchApp(): Promise<{ app: ElectronApplication; page: Page }> {
  const userDataDir = mkdtempSync(join(tmpdir(), 'locus-e2e-'))
  const app = await electron.launch({
    args: [join(__dirname, '../../out/main/index.js'), `--user-data-dir=${userDataDir}`],
    env: { ...process.env, LOCUS_GATEWAY_ORIGIN: GATEWAY_ORIGIN, NODE_ENV: 'production' }
  })
  const page = await app.firstWindow()
  await page.waitForLoadState('domcontentloaded')
  return { app, page }
}

export async function closeApp(app: ElectronApplication): Promise<void> {
  await app.close()
}
