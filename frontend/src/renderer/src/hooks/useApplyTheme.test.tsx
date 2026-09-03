import { renderHook } from '@testing-library/react'
import { Provider } from 'react-redux'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useApplyTheme } from './useApplyTheme'
import { createTestStore, type TestStore } from '../../test/testStore'
import { themeChanged } from '@renderer/store/slices/uiSlice'

let store: TestStore

function renderWithStore() {
  return renderHook(() => useApplyTheme(), { wrapper: ({ children }) => <Provider store={store}>{children}</Provider> })
}

describe('useApplyTheme', () => {
  beforeEach(() => {
    document.documentElement.classList.remove('light')
    store = createTestStore()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('adds the .light class when the theme is light', () => {
    store.dispatch(themeChanged('light'))
    renderWithStore()
    expect(document.documentElement.classList.contains('light')).toBe(true)
  })

  it('removes the .light class when the theme is dark', () => {
    document.documentElement.classList.add('light')
    store.dispatch(themeChanged('dark'))
    renderWithStore()
    expect(document.documentElement.classList.contains('light')).toBe(false)
  })

  it('follows prefers-color-scheme when the theme is "system"', () => {
    const matchMediaMock = vi.fn().mockReturnValue({
      matches: true, // prefers light
      addEventListener: vi.fn(),
      removeEventListener: vi.fn()
    })
    vi.stubGlobal('matchMedia', matchMediaMock)
    store.dispatch(themeChanged('system'))

    renderWithStore()

    expect(matchMediaMock).toHaveBeenCalledWith('(prefers-color-scheme: light)')
    expect(document.documentElement.classList.contains('light')).toBe(true)
  })
})
