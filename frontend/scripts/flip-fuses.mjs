import { flipFuses, FuseVersion, FuseV1Options } from '@electron/fuses'

// design-spec.md/technical-spec.md §8 hardening, applied post-package: disables the runAsNode
// escape hatch and CLI-flag/NODE_OPTIONS injection paths, keeps ASAR integrity validation on.
// Signing itself is a Part 2/Phase 19 concern — this only hardens what ships regardless of who
// signs it.
export default async function afterPack(context) {
  const { electronPlatformName, appOutDir } = context
  const executableName = context.packager.appInfo.productFilename
  const electronBinaryPath =
    electronPlatformName === 'darwin'
      ? `${appOutDir}/${executableName}.app/Contents/MacOS/${executableName}`
      : `${appOutDir}/${executableName}${electronPlatformName === 'win32' ? '.exe' : ''}`

  await flipFuses(electronBinaryPath, {
    version: FuseVersion.V1,
    [FuseV1Options.RunAsNode]: false,
    [FuseV1Options.EnableCliInspectArguments]: false,
    [FuseV1Options.EnableNodeOptionsEnvironmentVariable]: false,
    [FuseV1Options.OnlyLoadAppFromAsar]: true,
    [FuseV1Options.EnableEmbeddedAsarIntegrityValidation]: true
  })
}
