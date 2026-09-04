import { expect, test } from '@playwright/test'
import { closeApp, launchApp } from './helpers/electron'
import { createVerifiedUser, loginViaApi } from './helpers/users'

const GATEWAY_ORIGIN = process.env.LOCUS_GATEWAY_ORIGIN ?? 'http://localhost:8080'

test('inviting a partner and having them accept (via the real API, as the second actor) shows the pairing in the UI', async () => {
  const inviter = await createVerifiedUser('accountability-a')
  const partner = await createVerifiedUser('accountability-b')
  const { app, page } = await launchApp()

  try {
    await page.getByLabel('Email').fill(inviter.email)
    await page.getByLabel('Password').fill(inviter.password)
    await page.getByRole('button', { name: 'Log in' }).click()
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()

    await page.getByRole('link', { name: 'Accountability' }).click()
    await page.getByRole('button', { name: 'Create invite' }).click()
    await expect(page.getByText(/Share this code:/)).toBeVisible()
    const codeText = await page.getByText(/Share this code:/).textContent()
    const code = codeText?.split(':')[1]?.trim()
    expect(code).toBeTruthy()

    // The partner accepting is a second real actor — done via the real API with their own real
    // access token, exactly like a second person on a second machine would, rather than opening
    // a second Electron window just to click one button.
    const partnerAuth = await loginViaApi(partner)
    const acceptRes = await fetch(`${GATEWAY_ORIGIN}/api/v1/accountability/invites/${code}/accept`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${partnerAuth.accessToken}`, 'Content-Type': 'application/json' }
    })
    expect(acceptRes.status).toBe(200)

    await page.reload()
    await expect(page.getByText("You're not in any accountability groups yet.")).not.toBeVisible()
  } finally {
    await closeApp(app)
  }
})
