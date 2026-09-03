import { expect, test } from '@playwright/test'
import { closeApp, launchApp } from './helpers/electron'
import { getLatestVerificationToken } from './helpers/mailpit'
import { TEST_PASSWORD, createVerifiedUser } from './helpers/users'

const GATEWAY_ORIGIN = process.env.LOCUS_GATEWAY_ORIGIN ?? 'http://localhost:8080'

test('register through the real UI, verify via the real Mailpit-caught email, then log in and land on the Dashboard', async () => {
  const email = `e2e-register-${Date.now()}@example.com`
  const { app, page } = await launchApp()

  try {
    await page.getByRole('button', { name: 'Create an account' }).click()
    await page.getByLabel('Email').fill(email)
    await page.getByLabel('Password').fill(TEST_PASSWORD)
    await page.getByRole('button', { name: 'Register' }).click()

    await expect(page.getByText('Check your inbox')).toBeVisible()

    const token = await getLatestVerificationToken(email)
    const verifyRes = await fetch(`${GATEWAY_ORIGIN}/api/v1/auth/verify-email`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token })
    })
    expect(verifyRes.status).toBe(200)

    await page.getByRole('button', { name: "I've verified — log in" }).click()
    await expect(page.getByRole('heading', { name: 'Welcome back' })).toBeVisible()

    await page.getByLabel('Email').fill(email)
    await page.getByLabel('Password').fill(TEST_PASSWORD)
    await page.getByRole('button', { name: 'Log in' }).click()

    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
    await expect(page.getByText('No sessions yet')).toBeVisible()
  } finally {
    await closeApp(app)
  }
})

test('logging out returns to the login screen and a subsequent request is genuinely unauthenticated', async () => {
  const user = await createVerifiedUser('logout')

  const { app, page } = await launchApp()
  try {
    await page.getByLabel('Email').fill(user.email)
    await page.getByLabel('Password').fill(TEST_PASSWORD)
    await page.getByRole('button', { name: 'Log in' }).click()
    await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()

    await page.getByRole('button', { name: 'Log out' }).click()
    await expect(page.getByRole('heading', { name: 'Welcome back' })).toBeVisible()
  } finally {
    await closeApp(app)
  }
})
