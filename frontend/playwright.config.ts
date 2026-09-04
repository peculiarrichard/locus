import { defineConfig } from '@playwright/test'

// Real end-to-end coverage of the critical paths named across frd.md — drives the actual packaged
// Electron app (out/main/index.js) against the real local backend stack, the same way Phase 14's
// manual verification did, but repeatable. Requires `npm run build` and the local backend/infra
// (docker-compose + all 7 services + Gateway) already running; these tests do not mock the network.
export default defineConfig({
  testDir: './e2e',
  timeout: 60_000,
  expect: { timeout: 15_000 },
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: [['list']]
})
