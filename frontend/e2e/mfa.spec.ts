import { expect, test } from '@playwright/test'
import { closeApp, launchApp } from './helpers/electron'
import { createVerifiedUser } from './helpers/users'
import { computeTotp } from './helpers/totp'

test('enroll in MFA via a real QR-embedded secret, log out, and log back in through the real MFA challenge', async () => {
  const user = await createVerifiedUser('mfa')
  const { app, page } = await launchApp()

  try {
    await page.getByLabel('Email').fill(user.email)
    await page.getByLabel('Password').fill(user.password)
    await page.getByRole('button', { name: 'Log in' }).click()
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()

    await page.getByRole('link', { name: 'Settings' }).click()
    await page.getByRole('link', { name: 'Security' }).click()
    await expect(page.getByText('Not enabled')).toBeVisible()
    // The QR is rendered as a data: URL image, not readable text — read the real otpauth:// URI
    // straight off the network response instead of trying to decode the rendered QR pixels.
    const enrollResponsePromise = page.waitForResponse((res) => res.url().includes('/auth/mfa/enroll'))
    await page.getByRole('button', { name: 'Enable' }).click()
    const enrollResponse = await enrollResponsePromise
    const { otpAuthUri } = (await enrollResponse.json()) as { otpAuthUri: string }
    expect(otpAuthUri).toContain('otpauth://totp/')

    const code = computeTotp(otpAuthUri)
    await page.getByLabel('Code').fill(code)
    await page.getByRole('button', { name: 'Confirm' }).click()

    await expect(page.getByText(/save these recovery codes now/i)).toBeVisible()
    await page.getByRole('button', { name: "I've saved these codes" }).click()
    await expect(page.getByText('Enabled', { exact: true })).toBeVisible()

    await page.getByRole('button', { name: 'Log out' }).click()
    await expect(page.getByRole('heading', { name: 'Welcome back' })).toBeVisible()

    await page.getByLabel('Email').fill(user.email)
    await page.getByLabel('Password').fill(user.password)
    await page.getByRole('button', { name: 'Log in' }).click()

    await expect(page.getByRole('heading', { name: 'Enter your code' })).toBeVisible()
    await page.getByLabel('Code').fill(computeTotp(otpAuthUri))
    await page.getByRole('button', { name: 'Verify' }).click()

    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
  } finally {
    await closeApp(app)
  }
})
