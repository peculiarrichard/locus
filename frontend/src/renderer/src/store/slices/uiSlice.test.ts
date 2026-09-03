import { beforeEach, describe, expect, it } from 'vitest'
import reducer, { sidebarToggled, themeChanged } from './uiSlice'

describe('uiSlice', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('defaults to dark theme when nothing is persisted', () => {
    expect(reducer(undefined, { type: '@@INIT' }).theme).toBe('dark')
  })

  it('themeChanged updates state and persists to localStorage', () => {
    const state = reducer({ theme: 'dark', sidebarCollapsed: false }, themeChanged('light'))
    expect(state.theme).toBe('light')
    expect(localStorage.getItem('locus.theme')).toBe('light')
  })

  it('sidebarToggled flips the boolean', () => {
    const state = reducer({ theme: 'dark', sidebarCollapsed: false }, sidebarToggled())
    expect(state.sidebarCollapsed).toBe(true)
    expect(reducer(state, sidebarToggled()).sidebarCollapsed).toBe(false)
  })

  it('ignores a corrupt persisted value and falls back to dark', async () => {
    localStorage.setItem('locus.theme', 'not-a-real-theme')
    vi.resetModules()
    const fresh = await import('./uiSlice')
    expect(fresh.default(undefined, { type: '@@INIT' }).theme).toBe('dark')
  })
})
