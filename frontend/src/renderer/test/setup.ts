import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

// @testing-library/react's automatic afterEach(cleanup) only self-registers reliably when the
// globals it detects are already installed at import time — explicit is safer than relying on
// that detection, since a leaked mount from one test can otherwise still be listening (and
// mutating shared module-level state like tokenStore) during the next.
afterEach(() => {
  cleanup()
})

// Every test runs as if inside the real preload-bridged renderer — `window.locus` never exists
// in jsdom otherwise, and most hooks/screens call into it unconditionally.
Object.defineProperty(window, 'locus', {
  writable: true,
  value: {
    auth: {
      getRefreshToken: vi.fn().mockResolvedValue(null),
      setRefreshToken: vi.fn().mockResolvedValue(undefined),
      clearRefreshToken: vi.fn().mockResolvedValue(undefined)
    },
    session: {
      setStatus: vi.fn(),
      onTrayPause: vi.fn(() => () => {}),
      onTrayResume: vi.fn(() => () => {}),
      onTrayEnd: vi.fn(() => () => {})
    },
    distraction: {
      onBlur: vi.fn(() => () => {}),
      onFocus: vi.fn(() => () => {})
    }
  }
})
