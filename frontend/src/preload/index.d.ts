import type { LocusApi } from './index'

declare global {
  interface Window {
    locus: LocusApi
  }
}
