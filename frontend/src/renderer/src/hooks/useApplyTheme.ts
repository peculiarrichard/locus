import { useEffect } from 'react'
import { useAppSelector } from '@renderer/store/hooks'

// design-spec.md §2: dark is default; .light on :root drives the CSS custom property overrides.
// 'system' follows prefers-color-scheme live, per §2's "follow system" option.
export function useApplyTheme(): void {
  const theme = useAppSelector((state) => state.ui.theme)

  useEffect(() => {
    const root = document.documentElement
    const apply = (isLight: boolean): void => {
      root.classList.toggle('light', isLight)
    }

    if (theme === 'system') {
      const media = window.matchMedia('(prefers-color-scheme: light)')
      apply(media.matches)
      const listener = (event: MediaQueryListEvent): void => apply(event.matches)
      media.addEventListener('change', listener)
      return () => media.removeEventListener('change', listener)
    }

    apply(theme === 'light')
    return undefined
  }, [theme])
}
