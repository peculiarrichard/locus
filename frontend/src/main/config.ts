// Centralized runtime config for the main process. Gateway origin is the one thing that varies
// between local dev and a future packaged build — everything else about the hardening posture
// (CSP, navigation lockdown) is fixed regardless of environment.
export const GATEWAY_ORIGIN = process.env.LOCUS_GATEWAY_ORIGIN ?? 'http://localhost:8080'
export const IS_DEV = process.env.NODE_ENV === 'development' || !!process.env.ELECTRON_RENDERER_URL
